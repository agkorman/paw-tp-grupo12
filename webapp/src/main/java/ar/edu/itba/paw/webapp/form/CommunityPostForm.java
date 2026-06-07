package ar.edu.itba.paw.webapp.form;

import java.util.ArrayList;
import java.util.List;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public class CommunityPostForm {

    @NotBlank(message = "{validation.communityPost.title.required}")
    @Size(max = 120, message = "{validation.communityPost.title.max}")
    @Pattern(regexp = "^[^\\r\\n]*$", message = "{validation.communityPost.title.singleLine}")
    private String title;

    @NotBlank(message = "{validation.communityPost.body.required}")
    @Size(max = 5000, message = "{validation.communityPost.body.max}")
    private String body;

    private List<MultipartFile> files = new ArrayList<>();

    private List<Long> retainedImageIds = new ArrayList<>();

    private Long linkedReviewId;

    private String communitySlug;

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

    public List<MultipartFile> getFiles() {
        return files;
    }

    public void setFiles(final List<MultipartFile> files) {
        this.files = files == null ? new ArrayList<>() : files;
    }

    public List<Long> getRetainedImageIds() {
        return retainedImageIds;
    }

    public void setRetainedImageIds(final List<Long> retainedImageIds) {
        this.retainedImageIds = retainedImageIds == null ? new ArrayList<>() : retainedImageIds;
    }

    public Long getLinkedReviewId() {
        return linkedReviewId;
    }

    public void setLinkedReviewId(final Long linkedReviewId) {
        this.linkedReviewId = linkedReviewId;
    }

    public String getCommunitySlug() {
        return communitySlug;
    }

    public void setCommunitySlug(final String communitySlug) {
        this.communitySlug = communitySlug;
    }
}
