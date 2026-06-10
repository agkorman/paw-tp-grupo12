package ar.edu.itba.paw.webapp.auth;

import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.services.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PawUserDetailsServiceTest {

    private static final String EMAIL = "user@example.com";

    @Mock
    private UserService userService;

    @InjectMocks
    private PawUserDetailsService pawUserDetailsService;

    private static User user(final String role) {
        final User user = new User("username", EMAIL, "hashed-password", role, "es");
        user.setId(42L);
        return user;
    }

    @Test
    void loadUserByUsername_missingUser_throwsUsernameNotFound() {
        // Arrange
        when(userService.findByEmail(EMAIL)).thenReturn(Optional.empty());

        // Exercise
        final UsernameNotFoundException ex = assertThrows(UsernameNotFoundException.class,
                () -> pawUserDetailsService.loadUserByUsername(EMAIL));

        // Assertions
        assertEquals("No user found for email " + EMAIL, ex.getMessage());
    }

    @Test
    void loadUserByUsername_existingUser_mapsEmailAndAuthorities() {
        // Arrange
        when(userService.findByEmail(EMAIL)).thenReturn(Optional.of(user("admin")));

        // Exercise
        final UserDetails result = pawUserDetailsService.loadUserByUsername(EMAIL);

        // Assertions
        assertEquals(EMAIL, result.getUsername());
        assertEquals("hashed-password", result.getPassword());
        assertEquals(1, result.getAuthorities().size());
        assertEquals("ROLE_ADMIN", result.getAuthorities().iterator().next().getAuthority());
    }

    @Test
    void loadUserByUsername_existingUser_exposesDisplayNameAndId() {
        // Arrange
        when(userService.findByEmail(EMAIL)).thenReturn(Optional.of(user("user")));

        // Exercise
        final UserDetails result = pawUserDetailsService.loadUserByUsername(EMAIL);

        // Assertions
        assertTrue(result instanceof AuthenticatedUser);
        final AuthenticatedUser authenticatedUser = (AuthenticatedUser) result;
        assertEquals(42L, authenticatedUser.getId());
        assertEquals("username", authenticatedUser.getDisplayName());
        assertEquals("es", authenticatedUser.getPreferredLocale());
    }
}
