package ar.edu.itba.paw.webapp.form;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class CommunityPostForm {

    @NotBlank(message = "{validation.communityPost.title.required}")
    @Size(max = 120, message = "{validation.communityPost.title.max}")
    @Pattern(regexp = "^[^\\r\\n]*$", message = "{validation.communityPost.title.singleLine}")
    private String title;

    @NotBlank(message = "{validation.communityPost.body.required}")
    @Size(max = 5000, message = "{validation.communityPost.body.max}")
    private String body;

    public String getTitle() {
        return title;
    }

    public void setTitle(final String title) {
        this.title = title;
    }

    public String getBody() {
        return body;
    }

    public void setBody(final String body) {
        this.body = body;
    }
}
