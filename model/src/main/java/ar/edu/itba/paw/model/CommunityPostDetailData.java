package ar.edu.itba.paw.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CommunityPostDetailData implements Serializable {

    private final Community community;
    private final CommunityPost post;
    private final List<CommunityPostComment> comments;
    private final long helpfulCount;
    private final boolean helpfulByCurrentUser;
    private final long commentCount;
    private final String viewerRole;
    private final Long viewerUserId;

    public CommunityPostDetailData(final Community community, final CommunityPost post,
                                   final List<CommunityPostComment> comments,
                                   final long helpfulCount,
                                   final boolean helpfulByCurrentUser,
                                   final long commentCount,
                                   final String viewerRole,
                                   final Long viewerUserId) {
        this.community = community;
        this.post = post;
        this.comments = comments == null ? new ArrayList<>() : new ArrayList<>(comments);
        this.helpfulCount = helpfulCount;
        this.helpfulByCurrentUser = helpfulByCurrentUser;
        this.commentCount = commentCount;
        this.viewerRole = viewerRole;
        this.viewerUserId = viewerUserId;
    }

    public Community getCommunity() {
        return community;
    }

    public CommunityPost getPost() {
        return post;
    }

    public List<CommunityPostComment> getComments() {
        return new ArrayList<>(comments);
    }

    public long getHelpfulCount() {
        return helpfulCount;
    }

    public boolean isHelpfulByCurrentUser() {
        return helpfulByCurrentUser;
    }

    public boolean getHelpfulByCurrentUser() {
        return helpfulByCurrentUser;
    }

    public long getCommentCount() {
        return commentCount;
    }

    public String getViewerRole() {
        return viewerRole;
    }

    public boolean isViewerModerator() {
        return "moderator".equals(viewerRole);
    }

    public boolean isViewerMember() {
        return "member".equals(viewerRole) || "moderator".equals(viewerRole);
    }

    public boolean isPostDeletableByViewer() {
        return viewerUserId != null && post != null && post.getAuthorUserId() == viewerUserId;
    }

    public boolean isCommentDeletableByViewer(final CommunityPostComment comment) {
        return viewerUserId != null && comment != null && comment.getUserId() == viewerUserId;
    }
}
