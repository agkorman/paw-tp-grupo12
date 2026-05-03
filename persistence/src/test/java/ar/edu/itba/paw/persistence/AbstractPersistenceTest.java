package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.model.User;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
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

    protected User createUser(final String suffix) {
        return userDao.create("user-" + suffix, "user-" + suffix + "@example.com", "secret", "user");
    }

    protected Car createCar(final String suffix) {
        final long brandId = brandDao.create("Brand " + suffix).getId();
        final long bodyTypeId = bodyTypeDao.create("Body " + suffix).getId();
        return carDao.create(brandId, "Model " + suffix, bodyTypeId, 2026, "Description " + suffix,
                "combustion", 200, 6, "automatic", new BigDecimal("8.5"), 230, new BigDecimal("35000.00"));
    }

    protected Review createReview(final String suffix) {
        final User user = createUser("review-" + suffix);
        final Car car = createCar("review-" + suffix);
        return reviewDao.create(user.getId(), car.getId(), new BigDecimal("4.0"), "Title " + suffix,
                "Body " + suffix, "owner", 2026, 1000, true);
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
}
