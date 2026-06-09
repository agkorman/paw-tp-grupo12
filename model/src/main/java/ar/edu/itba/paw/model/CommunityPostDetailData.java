package ar.edu.itba.paw.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CommunityPostDetailData implements Serializable {

    private final Community community;
    private final CommunityPost post;
    private final Page<CommunityPostComment> commentsPage;
    private final long helpfulCount;
    private final boolean helpfulByCurrentUser;
    private final String viewerRole;
    private final Long viewerUserId;
    private final boolean viewerAdmin;
    private final Map<Long, Long> commentHelpfulCounts;
    private final Map<Long, Boolean> commentHelpfulByCurrentUser;

    public CommunityPostDetailData(final Community community, final CommunityPost post,
                                   final Page<CommunityPostComment> commentsPage,
                                   final long helpfulCount,
                                   final boolean helpfulByCurrentUser,
                                   final Map<Long, Long> commentHelpfulCounts,
                                   final Map<Long, Boolean> commentHelpfulByCurrentUser,
                                   final String viewerRole,
                                   final Long viewerUserId,
                                   final boolean viewerAdmin) {
        this.community = community;
        this.post = post;
        this.commentsPage = commentsPage == null
                ? Page.empty(Pagination.DEFAULT_PAGE, Pagination.REPLIES_PAGE_SIZE)
                : commentsPage;
        this.helpfulCount = helpfulCount;
        this.helpfulByCurrentUser = helpfulByCurrentUser;
        this.commentHelpfulCounts = commentHelpfulCounts == null
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(new LinkedHashMap<>(commentHelpfulCounts));
        this.commentHelpfulByCurrentUser = commentHelpfulByCurrentUser == null
                ? Collections.emptyMap()
                : Collections.unmodifiableMap(new LinkedHashMap<>(commentHelpfulByCurrentUser));
        this.viewerRole = viewerRole;
        this.viewerUserId = viewerUserId;
        this.viewerAdmin = viewerAdmin;
    }

    public Community getCommunity() {
        return community;
    }

    public CommunityPost getPost() {
        return post;
    }

    public List<CommunityPostComment> getComments() {
        return new ArrayList<>(commentsPage.getItems());
    }

    public Page<CommunityPostComment> getCommentsPage() {
        return commentsPage;
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
        return commentsPage.getTotalItems();
    }

    public long getHelpfulCountForComment(final long commentId) {
        return commentHelpfulCounts.getOrDefault(commentId, 0L);
    }

    public boolean isHelpfulByCurrentUserForComment(final long commentId) {
        return commentHelpfulByCurrentUser.getOrDefault(commentId, Boolean.FALSE);
    }

    public String getViewerRole() {
        return viewerRole;
    }

    public boolean isViewerModerator() {
        return CommunityRole.MODERATOR.equals(viewerRole);
    }

    public boolean isViewerMember() {
        return CommunityRole.MEMBER.equals(viewerRole) || CommunityRole.MODERATOR.equals(viewerRole);
    }

    public boolean isViewerAdmin() {
        return viewerAdmin;
    }

    public boolean isPostDeletableByViewer() {
        return viewerUserId != null && post != null && post.getAuthorUserId() == viewerUserId;
    }

    // Authorship grants both editing and deletion; moderators/admins can only hide.
    public boolean isPostEditableByViewer() {
        return isPostDeletableByViewer();
    }

    public boolean isPostHideableByViewer() {
        return post != null && !isPostDeletableByViewer() && (viewerAdmin || isViewerModerator());
    }

    public boolean isCommentDeletableByViewer(final CommunityPostComment comment) {
        return viewerUserId != null && comment != null && comment.getUserId() == viewerUserId;
    }

    // Authorship grants both editing and deletion; moderators/admins can only hide.
    public boolean isCommentEditableByViewer(final CommunityPostComment comment) {
        return isCommentDeletableByViewer(comment);
    }

    public boolean isCommentHideableByViewer(final CommunityPostComment comment) {
        return comment != null && !isCommentDeletableByViewer(comment) && (viewerAdmin || isViewerModerator());
    }
}
