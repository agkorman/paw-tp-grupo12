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
import javax.persistence.PreUpdate;
import javax.persistence.Table;

@Entity
@Table(name = "review_replies")
public class ReviewReply implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reply_id", nullable = false)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "review_id", nullable = false)
    private Review review;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "body", nullable = false)
    private String body;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false)
    private LocalDateTime updatedAt;

    ReviewReply() {}

    public ReviewReply(final Review review, final User user, final String body) {
        this.review = review;
        this.user = user;
        this.body = body;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    public long getId() { return id; }
    public void setId(final long id) { this.id = id; }

    public Review getReview() { return review; }
    public void setReview(final Review review) { this.review = review; }

    public long getReviewId() { return review.getId(); }

    public User getUser() { return user; }
    public void setUser(final User user) { this.user = user; }

    public long getUserId() { return user.getId(); }

    public String getAuthorUsername() {
        return user != null ? user.getUsername() : null;
    }

    public String getBody() { return body; }
    public void setBody(final String body) { this.body = body; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(final LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(final LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    @PreUpdate
    private void touchUpdatedAt() {
        updatedAt = LocalDateTime.now();
    }

}
