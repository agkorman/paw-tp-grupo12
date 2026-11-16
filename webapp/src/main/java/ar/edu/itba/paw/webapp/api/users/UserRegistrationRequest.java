package ar.edu.itba.paw.webapp.api.users;

import ar.edu.itba.paw.webapp.validation.Utf8Size;
import java.io.Serializable;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/** Registration input only. Password confirmation remains a client-side form concern. */
public class UserRegistrationRequest implements Serializable {

    @NotBlank(message = "{validation.user.displayName.required}")
    @Size(max = 50, message = "{validation.user.displayName.max}")
    @Pattern(regexp = "[A-Za-z0-9._-]+", message = "{validation.user.displayName.pattern}")
    private String displayName;

    @NotBlank(message = "{validation.user.email.required}")
    @Email(message = "{validation.email.invalid}")
    @Size(max = 100, message = "{validation.email.max}")
    private String email;

    @NotBlank(message = "{validation.user.password.required}")
    @Size(min = 8, max = 72, message = "{validation.user.password.size}")
    @Utf8Size(max = 72)
    private String password;

    public String getDisplayName() { return displayName; }
    public void setDisplayName(final String value) { displayName = value == null ? null : value.trim(); }
    public String getEmail() { return email; }
    public void setEmail(final String value) { email = value == null ? null : value.trim(); }
    public String getPassword() { return password; }
    public void setPassword(final String value) { password = value; }
}
