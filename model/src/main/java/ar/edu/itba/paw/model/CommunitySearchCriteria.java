package ar.edu.itba.paw.model;

import java.io.Serializable;
import java.util.Locale;
import java.util.Set;

public class CommunitySearchCriteria implements Serializable {

    public static final String SORT_ACTIVE = "active";
    public static final String SORT_MEMBERS = "members";
    public static final String SORT_NAME_ASC = "name_asc";
    public static final String SORT_NEWEST = "newest";

    public static final Set<String> ALLOWED_SORT_BY = Set.of(
            SORT_ACTIVE,
            SORT_MEMBERS,
            SORT_NAME_ASC,
            SORT_NEWEST
    );

    public static final String MEMBERSHIP_ALL      = "";
    public static final String MEMBERSHIP_JOINED   = "joined";
    public static final String MEMBERSHIP_NOT_JOINED = "not_joined";

    private String q;
    private String topic;
    private Boolean joinedOnly;
    private Boolean notJoinedOnly;
    private String sortBy;
    private Integer page;

    public CommunitySearchCriteria() {}

    public boolean hasAdvancedFilters() {
        return false;
    }

    public boolean isValid() {
        return sortBy == null || ALLOWED_SORT_BY.contains(sortBy);
    }

    public String getQ() {
        return q;
    }

    public void setQ(final String q) {
        this.q = q == null || q.trim().isEmpty() ? null : q.trim();
    }

    public String getTopic() {
        return topic;
    }

    public void setTopic(final String topic) {
        this.topic = topic == null || topic.trim().isEmpty()
                ? null
                : topic.trim().toLowerCase(Locale.ROOT);
    }

    public Boolean getJoinedOnly() {
        return joinedOnly;
    }

    public boolean isJoinedOnly() {
        return Boolean.TRUE.equals(joinedOnly);
    }

    public void setJoinedOnly(final Boolean joinedOnly) {
        this.joinedOnly = Boolean.TRUE.equals(joinedOnly);
    }

    public void setJoinedOnly(final String joinedOnly) {
        this.joinedOnly = "true".equalsIgnoreCase(joinedOnly) || "on".equalsIgnoreCase(joinedOnly);
    }

    public boolean isNotJoinedOnly() {
        return Boolean.TRUE.equals(notJoinedOnly);
    }

    public Boolean getNotJoinedOnly() {
        return notJoinedOnly;
    }

    public void setNotJoinedOnly(final Boolean notJoinedOnly) {
        this.notJoinedOnly = Boolean.TRUE.equals(notJoinedOnly);
    }

    /**
     * Convenience accessor for the toolbar membership dropdown.
     * Returns {@code "joined"}, {@code "not_joined"}, or {@code ""} (all).
     */
    public String getMembership() {
        if (Boolean.TRUE.equals(joinedOnly)) {
            return MEMBERSHIP_JOINED;
        }
        if (Boolean.TRUE.equals(notJoinedOnly)) {
            return MEMBERSHIP_NOT_JOINED;
        }
        return MEMBERSHIP_ALL;
    }

    /**
     * Accepts values {@code "joined"}, {@code "not_joined"}, or anything else for "all".
     * Updates the underlying {@code joinedOnly} / {@code notJoinedOnly} booleans.
     */
    public void setMembership(final String membership) {
        if (MEMBERSHIP_JOINED.equalsIgnoreCase(membership)) {
            this.joinedOnly    = true;
            this.notJoinedOnly = false;
        } else if (MEMBERSHIP_NOT_JOINED.equalsIgnoreCase(membership)) {
            this.joinedOnly    = false;
            this.notJoinedOnly = true;
        } else {
            this.joinedOnly    = false;
            this.notJoinedOnly = false;
        }
    }

    public String getSortBy() {
        return sortBy;
    }

    public void setSortBy(final String sortBy) {
        this.sortBy = sortBy == null || sortBy.trim().isEmpty()
                ? null
                : sortBy.trim().toLowerCase(Locale.ROOT);
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(final Integer page) {
        this.page = page;
    }
}
