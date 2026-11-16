package ar.edu.itba.paw.webapp.api.users;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.io.Serializable;
import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

/** Omitted fields are unchanged. Explicit null is invalid for these non-nullable profile fields. */
public class UserPatchRequest implements Serializable {

    @Size(min = 1, max = 50, message = "{validation.user.displayName.max}")
    @Pattern(regexp = "[A-Za-z0-9._-]+", message = "{validation.user.displayName.pattern}")
    private String displayName;

    @Pattern(regexp = "es|en", message = "{validation.user.preferredLocale.invalid}")
    private String preferredLocale;

    public String getDisplayName() { return displayName; }
    @JsonSetter(nulls = Nulls.FAIL)
    public void setDisplayName(final String value) { displayName = value.trim(); }
    public String getPreferredLocale() { return preferredLocale; }
    @JsonSetter(nulls = Nulls.FAIL)
    public void setPreferredLocale(final String value) { preferredLocale = value.trim(); }

    @JsonIgnore
    @AssertTrue(message = "{validation.user.patch.empty}")
    public boolean isChangePresent() { return displayName != null || preferredLocale != null; }
}
