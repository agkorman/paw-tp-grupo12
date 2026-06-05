package ar.edu.itba.paw.model;

import java.io.Serializable;
import java.util.Locale;
import java.util.Set;

/**
 * Bound as a {@code @ModelAttribute} on {@code GET /activity}. Holds the normalized
 * content-type filter, recency timeframe, ranking sort, and page for the mixed
 * activity feed. Getters return a safe default for blank or unknown values so the
 * feed and the filter form always have a valid selection.
 */
public class ActivityFeedCriteria implements Serializable {

    public static final String TYPE_ALL = "all";
    public static final String TYPE_REVIEWS = "reviews";
    public static final String TYPE_COMMUNITY = "community";

    public static final Set<String> ALLOWED_TYPE = Set.of(TYPE_ALL, TYPE_REVIEWS, TYPE_COMMUNITY);

    public static final String TIMEFRAME_ALL = "all";
    public static final String TIMEFRAME_TODAY = "today";
    public static final String TIMEFRAME_WEEK = "week";
    public static final String TIMEFRAME_MONTH = "month";

    public static final Set<String> ALLOWED_TIMEFRAME =
            Set.of(TIMEFRAME_ALL, TIMEFRAME_TODAY, TIMEFRAME_WEEK, TIMEFRAME_MONTH);

    public static final String SORT_TRENDING = "trending";
    public static final String SORT_CONTROVERSIAL = "controversial";
    public static final String SORT_LATEST = "latest";

    public static final Set<String> ALLOWED_SORT = Set.of(SORT_TRENDING, SORT_CONTROVERSIAL, SORT_LATEST);

    private String type;
    private String timeframe;
    private String sort;
    private Integer page;

    public ActivityFeedCriteria() {}

    /**
     * Whether the bound values are recognized. Getters fall back to defaults regardless,
     * so this is informational for the controller boundary rather than a hard gate.
     */
    public boolean isValid() {
        return (type == null || ALLOWED_TYPE.contains(type))
                && (timeframe == null || ALLOWED_TIMEFRAME.contains(timeframe))
                && (sort == null || ALLOWED_SORT.contains(sort));
    }

    public String getType() {
        return type != null && ALLOWED_TYPE.contains(type) ? type : TYPE_ALL;
    }

    public void setType(final String type) {
        this.type = normalize(type);
    }

    public String getTimeframe() {
        return timeframe != null && ALLOWED_TIMEFRAME.contains(timeframe) ? timeframe : TIMEFRAME_ALL;
    }

    public void setTimeframe(final String timeframe) {
        this.timeframe = normalize(timeframe);
    }

    public String getSort() {
        return sort != null && ALLOWED_SORT.contains(sort) ? sort : SORT_TRENDING;
    }

    public void setSort(final String sort) {
        this.sort = normalize(sort);
    }

    public Integer getPage() {
        return page;
    }

    public void setPage(final Integer page) {
        this.page = page;
    }

    private static String normalize(final String value) {
        return value == null || value.trim().isEmpty()
                ? null
                : value.trim().toLowerCase(Locale.ROOT);
    }
}
