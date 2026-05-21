package ar.edu.itba.paw.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class CommunityDetailData implements Serializable {

    private final Community community;
    private final List<CommunityTopic> topics;
    private final List<CommunityPostSummary> posts;
    private final long memberCount;
    private final long weeklyPostCount;
    private final boolean joined;

    public CommunityDetailData(final Community community, final List<CommunityTopic> topics,
                               final List<CommunityPostSummary> posts,
                               final long memberCount, final long weeklyPostCount,
                               final boolean joined) {
        this.community = community;
        this.topics = topics == null ? new ArrayList<>() : new ArrayList<>(topics);
        this.posts = posts == null ? new ArrayList<>() : new ArrayList<>(posts);
        this.memberCount = memberCount;
        this.weeklyPostCount = weeklyPostCount;
        this.joined = joined;
    }

    public Community getCommunity() {
        return community;
    }

    public List<CommunityTopic> getTopics() {
        return new ArrayList<>(topics);
    }

    public List<CommunityPostSummary> getPosts() {
        return new ArrayList<>(posts);
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
}
