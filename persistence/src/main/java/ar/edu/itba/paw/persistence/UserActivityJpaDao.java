package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Pagination;
import ar.edu.itba.paw.model.ProfileActivityItem;
import ar.edu.itba.paw.model.ProfileActivityItem.ItemType;
import java.util.List;
import java.util.stream.Collectors;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

@Repository
public class UserActivityJpaDao implements UserActivityDao {

    private static final int PAGE_SIZE = Pagination.PROFILE_ACTIVITY_PAGE_SIZE;

    @PersistenceContext
    private EntityManager em;

    @Override
    public Page<ProfileActivityItem> findAuthoredActivity(final long userId, final int page) {
        final long total = countAuthoredActivity(userId);
        if (total == 0L) {
            return Page.empty(Pagination.DEFAULT_PAGE, PAGE_SIZE);
        }
        final int normalizedPage = Pagination.normalizePage(page);
        final int effectivePage = Pagination.clampPage(normalizedPage, total, PAGE_SIZE);
        final long offset = Pagination.offsetFor(effectivePage, PAGE_SIZE);

        final List<?> rows = em.createNativeQuery(
                "SELECT type, entity_id FROM (" +
                "  SELECT 'REVIEW' AS type, review_id AS entity_id, created_at FROM reviews WHERE user_id = ?1 " +
                "  UNION ALL " +
                "  SELECT 'POST' AS type, post_id AS entity_id, created_at FROM community_posts WHERE author_user_id = ?1 AND hidden = false" +
                ") combined ORDER BY created_at DESC, entity_id DESC LIMIT ?2 OFFSET ?3"
        )
                .setParameter(1, userId)
                .setParameter(2, PAGE_SIZE)
                .setParameter(3, offset)
                .getResultList();

        final List<ProfileActivityItem> items = toActivityItems(rows);
        return new Page<>(items, effectivePage, PAGE_SIZE, total);
    }

    @Override
    public long countAuthoredActivity(final long userId) {
        final Number count = (Number) em.createNativeQuery(
                "SELECT " +
                "  (SELECT COUNT(*) FROM reviews WHERE user_id = ?1) + " +
                "  (SELECT COUNT(*) FROM community_posts WHERE author_user_id = ?1 AND hidden = false)"
        )
                .setParameter(1, userId)
                .getSingleResult();
        return count == null ? 0L : count.longValue();
    }

    @Override
    public Page<ProfileActivityItem> findLikedActivity(final long userId, final int page) {
        final long total = countLikedActivity(userId);
        if (total == 0L) {
            return Page.empty(Pagination.DEFAULT_PAGE, PAGE_SIZE);
        }
        final int normalizedPage = Pagination.normalizePage(page);
        final int effectivePage = Pagination.clampPage(normalizedPage, total, PAGE_SIZE);
        final long offset = Pagination.offsetFor(effectivePage, PAGE_SIZE);

        final List<?> rows = em.createNativeQuery(
                "SELECT type, entity_id FROM (" +
                "  SELECT 'REVIEW' AS type, review_id AS entity_id, created_at FROM review_likes WHERE user_id = ?1 " +
                "  UNION ALL " +
                "  SELECT 'POST' AS type, r.post_id AS entity_id, r.created_at " +
                "    FROM community_post_helpful_reactions r " +
                "    JOIN community_posts p ON p.post_id = r.post_id " +
                "    WHERE r.user_id = ?1 AND p.hidden = false" +
                ") combined ORDER BY created_at DESC, entity_id DESC LIMIT ?2 OFFSET ?3"
        )
                .setParameter(1, userId)
                .setParameter(2, PAGE_SIZE)
                .setParameter(3, offset)
                .getResultList();

        final List<ProfileActivityItem> items = toActivityItems(rows);
        return new Page<>(items, effectivePage, PAGE_SIZE, total);
    }

    @Override
    public long countLikedActivity(final long userId) {
        final Number count = (Number) em.createNativeQuery(
                "SELECT " +
                "  (SELECT COUNT(*) FROM review_likes WHERE user_id = ?1) + " +
                "  (SELECT COUNT(*) FROM community_post_helpful_reactions r " +
                "    JOIN community_posts p ON p.post_id = r.post_id " +
                "    WHERE r.user_id = ?1 AND p.hidden = false)"
        )
                .setParameter(1, userId)
                .getSingleResult();
        return count == null ? 0L : count.longValue();
    }

    private List<ProfileActivityItem> toActivityItems(final List<?> rows) {
        return rows.stream()
                .map(row -> {
                    final Object[] cols = (Object[]) row;
                    // The UNION ALL string literal column may come back fixed-width
                    // padded on some databases (e.g. HSQLDB CHAR), so trim before comparing.
                    final String type = ((String) cols[0]).trim();
                    final long entityId = ((Number) cols[1]).longValue();
                    return new ProfileActivityItem(
                            "POST".equals(type) ? ItemType.POST : ItemType.REVIEW,
                            entityId
                    );
                })
                .collect(Collectors.toList());
    }
}
