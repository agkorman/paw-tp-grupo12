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
@Table(name = "review_replies")
public class ReviewReply implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reply_id")
    private long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "review_id")
    private Review review;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "body")
    private String body;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public ReviewReply() {}

    public ReviewReply(final long id, final long reviewId, final long userId, final String authorUsername,
                       final String body, final LocalDateTime createdAt, final LocalDateTime updatedAt) {
        this.id = id;
        this.review = reviewReference(reviewId);
        this.user = userReference(userId, authorUsername);
        this.body = body;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public long getId() { return id; }
    public void setId(final long id) { this.id = id; }

    public Review getReview() { return review; }
    public void setReview(final Review review) { this.review = review; }

    public long getReviewId() { return review != null ? review.getId() : 0; }
    public void setReviewId(final long reviewId) { this.review = reviewReference(reviewId); }

    public User getUser() { return user; }
    public void setUser(final User user) { this.user = user; }

    public long getUserId() { return user != null ? user.getId() : 0; }
    public void setUserId(final long userId) { this.user = userReference(userId, null); }

    public String getAuthorUsername() {
        return user != null ? user.getUsername() : null;
    }
    public void setAuthorUsername(final String authorUsername) {
        if (user != null) {
            user.setUsername(authorUsername);
        }
    }

    public String getBody() { return body; }
    public void setBody(final String body) { this.body = body; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(final LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(final LocalDateTime updatedAt) { this.updatedAt = updatedAt; }

    private static Review reviewReference(final long id) {
        final Review review = new Review();
        review.setId(id);
        return review;
    }

    private static User userReference(final long id, final String username) {
        final User user = new User();
        user.setId(id);
        user.setUsername(username);
        return user;
    }
}
