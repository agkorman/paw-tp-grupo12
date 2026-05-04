package ar.edu.itba.paw.webapp.form;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class RegistrationForm {

    @NotBlank(message = "{auth.register.error.username.required}")
    @Size(max = 50, message = "{auth.register.error.username.max}")
    @Pattern(regexp = "^[A-Za-z0-9._-]+$", message = "{auth.register.error.username.pattern}")
    private String username;

    @NotBlank(message = "{auth.register.error.email.required}")
    @Email(message = "{auth.register.error.email.invalid}")
    @Size(max = 100, message = "{validation.email.max}")
    private String email;

    @NotBlank(message = "{auth.register.error.password.min}")
    @Size.List({
            @Size(min = 8, message = "{auth.register.error.password.min}"),
            @Size(max = 72, message = "{auth.register.error.password.max}")
    })
    private String password;

    private String confirmPassword;

    @AssertTrue(message = "{auth.register.error.password.mismatch}")
    public boolean isPasswordConfirmationMatching() {
        return password != null && password.equals(confirmPassword);
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(final String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(final String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(final String password) {
        this.password = password;
    }

    public String getConfirmPassword() {
        return confirmPassword;
    }

    public void setConfirmPassword(final String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }
}
