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
@Table(name = "admin_requests")
public class AdminRequest implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "admin_request_id")
    private long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submitted_by_user_id")
    private User submittedByUser;

    @Column(name = "submitter_email")
    private String submitterEmail;

    @Column(name = "motivation")
    private String motivation;

    @Column(name = "bio")
    private String bio;

    @Column(name = "justification")
    private String justification;

    @Column(name = "status")
    private String status;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public AdminRequest() {}

    public AdminRequest(final long id, final long submittedByUserId, final String submitterEmail,
                        final String motivation, final String bio, final String justification,
                        final String status, final LocalDateTime createdAt) {
        this.id = id;
        this.submittedByUser = userReference(submittedByUserId);
        this.submitterEmail = submitterEmail;
        this.motivation = motivation;
        this.bio = bio;
        this.justification = justification;
        this.status = status;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public long getSubmittedByUserId() {
        return submittedByUser != null ? submittedByUser.getId() : 0;
    }

    public void setSubmittedByUserId(final long submittedByUserId) {
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

    public String getMotivation() {
        return motivation;
    }

    public void setMotivation(final String motivation) {
        this.motivation = motivation;
    }

    public String getBio() {
        return bio;
    }

    public void setBio(final String bio) {
        this.bio = bio;
    }

    public String getJustification() {
        return justification;
    }

    public void setJustification(final String justification) {
        this.justification = justification;
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

    private static User userReference(final long id) {
        final User user = new User();
        user.setId(id);
        return user;
    }
}
