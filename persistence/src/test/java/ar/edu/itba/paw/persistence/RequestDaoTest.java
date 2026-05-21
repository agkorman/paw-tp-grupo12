package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.AdminRequest;
import ar.edu.itba.paw.model.BrandRequest;
import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.ImagePayload;
import ar.edu.itba.paw.model.CarRequest;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Pagination;
import ar.edu.itba.paw.model.User;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RequestDaoTest extends AbstractPersistenceTest {

    @Test
    public void shouldCreateAdminRequestAndFindPendingRequestByUser() {
        // Arrange
        final User user = createUser("admin-request");

        // Exercise
        final AdminRequest result = adminRequestDao.create(user.getId(), user.getEmail(), "Motivation", "Bio", "Justification", "pending");

        // Assertions
        assertEquals(user.getId(), result.getSubmittedByUserId());
        assertEquals("pending", result.getStatus());
        assertEquals(1, countRows(
                "SELECT COUNT(*) FROM admin_requests WHERE admin_request_id = ? AND submitted_by_user_id = ?",
                result.getId(), user.getId()
        ));
        assertEquals("Motivation", jdbcTemplate.queryForObject(
                "SELECT motivation FROM admin_requests WHERE admin_request_id = ?", String.class, result.getId()
        ));
        assertEquals("pending", jdbcTemplate.queryForObject(
                "SELECT status FROM admin_requests WHERE admin_request_id = ?", String.class, result.getId()
        ));
    }

    @Test
    public void shouldUpdateBrandRequestStatusWhenExpectedStatusMatches() {
        // Arrange
        final User user = createUser("brand-request");
        jdbcTemplate.update(
                "INSERT INTO brand_requests (submitted_by_user_id, submitter_email, name, comments, status) "
                        + "VALUES (?, ?, ?, ?, ?)",
                user.getId(), user.getEmail(), "New Brand", "Comment", "pending"
        );
        final long requestId = jdbcTemplate.queryForObject(
                "SELECT brand_request_id FROM brand_requests WHERE name = ?", Long.class, "New Brand"
        );

        // Exercise
        final boolean result = brandRequestDao.updateStatus(requestId, "pending", "approved");

        // Assertions
        assertTrue(result);
        assertEquals("approved", jdbcTemplate.queryForObject(
                "SELECT status FROM brand_requests WHERE brand_request_id = ?", String.class, requestId
        ));
        assertEquals(1, countRows("SELECT COUNT(*) FROM brand_requests WHERE status = ?", "approved"));
    }

    @Test
    public void shouldCreateBrandRequestAndPersistSubmitterAndStatus() {
        // Arrange
        final User user = createUser("brand-request-create");

        // Exercise
        final BrandRequest result = brandRequestDao.create(user.getId(), user.getEmail(), "Created Brand", "Comment", "pending");

        // Assertions
        assertEquals("Created Brand", result.getName());
        assertEquals(1, countRows(
                "SELECT COUNT(*) FROM brand_requests WHERE brand_request_id = ? AND submitted_by_user_id = ?",
                result.getId(), user.getId()
        ));
        assertEquals("pending", jdbcTemplate.queryForObject(
                "SELECT status FROM brand_requests WHERE brand_request_id = ?", String.class, result.getId()
        ));
    }

    @Test
    public void shouldNotUpdateBrandRequestStatusWhenExpectedStatusDoesNotMatch() {
        // Arrange
        final User user = createUser("brand-request-mismatch");
        jdbcTemplate.update(
                "INSERT INTO brand_requests (submitted_by_user_id, submitter_email, name, comments, status) "
                        + "VALUES (?, ?, ?, ?, ?)",
                user.getId(), user.getEmail(), "Other Brand", "Comment", "pending"
        );
        final long requestId = jdbcTemplate.queryForObject(
                "SELECT brand_request_id FROM brand_requests WHERE name = ?", Long.class, "Other Brand"
        );

        // Exercise
        final boolean result = brandRequestDao.updateStatus(requestId, "approved", "rejected");

        // Assertions
        assertFalse(result);
        assertEquals("pending", jdbcTemplate.queryForObject(
                "SELECT status FROM brand_requests WHERE brand_request_id = ?", String.class, requestId
        ));
    }

    @Test
    public void shouldCreateBodyTypeRequestAndReturnItByStatus() {
        // Arrange
        final User user = createUser("body-request");

        // Exercise
        final var result = bodyTypeRequestDao.create(user.getId(), user.getEmail(), "Roadster", "Comment", "pending");

        // Assertions
        assertEquals("Roadster", result.getName());
        assertEquals(1, countRows(
                "SELECT COUNT(*) FROM body_type_requests WHERE body_type_request_id = ? AND name = ?",
                result.getId(), "Roadster"
        ));
        assertEquals("pending", jdbcTemplate.queryForObject(
                "SELECT status FROM body_type_requests WHERE body_type_request_id = ?", String.class, result.getId()
        ));
    }

    @Test
    public void shouldPaginateAdminRequestsByStatusAndClampOutOfRangePage() {
        // Arrange
        final User user = createUser("admin-request-page");
        for (int i = 0; i < Pagination.REQUESTS_PAGE_SIZE + 1; i++) {
            jdbcTemplate.update(
                    "INSERT INTO admin_requests (submitted_by_user_id, submitter_email, motivation, bio, justification, status) "
                            + "VALUES (?, ?, ?, ?, ?, ?)",
                    user.getId(), user.getEmail(), "Motivation " + i, "Bio " + i, "Justification " + i, "pending"
            );
        }

        // Exercise
        final Page<AdminRequest> result = adminRequestDao.findByStatus("pending", 999);

        // Assertions
        assertEquals(Pagination.REQUESTS_PAGE_SIZE + 1L, result.getTotalItems());
        assertEquals(2, result.getPageNumber());
        assertEquals(1, result.getItems().size());
        assertEquals("pending", result.getItems().get(0).getStatus());
    }

    @Test
    public void shouldPaginateBrandRequestsByStatusAndExcludeOtherStatuses() {
        // Arrange
        final User user = createUser("brand-request-page");
        for (int i = 0; i < Pagination.REQUESTS_PAGE_SIZE + 1; i++) {
            jdbcTemplate.update(
                    "INSERT INTO brand_requests (submitted_by_user_id, submitter_email, name, comments, status) "
                            + "VALUES (?, ?, ?, ?, ?)",
                    user.getId(), user.getEmail(), "Paged Brand " + i, "Comment", "pending"
            );
        }
        jdbcTemplate.update(
                "INSERT INTO brand_requests (submitted_by_user_id, submitter_email, name, comments, status) "
                        + "VALUES (?, ?, ?, ?, ?)",
                user.getId(), user.getEmail(), "Approved Brand", "Comment", "approved"
        );

        // Exercise
        final Page<BrandRequest> result = brandRequestDao.findByStatus("pending", 2);

        // Assertions
        assertEquals(Pagination.REQUESTS_PAGE_SIZE + 1L, result.getTotalItems());
        assertEquals(2, result.getPageNumber());
        assertEquals(1, result.getItems().size());
        assertEquals("pending", result.getItems().get(0).getStatus());
    }

    @Test
    public void shouldUpdateBodyTypeRequestStatusWhenExpectedStatusMatches() {
        // Arrange
        final User user = createUser("body-request-update");
        jdbcTemplate.update(
                "INSERT INTO body_type_requests (submitted_by_user_id, submitter_email, name, comments, status) "
                        + "VALUES (?, ?, ?, ?, ?)",
                user.getId(), user.getEmail(), "Liftback", "Comment", "pending"
        );
        final long requestId = jdbcTemplate.queryForObject(
                "SELECT body_type_request_id FROM body_type_requests WHERE name = ?", Long.class, "Liftback"
        );

        // Exercise
        final boolean result = bodyTypeRequestDao.updateStatus(requestId, "pending", "rejected");

        // Assertions
        assertTrue(result);
        assertEquals("rejected", jdbcTemplate.queryForObject(
                "SELECT status FROM body_type_requests WHERE body_type_request_id = ?", String.class, requestId
        ));
        assertEquals(0, countRows(
                "SELECT COUNT(*) FROM body_type_requests WHERE body_type_request_id = ? AND status = ?",
                requestId, "pending"
        ));
    }

    @Test
    public void shouldCreateCarRequestWithSpecsAndPersistImageAndFuelType() {
        // Arrange
        final User submitter = createUser("car-request-create");
        final Car car = createCar("car-request-create");

        // Exercise
        final CarRequest result = carRequestDao.create(submitter.getId(), submitter.getEmail(), car.getBrandId(),
                car.getBodyTypeId(), 2026, "Requested Model", "Requested description", "image/png", new byte[]{1},
                "pending", "electric", 320, 8, "automatic", new BigDecimal("0.0"), 240, new BigDecimal("61000.00"));

        // Assertions
        assertEquals(1, countRows("SELECT COUNT(*) FROM car_requests WHERE car_request_id = ?", result.getId()));
        assertEquals("Requested Model", jdbcTemplate.queryForObject(
                "SELECT model FROM car_requests WHERE car_request_id = ?", String.class, result.getId()
        ));
        assertArrayEquals(new byte[]{1}, jdbcTemplate.queryForObject(
                "SELECT image_data FROM car_requests WHERE car_request_id = ?", byte[].class, result.getId()
        ));
        assertEquals("electric", jdbcTemplate.queryForObject(
                "SELECT fuel_type FROM car_requests WHERE car_request_id = ?", String.class, result.getId()
        ));
    }

    @Test
    public void shouldPaginateCarRequestsByStatusAndClampNegativePageToFirstPage() {
        // Arrange
        final User submitter = createUser("car-request-page");
        final Car car = createCar("car-request-page");
        for (int i = 0; i < Pagination.REQUESTS_PAGE_SIZE + 1; i++) {
            jdbcTemplate.update(
                    "INSERT INTO car_requests (submitted_by_user_id, submitter_email, brand_id, body_type_id, year, "
                            + "model, description, status, fuel_type, horsepower, airbag_count, transmission, "
                            + "fuel_consumption, max_speed_kmh, price_usd) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                    submitter.getId(), submitter.getEmail(), car.getBrandId(), car.getBodyTypeId(), 2026,
                    "Paged Request " + i, "Description", "pending", "hybrid", 250, 6, "automatic",
                    new BigDecimal("6.5"), 230, new BigDecimal("42000.00")
            );
        }

        // Exercise
        final Page<CarRequest> result = carRequestDao.findByStatus("pending", -4);

        // Assertions
        assertEquals(Pagination.REQUESTS_PAGE_SIZE + 1L, result.getTotalItems());
        assertEquals(1, result.getPageNumber());
        assertEquals(Pagination.REQUESTS_PAGE_SIZE, result.getItems().size());
        assertEquals("pending", result.getItems().get(0).getStatus());
    }

    @Test
    public void shouldUpdateAdminRequestStatusWhenExpectedStatusMatches() {
        // Arrange
        final User user = createUser("admin-request-update");
        jdbcTemplate.update(
                "INSERT INTO admin_requests (submitted_by_user_id, submitter_email, motivation, bio, justification, status) "
                        + "VALUES (?, ?, ?, ?, ?, ?)",
                user.getId(), user.getEmail(), "Motivation", "Bio", "Justification", "pending"
        );
        final long requestId = jdbcTemplate.queryForObject(
                "SELECT admin_request_id FROM admin_requests WHERE motivation = ?", Long.class, "Motivation"
        );

        // Exercise
        final boolean result = adminRequestDao.updateStatus(requestId, "pending", "approved");

        // Assertions
        assertTrue(result);
        assertEquals("approved", jdbcTemplate.queryForObject(
                "SELECT status FROM admin_requests WHERE admin_request_id = ?", String.class, requestId
        ));
        assertEquals(0, countRows(
                "SELECT COUNT(*) FROM admin_requests WHERE admin_request_id = ? AND status = ?",
                requestId, "pending"
        ));
    }

    @Test
    public void shouldUpdateCarRequestStatusWhenExpectedStatusMatches() {
        // Arrange
        final User submitter = createUser("car-request-status");
        final Car car = createCar("car-request-status");
        jdbcTemplate.update(
                "INSERT INTO car_requests (submitted_by_user_id, submitter_email, brand_id, body_type_id, year, "
                        + "model, description, status, fuel_type, horsepower, airbag_count, transmission, "
                        + "fuel_consumption, max_speed_kmh, price_usd) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                submitter.getId(), submitter.getEmail(), car.getBrandId(), car.getBodyTypeId(), 2026,
                "Status Model", "Description", "pending", "hybrid", 250, 6, "automatic",
                new BigDecimal("6.5"), 230, new BigDecimal("42000.00")
        );
        final long requestId = jdbcTemplate.queryForObject(
                "SELECT car_request_id FROM car_requests WHERE model = ?", Long.class, "Status Model"
        );

        // Exercise
        final boolean result = carRequestDao.updateStatus(requestId, "pending", "approved");

        // Assertions
        assertTrue(result);
        assertEquals("approved", jdbcTemplate.queryForObject(
                "SELECT status FROM car_requests WHERE car_request_id = ?", String.class, requestId
        ));
        assertEquals(0, countRows(
                "SELECT COUNT(*) FROM car_requests WHERE car_request_id = ? AND status = ?",
                requestId, "pending"
        ));
    }

    @Test
    public void shouldBindAnonymousCarRequestsToUserByEmailIgnoringCaseAndOnlyTouchingAnonymousRows() {
        // Arrange
        final User boundUser = createUser("car-request-bind");
        final User otherUser = createUser("car-request-other");
        final Car car = createCar("car-request-bind");
        jdbcTemplate.update(
                "INSERT INTO car_requests "
                        + "(submitted_by_user_id, submitter_email, brand_id, body_type_id, model, description, status) "
                        + "VALUES (NULL, ?, ?, ?, ?, ?, ?)",
                "  Anon@Example.com  ", car.getBrandId(), car.getBodyTypeId(), "Anon Model", "Anon", "pending"
        );
        final long anonymousId = jdbcTemplate.queryForObject(
                "SELECT car_request_id FROM car_requests WHERE model = ?", Long.class, "Anon Model"
        );
        jdbcTemplate.update(
                "INSERT INTO car_requests (submitted_by_user_id, submitter_email, brand_id, body_type_id, year, "
                        + "model, description, status, fuel_type, horsepower, airbag_count, transmission, "
                        + "fuel_consumption, max_speed_kmh, price_usd) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                otherUser.getId(), "anon@example.com", car.getBrandId(), car.getBodyTypeId(), 2026,
                "Other Model", "Other", "pending", "combustion", 200, 6, "automatic",
                new BigDecimal("8.0"), 220, new BigDecimal("30000.00")
        );
        final long alreadyAttributedId = jdbcTemplate.queryForObject(
                "SELECT car_request_id FROM car_requests WHERE model = ?", Long.class, "Other Model"
        );

        // Exercise
        final int result = carRequestDao.bindRequestsToUserByEmail(boundUser.getId(), "anon@example.com");

        // Assertions
        assertEquals(1, result);
        assertEquals(boundUser.getId(), jdbcTemplate.queryForObject(
                "SELECT submitted_by_user_id FROM car_requests WHERE car_request_id = ?", Long.class, anonymousId
        ));
        assertEquals(otherUser.getId(), jdbcTemplate.queryForObject(
                "SELECT submitted_by_user_id FROM car_requests WHERE car_request_id = ?", Long.class, alreadyAttributedId
        ));
    }

    @Test
    public void shouldReplaceCarRequestImagesAndFindImagePayload() {
        // Arrange
        final User user = createUser("request-images");
        final Car car = createCar("request-images");
        jdbcTemplate.update(
                "INSERT INTO car_requests (submitted_by_user_id, submitter_email, brand_id, body_type_id, year, "
                        + "model, description, status, fuel_type, horsepower, airbag_count, transmission, "
                        + "fuel_consumption, max_speed_kmh, price_usd) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                user.getId(), user.getEmail(), car.getBrandId(), car.getBodyTypeId(), 2026, "Request Image",
                "Description", "pending", "combustion", 180, 6, "manual", new BigDecimal("7.0"), 220,
                new BigDecimal("30000.00")
        );
        final long requestId = jdbcTemplate.queryForObject(
                "SELECT car_request_id FROM car_requests WHERE model = ?", Long.class, "Request Image"
        );

        // Exercise
        carRequestDao.replaceImages(requestId, List.of(new ImagePayload("image/png", new byte[]{5, 6})));

        // Assertions
        assertEquals(1, countRows(
                "SELECT COUNT(*) FROM car_request_images WHERE car_request_id = ?",
                requestId
        ));
        assertEquals("image/png", jdbcTemplate.queryForObject(
                "SELECT content_type FROM car_request_images WHERE car_request_id = ?",
                String.class, requestId
        ));
        assertArrayEquals(new byte[]{5, 6}, jdbcTemplate.queryForObject(
                "SELECT image_data FROM car_request_images WHERE car_request_id = ?",
                byte[].class, requestId
        ));
    }

    @Test
    public void shouldReturnFalseWhenUpdatingAdminRequestStatusWithMismatch() {
        // Arrange
        final User user = createUser("admin-request-mismatch");
        jdbcTemplate.update(
                "INSERT INTO admin_requests (submitted_by_user_id, submitter_email, motivation, bio, justification, status) "
                        + "VALUES (?, ?, ?, ?, ?, ?)",
                user.getId(), user.getEmail(), "Motivation", "Bio", "Justification", "pending"
        );
        final long requestId = jdbcTemplate.queryForObject(
                "SELECT admin_request_id FROM admin_requests WHERE submitter_email = ?",
                Long.class, user.getEmail()
        );

        // Exercise
        final boolean result = adminRequestDao.updateStatus(requestId, "approved", "rejected");

        // Assertions
        assertFalse(result);
        assertEquals("pending", jdbcTemplate.queryForObject(
                "SELECT status FROM admin_requests WHERE admin_request_id = ?", String.class, requestId
        ));
    }

    @Test
    public void shouldReturnFalseWhenUpdatingBodyTypeRequestStatusWhenExpectedDoesNotMatch() {
        // Arrange
        final User user = createUser("body-request-mismatch");
        jdbcTemplate.update(
                "INSERT INTO body_type_requests (submitted_by_user_id, submitter_email, name, comments, status) "
                        + "VALUES (?, ?, ?, ?, ?)",
                user.getId(), user.getEmail(), "Mismatch Body", "Comment", "pending"
        );
        final long requestId = jdbcTemplate.queryForObject(
                "SELECT body_type_request_id FROM body_type_requests WHERE name = ?", Long.class, "Mismatch Body"
        );

        // Exercise
        final boolean result = bodyTypeRequestDao.updateStatus(requestId, "approved", "rejected");

        // Assertions
        assertFalse(result);
        assertEquals("pending", jdbcTemplate.queryForObject(
                "SELECT status FROM body_type_requests WHERE body_type_request_id = ?", String.class, requestId
        ));
    }

    @Test
    public void shouldReturnFalseWhenUpdatingCarRequestStatusWhenExpectedDoesNotMatch() {
        // Arrange
        final User submitter = createUser("car-request-mismatch");
        final Car car = createCar("car-request-mismatch");
        jdbcTemplate.update(
                "INSERT INTO car_requests (submitted_by_user_id, submitter_email, brand_id, body_type_id, year, "
                        + "model, description, status, fuel_type, horsepower, airbag_count, transmission, "
                        + "fuel_consumption, max_speed_kmh, price_usd) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                submitter.getId(), submitter.getEmail(), car.getBrandId(), car.getBodyTypeId(), 2026,
                "Mismatch Model", "Description", "pending", "combustion", 200, 6, "manual",
                new BigDecimal("8.0"), 220, new BigDecimal("30000.00")
        );
        final long requestId = jdbcTemplate.queryForObject(
                "SELECT car_request_id FROM car_requests WHERE model = ?", Long.class, "Mismatch Model"
        );

        // Exercise
        final boolean result = carRequestDao.updateStatus(requestId, "approved", "rejected");

        // Assertions
        assertFalse(result);
        assertEquals("pending", jdbcTemplate.queryForObject(
                "SELECT status FROM car_requests WHERE car_request_id = ?", String.class, requestId
        ));
    }

    @Test
    public void shouldReturnTrueWhenPendingAdminRequestExistsForUser() {
        // Arrange
        final User user = createUser("admin-request-exists");
        jdbcTemplate.update(
                "INSERT INTO admin_requests (submitted_by_user_id, submitter_email, motivation, bio, justification, status) "
                        + "VALUES (?, ?, ?, ?, ?, ?)",
                user.getId(), user.getEmail(), "Motivation", "Bio", "Justification", "pending"
        );

        // Exercise
        final boolean result = adminRequestDao.existsPendingByUser(user.getId());

        // Assertions
        assertTrue(result);
    }

    @Test
    public void shouldReturnFalseWhenNoPendingAdminRequestExistsForUser() {
        // Arrange
        final User user = createUser("admin-request-none");

        // Exercise
        final boolean result = adminRequestDao.existsPendingByUser(user.getId());

        // Assertions
        assertFalse(result);
    }

    @Test
    public void shouldReplaceCarRequestImagesAndRemovePreviousImages() {
        // Arrange
        final User user = createUser("request-images-replace");
        final Car car = createCar("request-images-replace");
        jdbcTemplate.update(
                "INSERT INTO car_requests (submitted_by_user_id, submitter_email, brand_id, body_type_id, year, "
                        + "model, description, status, fuel_type, horsepower, airbag_count, transmission, "
                        + "fuel_consumption, max_speed_kmh, price_usd) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)",
                user.getId(), user.getEmail(), car.getBrandId(), car.getBodyTypeId(), 2026, "Request Image Replace",
                "Description", "pending", "combustion", 180, 6, "manual", new BigDecimal("7.0"), 220,
                new BigDecimal("30000.00")
        );
        final long requestId = jdbcTemplate.queryForObject(
                "SELECT car_request_id FROM car_requests WHERE model = ?", Long.class, "Request Image Replace"
        );
        jdbcTemplate.update(
                "INSERT INTO car_request_images (car_request_id, display_order, content_type, image_data) "
                        + "VALUES (?, ?, ?, ?)",
                requestId, 0, "image/png", new byte[]{1}
        );

        // Exercise
        carRequestDao.replaceImages(requestId, List.of(new ImagePayload("image/jpeg", new byte[]{9})));

        // Assertions
        assertEquals(1, countRows("SELECT COUNT(*) FROM car_request_images WHERE car_request_id = ?", requestId));
        assertEquals(0, countRows(
                "SELECT COUNT(*) FROM car_request_images WHERE car_request_id = ? AND content_type = ?",
                requestId, "image/png"
        ));
        assertEquals("image/jpeg", jdbcTemplate.queryForObject(
                "SELECT content_type FROM car_request_images WHERE car_request_id = ?",
                String.class, requestId
        ));
        assertArrayEquals(new byte[]{9}, jdbcTemplate.queryForObject(
                "SELECT image_data FROM car_request_images WHERE car_request_id = ?",
                byte[].class, requestId
        ));
    }

    @Test
    public void shouldPaginateBodyTypeRequestsByStatusAcrossMultiplePages() {
        // Arrange
        final User user = createUser("body-req-paged");
        for (int i = 0; i < Pagination.REQUESTS_PAGE_SIZE + 1; i++) {
            jdbcTemplate.update(
                    "INSERT INTO body_type_requests (submitted_by_user_id, submitter_email, name, comments, status) "
                            + "VALUES (?, ?, ?, ?, ?)",
                    user.getId(), user.getEmail(), "Body Type " + i, "Comment", "pending"
            );
        }

        // Exercise
        final var result = bodyTypeRequestDao.findByStatus("pending", 2);

        // Assertions
        assertEquals(Pagination.REQUESTS_PAGE_SIZE + 1L, result.getTotalItems());
        assertEquals(2, result.getPageNumber());
        assertEquals(1, result.getItems().size());
    }
}
