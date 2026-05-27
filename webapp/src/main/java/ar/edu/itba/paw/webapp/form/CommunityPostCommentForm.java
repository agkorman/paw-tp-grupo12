package ar.edu.itba.paw.webapp.form;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class CommunityPostCommentForm {

    @NotBlank(message = "{validation.communityPostComment.body.required}")
    @Size(max = 1000, message = "{validation.communityPostComment.body.max}")
    private String body;

    public String getBody() {
        return body;
    }

    public void setBody(final String body) {
        this.body = body;
    }
}
