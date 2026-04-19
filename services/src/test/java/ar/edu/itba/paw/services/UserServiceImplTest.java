package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.model.CarRequest;
import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.model.ReviewStats;
import ar.edu.itba.paw.persistence.CarRequestDao;
import ar.edu.itba.paw.persistence.ReviewDao;
import ar.edu.itba.paw.persistence.UserDao;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class UserServiceImplTest {

    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    void createUserStoresBcryptPasswordAndNormalizesEmail() {
        final FakeUserDao userDao = new FakeUserDao();
        final UserService userService = userService(userDao);

        final User user = userService.createUser(" driver ", "DRIVER@example.com ", "password123");

        assertEquals("driver", user.getUsername());
        assertEquals("driver@example.com", user.getEmail());
        assertEquals("user", user.getRole());
        assertNotEquals("password123", user.getPassword());
        assertTrue(user.getPassword().startsWith("$2"));
        assertTrue(passwordEncoder.matches("password123", user.getPassword()));
    }

    @Test
    void createUserRejectsDuplicateUsername() {
        final FakeUserDao userDao = new FakeUserDao();
        userDao.create("driver", "driver@example.com", "encoded", "user");
        final UserService userService = userService(userDao);

        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser("driver", "other@example.com", "password123"));
    }

    @Test
    void createUserRejectsDuplicateEmail() {
        final FakeUserDao userDao = new FakeUserDao();
        userDao.create("driver", "driver@example.com", "encoded", "user");
        final UserService userService = userService(userDao);

        assertThrows(IllegalArgumentException.class,
                () -> userService.createUser("other", "DRIVER@example.com", "password123"));
    }

    @Test
    void createUserBindsLegacyContentWithoutChangingEmailsOrReassigningRows() {
        final FakeUserDao userDao = new FakeUserDao();
        final FakeReviewDao reviewDao = new FakeReviewDao();
        final FakeCarRequestDao carRequestDao = new FakeCarRequestDao();
        reviewDao.addReview(null, "DRIVER@example.com ");
        reviewDao.addReview(null, "other@example.com");
        reviewDao.addReview(99L, "driver@example.com");
        carRequestDao.addRequest(null, " driver@example.com");
        carRequestDao.addRequest(null, "other@example.com");
        carRequestDao.addRequest(99L, "driver@example.com");
        final UserService userService = new UserServiceImpl(userDao, reviewDao, carRequestDao, passwordEncoder);

        final User user = userService.createUser("driver", "driver@example.com", "password123");

        assertEquals(user.getId(), reviewDao.userIdAt(0));
        assertEquals("DRIVER@example.com ", reviewDao.emailAt(0));
        assertNull(reviewDao.userIdAt(1));
        assertEquals(99L, reviewDao.userIdAt(2));
        assertEquals(user.getId(), carRequestDao.userIdAt(0));
        assertEquals(" driver@example.com", carRequestDao.emailAt(0));
        assertNull(carRequestDao.userIdAt(1));
        assertEquals(99L, carRequestDao.userIdAt(2));
    }


    @Test
    void getModeratorsEmailsLoadsModeratorAndAdminRoles() {
        final FakeUserDao userDao = new FakeUserDao();
        userDao.create("user", "user@example.com", "encoded", "user");
        userDao.create("mod", "mod@example.com", "encoded", "moderator");
        userDao.create("admin", "admin@example.com", "encoded", "admin");
        final UserService userService = userService(userDao);

        final List<String> emails = userService.getModeratorsEmails();

        assertEquals(List.of("mod@example.com", "admin@example.com"), emails);
    }

    private UserService userService(final FakeUserDao userDao) {
        return new UserServiceImpl(userDao, new FakeReviewDao(), new FakeCarRequestDao(), passwordEncoder);
    }

    private static final class FakeUserDao implements UserDao {
        private final List<User> users = new ArrayList<>();
        private long nextId = 1;

        @Override
        public Optional<User> findById(final long id) {
            return users.stream()
                    .filter(user -> user.getId() == id)
                    .findFirst();
        }

        @Override
        public Optional<User> findByEmail(final String email) {
            return users.stream()
                    .filter(user -> user.getEmail().equalsIgnoreCase(email))
                    .findFirst();
        }

        @Override
        public Optional<User> findByUsername(final String username) {
            return users.stream()
                    .filter(user -> user.getUsername().equals(username))
                    .findFirst();
        }

        @Override
        public User create(final String username, final String email, final String password, final String role) {
            final User user = new User(nextId++, username, email, password, role, LocalDateTime.now());
            users.add(user);
            return user;
        }

        @Override
        public List<String> findEmailsByRoles(final Collection<String> roles) {
            final List<String> normalizedRoles = roles.stream()
                    .map(role -> role.toLowerCase(Locale.ROOT))
                    .toList();
            return users.stream()
                    .filter(user -> normalizedRoles.contains(user.getRole()))
                    .map(User::getEmail)
                    .toList();
        }
    }

    private static final class FakeReviewDao implements ReviewDao {
        private final List<LegacyIdentity> reviews = new ArrayList<>();

        private void addReview(final Long userId, final String email) {
            reviews.add(new LegacyIdentity(userId, email));
        }

        private Long userIdAt(final int index) {
            return reviews.get(index).userId;
        }

        private String emailAt(final int index) {
            return reviews.get(index).email;
        }

        @Override
        public int bindReviewsToUserByEmail(final long userId, final String email) {
            int updated = 0;
            for (final LegacyIdentity review : reviews) {
                if (review.userId == null && matchesEmail(review.email, email)) {
                    review.userId = userId;
                    updated++;
                }
            }
            return updated;
        }

        @Override
        public Optional<Review> findById(final long id) {
            return Optional.empty();
        }

        @Override
        public List<Review> findAll() {
            return Collections.emptyList();
        }

        @Override
        public List<Review> findByCarId(final long carId) {
            return Collections.emptyList();
        }

        @Override
        public Optional<Review> findLatestByCarId(final long carId) {
            return Optional.empty();
        }

        @Override
        public Optional<Review> findTopRatedLatestByCarId(final long carId) {
            return Optional.empty();
        }

        @Override
        public List<Review> findByCarIdOrderByRatingAsc(final long carId) {
            return Collections.emptyList();
        }

        @Override
        public List<Review> findByCarIdOrderByRatingDesc(final long carId) {
            return Collections.emptyList();
        }

        @Override
        public List<Review> findByUserId(final long userId) {
            return Collections.emptyList();
        }

        @Override
        public Optional<ReviewStats> findStatsByCarId(final long carId) {
            return Optional.empty();
        }

        @Override
        public List<ReviewStats> findStatsByCarIds(final Collection<Long> carIds) {
            return Collections.emptyList();
        }

        @Override
        public Review create(final long userId, final long carId, final BigDecimal rating, final String title,
                             final String body, final String ownershipStatus, final Integer modelYear,
                             final Integer mileageKm, final Boolean wouldRecommend) {
            throw new UnsupportedOperationException();
        }

        @Override
        public Optional<Review> update(final long id, final long carId, final BigDecimal rating, final String title,
                                       final String body, final String ownershipStatus, final Integer modelYear,
                                       final Integer mileageKm, final Boolean wouldRecommend) {
            return Optional.empty();
        }

        @Override
        public boolean delete(final long id) {
            return false;
        }
    }

    private static final class FakeCarRequestDao implements CarRequestDao {
        private final List<LegacyIdentity> requests = new ArrayList<>();

        private void addRequest(final Long userId, final String email) {
            requests.add(new LegacyIdentity(userId, email));
        }

        private Long userIdAt(final int index) {
            return requests.get(index).userId;
        }

        private String emailAt(final int index) {
            return requests.get(index).email;
        }

        @Override
        public int bindRequestsToUserByEmail(final long userId, final String email) {
            int updated = 0;
            for (final LegacyIdentity request : requests) {
                if (request.userId == null && matchesEmail(request.email, email)) {
                    request.userId = userId;
                    updated++;
                }
            }
            return updated;
        }

        @Override
        public Optional<CarRequest> findById(final long id) {
            return Optional.empty();
        }

        @Override
        public List<CarRequest> findAll() {
            return Collections.emptyList();
        }

        @Override
        public List<CarRequest> findByStatus(final String status) {
            return Collections.emptyList();
        }

        @Override
        public CarRequest create(final long submittedByUserId, final long brandId, final long bodyTypeId,
                                 final String model, final String description, final String imageContentType,
                                 final byte[] imageData, final String status) {
            throw new UnsupportedOperationException();
        }

        @Override
        public boolean updateStatus(final long id, final String expectedStatus, final String newStatus) {
            return false;
        }
    }

    private static boolean matchesEmail(final String rowEmail, final String email) {
        return rowEmail != null && email != null && rowEmail.trim().equalsIgnoreCase(email);
    }

    private static final class LegacyIdentity {
        private Long userId;
        private final String email;

        private LegacyIdentity(final Long userId, final String email) {
            this.userId = userId;
            this.email = email;
        }
    }
}
