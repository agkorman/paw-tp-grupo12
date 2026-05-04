package ar.edu.itba.paw.webapp.form;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class ProfileForm {

    @NotBlank(message = "{profile.edit.error.username.required}")
    @Size(max = 50, message = "{profile.edit.error.username.max}")
    @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "{profile.edit.error.username.pattern}")
    private String displayName;

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(final String displayName) {
        this.displayName = displayName;
    }
}
