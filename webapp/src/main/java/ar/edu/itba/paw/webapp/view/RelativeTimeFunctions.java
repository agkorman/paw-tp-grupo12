package ar.edu.itba.paw.webapp.view;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * EL functions exposed to JSP/tag files. {@link #timeAgo(LocalDateTime)} computes only the relative
 * time bucket (unit + quantity); the localized text is rendered in the view via {@code <spring:message>}.
 */
public final class RelativeTimeFunctions {

    private RelativeTimeFunctions() {
    }

    public static RelativeTime timeAgo(final LocalDateTime createdAt) {
        if (createdAt == null) {
            return null;
        }

        final Duration elapsed = Duration.between(createdAt, LocalDateTime.now());
        if (elapsed.isNegative() || elapsed.toMinutes() < 1) {
            return new RelativeTime("common.timeAgo.now", 0);
        }
        if (elapsed.toHours() < 1) {
            return bucket("common.timeAgo.minute", elapsed.toMinutes());
        }
        if (elapsed.toDays() < 1) {
            return bucket("common.timeAgo.hour", elapsed.toHours());
        }
        if (elapsed.toDays() < 30) {
            return bucket("common.timeAgo.day", elapsed.toDays());
        }
        if (elapsed.toDays() < 365) {
            return bucket("common.timeAgo.month", elapsed.toDays() / 30);
        }
        return bucket("common.timeAgo.year", elapsed.toDays() / 365);
    }

    private static RelativeTime bucket(final String keyPrefix, final long quantity) {
        final String suffix = quantity == 1 ? ".one" : ".many";
        return new RelativeTime(keyPrefix + suffix, quantity);
    }
}
