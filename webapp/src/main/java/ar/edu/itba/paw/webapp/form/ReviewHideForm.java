package ar.edu.itba.paw.webapp.form;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class ReviewHideForm {

    @NotBlank(message = "{review.hide.reason.required}")
    @Size(min = 10, max = 600, message = "{validation.review.hide.reason.size}")
    private String reason;

    public String getReason() {
        return reason;
    }

    public void setReason(final String reason) {
        this.reason = reason;
    }
}
