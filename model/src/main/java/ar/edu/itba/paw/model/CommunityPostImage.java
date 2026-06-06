package ar.edu.itba.paw.model;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "community_post_images")
public class CommunityPostImage extends BaseImage {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "post_id")
    private CommunityPost post;

    public CommunityPostImage() {}

    public CommunityPost getPost() {
        return post;
    }

    public void setPost(final CommunityPost post) {
        this.post = post;
    }

    public long getPostId() {
        return post != null ? post.getId() : 0;
    }
}
