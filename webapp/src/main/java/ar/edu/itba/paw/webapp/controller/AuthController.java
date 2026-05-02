package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.services.UserService;
import ar.edu.itba.paw.webapp.auth.LoginRedirectUtils;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.regex.Pattern;

@Controller
public class AuthController {

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
            mav.addObject("loginError", "Email o contraseña inválidos.");
        }
        if (logout != null) {
            mav.addObject("loginMessage", "Sesión cerrada.");
        }
        if (registered != null) {
            mav.addObject("loginMessage", "Cuenta creada. Ya podés iniciar sesión.");
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
            final String validationError = validateRegistration(normalizedUsername, normalizedEmail, password, confirmPassword);
            if (validationError != null) {
                return registerFormWithError(validationError, normalizedUsername, normalizedEmail);
            }
            userService.createUser(normalizedUsername, normalizedEmail, password);
        } catch (final IllegalArgumentException e) {
            return registerFormWithError(e.getMessage(), normalizedUsername, normalizedEmail);
        } catch (final DataIntegrityViolationException e) {
            return registerFormWithError("Ese usuario o email ya está registrado.", normalizedUsername, normalizedEmail);
        } catch (final DataAccessException e) {
            return registerFormWithError("No pudimos crear la cuenta en este momento. Intentá nuevamente.", normalizedUsername, normalizedEmail);
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
            return false;
        }
    }

    private String validateRegistration(final String username, final String email,
                                        final String password, final String confirmPassword) {
        if (username == null) {
            return "El nombre de usuario es obligatorio.";
        }
        if (username.length() > USERNAME_MAX_LENGTH) {
            return "El nombre de usuario debe tener como máximo 50 caracteres.";
        }
        if (!USERNAME_PATTERN.matcher(username).matches()) {
            return "El nombre de usuario solo puede usar letras, números, punto, guion y guion bajo.";
        }
        if (userService.findByUsername(username).isPresent()) {
            return "Ese nombre de usuario ya está en uso.";
        }

        if (email == null) {
            return "El email es obligatorio.";
        }
        if (email.length() > EMAIL_MAX_LENGTH || !SIMPLE_EMAIL_PATTERN.matcher(email).matches()) {
            return "Ingresá un email válido.";
        }
        if (userService.findByEmail(email).isPresent()) {
            return "Ese email ya está registrado.";
        }

        if (password == null || password.length() < PASSWORD_MIN_LENGTH) {
            return "La contraseña debe tener al menos 8 caracteres.";
        }
        if (password.length() > PASSWORD_MAX_LENGTH) {
            return "La contraseña debe tener como máximo 72 caracteres.";
        }
        if (!password.equals(confirmPassword)) {
            return "Las contraseñas no coinciden.";
        }

        return null;
    }

    private ModelAndView registerFormWithError(final String error, final String username, final String email) {
        final ModelAndView mav = new ModelAndView("register.jsp");
        mav.addObject("registrationError", error);
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
