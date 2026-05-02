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
        assertTrue(adminRequestDao.existsPendingByUser(user.getId()));
        assertEquals(result.getId(), adminRequestDao.findByStatus("pending").get(0).getId());
    }

    @Test
    public void shouldUpdateBrandRequestStatusOnlyWhenExpectedStatusMatches() {
        // Arrange
        final User user = createUser("brand-request");
        final BrandRequest request = brandRequestDao.create(user.getId(), user.getEmail(), "New Brand", "Comment", "pending");

        // Exercise
        final boolean result = brandRequestDao.updateStatus(request.getId(), "pending", "approved");

        // Assertions
        assertTrue(result);
        assertFalse(brandRequestDao.updateStatus(request.getId(), "pending", "rejected"));
        assertEquals("approved", brandRequestDao.findById(request.getId()).orElseThrow().getStatus());
        assertEquals(1, brandRequestDao.countByStatus("approved"));
    }

    @Test
    public void shouldCreateBodyTypeRequestAndReturnItByStatus() {
        // Arrange
        final User user = createUser("body-request");

        // Exercise
        final var result = bodyTypeRequestDao.create(user.getId(), user.getEmail(), "Roadster", "Comment", "pending");

        // Assertions
        assertEquals("Roadster", result.getName());
        assertEquals(result.getId(), bodyTypeRequestDao.findByStatus("pending").get(0).getId());
        assertEquals(1, bodyTypeRequestDao.findByStatus("pending", 1).getTotalItems());
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
        assertEquals("Requested Model", persisted.getModel());
        assertArrayEquals(new byte[]{1}, persisted.getImageData());
        assertEquals("electric", persisted.getFuelType());
        assertEquals(submitter.getId(), persisted.getSubmittedByUserId());
        assertEquals("pending", persisted.getStatus());
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
        assertEquals(boundUser.getId(), carRequestDao.findById(anonymousId).orElseThrow().getSubmittedByUserId());
        assertEquals(otherUser.getId(), carRequestDao.findById(alreadyAttributed.getId()).orElseThrow().getSubmittedByUserId());
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
        assertEquals(1, images.size());
        assertEquals("image/png", image.getContentType());
        assertArrayEquals(new byte[]{5, 6}, image.getImageData());
    }
}
