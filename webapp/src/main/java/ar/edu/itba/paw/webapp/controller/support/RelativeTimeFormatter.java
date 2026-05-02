package ar.edu.itba.paw.webapp.controller.support;

import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;

@Component
public class RelativeTimeFormatter {

    private final MessageSource messageSource;

    public RelativeTimeFormatter(final MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public String format(final LocalDateTime createdAt) {
        if (createdAt == null) {
            return "";
        }

        final Duration elapsed = Duration.between(createdAt, LocalDateTime.now());
        if (elapsed.isNegative() || elapsed.toMinutes() < 1) {
            return message("common.timeAgo.now");
        }
        if (elapsed.toHours() < 1) {
            return quantityMessage("common.timeAgo.minute", elapsed.toMinutes());
        }
        if (elapsed.toDays() < 1) {
            return quantityMessage("common.timeAgo.hour", elapsed.toHours());
        }
        if (elapsed.toDays() < 30) {
            return quantityMessage("common.timeAgo.day", elapsed.toDays());
        }
        if (elapsed.toDays() < 365) {
            return quantityMessage("common.timeAgo.month", elapsed.toDays() / 30);
        }
        return quantityMessage("common.timeAgo.year", elapsed.toDays() / 365);
    }

    private String quantityMessage(final String keyPrefix, final long quantity) {
        final String suffix = quantity == 1 ? ".one" : ".many";
        return message(keyPrefix + suffix, quantity);
    }

    private String message(final String key, final Object... arguments) {
        final Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(key, arguments, locale);
    }
}
