package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.AdminRequest;
import ar.edu.itba.paw.model.BrandRequest;
import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.CarImagePayload;
import ar.edu.itba.paw.model.CarRequest;
import ar.edu.itba.paw.model.CarRequestImage;
import ar.edu.itba.paw.model.User;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class RequestJdbcDaoTest extends AbstractPersistenceTest {

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
        assertTrue(adminRequestDao.existsPendingByUser(user.getId()));
        assertEquals(result.getId(), adminRequestDao.findByStatus("pending").get(0).getId());
    }

    @Test
    public void shouldUpdateBrandRequestStatusWhenExpectedStatusMatches() {
        // Arrange
        final User user = createUser("brand-request");
        final BrandRequest request = brandRequestDao.create(user.getId(), user.getEmail(), "New Brand", "Comment", "pending");

        // Exercise
        final boolean result = brandRequestDao.updateStatus(request.getId(), "pending", "approved");

        // Assertions
        assertTrue(result);
        assertEquals("approved", jdbcTemplate.queryForObject(
                "SELECT status FROM brand_requests WHERE brand_request_id = ?", String.class, request.getId()
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
        final BrandRequest request = brandRequestDao.create(user.getId(), user.getEmail(), "Other Brand", "Comment", "pending");

        // Exercise
        final boolean result = brandRequestDao.updateStatus(request.getId(), "approved", "rejected");

        // Assertions
        assertFalse(result);
        assertEquals("pending", jdbcTemplate.queryForObject(
                "SELECT status FROM brand_requests WHERE brand_request_id = ?", String.class, request.getId()
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
        assertEquals(result.getId(), bodyTypeRequestDao.findByStatus("pending").get(0).getId());
        assertEquals(1, bodyTypeRequestDao.findByStatus("pending", 1).getTotalItems());
    }

    @Test
    public void shouldUpdateBodyTypeRequestStatusWhenExpectedStatusMatches() {
        // Arrange
        final User user = createUser("body-request-update");
        final var request = bodyTypeRequestDao.create(user.getId(), user.getEmail(), "Liftback", "Comment", "pending");

        // Exercise
        final boolean result = bodyTypeRequestDao.updateStatus(request.getId(), "pending", "rejected");

        // Assertions
        assertTrue(result);
        assertEquals("rejected", jdbcTemplate.queryForObject(
                "SELECT status FROM body_type_requests WHERE body_type_request_id = ?", String.class, request.getId()
        ));
        assertEquals(0, countRows(
                "SELECT COUNT(*) FROM body_type_requests WHERE body_type_request_id = ? AND status = ?",
                request.getId(), "pending"
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
        final CarRequest persisted = carRequestDao.findById(result.getId()).orElseThrow();
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
        assertEquals("Requested Model", persisted.getModel());
        assertArrayEquals(new byte[]{1}, persisted.getImageData());
        assertEquals("electric", persisted.getFuelType());
        assertEquals(submitter.getId(), persisted.getSubmittedByUserId());
        assertEquals("pending", persisted.getStatus());
    }

    @Test
    public void shouldUpdateAdminRequestStatusWhenExpectedStatusMatches() {
        // Arrange
        final User user = createUser("admin-request-update");
        final AdminRequest request = adminRequestDao.create(user.getId(), user.getEmail(), "Motivation", "Bio", "Justification", "pending");

        // Exercise
        final boolean result = adminRequestDao.updateStatus(request.getId(), "pending", "approved");

        // Assertions
        assertTrue(result);
        assertEquals("approved", jdbcTemplate.queryForObject(
                "SELECT status FROM admin_requests WHERE admin_request_id = ?", String.class, request.getId()
        ));
        assertEquals(0, countRows(
                "SELECT COUNT(*) FROM admin_requests WHERE admin_request_id = ? AND status = ?",
                request.getId(), "pending"
        ));
    }

    @Test
    public void shouldUpdateCarRequestStatusWhenExpectedStatusMatches() {
        // Arrange
        final User submitter = createUser("car-request-status");
        final Car car = createCar("car-request-status");
        final CarRequest request = carRequestDao.create(submitter.getId(), submitter.getEmail(), car.getBrandId(),
                car.getBodyTypeId(), 2026, "Status Model", "Description", null, null, "pending",
                "hybrid", 250, 6, "automatic", new BigDecimal("6.5"), 230, new BigDecimal("42000.00"));

        // Exercise
        final boolean result = carRequestDao.updateStatus(request.getId(), "pending", "approved");

        // Assertions
        assertTrue(result);
        assertEquals("approved", jdbcTemplate.queryForObject(
                "SELECT status FROM car_requests WHERE car_request_id = ?", String.class, request.getId()
        ));
        assertEquals(0, countRows(
                "SELECT COUNT(*) FROM car_requests WHERE car_request_id = ? AND status = ?",
                request.getId(), "pending"
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
        final CarRequest alreadyAttributed = carRequestDao.create(otherUser.getId(), "anon@example.com",
                car.getBrandId(), car.getBodyTypeId(), 2026, "Other Model", "Other", null, null, "pending",
                "combustion", 200, 6, "automatic", new BigDecimal("8.0"), 220, new BigDecimal("30000.00"));

        // Exercise
        final int result = carRequestDao.bindRequestsToUserByEmail(boundUser.getId(), "anon@example.com");

        // Assertions
        assertEquals(1, result);
        assertEquals(boundUser.getId(), jdbcTemplate.queryForObject(
                "SELECT submitted_by_user_id FROM car_requests WHERE car_request_id = ?", Long.class, anonymousId
        ));
        assertEquals(otherUser.getId(), jdbcTemplate.queryForObject(
                "SELECT submitted_by_user_id FROM car_requests WHERE car_request_id = ?", Long.class, alreadyAttributed.getId()
        ));
    }

    @Test
    public void shouldReplaceCarRequestImagesAndFindImagePayload() {
        // Arrange
        final User user = createUser("request-images");
        final Car car = createCar("request-images");
        final CarRequest request = carRequestDao.create(user.getId(), user.getEmail(), car.getBrandId(),
                car.getBodyTypeId(), 2026, "Request Image", "Description", null, null, "pending",
                "combustion", 180, 6, "manual", new BigDecimal("7.0"), 220, new BigDecimal("30000.00"));

        // Exercise
        carRequestDao.replaceImages(request.getId(), List.of(new CarImagePayload("image/png", new byte[]{5, 6})));

        // Assertions
        final List<CarRequestImage> images = carRequestDao.findImagesByRequestId(request.getId());
        final CarRequestImage image = carRequestDao.findImageByRequestIdAndImageId(request.getId(), images.get(0).getImageId()).orElseThrow();
        assertEquals(1, countRows(
                "SELECT COUNT(*) FROM car_request_images WHERE car_request_id = ?",
                request.getId()
        ));
        assertEquals("image/png", jdbcTemplate.queryForObject(
                "SELECT content_type FROM car_request_images WHERE car_request_id = ?",
                String.class, request.getId()
        ));
        assertArrayEquals(new byte[]{5, 6}, jdbcTemplate.queryForObject(
                "SELECT image_data FROM car_request_images WHERE car_request_id = ?",
                byte[].class, request.getId()
        ));
        assertEquals(1, images.size());
        assertEquals("image/png", image.getContentType());
        assertArrayEquals(new byte[]{5, 6}, image.getImageData());
    }

    @Test
    public void shouldReplaceCarRequestImagesAndRemovePreviousImages() {
        // Arrange
        final User user = createUser("request-images-replace");
        final Car car = createCar("request-images-replace");
        final CarRequest request = carRequestDao.create(user.getId(), user.getEmail(), car.getBrandId(),
                car.getBodyTypeId(), 2026, "Request Image Replace", "Description", null, null, "pending",
                "combustion", 180, 6, "manual", new BigDecimal("7.0"), 220, new BigDecimal("30000.00"));
        carRequestDao.replaceImages(request.getId(), List.of(new CarImagePayload("image/png", new byte[]{1})));

        // Exercise
        carRequestDao.replaceImages(request.getId(), List.of(new CarImagePayload("image/jpeg", new byte[]{9})));

        // Assertions
        assertEquals(1, countRows("SELECT COUNT(*) FROM car_request_images WHERE car_request_id = ?", request.getId()));
        assertEquals(0, countRows(
                "SELECT COUNT(*) FROM car_request_images WHERE car_request_id = ? AND content_type = ?",
                request.getId(), "image/png"
        ));
        assertEquals("image/jpeg", jdbcTemplate.queryForObject(
                "SELECT content_type FROM car_request_images WHERE car_request_id = ?",
                String.class, request.getId()
        ));
        assertArrayEquals(new byte[]{9}, jdbcTemplate.queryForObject(
                "SELECT image_data FROM car_request_images WHERE car_request_id = ?",
                byte[].class, request.getId()
        ));
    }
}
