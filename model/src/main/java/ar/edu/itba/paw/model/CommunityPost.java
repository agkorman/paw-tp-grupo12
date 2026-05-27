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
@Table(name = "community_posts")
public class CommunityPost implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "community_id")
    private Community community;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_user_id")
    private User author;

    @Column(name = "slug")
    private String slug;

    @Column(name = "title")
    private String title;

    @Column(name = "body")
    private String body;

    @Column(name = "external_url")
    private String externalUrl;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "linked_review_id")
    private Review linkedReview;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false)
    private LocalDateTime updatedAt;

    @Column(name = "hidden")
    private boolean hidden;

    public CommunityPost() {}

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public Community getCommunity() {
        return community;
    }

    public void setCommunity(final Community community) {
        this.community = community;
    }

    public long getCommunityId() {
        return community != null ? community.getId() : 0L;
    }

    public User getAuthor() {
        return author;
    }

    public void setAuthor(final User author) {
        this.author = author;
    }

    public long getAuthorUserId() {
        return author != null ? author.getId() : 0L;
    }

    public String getAuthorUsername() {
        return author != null ? author.getUsername() : null;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(final String slug) {
        this.slug = slug;
    }

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

    public String getExternalUrl() {
        return externalUrl;
    }

    public void setExternalUrl(final String externalUrl) {
        this.externalUrl = externalUrl;
    }

    public Review getLinkedReview() {
        return linkedReview;
    }

    public void setLinkedReview(final Review linkedReview) {
        this.linkedReview = linkedReview;
    }

    public Long getLinkedReviewId() {
        return linkedReview != null ? linkedReview.getId() : null;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(final LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(final LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public boolean isHidden() {
        return hidden;
    }

    public void setHidden(final boolean hidden) {
        this.hidden = hidden;
    }

    @PreUpdate
    private void touchUpdatedAt() {
        updatedAt = LocalDateTime.now();
    }
}
