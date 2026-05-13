package ar.edu.itba.paw.webapp.auth;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LoginRedirectUtilsTest {

    @Test
    void safeRedirect_contextPrefixedPath_stripsContextPath() {
        // Arrange
        final String redirect = "/paw-2026a-12/reviews/car/42?page=2#reviewsFeed";
        final String contextPath = "/paw-2026a-12";
        // Exercise
        final Optional<String> result = LoginRedirectUtils.safeRedirect(redirect, contextPath);
        // Assertions
        assertTrue(result.isPresent());
        assertEquals("/reviews/car/42?page=2#reviewsFeed", result.get());
    }

    @Test
    void safeRedirect_contextRootWithQuery_preservesRootPath() {
        // Arrange
        final String redirect = "/paw-2026a-12?intent=like-1";
        final String contextPath = "/paw-2026a-12";
        // Exercise
        final Optional<String> result = LoginRedirectUtils.safeRedirect(redirect, contextPath);
        // Assertions
        assertTrue(result.isPresent());
        assertEquals("/?intent=like-1", result.get());
    }

    @Test
    void safeRedirect_externalUrl_rejectsRedirect() {
        // Arrange
        final String redirect = "https://example.com/reviews";
        final String contextPath = "/paw-2026a-12";
        // Exercise
        final Optional<String> result = LoginRedirectUtils.safeRedirect(redirect, contextPath);
        // Assertions
        assertTrue(result.isEmpty());
    }
}
