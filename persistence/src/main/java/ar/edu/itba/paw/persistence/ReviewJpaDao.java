package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Pagination;
import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.model.ReviewStats;
import ar.edu.itba.paw.model.ReviewTag;
import ar.edu.itba.paw.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class ReviewJpaDao implements ReviewDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReviewJpaDao.class);

    private static final String DEFAULT_ORDER = "r.createdAt DESC, r.id DESC";
    private static final String RATING_ASC_ORDER = "r.rating ASC, r.createdAt DESC, r.id DESC";
    private static final String RATING_DESC_ORDER = "r.rating DESC, r.createdAt DESC, r.id DESC";

    private static final String DEFAULT_ORDER_NATIVE = "created_at DESC, review_id DESC";
    private static final String RATING_ASC_ORDER_NATIVE = "rating ASC, created_at DESC, review_id DESC";
    private static final String RATING_DESC_ORDER_NATIVE = "rating DESC, created_at DESC, review_id DESC";

    @PersistenceContext
    private EntityManager em;

    private final ReviewTagDao reviewTagDao;

    @Autowired
    public ReviewJpaDao(final ReviewTagDao reviewTagDao) {
        this.reviewTagDao = reviewTagDao;
    }

    private void attachTags(final Collection<Review> reviews) {
        if (reviews == null || reviews.isEmpty()) {
            return;
        }
        final List<Long> ids = reviews.stream().map(Review::getId).collect(Collectors.toList());
        final Map<Long, List<ReviewTag>> tagsByReview = reviewTagDao.findByReviewIds(ids);
        for (final Review review : reviews) {
            review.setTags(tagsByReview.getOrDefault(review.getId(), Collections.emptyList()));
        }
    }

    private List<Review> loadByIds(final List<Long> ids, final String jpqlOrder) {
        return em.createQuery(
                "SELECT r FROM Review r LEFT JOIN FETCH r.user WHERE r.id IN :ids ORDER BY " + jpqlOrder,
                Review.class)
                .setParameter("ids", ids)
                .getResultList();
    }

    private Page<Review> findPaginatedByNativeIds(final String countSql, final String idsSql,
                                                  final List<Object> params, final int page,
                                                  final int pageSize, final String jpqlOrder) {
        // count
        javax.persistence.Query countQuery = em.createNativeQuery(countSql);
        for (int i = 0; i < params.size(); i++) {
            countQuery.setParameter(i + 1, params.get(i));
        }
        final Number total = (Number) countQuery.getSingleResult();
        final long totalItems = total == null ? 0L : total.longValue();

        if (totalItems == 0L) {
            return Page.empty(Pagination.DEFAULT_PAGE, pageSize);
        }

        final int effectivePage = Pagination.clampPage(Pagination.normalizePage(page), totalItems, pageSize);
        final long offset = Pagination.offsetFor(effectivePage, pageSize);

        // IDs
        javax.persistence.Query idsQuery = em.createNativeQuery(idsSql + " LIMIT ? OFFSET ?");
        for (int i = 0; i < params.size(); i++) {
            idsQuery.setParameter(i + 1, params.get(i));
        }
        idsQuery.setParameter(params.size() + 1, pageSize);
        idsQuery.setParameter(params.size() + 2, offset);

        final List<?> ids = idsQuery.getResultList();
        if (ids.isEmpty()) {
            return Page.empty(effectivePage, pageSize);
        }

        final List<Long> longIds = ids.stream().map(r -> ((Number) r).longValue()).collect(Collectors.toList());
        final List<Review> items = loadByIds(longIds, jpqlOrder);
        attachTags(items);
        return new Page<>(items, effectivePage, pageSize, totalItems);
    }

    @Override
    public List<Review> findAll() {
        final List<Review> reviews = em.createQuery(
                "SELECT r FROM Review r LEFT JOIN FETCH r.user ORDER BY " + DEFAULT_ORDER,
                Review.class)
                .getResultList();
        attachTags(reviews);
        return reviews;
    }

    @Override
    public Page<Review> findLatest(final int page) {
        final int pageSize = Pagination.REVIEWS_PAGE_SIZE;
        final long totalItems = countAll();
        if (totalItems == 0L) {
            return Page.empty(Pagination.DEFAULT_PAGE, pageSize);
        }
        final int effectivePage = Pagination.clampPage(Pagination.normalizePage(page), totalItems, pageSize);
        final List<Review> reviews = em.createQuery(
                "SELECT r FROM Review r LEFT JOIN FETCH r.user ORDER BY " + DEFAULT_ORDER,
                Review.class)
                .setFirstResult((int) Pagination.offsetFor(effectivePage, pageSize))
                .setMaxResults(pageSize)
                .getResultList();
        attachTags(reviews);
        return new Page<>(reviews, effectivePage, pageSize, totalItems);
    }

    @Override
    public long countAll() {
        return em.createQuery("SELECT COUNT(r) FROM Review r", Long.class).getSingleResult();
    }

    @Override
    public Page<Review> findByFollowedUsers(final long followerId, final int page) {
        return findPaginatedByNativeIds(
                "SELECT COUNT(*) FROM reviews r JOIN user_follows uf ON uf.followed_id = r.user_id WHERE uf.follower_id = ?",
                "SELECT r.review_id FROM reviews r JOIN user_follows uf ON uf.followed_id = r.user_id WHERE uf.follower_id = ? ORDER BY " + DEFAULT_ORDER_NATIVE,
                List.of(followerId), page, Pagination.REVIEWS_PAGE_SIZE, DEFAULT_ORDER);
    }

    @Override
    public long countByFollowedUsers(final long followerId) {
        final Number count = (Number) em.createNativeQuery(
                "SELECT COUNT(*) FROM reviews r JOIN user_follows uf ON uf.followed_id = r.user_id WHERE uf.follower_id = ?")
                .setParameter(1, followerId)
                .getSingleResult();
        return count == null ? 0L : count.longValue();
    }

    @Override
    public Page<Review> findByFavoriteCars(final long userId, final int page) {
        return findPaginatedByNativeIds(
                "SELECT COUNT(*) FROM reviews r JOIN car_favorites cf ON cf.car_id = r.car_id WHERE cf.user_id = ?",
                "SELECT r.review_id FROM reviews r JOIN car_favorites cf ON cf.car_id = r.car_id WHERE cf.user_id = ? ORDER BY " + DEFAULT_ORDER_NATIVE,
                List.of(userId), page, Pagination.REVIEWS_PAGE_SIZE, DEFAULT_ORDER);
    }

    @Override
    public long countByFavoriteCars(final long userId) {
        final Number count = (Number) em.createNativeQuery(
                "SELECT COUNT(*) FROM reviews r JOIN car_favorites cf ON cf.car_id = r.car_id WHERE cf.user_id = ?")
                .setParameter(1, userId)
                .getSingleResult();
        return count == null ? 0L : count.longValue();
    }

    @Override
    public Map<Long, Integer> findDefaultPagesByReviewIds(final Collection<Long> reviewIds) {
        final List<Long> normalizedIds = reviewIds == null
                ? List.of()
                : reviewIds.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
        if (normalizedIds.isEmpty()) {
            return Collections.emptyMap();
        }

        final String inClause = normalizedIds.stream().map(id -> "?").collect(Collectors.joining(","));
        final javax.persistence.Query query = em.createNativeQuery(
                "SELECT ranked.review_id, (((ranked.review_position - 1) / ?) + 1) AS page_number " +
                "FROM (" +
                "SELECT r.review_id, " +
                "ROW_NUMBER() OVER (PARTITION BY r.car_id ORDER BY " + DEFAULT_ORDER_NATIVE + ") AS review_position " +
                "FROM reviews r " +
                "WHERE r.car_id IN (SELECT target.car_id FROM reviews target WHERE target.review_id IN (" + inClause + "))" +
                ") ranked " +
                "WHERE ranked.review_id IN (" + inClause + ")");

        query.setParameter(1, Pagination.REVIEWS_PAGE_SIZE);
        int idx = 2;
        for (final Long id : normalizedIds) {
            query.setParameter(idx++, id);
        }
        for (final Long id : normalizedIds) {
            query.setParameter(idx++, id);
        }

        final List<?> rawRows = query.getResultList();
        final Map<Long, Integer> pagesByReviewId = new HashMap<>();
        for (final Object element : rawRows) {
            final Object[] row = (Object[]) element;
            pagesByReviewId.put(((Number) row[0]).longValue(), ((Number) row[1]).intValue());
        }
        return pagesByReviewId;
    }

    @Override
    public Optional<Review> findById(final long id) {
        final List<Review> results = em.createQuery(
                "SELECT r FROM Review r LEFT JOIN FETCH r.user WHERE r.id = :id",
                Review.class)
                .setParameter("id", id)
                .getResultList();
        final Optional<Review> review = results.stream().findFirst();
        review.ifPresent(r -> attachTags(List.of(r)));
        return review;
    }

    @Override
    public List<Review> findByIds(final Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        final List<Review> reviews = em.createQuery(
                "SELECT r FROM Review r LEFT JOIN FETCH r.user WHERE r.id IN :ids ORDER BY " + DEFAULT_ORDER,
                Review.class)
                .setParameter("ids", ids)
                .getResultList();
        attachTags(reviews);
        return reviews;
    }

    @Override
    public List<Review> findByCarIds(final Collection<Long> carIds) {
        if (carIds == null || carIds.isEmpty()) {
            return List.of();
        }
        final List<Review> reviews = em.createQuery(
                "SELECT r FROM Review r LEFT JOIN FETCH r.user WHERE r.car.id IN :carIds ORDER BY " + DEFAULT_ORDER,
                Review.class)
                .setParameter("carIds", carIds)
                .getResultList();
        attachTags(reviews);
        return reviews;
    }

    @Override
    public List<Review> findByCarId(final long carId) {
        return findByCarIdOrdered(carId, DEFAULT_ORDER);
    }

    @Override
    public Page<Review> findByCarId(final long carId, final int page) {
        return findByCarIdPaginated(carId, DEFAULT_ORDER_NATIVE, DEFAULT_ORDER, page);
    }

    @Override
    public Optional<Review> findLatestByCarId(final long carId) {
        final List<Review> results = em.createQuery(
                "SELECT r FROM Review r LEFT JOIN FETCH r.user WHERE r.car.id = :carId ORDER BY r.createdAt DESC",
                Review.class)
                .setParameter("carId", carId)
                .setMaxResults(1)
                .getResultList();
        final Optional<Review> review = results.stream().findFirst();
        review.ifPresent(r -> attachTags(List.of(r)));
        return review;
    }

    @Override
    public Optional<Review> findTopRatedLatestByCarId(final long carId) {
        final List<Review> results = em.createQuery(
                "SELECT r FROM Review r LEFT JOIN FETCH r.user WHERE r.car.id = :carId ORDER BY r.rating DESC, r.createdAt DESC, r.id DESC",
                Review.class)
                .setParameter("carId", carId)
                .setMaxResults(1)
                .getResultList();
        final Optional<Review> review = results.stream().findFirst();
        review.ifPresent(r -> attachTags(List.of(r)));
        return review;
    }

    @Override
    public List<Review> findByCarIdOrderByRatingAsc(final long carId) {
        return findByCarIdOrdered(carId, RATING_ASC_ORDER);
    }

    @Override
    public Page<Review> findByCarIdOrderByRatingAsc(final long carId, final int page) {
        return findByCarIdPaginated(carId, RATING_ASC_ORDER_NATIVE, RATING_ASC_ORDER, page);
    }

    @Override
    public List<Review> findByCarIdOrderByRatingDesc(final long carId) {
        return findByCarIdOrdered(carId, RATING_DESC_ORDER);
    }

    @Override
    public Page<Review> findByCarIdOrderByRatingDesc(final long carId, final int page) {
        return findByCarIdPaginated(carId, RATING_DESC_ORDER_NATIVE, RATING_DESC_ORDER, page);
    }

    @Override
    public long countByCarId(final long carId) {
        return em.createQuery(
                "SELECT COUNT(r) FROM Review r WHERE r.car.id = :carId", Long.class)
                .setParameter("carId", carId)
                .getSingleResult();
    }

    @Override
    public List<Review> findByUserId(final long userId) {
        final List<Review> reviews = em.createQuery(
                "SELECT r FROM Review r LEFT JOIN FETCH r.user WHERE r.user.id = :userId ORDER BY r.createdAt DESC",
                Review.class)
                .setParameter("userId", userId)
                .getResultList();
        attachTags(reviews);
        return reviews;
    }

    @Override
    public Page<Review> findByUserId(final long userId, final int page) {
        final int pageSize = Pagination.REVIEWS_PAGE_SIZE;
        final long totalItems = countByUserId(userId);
        if (totalItems == 0L) {
            return Page.empty(Pagination.DEFAULT_PAGE, pageSize);
        }
        final int effectivePage = Pagination.clampPage(Pagination.normalizePage(page), totalItems, pageSize);
        final List<Review> reviews = em.createQuery(
                "SELECT r FROM Review r LEFT JOIN FETCH r.user WHERE r.user.id = :userId ORDER BY " + DEFAULT_ORDER,
                Review.class)
                .setParameter("userId", userId)
                .setFirstResult((int) Pagination.offsetFor(effectivePage, pageSize))
                .setMaxResults(pageSize)
                .getResultList();
        attachTags(reviews);
        return new Page<>(reviews, effectivePage, pageSize, totalItems);
    }

    @Override
    public long countByUserId(final long userId) {
        return em.createQuery(
                "SELECT COUNT(r) FROM Review r WHERE r.user.id = :userId", Long.class)
                .setParameter("userId", userId)
                .getSingleResult();
    }

    @Override
    public Optional<ReviewStats> findStatsByCarId(final long carId) {
        final List<?> rawRows = em.createNativeQuery(
                "SELECT c.car_id, COUNT(r.review_id) AS review_count, ROUND(AVG(r.rating), 1) AS average_rating " +
                "FROM cars c LEFT JOIN reviews r ON r.car_id = c.car_id WHERE c.car_id = ? GROUP BY c.car_id")
                .setParameter(1, carId)
                .getResultList();
        return rawRows.stream().findFirst().map(element -> {
            final Object[] r = (Object[]) element;
            return new ReviewStats(
                    ((Number) r[0]).longValue(),
                    ((Number) r[1]).longValue(),
                    r[2] != null ? new BigDecimal(r[2].toString()) : null);
        });
    }

    @Override
    public List<ReviewStats> findStatsByCarIds(final Collection<Long> carIds) {
        if (carIds == null || carIds.isEmpty()) {
            return List.of();
        }
        final String placeholders = carIds.stream().map(id -> "?").collect(Collectors.joining(","));
        final javax.persistence.Query query = em.createNativeQuery(
                "SELECT c.car_id, COUNT(r.review_id) AS review_count, ROUND(AVG(r.rating), 1) AS average_rating " +
                "FROM cars c LEFT JOIN reviews r ON r.car_id = c.car_id " +
                "WHERE c.car_id IN (" + placeholders + ") GROUP BY c.car_id ORDER BY c.car_id");
        int i = 1;
        for (final Long id : carIds) {
            query.setParameter(i++, id);
        }
        final List<?> rawRows = query.getResultList();
        return rawRows.stream().map(element -> {
            final Object[] r = (Object[]) element;
            return new ReviewStats(
                    ((Number) r[0]).longValue(),
                    ((Number) r[1]).longValue(),
                    r[2] != null ? new BigDecimal(r[2].toString()) : null);
        }).collect(Collectors.toList());
    }

    @Override
    public Review create(final long userId, final long carId, final BigDecimal rating, final String title,
                         final String body, final String ownershipStatus, final Integer modelYear,
                         final Integer mileageKm, final Boolean wouldRecommend) {
        final Review review = new Review();
        if (userId > 0) {
            review.setUser(em.getReference(User.class, userId));
        }
        review.setCar(em.getReference(Car.class, carId));
        review.setRating(rating);
        review.setTitle(title);
        review.setBody(body);
        review.setOwnershipStatus(ownershipStatus);
        review.setModelYear(modelYear);
        review.setMileageKm(mileageKm);
        review.setWouldRecommend(wouldRecommend);
        em.persist(review);
        LOGGER.info("created review id={} userId={} carId={} rating={}", review.getId(), userId, carId, rating);
        return review;
    }

    @Override
    public int bindReviewsToUserByEmail(final long userId, final String email) {
        final int bound = em.createQuery(
                "UPDATE Review r SET r.user = :user " +
                "WHERE r.user IS NULL AND r.reviewerEmail IS NOT NULL " +
                "AND LOWER(TRIM(r.reviewerEmail)) = LOWER(:email)")
                .setParameter("user", em.getReference(User.class, userId))
                .setParameter("email", email)
                .executeUpdate();
        if (bound > 0) {
            LOGGER.info("bound {} reviews to user id={} email={}", bound, userId, email);
        }
        return bound;
    }

    @Override
    public Optional<Review> update(final long id, final long carId, final BigDecimal rating, final String title,
                                   final String body, final String ownershipStatus, final Integer modelYear,
                                   final Integer mileageKm, final Boolean wouldRecommend) {
        final Review review = em.find(Review.class, id);
        if (review == null) {
            LOGGER.warn("review update affected 0 rows id={}", id);
            return Optional.empty();
        }
        review.setCar(em.getReference(Car.class, carId));
        review.setRating(rating);
        review.setTitle(title);
        review.setBody(body);
        review.setOwnershipStatus(ownershipStatus);
        review.setModelYear(modelYear);
        review.setMileageKm(mileageKm);
        review.setWouldRecommend(wouldRecommend);
        LOGGER.info("updated review id={} rating={}", id, rating);
        return Optional.of(review);
    }

    @Override
    public boolean delete(final long id) {
        final Review review = em.find(Review.class, id);
        if (review == null) {
            LOGGER.warn("review delete affected 0 rows id={}", id);
            return false;
        }
        em.remove(review);
        LOGGER.info("deleted review id={}", id);
        return true;
    }

    private List<Review> findByCarIdOrdered(final long carId, final String jpqlOrder) {
        final List<Review> reviews = em.createQuery(
                "SELECT r FROM Review r LEFT JOIN FETCH r.user WHERE r.car.id = :carId ORDER BY " + jpqlOrder,
                Review.class)
                .setParameter("carId", carId)
                .getResultList();
        attachTags(reviews);
        return reviews;
    }

    private Page<Review> findByCarIdPaginated(final long carId, final String nativeOrder,
                                               final String jpqlOrder, final int page) {
        final int pageSize = Pagination.REVIEWS_PAGE_SIZE;
        final long totalItems = countByCarId(carId);
        if (totalItems == 0L) {
            return Page.empty(Pagination.DEFAULT_PAGE, pageSize);
        }
        final int effectivePage = Pagination.clampPage(Pagination.normalizePage(page), totalItems, pageSize);
        final List<Review> reviews = em.createQuery(
                "SELECT r FROM Review r LEFT JOIN FETCH r.user WHERE r.car.id = :carId ORDER BY " + jpqlOrder,
                Review.class)
                .setParameter("carId", carId)
                .setFirstResult((int) Pagination.offsetFor(effectivePage, pageSize))
                .setMaxResults(pageSize)
                .getResultList();
        attachTags(reviews);
        return new Page<>(reviews, effectivePage, pageSize, totalItems);
    }
}
