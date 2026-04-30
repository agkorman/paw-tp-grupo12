package ar.edu.itba.paw.webapp.util;

import java.time.Duration;
import java.time.LocalDateTime;

public final class RelativeTimeFormatter {

    private RelativeTimeFormatter() { }

    public static String timeAgo(final LocalDateTime createdAt) {
        if (createdAt == null) {
            return "";
        }

        final Duration elapsed = Duration.between(createdAt, LocalDateTime.now());
        if (elapsed.isNegative() || elapsed.toMinutes() < 1) {
            return "recién";
        }
        if (elapsed.toHours() < 1) {
            final long minutes = elapsed.toMinutes();
            return "hace " + minutes + " " + (minutes == 1 ? "minuto" : "minutos");
        }
        if (elapsed.toDays() < 1) {
            final long hours = elapsed.toHours();
            return "hace " + hours + " " + (hours == 1 ? "hora" : "horas");
        }
        if (elapsed.toDays() < 30) {
            final long days = elapsed.toDays();
            return "hace " + days + " " + (days == 1 ? "día" : "días");
        }
        if (elapsed.toDays() < 365) {
            final long months = elapsed.toDays() / 30;
            return "hace " + months + " " + (months == 1 ? "mes" : "meses");
        }

        final long years = elapsed.toDays() / 365;
        return "hace " + years + " " + (years == 1 ? "año" : "años");
    }
}
