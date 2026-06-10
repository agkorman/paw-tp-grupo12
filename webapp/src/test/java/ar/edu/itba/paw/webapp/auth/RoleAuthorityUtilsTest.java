package ar.edu.itba.paw.webapp.auth;

import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RoleAuthorityUtilsTest {

    @Test
    void toAuthority_nullRole_defaultsToUserAuthority() {
        // Arrange
        final String role = null;
        // Exercise
        final String result = RoleAuthorityUtils.toAuthority(role);
        // Assertions
        assertEquals("ROLE_USER", result);
    }

    @Test
    void toAuthority_blankRole_defaultsToUserAuthority() {
        // Arrange
        final String role = "   ";
        // Exercise
        final String result = RoleAuthorityUtils.toAuthority(role);
        // Assertions
        assertEquals("ROLE_USER", result);
    }

    @Test
    void toAuthority_lowercaseRole_isUppercasedAndPrefixed() {
        // Arrange
        final String role = "admin";
        // Exercise
        final String result = RoleAuthorityUtils.toAuthority(role);
        // Assertions
        assertEquals("ROLE_ADMIN", result);
    }

    @Test
    void toAuthority_roleWithSurroundingWhitespace_isTrimmedAndPrefixed() {
        // Arrange
        final String role = "  moderator  ";
        // Exercise
        final String result = RoleAuthorityUtils.toAuthority(role);
        // Assertions
        assertEquals("ROLE_MODERATOR", result);
    }

    @Test
    void toAuthority_alreadyPrefixedRole_isNotPrefixedAgain() {
        // Arrange
        final String role = "ROLE_ADMIN";
        // Exercise
        final String result = RoleAuthorityUtils.toAuthority(role);
        // Assertions
        assertEquals("ROLE_ADMIN", result);
    }

    @Test
    void authoritiesForRole_returnsSingleMappedAuthority() {
        // Arrange
        final String role = "admin";
        // Exercise
        final Collection<? extends GrantedAuthority> result = RoleAuthorityUtils.authoritiesForRole(role);
        // Assertions
        assertEquals(1, result.size());
        assertEquals("ROLE_ADMIN", result.iterator().next().getAuthority());
    }
}
