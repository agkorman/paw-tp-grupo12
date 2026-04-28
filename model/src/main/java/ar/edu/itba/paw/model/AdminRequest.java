package ar.edu.itba.paw.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class AdminRequest implements Serializable {

    private long id;
    private long submittedByUserId;
    private String submitterEmail;
    private String motivation;
    private String bio;
    private String justification;
    private String status;
    private LocalDateTime createdAt;

    public AdminRequest() {}

    public AdminRequest(final long id, final long submittedByUserId, final String submitterEmail,
                        final String motivation, final String bio, final String justification,
                        final String status, final LocalDateTime createdAt) {
        this.id = id;
        this.submittedByUserId = submittedByUserId;
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
        return submittedByUserId;
    }

    public void setSubmittedByUserId(final long submittedByUserId) {
        this.submittedByUserId = submittedByUserId;
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
}
