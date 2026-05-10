package ar.edu.itba.paw.model;

import java.io.Serializable;

public class EmailRecipient implements Serializable {

    private String email;
    private String preferredLocale;

    public EmailRecipient() {
    }

    public EmailRecipient(final String email, final String preferredLocale) {
        this.email = email;
        this.preferredLocale = preferredLocale;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getPreferredLocale() {
        return preferredLocale;
    }

    public void setPreferredLocale(final String preferredLocale) {
        this.preferredLocale = preferredLocale;
    }
}
