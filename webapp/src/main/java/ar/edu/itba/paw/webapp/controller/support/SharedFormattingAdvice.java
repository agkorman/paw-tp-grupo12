package ar.edu.itba.paw.webapp.controller.support;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
public class SharedFormattingAdvice {

    private final RelativeTimeFormatter relativeTimeFormatter;

    public SharedFormattingAdvice(final RelativeTimeFormatter relativeTimeFormatter) {
        this.relativeTimeFormatter = relativeTimeFormatter;
    }

    @ModelAttribute("relativeTimeFormatter")
    public RelativeTimeFormatter relativeTimeFormatter() {
        return relativeTimeFormatter;
    }
}
