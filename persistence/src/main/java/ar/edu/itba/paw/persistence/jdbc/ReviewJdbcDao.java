package ar.edu.itba.paw.persistence.jdbc;

import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.persistence.ReviewDao;
import ar.edu.itba.paw.persistence.ReviewTagDao;
import ar.edu.itba.paw.model.Pagination;
import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.model.ReviewStats;
import ar.edu.itba.paw.model.ReviewTag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;


public class ReviewJdbcDao implements ReviewDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReviewJdbcDao.class);

    private static final String REVIEW_SELECT =
            "SELECT r.review_id, r.user_id, r.reviewer_email, u.username AS reviewer_username, "
                    + "r.car_id, r.rating, r.title, r.body, r.ownership_status, r.model_year, r.mileage_km, "
                    + "r.would_recommend, r.created_at, r.updated_at "
                    + "FROM reviews r LEFT JOIN users u ON r.user_id = u.user_id ";

    private static final String REVIEW_STATS_SELECT =
            "SELECT c.car_id, COUNT(r.review_id) AS review_count, ROUND(AVG(r.rating), 1) AS average_rating "
                    + "FROM cars c LEFT JOIN reviews r ON r.car_id = c.car_id ";
    private static final String DEFAULT_REVIEW_ORDER = "r.created_at DESC, r.review_id DESC";
    private static final String RATING_ASC_REVIEW_ORDER = "r.rating ASC, r.created_at DESC, r.review_id DESC";
    private static final String RATING_DESC_REVIEW_ORDER = "r.rating DESC, r.created_at DESC, r.review_id DESC";

    private final JdbcTemplate jdbcTemplate;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;
    private final ReviewTagDao reviewTagDao;

    private static Long getNullableLong(final java.sql.ResultSet rs, final String columnName) throws java.sql.SQLException {
        final Number value = (Number) rs.getObject(columnName);
        return value == null ? null : value.longValue();
    }

    private static final RowMapper<Review> ROW_MAPPER = (rs, rowNum) -> new Review(
            rs.getLong("review_id"),
            getNullableLong(rs, "user_id"),
            rs.getString("reviewer_email"),
            rs.getString("reviewer_username"),
            rs.getLong("car_id"),
            rs.getBigDecimal("rating"),
            rs.getString("title"),
            rs.getString("body"),
            rs.getString("ownership_status"),
            rs.getObject("model_year", Integer.class),
            rs.getObject("mileage_km", Integer.class),
            rs.getObject("would_recommend") != null ? rs.getBoolean("would_recommend") : null,
            rs.getTimestamp("created_at").toLocalDateTime(),
            rs.getTimestamp("updated_at").toLocalDateTime()
    );

    private static final RowMapper<ReviewStats> REVIEW_STATS_ROW_MAPPER = (rs, rowNum) -> new ReviewStats(
            rs.getLong("car_id"),
            rs.getLong("review_count"),
            rs.getBigDecimal("average_rating")
    );

    @Autowired
    public ReviewJdbcDao(final DataSource dataSource, final ReviewTagDao reviewTagDao) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.namedParameterJdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("reviews")
                .usingColumns("user_id", "reviewer_email", "car_id", "rating", "title", "body",
                        "ownership_status", "model_year", "mileage_km", "would_recommend")
                .usingGeneratedKeyColumns("review_id");
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

    private Optional<Review> attachTags(final Optional<Review> review) {
        review.ifPresent(r -> attachTags(List.of(r)));
        return review;
    }

    private List<Review> attachTagsToList(final List<Review> reviews) {
        attachTags(reviews);
        return reviews;
    }

    @Override
    public List<Review> findAll() {
        return attachTagsToList(jdbcTemplate.query(
                REVIEW_SELECT + "ORDER BY " + DEFAULT_REVIEW_ORDER,
                ROW_MAPPER
        ));
    }

    @Override
    public Page<Review> findLatest(final int page) {
        return findPaginated("", new Object[0], page, Pagination.REVIEWS_PAGE_SIZE, countAll());
    }

    @Override
    public long countAll() {
        return countReviews("SELECT count(*) FROM reviews");
    }

    @Override
    public Page<Review> findByFollowedUsers(final long followerId, final int page) {
        return findPaginated(
                "JOIN user_follows uf ON uf.followed_id = r.user_id WHERE uf.follower_id = ? ",
                new Object[]{followerId},
                page,
                Pagination.REVIEWS_PAGE_SIZE,
                countByFollowedUsers(followerId)
        );
    }

    @Override
    public long countByFollowedUsers(final long followerId) {
        return countReviews(
                "SELECT count(*) FROM reviews r "
                        + "JOIN user_follows uf ON uf.followed_id = r.user_id "
                        + "WHERE uf.follower_id = ?",
                followerId
        );
    }

    @Override
    public Page<Review> findByFavoriteCars(final long userId, final int page) {
        return findPaginated(
                "JOIN car_favorites cf ON cf.car_id = r.car_id WHERE cf.user_id = ? ",
                new Object[]{userId},
                page,
                Pagination.REVIEWS_PAGE_SIZE,
                countByFavoriteCars(userId)
        );
    }

    @Override
    public long countByFavoriteCars(final long userId) {
        return countReviews(
                "SELECT count(*) FROM reviews r "
                        + "JOIN car_favorites cf ON cf.car_id = r.car_id "
                        + "WHERE cf.user_id = ?",
                userId
        );
    }

    @Override
    public Map<Long, Integer> findDefaultPagesByReviewIds(final Collection<Long> reviewIds) {
        final List<Long> normalizedIds = reviewIds == null
                ? List.of()
                : reviewIds.stream()
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        if (normalizedIds.isEmpty()) {
            return Collections.emptyMap();
        }

        final MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("reviewIds", normalizedIds)
                .addValue("pageSize", Pagination.REVIEWS_PAGE_SIZE);
        return namedParameterJdbcTemplate.query(
                "SELECT ranked.review_id, (((ranked.review_position - 1) / :pageSize) + 1)::int AS page_number "
                        + "FROM ("
                        + "SELECT r.review_id, "
                        + "ROW_NUMBER() OVER (PARTITION BY r.car_id ORDER BY "
                        + DEFAULT_REVIEW_ORDER
                        + ") AS review_position "
                        + "FROM reviews r "
                        + "WHERE r.car_id IN ("
                        + "SELECT target.car_id FROM reviews target WHERE target.review_id IN (:reviewIds)"
                        + ")"
                        + ") ranked "
                        + "WHERE ranked.review_id IN (:reviewIds)",
                params,
                rs -> {
                    final Map<Long, Integer> pagesByReviewId = new HashMap<>();
                    while (rs.next()) {
                        pagesByReviewId.put(rs.getLong("review_id"), rs.getInt("page_number"));
                    }
                    return pagesByReviewId;
                }
        );
    }

    private Page<Review> findPaginated(final String joinAndWhereClause, final Object[] params, final int page,
                                       final int pageSize, final long total) {
        final int effectivePageSize = pageSize > 0 ? pageSize : Pagination.REVIEWS_PAGE_SIZE;
        if (total == 0L) {
            return Page.empty(Pagination.DEFAULT_PAGE, effectivePageSize);
        }
        final int effectivePage = Pagination.clampPage(Pagination.normalizePage(page), total, effectivePageSize);
        final long offset = Pagination.offsetFor(effectivePage, effectivePageSize);
        final Object[] queryParams = new Object[params.length + 2];
        System.arraycopy(params, 0, queryParams, 0, params.length);
        queryParams[params.length] = effectivePageSize;
        queryParams[params.length + 1] = offset;
        final List<Review> items = attachTagsToList(jdbcTemplate.query(
                REVIEW_SELECT + joinAndWhereClause + "ORDER BY " + DEFAULT_REVIEW_ORDER + " LIMIT ? OFFSET ?",
                ROW_MAPPER,
                queryParams
        ));
        return new Page<>(items, effectivePage, effectivePageSize, total);
    }

    private long countReviews(final String sql, final Object... params) {
        final Long count = jdbcTemplate.queryForObject(sql, Long.class, params);
        return count == null ? 0L : count;
    }

    @Override
    public Optional<Review> findById(long id) {
        return attachTags(jdbcTemplate.query(
                REVIEW_SELECT + "WHERE r.review_id = ?",
                ROW_MAPPER, id
        ).stream().findFirst());
    }

    @Override
    public List<Review> findByIds(final Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return List.of();
        }
        return attachTagsToList(namedParameterJdbcTemplate.query(
                REVIEW_SELECT + "WHERE r.review_id IN (:reviewIds)",
                new MapSqlParameterSource("reviewIds", ids),
                ROW_MAPPER
        ));
    }

    @Override
    public List<Review> findByCarIds(final Collection<Long> carIds) {
        if (carIds == null || carIds.isEmpty()) {
            return List.of();
        }
        return attachTagsToList(namedParameterJdbcTemplate.query(
                REVIEW_SELECT + "WHERE r.car_id IN (:carIds) ORDER BY " + DEFAULT_REVIEW_ORDER,
                new MapSqlParameterSource("carIds", carIds),
                ROW_MAPPER
        ));
    }

    @Override
    public List<Review> findByCarId(long carId) {
        return findByCarIdOrdered(carId, DEFAULT_REVIEW_ORDER);
    }

    @Override
    public Page<Review> findByCarId(final long carId, final int page) {
        return findByCarIdPaginated(carId, DEFAULT_REVIEW_ORDER, page);
    }

    @Override
    public Optional<Review> findLatestByCarId(final long carId) {
        return attachTags(jdbcTemplate.query(
                REVIEW_SELECT + "WHERE r.car_id = ? ORDER BY r.created_at DESC LIMIT 1",
                ROW_MAPPER, carId
        ).stream().findFirst());
    }

    @Override
    public Optional<Review> findTopRatedLatestByCarId(final long carId) {
        return attachTags(jdbcTemplate.query(
                REVIEW_SELECT + "WHERE r.car_id = ? ORDER BY r.rating DESC, r.created_at DESC, r.review_id DESC LIMIT 1",
                ROW_MAPPER, carId
        ).stream().findFirst());
    }

    @Override
    public List<Review> findByCarIdOrderByRatingAsc(final long carId) {
        return findByCarIdOrdered(carId, RATING_ASC_REVIEW_ORDER);
    }

    @Override
    public Page<Review> findByCarIdOrderByRatingAsc(final long carId, final int page) {
        return findByCarIdPaginated(carId, RATING_ASC_REVIEW_ORDER, page);
    }

    @Override
    public List<Review> findByCarIdOrderByRatingDesc(final long carId) {
        return findByCarIdOrdered(carId, RATING_DESC_REVIEW_ORDER);
    }

    @Override
    public Page<Review> findByCarIdOrderByRatingDesc(final long carId, final int page) {
        return findByCarIdPaginated(carId, RATING_DESC_REVIEW_ORDER, page);
    }

    @Override
    public long countByCarId(final long carId) {
        final Long count = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM reviews WHERE car_id = ?", Long.class, carId);
        return count == null ? 0L : count;
    }

    private List<Review> findByCarIdOrdered(final long carId, final String orderByClause) {
        return attachTagsToList(jdbcTemplate.query(
                REVIEW_SELECT + "WHERE r.car_id = ? ORDER BY " + orderByClause,
                ROW_MAPPER, carId
        ));
    }

    private Page<Review> findByCarIdPaginated(final long carId, final String orderByClause, final int page) {
        final int normalizedPage = Pagination.normalizePage(page);
        final int pageSize = Pagination.REVIEWS_PAGE_SIZE;
        final long total = countByCarId(carId);
        if (total == 0L) {
            return Page.empty(Pagination.DEFAULT_PAGE, pageSize);
        }
        final int effectivePage = Pagination.clampPage(normalizedPage, total, pageSize);
        final long offset = Pagination.offsetFor(effectivePage, pageSize);
        final List<Review> items = attachTagsToList(jdbcTemplate.query(
                REVIEW_SELECT + "WHERE r.car_id = ? ORDER BY " + orderByClause + " LIMIT ? OFFSET ?",
                ROW_MAPPER, carId, pageSize, offset
        ));
        return new Page<>(items, effectivePage, pageSize, total);
    }

    @Override
    public List<Review> findByUserId(long userId) {
        return attachTagsToList(jdbcTemplate.query(
                REVIEW_SELECT + "WHERE r.user_id = ? ORDER BY r.created_at DESC",
                ROW_MAPPER, userId
        ));
    }

    @Override
    public Page<Review> findByUserId(final long userId, final int page) {
        final int normalizedPage = Pagination.normalizePage(page);
        final int pageSize = Pagination.REVIEWS_PAGE_SIZE;
        final long total = countByUserId(userId);
        if (total == 0L) {
            return Page.empty(Pagination.DEFAULT_PAGE, pageSize);
        }
        final int effectivePage = Pagination.clampPage(normalizedPage, total, pageSize);
        final long offset = Pagination.offsetFor(effectivePage, pageSize);
        final List<Review> items = attachTagsToList(jdbcTemplate.query(
                REVIEW_SELECT + "WHERE r.user_id = ? ORDER BY r.created_at DESC, r.review_id DESC LIMIT ? OFFSET ?",
                ROW_MAPPER, userId, pageSize, offset
        ));
        return new Page<>(items, effectivePage, pageSize, total);
    }

    @Override
    public long countByUserId(final long userId) {
        final Long count = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM reviews WHERE user_id = ?", Long.class, userId);
        return count == null ? 0L : count;
    }

    @Override
    public Optional<ReviewStats> findStatsByCarId(final long carId) {
        return jdbcTemplate.query(
                REVIEW_STATS_SELECT + "WHERE c.car_id = ? GROUP BY c.car_id",
                REVIEW_STATS_ROW_MAPPER,
                carId
        ).stream().findFirst();
    }

    @Override
    public List<ReviewStats> findStatsByCarIds(final Collection<Long> carIds) {
        if (carIds == null || carIds.isEmpty()) {
            return List.of();
        }

        return namedParameterJdbcTemplate.query(
                REVIEW_STATS_SELECT + "WHERE c.car_id IN (:carIds) GROUP BY c.car_id ORDER BY c.car_id",
                new MapSqlParameterSource("carIds", carIds),
                REVIEW_STATS_ROW_MAPPER
        );
    }

    @Override
    public Review create(long userId, long carId, BigDecimal rating, String title, String body,
                         String ownershipStatus, Integer modelYear, Integer mileageKm, Boolean wouldRecommend) {
        Map<String, Object> params = new HashMap<>();
        params.put("user_id", userId);
        params.put("reviewer_email", null);
        params.put("car_id", carId);
        params.put("rating", rating);
        params.put("title", title);
        params.put("body", body);
        params.put("ownership_status", ownershipStatus);
        params.put("model_year", modelYear);
        params.put("mileage_km", mileageKm);
        params.put("would_recommend", wouldRecommend);

        long id = jdbcInsert.executeAndReturnKey(params).longValue();
        LOGGER.info("created review id={} userId={} carId={} rating={}", id, userId, carId, rating);
        return findById(id).orElseThrow();
    }

    @Override
    public int bindReviewsToUserByEmail(final long userId, final String email) {
        final int bound = jdbcTemplate.update(
                "UPDATE reviews SET user_id = ? "
                        + "WHERE user_id IS NULL AND reviewer_email IS NOT NULL "
                        + "AND LOWER(BTRIM(reviewer_email)) = LOWER(?)",
                userId,
                email
        );
        if (bound > 0) {
            LOGGER.info("bound {} reviews to user id={} email={}", bound, userId, email);
        }
        return bound;
    }

    @Override
    public Optional<Review> update(final long id, final long carId,
                                   final BigDecimal rating, final String title, final String body,
                                   final String ownershipStatus, final Integer modelYear,
                                   final Integer mileageKm, final Boolean wouldRecommend) {
        final int updated = jdbcTemplate.update(
                "UPDATE reviews SET car_id = ?, rating = ?, title = ?, body = ?, "
                        + "ownership_status = ?, model_year = ?, mileage_km = ?, would_recommend = ?, "
                        + "updated_at = CURRENT_TIMESTAMP WHERE review_id = ?",
                carId, rating, title, body, ownershipStatus, modelYear, mileageKm, wouldRecommend, id
        );
        if (updated == 0) {
            LOGGER.warn("review update affected 0 rows id={}", id);
            return Optional.empty();
        }
        LOGGER.info("updated review id={} rating={}", id, rating);
        return findById(id);
    }

    @Override
    public boolean delete(long id) {
        final boolean deleted = jdbcTemplate.update("DELETE FROM reviews WHERE review_id = ?", id) > 0;
        if (deleted) {
            LOGGER.info("deleted review id={}", id);
        } else {
            LOGGER.warn("review delete affected 0 rows id={}", id);
        }
        return deleted;
    }
}
