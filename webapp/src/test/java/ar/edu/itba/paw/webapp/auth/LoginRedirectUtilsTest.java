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

    @Test
    void safeRedirect_contextStripCreatesProtocolRelative_rejectsRedirect() {
        // Arrange
        final String redirect = "/paw-2026a-12//evil.com";
        final String contextPath = "/paw-2026a-12";
        // Exercise
        final Optional<String> result = LoginRedirectUtils.safeRedirect(redirect, contextPath);
        // Assertions
        assertTrue(result.isEmpty());
    }

    @Test
    void safeRefererPath_absoluteUrlWithQuery_keepsPathAndQuery() {
        // Arrange
        final String referer = "http://localhost/cars?sort=popular";
        final String contextPath = "";
        // Exercise
        final Optional<String> result = LoginRedirectUtils.safeRefererPath(referer, contextPath);
        // Assertions
        assertTrue(result.isPresent());
        assertEquals("/cars?sort=popular", result.get());
    }

    @Test
    void safeRefererPath_contextPrefixedPath_stripsContextPath() {
        // Arrange
        final String referer = "http://localhost/paw-2026a-12/admin?page=6";
        final String contextPath = "/paw-2026a-12";
        // Exercise
        final Optional<String> result = LoginRedirectUtils.safeRefererPath(referer, contextPath);
        // Assertions
        assertTrue(result.isPresent());
        assertEquals("/admin?page=6", result.get());
    }

    @Test
    void safeRefererPath_contextRootReferer_returnsRootPath() {
        // Arrange
        final String referer = "http://localhost/paw-2026a-12";
        final String contextPath = "/paw-2026a-12";
        // Exercise
        final Optional<String> result = LoginRedirectUtils.safeRefererPath(referer, contextPath);
        // Assertions
        assertTrue(result.isPresent());
        assertEquals("/", result.get());
    }

    @Test
    void safeRefererPath_externalHost_discardsHostAndKeepsLocalPath() {
        // Arrange
        final String referer = "https://evil.com/cars?x=1";
        final String contextPath = "";
        // Exercise
        final Optional<String> result = LoginRedirectUtils.safeRefererPath(referer, contextPath);
        // Assertions
        assertTrue(result.isPresent());
        assertEquals("/cars?x=1", result.get());
    }

    @Test
    void safeRefererPath_contextStripCreatesProtocolRelative_rejectsRedirect() {
        // Arrange
        final String referer = "http://localhost/paw-2026a-12//evil.com";
        final String contextPath = "/paw-2026a-12";
        // Exercise
        final Optional<String> result = LoginRedirectUtils.safeRefererPath(referer, contextPath);
        // Assertions
        assertTrue(result.isEmpty());
    }

    @Test
    void safeRefererPath_blankReferer_returnsEmpty() {
        // Arrange
        final String referer = "   ";
        final String contextPath = "/paw-2026a-12";
        // Exercise
        final Optional<String> result = LoginRedirectUtils.safeRefererPath(referer, contextPath);
        // Assertions
        assertTrue(result.isEmpty());
    }

    @Test
    void safeRefererPath_nullReferer_returnsEmpty() {
        // Arrange
        final String referer = null;
        final String contextPath = "/paw-2026a-12";
        // Exercise
        final Optional<String> result = LoginRedirectUtils.safeRefererPath(referer, contextPath);
        // Assertions
        assertTrue(result.isEmpty());
    }

    @Test
    void safeRefererPath_hostOnlyNoPath_returnsEmpty() {
        // Arrange
        final String referer = "http://localhost";
        final String contextPath = "";
        // Exercise
        final Optional<String> result = LoginRedirectUtils.safeRefererPath(referer, contextPath);
        // Assertions
        assertTrue(result.isEmpty());
    }
}
