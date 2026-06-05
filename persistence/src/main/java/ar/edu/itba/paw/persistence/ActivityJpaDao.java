package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.ActivityFeedCriteria;
import ar.edu.itba.paw.model.ActivityFeedReference;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Pagination;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class ActivityJpaDao implements ActivityDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActivityJpaDao.class);

    private static final String REVIEW_TYPE = "'" + ActivityFeedReference.TYPE_REVIEW + "'";
    private static final String COMMUNITY_POST_TYPE = "'" + ActivityFeedReference.TYPE_COMMUNITY_POST + "'";

    /**
     * A comment/reply is worth this many likes/helpful reactions in the trending score:
     * {@code (approvals + DISCUSSION_WEIGHT * discussions) * recency_factor}.
     */
    private static final int DISCUSSION_WEIGHT = 2;

    /**
     * Deterministic tie-breaker shared by every sort: newest first, then a stable
     * ordering by item type and id so pagination never reorders equal-scored rows.
     */
    private static final String TIE_BREAKER = "created_at DESC, item_type ASC, item_id DESC";

    @PersistenceContext
    private EntityManager em;

    @Override
    public Page<ActivityFeedReference> findFeed(final ActivityFeedCriteria criteria) {
        final String type = criteria.getType();
        final boolean includeReviews = !ActivityFeedCriteria.TYPE_COMMUNITY.equals(type);
        final boolean includePosts = !ActivityFeedCriteria.TYPE_REVIEWS.equals(type);
        final LocalDateTime timeframeCutoff = timeframeCutoff(criteria.getTimeframe());

        final long totalItems = countFeed(includeReviews, includePosts, timeframeCutoff);
        if (totalItems <= 0L) {
            LOGGER.debug("activity feed empty type={} timeframe={}", type, criteria.getTimeframe());
            return Page.empty(Pagination.DEFAULT_PAGE, Pagination.ACTIVITY_PAGE_SIZE);
        }

        final int effectivePage = Pagination.clampPage(
                Pagination.normalizePage(criteria.getPage()), totalItems, Pagination.ACTIVITY_PAGE_SIZE);

        final String rankUnion = buildRankUnion(includeReviews, includePosts, timeframeCutoff);
        final String idsSql = "SELECT item_type, item_id FROM (" + rankUnion + ") activity_items"
                + " ORDER BY " + orderExpression(criteria.getSort()) + ", " + TIE_BREAKER
                + " LIMIT ? OFFSET ?";
        final Query idsQuery = em.createNativeQuery(idsSql);

        int index = 1;
        index = bindTimeframe(idsQuery, index, includeReviews, includePosts, timeframeCutoff);
        index = bindRecencyCutoffs(idsQuery, index, criteria.getSort());
        idsQuery.setParameter(index++, Pagination.ACTIVITY_PAGE_SIZE);
        idsQuery.setParameter(index, Pagination.offsetFor(effectivePage, Pagination.ACTIVITY_PAGE_SIZE));

        final List<?> rows = idsQuery.getResultList();
        final List<ActivityFeedReference> items = new ArrayList<>();
        for (final Object row : rows) {
            final Object[] values = (Object[]) row;
            items.add(new ActivityFeedReference(
                    values[0].toString().trim(),
                    ((Number) values[1]).longValue()
            ));
        }
        LOGGER.debug("loaded activity feed page={} sort={} itemCount={} totalItems={}",
                effectivePage, criteria.getSort(), items.size(), totalItems);
        return new Page<>(items, effectivePage, Pagination.ACTIVITY_PAGE_SIZE, totalItems);
    }

    private long countFeed(final boolean includeReviews, final boolean includePosts,
                           final LocalDateTime timeframeCutoff) {
        final List<String> arms = new ArrayList<>();
        if (includeReviews) {
            arms.add("SELECT r.review_id FROM reviews r" + timeframeClause("r", timeframeCutoff));
        }
        if (includePosts) {
            arms.add("SELECT p.post_id FROM community_posts p WHERE p.hidden = false"
                    + timeframeAndClause("p", timeframeCutoff));
        }
        final String countUnion = String.join(" UNION ALL ", arms);
        final Query countQuery = em.createNativeQuery(
                "SELECT COUNT(*) FROM (" + countUnion + ") activity_items");
        bindTimeframe(countQuery, 1, includeReviews, includePosts, timeframeCutoff);
        final Number total = (Number) countQuery.getSingleResult();
        return total == null ? 0L : total.longValue();
    }

    /**
     * Builds the rank union exposing per-row {@code approvals}/{@code discussions} engagement
     * counts via correlated subqueries. Note: because the sort key is derived from these counts,
     * the score is evaluated for every matching row before {@code LIMIT} — this is O(rows) count
     * subqueries per request. Acceptable at the current scale (the count source tables are indexed
     * on the FK columns); if the feed grows large, precompute the engagement counts instead.
     */
    private String buildRankUnion(final boolean includeReviews, final boolean includePosts,
                                  final LocalDateTime timeframeCutoff) {
        final List<String> arms = new ArrayList<>();
        if (includeReviews) {
            arms.add("SELECT " + REVIEW_TYPE + " AS item_type, r.review_id AS item_id, r.created_at AS created_at, "
                    + "(SELECT COUNT(*) FROM review_likes rl WHERE rl.review_id = r.review_id) AS approvals, "
                    + "(SELECT COUNT(*) FROM review_replies rr WHERE rr.review_id = r.review_id) AS discussions "
                    + "FROM reviews r" + timeframeClause("r", timeframeCutoff));
        }
        if (includePosts) {
            arms.add("SELECT " + COMMUNITY_POST_TYPE + " AS item_type, p.post_id AS item_id, p.created_at AS created_at, "
                    + "(SELECT COUNT(*) FROM community_post_helpful_reactions h WHERE h.post_id = p.post_id) AS approvals, "
                    // Count all comments (not just visible ones) to match the comment metric shown on the card,
                    // which comes from CommunityDao.countCommentsByPostIds and does not filter on hidden.
                    + "(SELECT COUNT(*) FROM community_post_comments c WHERE c.post_id = p.post_id) AS discussions "
                    + "FROM community_posts p WHERE p.hidden = false" + timeframeAndClause("p", timeframeCutoff));
        }
        return String.join(" UNION ALL ", arms);
    }

    /**
     * Score expression evaluated by the outer query over the {@code approvals},
     * {@code discussions}, and {@code created_at} columns of the rank union. Uses only
     * portable arithmetic and {@code CASE}/{@code >=} comparisons so the same SQL runs on
     * PostgreSQL (production) and HSQLDB (persistence tests).
     */
    private String orderExpression(final String sort) {
        if (ActivityFeedCriteria.SORT_LATEST.equals(sort)) {
            // Recency only; the shared tie-breaker already orders by created_at DESC.
            return "created_at DESC";
        }
        if (ActivityFeedCriteria.SORT_CONTROVERSIAL.equals(sort)) {
            // High discussion, low approval. 1.0 multiplier forces decimal division.
            return "(discussions * 1.0 / (1 + approvals)) DESC";
        }
        // Trending: weighted engagement decayed by a recency bucket (3 cutoff params).
        return "((approvals + " + DISCUSSION_WEIGHT + " * discussions) * "
                + "(CASE WHEN created_at >= ? THEN 3 WHEN created_at >= ? THEN 2 WHEN created_at >= ? THEN 1 ELSE 0.5 END)) DESC";
    }

    private int bindRecencyCutoffs(final Query query, final int startIndex, final String sort) {
        if (!ActivityFeedCriteria.SORT_TRENDING.equals(sort)) {
            return startIndex;
        }
        final LocalDateTime now = LocalDateTime.now();
        int index = startIndex;
        query.setParameter(index++, now.minusDays(1));
        query.setParameter(index++, now.minusDays(7));
        query.setParameter(index++, now.minusDays(30));
        return index;
    }

    private int bindTimeframe(final Query query, final int startIndex, final boolean includeReviews,
                              final boolean includePosts, final LocalDateTime timeframeCutoff) {
        if (timeframeCutoff == null) {
            return startIndex;
        }
        int index = startIndex;
        if (includeReviews) {
            query.setParameter(index++, timeframeCutoff);
        }
        if (includePosts) {
            query.setParameter(index++, timeframeCutoff);
        }
        return index;
    }

    private String timeframeClause(final String alias, final LocalDateTime timeframeCutoff) {
        return timeframeCutoff == null ? "" : " WHERE " + alias + ".created_at >= ?";
    }

    private String timeframeAndClause(final String alias, final LocalDateTime timeframeCutoff) {
        return timeframeCutoff == null ? "" : " AND " + alias + ".created_at >= ?";
    }

    private LocalDateTime timeframeCutoff(final String timeframe) {
        final LocalDateTime now = LocalDateTime.now();
        switch (timeframe) {
            case ActivityFeedCriteria.TIMEFRAME_TODAY:
                // "today" means the last 24 hours (a rolling window), not the current calendar day.
                return now.minusDays(1);
            case ActivityFeedCriteria.TIMEFRAME_WEEK:
                return now.minusDays(7);
            case ActivityFeedCriteria.TIMEFRAME_MONTH:
                return now.minusDays(30);
            case ActivityFeedCriteria.TIMEFRAME_ALL:
            default:
                return null;
        }
    }
}
