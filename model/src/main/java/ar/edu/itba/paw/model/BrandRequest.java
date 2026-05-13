package ar.edu.itba.paw.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "brand_requests")
public class BrandRequest implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "brand_request_id")
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submitted_by_user_id")
    private User submittedByUser;

    @Column(name = "submitter_email")
    private String submitterEmail;

    @Column(name = "name")
    private String name;

    @Column(name = "comments")
    private String comments;

    @Column(name = "status")
    private String status;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public BrandRequest() {}

    public BrandRequest(final long id, final Long submittedByUserId, final String submitterEmail,
                        final String name, final String comments, final String status,
                        final LocalDateTime createdAt) {
        this.id = id;
        this.submittedByUser = userReference(submittedByUserId);
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
        return submittedByUser != null ? submittedByUser.getId() : null;
    }

    public void setSubmittedByUserId(final Long submittedByUserId) {
        this.submittedByUser = userReference(submittedByUserId);
    }

    public User getSubmittedByUser() {
        return submittedByUser;
    }

    public void setSubmittedByUser(final User submittedByUser) {
        this.submittedByUser = submittedByUser;
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

    private static User userReference(final Long id) {
        if (id == null) {
            return null;
        }
        final User user = new User();
        user.setId(id);
        return user;
    }
}
