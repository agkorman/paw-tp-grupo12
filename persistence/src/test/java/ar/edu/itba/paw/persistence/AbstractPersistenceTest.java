package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.BodyType;
import ar.edu.itba.paw.model.Brand;
import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.model.ReviewReply;
import ar.edu.itba.paw.model.User;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = TestConfiguration.class)
@Transactional
@Rollback
abstract class AbstractPersistenceTest {

    @Autowired
    protected BrandDao brandDao;

    @Autowired
    protected BodyTypeDao bodyTypeDao;

    @Autowired
    protected UserDao userDao;

    @Autowired
    protected CarDao carDao;

    @Autowired
    protected CarImageDao carImageDao;

    @Autowired
    protected ReviewImageDao reviewImageDao;

    @Autowired
    protected CarFavoriteDao carFavoriteDao;

    @Autowired
    protected UserFollowDao userFollowDao;

    @Autowired
    protected AdminRequestDao adminRequestDao;

    @Autowired
    protected BrandRequestDao brandRequestDao;

    @Autowired
    protected BodyTypeRequestDao bodyTypeRequestDao;

    @Autowired
    protected CarRequestDao carRequestDao;

    @Autowired
    protected ReviewDao reviewDao;

    @Autowired
    protected ReviewReplyDao reviewReplyDao;

    @Autowired
    protected ReviewLikeDao reviewLikeDao;

    @Autowired
    protected ReviewTagDao reviewTagDao;

    @Autowired
    protected JdbcTemplate jdbcTemplate;

    @Autowired
    private JpaTestFlusher jpaTestFlusher;

    @PersistenceContext
    protected EntityManager em;

    protected void flushAndClear() {
        jpaTestFlusher.flushAndClear();
    }

    protected User createUser(final String suffix) {
        return insertUser("user-" + suffix, "user-" + suffix + "@example.com", "secret", "user");
    }

    protected Car createCar(final String suffix) {
        final long brandId = insertBrand("Brand " + suffix).getId();
        final long bodyTypeId = insertBodyType("Body " + suffix).getId();
        return insertCar(brandId, "Brand " + suffix, "Model " + suffix, bodyTypeId, "Body " + suffix,
                2026, "Description " + suffix, "combustion", 200, 6, "automatic",
                new BigDecimal("8.5"), 230, new BigDecimal("35000.00"));
    }

    protected Review createReview(final String suffix) {
        final User user = createUser("review-" + suffix);
        final Car car = createCar("review-" + suffix);
        return insertReview(user.getId(), "user-review-" + suffix, car.getId(), new BigDecimal("4.0"), "Title " + suffix,
                "Body " + suffix, "owner", 2026, 1000, true);
    }

    protected User insertUser(final String username, final String email, final String password, final String role) {
        jdbcTemplate.update(
                "INSERT INTO users (username, email, password, role) VALUES (?, ?, ?, ?)",
                username, email, password, role
        );
        final long id = jdbcTemplate.queryForObject("SELECT user_id FROM users WHERE email = ?", Long.class, email);
        final User user = new User(username, email, password, role, "es");
        user.setId(id);
        return user;
    }

    protected Brand insertBrand(final String name) {
        jdbcTemplate.update("INSERT INTO brands (name) VALUES (?)", name);
        final long id = jdbcTemplate.queryForObject("SELECT brand_id FROM brands WHERE name = ?", Long.class, name);
        final Brand brand = new Brand(name);
        brand.setId(id);
        return brand;
    }

    protected BodyType insertBodyType(final String name) {
        jdbcTemplate.update("INSERT INTO body_types (name) VALUES (?)", name);
        final long id = jdbcTemplate.queryForObject("SELECT body_type_id FROM body_types WHERE name = ?", Long.class, name);
        final BodyType bodyType = new BodyType(name);
        bodyType.setId(id);
        return bodyType;
    }

    protected Car insertCar(final long brandId, final String brandName, final String model, final long bodyTypeId,
                            final String bodyType, final Integer year, final String description,
                            final String fuelType, final Integer horsepower, final Integer airbagCount,
                            final String transmission, final BigDecimal fuelConsumption,
                            final Integer maxSpeedKmh, final BigDecimal priceUsd) {
        jdbcTemplate.update(
                "INSERT INTO cars (brand_id, model, body_type_id, year, description, fuel_type, horsepower, "
                        + "airbag_count, transmission, fuel_consumption, max_speed_kmh, price_usd) "
                        + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                brandId, model, bodyTypeId, year, description, fuelType, horsepower, airbagCount, transmission,
                fuelConsumption, maxSpeedKmh, priceUsd
        );
        final long id = jdbcTemplate.queryForObject(
                "SELECT car_id FROM cars WHERE brand_id = ? AND model = ? AND body_type_id = ? AND year = ?",
                Long.class, brandId, model, bodyTypeId, year
        );
        final Car car = new Car(entityBrand(brandId, brandName), model, entityBodyType(bodyTypeId, bodyType));
        car.setId(id);
        car.setYear(year);
        car.setDescription(description);
        car.setHasImage(false);
        car.setFuelType(fuelType);
        car.setHorsepower(horsepower);
        car.setAirbagCount(airbagCount);
        car.setTransmission(transmission);
        car.setFuelConsumption(fuelConsumption);
        car.setMaxSpeedKmh(maxSpeedKmh);
        car.setPriceUsd(priceUsd);
        return car;
    }

    protected Review insertReview(final long userId, final String reviewerUsername, final long carId,
                                  final BigDecimal rating, final String title, final String body,
                                  final String ownershipStatus, final Integer modelYear,
                                  final Integer mileageKm, final Boolean wouldRecommend) {
        jdbcTemplate.update(
                "INSERT INTO reviews (user_id, car_id, rating, title, body, ownership_status, model_year, "
                        + "mileage_km, would_recommend) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)",
                userId, carId, rating, title, body, ownershipStatus, modelYear, mileageKm, wouldRecommend
        );
        final long id = jdbcTemplate.queryForObject("SELECT review_id FROM reviews WHERE title = ?", Long.class, title);
        final Review review = new Review(entityCar(carId), rating, title, body);
        review.setId(id);
        review.setUser(entityUser(userId, reviewerUsername));
        review.setOwnershipStatus(ownershipStatus);
        review.setModelYear(modelYear);
        review.setMileageKm(mileageKm);
        review.setWouldRecommend(wouldRecommend);
        return review;
    }

    protected ReviewReply insertReviewReply(final long reviewId, final long userId, final String authorUsername,
                                            final String body) {
        jdbcTemplate.update(
                "INSERT INTO review_replies (review_id, user_id, body) VALUES (?, ?, ?)",
                reviewId, userId, body
        );
        final long id = jdbcTemplate.queryForObject(
                "SELECT reply_id FROM review_replies WHERE review_id = ? AND user_id = ? AND body = ?",
                Long.class, reviewId, userId, body
        );
        final ReviewReply reply = new ReviewReply(entityReview(reviewId), entityUser(userId, authorUsername), body);
        reply.setId(id);
        return reply;
    }

    protected short createReviewTag(final String code, final String sentiment, final String dimension) {
        jdbcTemplate.update(
                "INSERT INTO review_tags (code, label_es, sentiment, dimension) VALUES (?, ?, ?, ?)",
                code,
                "Etiqueta " + code,
                sentiment,
                dimension
        );
        return jdbcTemplate.queryForObject("SELECT tag_id FROM review_tags WHERE code = ?", Short.class, code);
    }

    protected int countRows(final String sql, final Object... args) {
        return jdbcTemplate.queryForObject(sql, Integer.class, args);
    }

    private User entityUser(final long id, final String username) {
        final User user = new User(username, null, null, null, null);
        user.setId(id);
        return user;
    }

    private Brand entityBrand(final long id, final String name) {
        final Brand brand = new Brand(name);
        brand.setId(id);
        return brand;
    }

    private BodyType entityBodyType(final long id, final String name) {
        final BodyType bodyType = new BodyType(name);
        bodyType.setId(id);
        return bodyType;
    }

    private Car entityCar(final long id) {
        final Car car = new Car(entityBrand(0, null), null, entityBodyType(0, null));
        car.setId(id);
        return car;
    }

    private Review entityReview(final long id) {
        final Review review = new Review(entityCar(0), null, null, null);
        review.setId(id);
        return review;
    }
}
