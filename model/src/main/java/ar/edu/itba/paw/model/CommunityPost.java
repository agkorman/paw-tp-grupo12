package ar.edu.itba.paw.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
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
import javax.persistence.Transient;

@Entity
@Table(name = "community_posts")
public class CommunityPost implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id", nullable = false)
    private long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "community_id", nullable = false)
    private Community community;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "author_user_id", nullable = false)
    private User author;

    @Column(name = "slug", nullable = false, length = 80)
    private String slug;

    @Column(name = "title", nullable = false, length = 120)
    private String title;

    @Column(name = "body", nullable = true)
    private String body;

    @Column(name = "external_url", nullable = true, length = 2048)
    private String externalUrl;

    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "linked_review_id", nullable = true)
    private Review linkedReview;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false, insertable = false)
    private LocalDateTime updatedAt;

    @Column(name = "hidden", nullable = false)
    private boolean hidden;

    @Transient
    private List<ImageMetadata> images = new ArrayList<>();

    public CommunityPost() {}

    public CommunityPost(final Community community, final User author, final String slug,
                         final String title, final boolean hidden) {
        this();
        this.community = community;
        this.author = author;
        this.slug = slug;
        this.title = title;
        this.hidden = hidden;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

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
        return community.getId();
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

    public List<ImageMetadata> getImages() {
        return images;
    }

    public void setImages(final List<ImageMetadata> images) {
        this.images = images == null ? new ArrayList<>() : images;
    }

    @PreUpdate
    private void touchUpdatedAt() {
        updatedAt = LocalDateTime.now();
    }
}
