package ar.edu.itba.paw.webapp.form;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class ReviewReplyForm {

    @NotBlank(message = "{review.reply.body.required}")
    @Size(max = 1000, message = "{validation.review.reply.body.max}")
    private String body;

    public String getBody() {
        return body;
    }

    public void setBody(final String body) {
        this.body = body;
    }
}
