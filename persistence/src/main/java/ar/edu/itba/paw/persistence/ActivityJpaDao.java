package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.ActivityFeedReference;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Pagination;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import org.springframework.stereotype.Repository;

@Repository
public class ActivityJpaDao implements ActivityDao {

    private static final String REVIEW_TYPE = "'" + ActivityFeedReference.TYPE_REVIEW + "'";
    private static final String COMMUNITY_POST_TYPE = "'" + ActivityFeedReference.TYPE_COMMUNITY_POST + "'";
    private static final String ACTIVITY_UNION_SQL =
            "SELECT " + REVIEW_TYPE + " AS item_type, r.review_id AS item_id, r.created_at AS created_at " +
            "FROM reviews r " +
            "UNION ALL " +
            "SELECT " + COMMUNITY_POST_TYPE + " AS item_type, p.post_id AS item_id, p.created_at AS created_at " +
            "FROM community_posts p " +
            "WHERE p.hidden = false";
    private static final String ACTIVITY_ORDER_SQL = " ORDER BY created_at DESC, item_type ASC, item_id DESC";

    @PersistenceContext
    private EntityManager em;

    @Override
    public Page<ActivityFeedReference> findLatest(final int page) {
        final Number total = (Number) em.createNativeQuery(
                "SELECT COUNT(*) FROM (" + ACTIVITY_UNION_SQL + ") activity_items"
        ).getSingleResult();
        final long totalItems = total == null ? 0L : total.longValue();
        if (totalItems <= 0L) {
            return Page.empty(Pagination.DEFAULT_PAGE, Pagination.ACTIVITY_PAGE_SIZE);
        }

        final int effectivePage = Pagination.clampPage(Pagination.normalizePage(page), totalItems, Pagination.ACTIVITY_PAGE_SIZE);
        final Query idsQuery = em.createNativeQuery(
                "SELECT item_type, item_id FROM (" + ACTIVITY_UNION_SQL + ") activity_items" +
                        ACTIVITY_ORDER_SQL + " LIMIT ? OFFSET ?"
        );
        idsQuery.setParameter(1, Pagination.ACTIVITY_PAGE_SIZE);
        idsQuery.setParameter(2, Pagination.offsetFor(effectivePage, Pagination.ACTIVITY_PAGE_SIZE));

        final List<?> rows = idsQuery.getResultList();
        final List<ActivityFeedReference> items = new ArrayList<>();
        for (final Object row : rows) {
            final Object[] values = (Object[]) row;
            items.add(new ActivityFeedReference(
                    values[0].toString().trim(),
                    ((Number) values[1]).longValue()
            ));
        }
        return new Page<>(items, effectivePage, Pagination.ACTIVITY_PAGE_SIZE, totalItems);
    }
}
