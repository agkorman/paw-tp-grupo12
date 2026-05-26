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

    private String q;
    private String topic;
    private Boolean joinedOnly;
    private String sortBy;
    private Integer page;

    public CommunitySearchCriteria() {}

    public boolean hasAdvancedFilters() {
        return Boolean.TRUE.equals(joinedOnly);
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
