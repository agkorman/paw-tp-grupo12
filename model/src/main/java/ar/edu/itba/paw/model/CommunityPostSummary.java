package ar.edu.itba.paw.model;

import java.io.Serializable;

public class CommunityPostSummary implements Serializable {

    private final CommunityPost post;
    private final long helpfulCount;
    private final long commentCount;

    public CommunityPostSummary(final CommunityPost post, final long helpfulCount, final long commentCount) {
        this.post = post;
        this.helpfulCount = helpfulCount;
        this.commentCount = commentCount;
    }

    public CommunityPost getPost() {
        return post;
    }

    public long getHelpfulCount() {
        return helpfulCount;
    }

    public long getCommentCount() {
        return commentCount;
    }
}
