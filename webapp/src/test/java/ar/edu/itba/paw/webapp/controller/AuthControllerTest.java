package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.services.UserService;
import org.junit.Test;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Optional;

import static org.junit.Assert.assertEquals;

public class AuthControllerTest {

    @Test
    public void createAccountWithMissingCurlFieldsReturnsRegistrationError() {
        final AuthController controller = new AuthController(new FakeUserService());

        final ModelAndView mav = controller.createAccount(null, null, null, null, null);

        assertEquals("register.jsp", mav.getViewName());
        assertEquals("El nombre de usuario es obligatorio.", mav.getModel().get("registrationError"));
    }

    @Test
    public void createAccountWithIntegrityFailureReturnsRegistrationError() {
        final FakeUserService userService = new FakeUserService();
        userService.throwIntegrityFailure = true;
        final AuthController controller = new AuthController(userService);

        final ModelAndView mav = controller.createAccount(
                "driver", "driver@example.com", "password123", "password123", null);

        assertEquals("register.jsp", mav.getViewName());
        assertEquals("Ese usuario o email ya está registrado.", mav.getModel().get("registrationError"));
    }

    @Test
    public void createAccountWithValidFieldsRedirectsToLogin() {
        final AuthController controller = new AuthController(new FakeUserService());

        final ModelAndView mav = controller.createAccount(
                "driver", "driver@example.com", "password123", "password123", null);

        assertEquals("redirect:/login?registered", mav.getViewName());
    }

    private static final class FakeUserService implements UserService {
        private boolean throwIntegrityFailure;

        @Override
        public Optional<User> getUserById(final long id) {
            return Optional.empty();
        }

        @Override
        public Optional<User> findByEmail(final String email) {
            return Optional.empty();
        }

        @Override
        public Optional<User> findByUsername(final String username) {
            return Optional.empty();
        }

        @Override
        public User createUser(final String username, final String email, final String rawPassword) {
            if (throwIntegrityFailure) {
                throw new DataIntegrityViolationException("duplicate");
            }
            return new User();
        }

        @Override
        public List<String> getModeratorsEmails() {
            return List.of();
        }
    }
}
