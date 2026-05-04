package ar.edu.itba.paw.webapp.form;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class AdminRoleRequestForm {

    @NotBlank(message = "{validation.adminRequest.motivation.required}")
    @Size(max = 2000, message = "{validation.adminRequest.motivation.max}")
    private String motivation;

    @NotBlank(message = "{validation.adminRequest.bio.required}")
    @Size(max = 2000, message = "{validation.adminRequest.bio.max}")
    private String bio;

    @NotBlank(message = "{validation.adminRequest.justification.required}")
    @Size(max = 2000, message = "{validation.adminRequest.justification.max}")
    private String justification;

    public String getMotivation() {
        return motivation;
    }

    public void setMotivation(final String motivation) {
        this.motivation = motivation;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(final String bio) {
        this.bio = bio;
    }

    public String getJustification() {
        return justification;
    }

    public void setJustification(final String justification) {
        this.justification = justification;
    }
}
