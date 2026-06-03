package ar.edu.itba.paw.model;

import java.io.Serializable;

public class ActivityFeedReference implements Serializable {

    public static final String TYPE_REVIEW = "review";
    public static final String TYPE_COMMUNITY_POST = "communityPost";

    private final String type;
    private final long itemId;

    public ActivityFeedReference(final String type, final long itemId) {
        this.type = type;
        this.itemId = itemId;
    }

    public String getType() {
        return type;
    }

    public long getItemId() {
        return itemId;
    }

    public boolean isReview() {
        return TYPE_REVIEW.equals(type);
    }

    public boolean isCommunityPost() {
        return TYPE_COMMUNITY_POST.equals(type);
    }
}
