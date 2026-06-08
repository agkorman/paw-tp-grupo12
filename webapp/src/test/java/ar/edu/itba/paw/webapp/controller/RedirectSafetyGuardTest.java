package ar.edu.itba.paw.webapp.controller;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Architectural guard: user-supplied redirect targets (the {@code Referer} header
 * and {@code redirect} request params) must be validated through
 * {@code LoginRedirectUtils} before being turned into a {@code redirect:} view.
 * Raw {@code URI.create(...)} parsing of a referer, or reading the {@code Referer}
 * header in a controller that does not delegate to {@code LoginRedirectUtils}, is
 * an open-redirect vector. This test scans the controller sources and fails if the
 * pattern reappears anywhere outside the centralized helper.
 */
class RedirectSafetyGuardTest {

    private static final Path CONTROLLER_DIR =
        Paths.get("src/main/java/ar/edu/itba/paw/webapp/controller");

    // Reading the Referer header is only safe when the same file routes it through the helper.
    private static final Pattern READS_REFERER =
        Pattern.compile("Referer|getHeader\\s*\\(\\s*\"(?i:referer)\"");
    private static final Pattern USES_HELPER =
        Pattern.compile("LoginRedirectUtils\\s*\\.\\s*(safeRedirect|safeRefererPath|safeIntent)");
    private static final Pattern RAW_URI_PARSE =
        Pattern.compile("URI\\s*\\.\\s*create");
    private static final Pattern REMOVED_HELPER =
        Pattern.compile("strip(Current)?ContextPath");

    @Test
    void controllersDoNotParseRefererOrRedirectsOutsideLoginRedirectUtils() {
        final List<String> violations = new ArrayList<>();
        for (final Path source : controllerSources()) {
            final String body = read(source);
            final String name = source.getFileName().toString();

            if (READS_REFERER.matcher(body).find() && !USES_HELPER.matcher(body).find()) {
                violations.add(name
                    + ": reads the Referer header but never calls LoginRedirectUtils");
            }
            if (RAW_URI_PARSE.matcher(body).find()) {
                violations.add(name
                    + ": uses URI.create(...) directly; resolve redirects via LoginRedirectUtils instead");
            }
            if (REMOVED_HELPER.matcher(body).find()) {
                violations.add(name
                    + ": references the removed stripContextPath helper; use LoginRedirectUtils / pathWithoutQuery");
            }
        }
        assertTrue(violations.isEmpty(),
            "Unsafe redirect handling detected:\n" + String.join("\n", violations));
    }

    private static List<Path> controllerSources() {
        try (Stream<Path> paths = Files.walk(CONTROLLER_DIR)) {
            return paths
                .filter(p -> p.getFileName().toString().endsWith(".java"))
                .collect(Collectors.toList());
        } catch (final IOException e) {
            throw new UncheckedIOException("cannot scan controller sources at " + CONTROLLER_DIR, e);
        }
    }

    private static String read(final Path source) {
        try {
            return new String(Files.readAllBytes(source));
        } catch (final IOException e) {
            throw new UncheckedIOException("cannot read " + source, e);
        }
    }
}
