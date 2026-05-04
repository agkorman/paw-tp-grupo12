package ar.edu.itba.paw.webapp.form;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

public class CatalogNameRequestForm {

    @NotBlank(message = "{validation.catalogRequest.name.required}")
    @Size(max = 80, message = "{validation.catalogRequest.name.max}")
    private String name;

    @Size(max = 500, message = "{validation.catalogRequest.comments.max}")
    private String comments;

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(final String comments) {
        this.comments = comments;
    }
}
