package ar.edu.itba.paw.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class ReviewReply implements Serializable {

    private long id;
    private long reviewId;
    private long userId;
    private String authorUsername;
    private String body;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ReviewReply() {}

    public ReviewReply(final long id, final long reviewId, final long userId, final String authorUsername,
                       final String body, final LocalDateTime createdAt, final LocalDateTime updatedAt) {
        this.id = id;
        this.reviewId = reviewId;
        this.userId = userId;
        this.authorUsername = authorUsername;
        this.body = body;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public long getId() { return id; }
    public void setId(final long id) { this.id = id; }

    public long getReviewId() { return reviewId; }
    public void setReviewId(final long reviewId) { this.reviewId = reviewId; }

    public long getUserId() { return userId; }
    public void setUserId(final long userId) { this.userId = userId; }

    public String getAuthorUsername() { return authorUsername; }
    public void setAuthorUsername(final String authorUsername) { this.authorUsername = authorUsername; }

    public String getBody() { return body; }
    public void setBody(final String body) { this.body = body; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(final LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(final LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
