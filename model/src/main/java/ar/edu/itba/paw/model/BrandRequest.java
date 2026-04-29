package ar.edu.itba.paw.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class BrandRequest implements Serializable {

    private long id;
    private Long submittedByUserId;
    private String submitterEmail;
    private String name;
    private String comments;
    private String status;
    private LocalDateTime createdAt;

    public BrandRequest() {}

    public BrandRequest(final long id, final Long submittedByUserId, final String submitterEmail,
                        final String name, final String comments, final String status,
                        final LocalDateTime createdAt) {
        this.id = id;
        this.submittedByUserId = submittedByUserId;
        this.submitterEmail = submitterEmail;
        this.name = name;
        this.comments = comments;
        this.status = status;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public Long getSubmittedByUserId() {
        return submittedByUserId;
    }

    public void setSubmittedByUserId(final Long submittedByUserId) {
        this.submittedByUserId = submittedByUserId;
    }

    public String getSubmitterEmail() {
        return submitterEmail;
    }

    public void setSubmitterEmail(final String submitterEmail) {
        this.submitterEmail = submitterEmail;
    }

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

    public String getStatus() {
        return status;
    }

    public void setStatus(final String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(final LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
