package ar.edu.itba.paw.webapp.form;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class CommunityHideForm {

    @NotBlank(message = "{communities.hide.reason.required}")
    @Size(min = 10, max = 600, message = "{validation.communityHide.reason.size}")
    private String reason;

    public String getReason() {
        return reason;
    }

    public void setReason(final String reason) {
        this.reason = reason;
    }
}
