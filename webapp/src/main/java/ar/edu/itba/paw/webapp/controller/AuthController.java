package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.services.UserService;
import ar.edu.itba.paw.webapp.auth.LoginRedirectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import ar.edu.itba.paw.webapp.util.LogSanitizer;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.regex.Pattern;

@Controller
public class AuthController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);
    private static final int USERNAME_MAX_LENGTH = 50;
    private static final int EMAIL_MAX_LENGTH = 100;
    private static final int PASSWORD_MIN_LENGTH = 8;
    private static final int PASSWORD_MAX_LENGTH = 72;
    private static final Pattern SIMPLE_EMAIL_PATTERN =
            Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("^[A-Za-z0-9._-]+$");

    private static final SecurityContextRepository SECURITY_CONTEXT_REPOSITORY = new HttpSessionSecurityContextRepository();

    private final UserService userService;
    private final AuthenticationManager authenticationManager;

    @Autowired
    public AuthController(final UserService userService, final AuthenticationManager authenticationManager) {
        this.userService = userService;
        this.authenticationManager = authenticationManager;
    }

    @RequestMapping(value = "/login", method = RequestMethod.GET)
    public ModelAndView login(@RequestParam(value = "error", required = false) final String error,
                              @RequestParam(value = "logout", required = false) final String logout,
                              @RequestParam(value = "registered", required = false) final String registered,
                              @RequestParam(value = LoginRedirectUtils.REDIRECT_PARAM, required = false) final String redirect,
                              @RequestParam(value = LoginRedirectUtils.INTENT_PARAM, required = false) final String intent,
                              final Authentication authentication) {
        final String safeRedirect = LoginRedirectUtils.safeRedirect(redirect).orElse(null);
        final String safeIntent = LoginRedirectUtils.safeIntent(intent).orElse(null);
        if (isLoggedIn(authentication)) {
            return new ModelAndView("redirect:" + LoginRedirectUtils.appendIntent(
                    safeRedirect == null ? "/" : safeRedirect,
                    safeIntent
            ));
        }

        final ModelAndView mav = new ModelAndView("login.jsp");
        mav.addObject("loginRedirect", safeRedirect);
        mav.addObject("loginIntent", safeIntent);
        if (error != null) {
            mav.addObject("loginErrorCode", "auth.login.error");
        }
        if (logout != null) {
            mav.addObject("loginMessageCode", "auth.login.logout");
        }
        if (registered != null) {
            mav.addObject("loginMessageCode", "auth.login.registered");
        }
        return mav;
    }

    @RequestMapping(value = "/register", method = RequestMethod.GET)
    public ModelAndView register(final Authentication authentication) {
        if (isLoggedIn(authentication)) {
            return new ModelAndView("redirect:/");
        }
        return new ModelAndView("register.jsp");
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ModelAndView createAccount(@RequestParam(value = "username", required = false) final String username,
                                      @RequestParam(value = "email", required = false) final String email,
                                      @RequestParam(value = "password", required = false) final String password,
                                      @RequestParam(value = "confirmPassword", required = false) final String confirmPassword,
                                      final Authentication authentication,
                                      final HttpServletRequest request,
                                      final HttpServletResponse response) {
        if (isLoggedIn(authentication)) {
            return new ModelAndView("redirect:/");
        }

        final String normalizedUsername = ControllerUtils.normalize(username);
        final String normalizedEmail = ControllerUtils.normalizeEmail(email);

        try {
            final String validationErrorCode =
                    validateRegistration(normalizedUsername, normalizedEmail, password, confirmPassword);
            if (validationErrorCode != null) {
                LOGGER.warn("registration rejected email={} reasonCode={}", LogSanitizer.forLog(normalizedEmail, LogSanitizer.MAX_LOG_EMAIL_CODE_POINTS), validationErrorCode);
                return registerFormWithError(validationErrorCode, normalizedUsername, normalizedEmail);
            }
            userService.createUser(normalizedUsername, normalizedEmail, password);
            LOGGER.info("registered new user email={} username={}", normalizedEmail, normalizedUsername);
        } catch (final IllegalArgumentException e) {
            final String errorCode = registrationErrorCode(e.getMessage());
            LOGGER.warn("registration rejected email={} reasonCode={}", LogSanitizer.forLog(normalizedEmail, LogSanitizer.MAX_LOG_EMAIL_CODE_POINTS), errorCode);
            return registerFormWithError(errorCode, normalizedUsername, normalizedEmail);
        } catch (final DataIntegrityViolationException e) {
            LOGGER.warn("registration rejected: integrity violation email={}", LogSanitizer.forLog(normalizedEmail, LogSanitizer.MAX_LOG_EMAIL_CODE_POINTS));
            return registerFormWithError("auth.register.error.duplicate", normalizedUsername, normalizedEmail);
        } catch (final DataAccessException e) {
            LOGGER.error("Database error while creating user {}", LogSanitizer.forLog(normalizedEmail, LogSanitizer.MAX_LOG_EMAIL_CODE_POINTS), e);
            return registerFormWithError("auth.register.error.unavailable", normalizedUsername, normalizedEmail);
        }

        if (autoLogin(normalizedEmail, password, request, response)) {
            return new ModelAndView("redirect:/");
        }
        return new ModelAndView("redirect:/login?registered");
    }

    private boolean autoLogin(final String email, final String rawPassword,
                              final HttpServletRequest request, final HttpServletResponse response) {
        try {
            final Authentication authentication = authenticationManager.authenticate(
                    UsernamePasswordAuthenticationToken.unauthenticated(email, rawPassword));
            final SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);
            SecurityContextHolder.setContext(context);
            SECURITY_CONTEXT_REPOSITORY.saveContext(context, request, response);
            return true;
        } catch (final AuthenticationException e) {
            LOGGER.warn("auto-login failed after registration email={}", LogSanitizer.forLog(email, LogSanitizer.MAX_LOG_EMAIL_CODE_POINTS), e);
            return false;
        }
    }

    private String validateRegistration(final String username, final String email,
                                        final String password, final String confirmPassword) {
        if (username == null) {
            return "auth.register.error.username.required";
        }
        if (username.length() > USERNAME_MAX_LENGTH) {
            return "auth.register.error.username.max";
        }
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            return "auth.register.error.username.pattern";
        }
        if (userService.findByUsername(username).isPresent()) {
            return "auth.register.error.username.exists";
        }

        if (email == null) {
            return "auth.register.error.email.required";
        }
        if (email.length() > EMAIL_MAX_LENGTH || !SIMPLE_EMAIL_PATTERN.matcher(email).matches()) {
            return "auth.register.error.email.invalid";
        }
        if (userService.findByEmail(email).isPresent()) {
            return "auth.register.error.email.exists";
        }

        if (password == null || password.length() < PASSWORD_MIN_LENGTH) {
            return "auth.register.error.password.min";
        }
        if (password.length() > PASSWORD_MAX_LENGTH) {
            return "auth.register.error.password.max";
        }
        if (!password.equals(confirmPassword)) {
            return "auth.register.error.password.mismatch";
        }

        return null;
    }

    private String registrationErrorCode(final String errorMessage) {
        if ("Username is required.".equals(errorMessage)) {
            return "auth.register.error.username.required";
        }
        if ("Email is required.".equals(errorMessage)) {
            return "auth.register.error.email.required";
        }
        if ("Password is required.".equals(errorMessage)) {
            return "auth.register.error.password.min";
        }
        if ("Username is already registered.".equals(errorMessage)) {
            return "auth.register.error.username.exists";
        }
        if ("Email is already registered.".equals(errorMessage)) {
            return "auth.register.error.email.exists";
        }
        return "auth.register.error.unavailable";
    }

    private ModelAndView registerFormWithError(final String errorCode, final String username, final String email) {
        final ModelAndView mav = new ModelAndView("register.jsp");
        mav.addObject("registrationErrorCode", errorCode);
        mav.addObject("username", username);
        mav.addObject("email", email);
        return mav;
    }

    private boolean isLoggedIn(final Authentication authentication) {
        return authentication != null
                && authentication.isAuthenticated()
                && !(authentication instanceof AnonymousAuthenticationToken);
    }

}
