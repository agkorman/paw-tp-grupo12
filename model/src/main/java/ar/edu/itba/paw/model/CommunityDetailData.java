package ar.edu.itba.paw.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CommunityDetailData implements Serializable {

    private final Community community;
    private final List<CommunityTopic> topics;
    private final Page<CommunityPostSummary> posts;
    private final long memberCount;
    private final long weeklyPostCount;
    private final boolean joined;
    private final String viewerRole;
    private final String currentSort;
    private final boolean viewerCreator;

    public CommunityDetailData(final Community community, final List<CommunityTopic> topics,
                               final List<CommunityPostSummary> posts,
                               final long memberCount, final long weeklyPostCount,
                               final boolean joined, final String viewerRole,
                               final String currentSort, final boolean viewerCreator) {
        this(community, topics, new Page<>(
                posts == null ? new ArrayList<>() : new ArrayList<>(posts),
                Pagination.DEFAULT_PAGE,
                posts == null ? Pagination.COMMUNITY_POSTS_PAGE_SIZE : posts.size(),
                posts == null ? 0L : posts.size()
        ), memberCount, weeklyPostCount, joined, viewerRole, currentSort, viewerCreator);
    }

    public CommunityDetailData(final Community community, final List<CommunityTopic> topics,
                               final Page<CommunityPostSummary> posts,
                               final long memberCount, final long weeklyPostCount,
                               final boolean joined, final String viewerRole,
                               final String currentSort, final boolean viewerCreator) {
        this.community = community;
        this.topics = topics == null ? new ArrayList<>() : new ArrayList<>(topics);
        this.posts = posts == null ? Page.empty(Pagination.DEFAULT_PAGE, Pagination.COMMUNITY_POSTS_PAGE_SIZE) : posts;
        this.memberCount = memberCount;
        this.weeklyPostCount = weeklyPostCount;
        this.joined = joined;
        this.viewerRole = viewerRole;
        this.currentSort = currentSort;
        this.viewerCreator = viewerCreator;
    }

    public Community getCommunity() {
        return community;
    }

    public List<CommunityTopic> getTopics() {
        return new ArrayList<>(topics);
    }

    public List<CommunityPostSummary> getPosts() {
        return new ArrayList<>(posts.getItems());
    }

    public Page<CommunityPostSummary> getPostsPage() {
        return posts;
    }

    public long getMemberCount() {
        return memberCount;
    }

    public long getWeeklyPostCount() {
        return weeklyPostCount;
    }

    public boolean isJoined() {
        return joined;
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

    public String getCurrentSort() {
        return currentSort;
    }

    public boolean isViewerCreator() {
        return viewerCreator;
    }
}
