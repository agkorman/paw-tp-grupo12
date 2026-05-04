package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.services.UserService;
import ar.edu.itba.paw.services.exception.EmailAlreadyExistsException;
import ar.edu.itba.paw.services.exception.InvalidServiceInputException;
import ar.edu.itba.paw.services.exception.UsernameAlreadyExistsException;
import ar.edu.itba.paw.webapp.auth.LoginRedirectUtils;
import ar.edu.itba.paw.webapp.form.RegistrationForm;
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
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import ar.edu.itba.paw.webapp.util.LogSanitizer;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Controller
public class AuthController {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthController.class);

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
    public ModelAndView createAccount(@Valid @ModelAttribute("registrationForm") final RegistrationForm registrationForm,
                                      final BindingResult errors,
                                      final Authentication authentication,
                                      final HttpServletRequest request,
                                      final HttpServletResponse response) {
        if (isLoggedIn(authentication)) {
            return new ModelAndView("redirect:/");
        }

        final String normalizedUsername = ControllerUtils.normalize(registrationForm.getUsername());
        final String normalizedEmail = ControllerUtils.normalizeEmail(registrationForm.getEmail());

        try {
            if (errors.hasErrors()) {
                final String validationErrorCode = registrationErrorCode(errors);
                LOGGER.warn("registration rejected email={} reasonCode={}", LogSanitizer.forLog(normalizedEmail, LogSanitizer.MAX_LOG_EMAIL_CODE_POINTS), validationErrorCode);
                return registerFormWithError(validationErrorCode, normalizedUsername, normalizedEmail);
            }
            userService.createUser(normalizedUsername, normalizedEmail, registrationForm.getPassword());
            LOGGER.info("registered new user email={} username={}", normalizedEmail, normalizedUsername);
        } catch (final UsernameAlreadyExistsException e) {
            LOGGER.warn("registration rejected email={} reasonCode={}", LogSanitizer.forLog(normalizedEmail, LogSanitizer.MAX_LOG_EMAIL_CODE_POINTS), "auth.register.error.username.exists");
            return registerFormWithError("auth.register.error.username.exists", normalizedUsername, normalizedEmail);
        } catch (final EmailAlreadyExistsException e) {
            LOGGER.warn("registration rejected email={} reasonCode={}", LogSanitizer.forLog(normalizedEmail, LogSanitizer.MAX_LOG_EMAIL_CODE_POINTS), "auth.register.error.email.exists");
            return registerFormWithError("auth.register.error.email.exists", normalizedUsername, normalizedEmail);
        } catch (final InvalidServiceInputException e) {
            LOGGER.warn("registration rejected by service validation email={} type={}",
                    LogSanitizer.forLog(normalizedEmail, LogSanitizer.MAX_LOG_EMAIL_CODE_POINTS),
                    e.getClass().getSimpleName());
            return registerFormWithError("auth.register.error.unavailable", normalizedUsername, normalizedEmail);
        } catch (final DataIntegrityViolationException e) {
            LOGGER.warn("registration rejected: integrity violation email={}", LogSanitizer.forLog(normalizedEmail, LogSanitizer.MAX_LOG_EMAIL_CODE_POINTS));
            return registerFormWithError("auth.register.error.duplicate", normalizedUsername, normalizedEmail);
        } catch (final DataAccessException e) {
            LOGGER.error("Database error while creating user {}", LogSanitizer.forLog(normalizedEmail, LogSanitizer.MAX_LOG_EMAIL_CODE_POINTS), e);
            return registerFormWithError("auth.register.error.unavailable", normalizedUsername, normalizedEmail);
        }

        if (autoLogin(normalizedEmail, registrationForm.getPassword(), request, response)) {
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
            LOGGER.warn("auto-login failed after registration email={} type={}",
                    LogSanitizer.forLog(email, LogSanitizer.MAX_LOG_EMAIL_CODE_POINTS),
                    e.getClass().getSimpleName());
            return false;
        }
    }

    private String registrationErrorCode(final BindingResult errors) {
        final FieldError fieldError = errors.getFieldError();
        if (fieldError == null) {
            return "auth.register.error.password.mismatch";
        }
        switch (fieldError.getField()) {
            case "username":
                if ("NotBlank".equals(fieldError.getCode())) {
                    return "auth.register.error.username.required";
                }
                if ("Size".equals(fieldError.getCode())) {
                    return "auth.register.error.username.max";
                }
                return "auth.register.error.username.pattern";
            case "email":
                if ("NotBlank".equals(fieldError.getCode())) {
                    return "auth.register.error.email.required";
                }
                return "auth.register.error.email.invalid";
            case "password":
                if ("Size".equals(fieldError.getCode())) {
                    final Object rejected = fieldError.getRejectedValue();
                    return rejected instanceof String && ((String) rejected).length() > 72
                            ? "auth.register.error.password.max"
                            : "auth.register.error.password.min";
                }
                return "auth.register.error.password.min";
            default:
                return "auth.register.error.password.mismatch";
        }
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
