package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.CarImagePayload;
import ar.edu.itba.paw.model.CarRequest;
import ar.edu.itba.paw.persistence.CarDao;
import ar.edu.itba.paw.persistence.CarImageDao;
import ar.edu.itba.paw.persistence.CarRequestDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CarRequestServiceImplTest {

    private static final long REQUEST_ID = 1L;
    private static final long BRAND_ID = 11L;
    private static final long BODY_TYPE_ID = 22L;
    private static final long USER_ID = 33L;
    private static final String EMAIL = "user@example.com";
    private static final byte[] IMAGE_BYTES = new byte[]{1, 2, 3, 4};
    private static final String CONTENT_TYPE = "image/png";

    @Mock
    private CarRequestDao carRequestDao;
    @Mock
    private CarDao carDao;
    @Mock
    private CarImageDao carImageDao;

    @InjectMocks
    private CarRequestServiceImpl carRequestService;

    private static CarRequest pendingRequest() {
        return new CarRequest(REQUEST_ID, USER_ID, EMAIL, BRAND_ID, BODY_TYPE_ID, 2024, "Corolla",
                "A nice car description.", CONTENT_TYPE, IMAGE_BYTES, CarRequestService.STATUS_PENDING,
                LocalDateTime.now(), "GASOLINE", 130, 6, "MANUAL",
                new BigDecimal("6.5"), 190, new BigDecimal("25000.00"));
    }

    @Test
    public void shouldCreatePendingRequestWithNormalizedFieldsAndCoverImage() {
        // Arrange
        final CarImagePayload image = new CarImagePayload(CONTENT_TYPE, IMAGE_BYTES);
        final CarRequest created = pendingRequest();
        when(carRequestDao.create(eq(USER_ID), eq(EMAIL), eq(BRAND_ID), eq(BODY_TYPE_ID), eq(2024),
                eq("Corolla"), eq("A nice car description."), eq(CONTENT_TYPE), any(byte[].class),
                eq(CarRequestService.STATUS_PENDING), eq("GASOLINE"), eq(130), eq(6), eq("MANUAL"),
                eq(new BigDecimal("6.5")), eq(190), eq(new BigDecimal("25000.00"))))
                .thenReturn(created);

        // Exercise
        final CarRequest result = carRequestService.createPendingRequest(USER_ID, EMAIL, BRAND_ID, BODY_TYPE_ID, 2024,
                "  Corolla  ", "  A nice car description.  ", List.of(image),
                "GASOLINE", 130, 6, "MANUAL", new BigDecimal("6.5"), 190, new BigDecimal("25000.00"));

        // Assertions
        assertEquals(REQUEST_ID, result.getId());
        assertEquals("Corolla", result.getModel());
        assertEquals(CarRequestService.STATUS_PENDING, result.getStatus());
    }

    @Test
    public void shouldRejectCreatePendingWhenModelIsBlank() {
        // Arrange
        final CarImagePayload image = new CarImagePayload(CONTENT_TYPE, IMAGE_BYTES);

        // Exercise
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> carRequestService.createPendingRequest(USER_ID, EMAIL, BRAND_ID, BODY_TYPE_ID, 2024,
                        "  ", "desc", List.of(image), "GASOLINE", 130, 6, "MANUAL",
                        new BigDecimal("6.5"), 190, new BigDecimal("25000.00")));

        // Assertions
        assertEquals("Model is required for car requests.", ex.getMessage());
    }

    @Test
    public void shouldRejectCreatePendingWhenDescriptionIsBlank() {
        // Arrange
        final CarImagePayload image = new CarImagePayload(CONTENT_TYPE, IMAGE_BYTES);

        // Exercise
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> carRequestService.createPendingRequest(USER_ID, EMAIL, BRAND_ID, BODY_TYPE_ID, 2024,
                        "Corolla", "  ", List.of(image), "GASOLINE", 130, 6, "MANUAL",
                        new BigDecimal("6.5"), 190, new BigDecimal("25000.00")));

        // Assertions
        assertEquals("Description is required for car requests.", ex.getMessage());
    }

    @Test
    public void shouldRejectCreatePendingWhenImageIsInvalid() {
        // Arrange
        final CarImagePayload bad = new CarImagePayload(null, IMAGE_BYTES);

        // Exercise
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> carRequestService.createPendingRequest(USER_ID, EMAIL, BRAND_ID, BODY_TYPE_ID, 2024,
                        "Corolla", "desc", List.of(bad), "GASOLINE", 130, 6, "MANUAL",
                        new BigDecimal("6.5"), 190, new BigDecimal("25000.00")));

        // Assertions
        assertEquals("Image metadata and payload must be provided together.", ex.getMessage());
    }

    @Test
    public void shouldReturnEmptyListWhenStatusFilterIsBlank() {
        // Arrange
        final String blankStatus = "  ";

        // Exercise
        final List<CarRequest> result = carRequestService.getCarRequestsByStatus(blankStatus);

        // Assertions
        assertTrue(result.isEmpty());
    }

    @Test
    public void shouldReturnZeroCountWhenStatusFilterIsNull() {
        // Arrange
        final String nullStatus = null;

        // Exercise
        final long result = carRequestService.countCarRequestsByStatus(nullStatus);

        // Assertions
        assertEquals(0L, result);
    }

    @Test
    public void shouldDelegateCountToDaoWithNormalizedStatus() {
        // Arrange
        when(carRequestDao.countByStatus("pending")).thenReturn(4L);

        // Exercise
        final long result = carRequestService.countCarRequestsByStatus("  pending  ");

        // Assertions
        assertEquals(4L, result);
    }

    @Test
    public void shouldNotApproveWhenRequestIsNotPending() {
        // Arrange
        final CarRequest approved = new CarRequest(REQUEST_ID, USER_ID, EMAIL, BRAND_ID, BODY_TYPE_ID, 2024, "Corolla",
                "desc", CONTENT_TYPE, IMAGE_BYTES, CarRequestService.STATUS_APPROVED, LocalDateTime.now(),
                null, null, null, null, null, null, null);
        when(carRequestDao.findById(REQUEST_ID)).thenReturn(Optional.of(approved));

        // Exercise
        final boolean result = carRequestService.approvePendingRequest(REQUEST_ID);

        // Assertions
        assertFalse(result);
    }

    @Test
    public void shouldNotApproveWhenRequestNotFound() {
        // Arrange
        when(carRequestDao.findById(REQUEST_ID)).thenReturn(Optional.empty());

        // Exercise
        final boolean result = carRequestService.approvePendingRequest(REQUEST_ID);

        // Assertions
        assertFalse(result);
    }

    @Test
    public void shouldRejectApproveOverloadWithMismatchedImagePair() {
        // Arrange
        when(carRequestDao.findById(REQUEST_ID)).thenReturn(Optional.of(pendingRequest()));

        // Exercise
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> carRequestService.approvePendingRequest(REQUEST_ID, BRAND_ID, "Corolla", BODY_TYPE_ID, 2024,
                        "desc", Optional.of(CONTENT_TYPE), Optional.empty(),
                        "GASOLINE", 130, 6, "MANUAL", new BigDecimal("6.5"), 190, new BigDecimal("25000.00")));

        // Assertions
        assertEquals("Image metadata and payload must be provided together.", ex.getMessage());
    }

    @Test
    public void shouldApproveAndCreateCarWhenRequestIsPending() {
        // Arrange
        final Car createdCar = new Car(99L, BRAND_ID, "Toyota", "Corolla", BODY_TYPE_ID, 2024, "sedan",
                "desc", LocalDateTime.now(), false, "GASOLINE", 130, 6, "MANUAL",
                new BigDecimal("6.5"), 190, new BigDecimal("25000.00"));
        when(carRequestDao.findById(REQUEST_ID)).thenReturn(Optional.of(pendingRequest()));
        when(carRequestDao.updateStatus(REQUEST_ID, CarRequestService.STATUS_PENDING,
                CarRequestService.STATUS_APPROVED)).thenReturn(true);
        when(carDao.create(eq(BRAND_ID), eq("Corolla"), eq(BODY_TYPE_ID), eq(2024), eq("desc"),
                eq("GASOLINE"), eq(130), eq(6), eq("MANUAL"), eq(new BigDecimal("6.5")), eq(190),
                eq(new BigDecimal("25000.00")))).thenReturn(createdCar);

        // Exercise
        final boolean result = carRequestService.approvePendingRequest(REQUEST_ID, BRAND_ID, "  Corolla  ",
                BODY_TYPE_ID, 2024, "  desc  ",
                List.of(new CarImagePayload(CONTENT_TYPE, IMAGE_BYTES)),
                "GASOLINE", 130, 6, "MANUAL", new BigDecimal("6.5"), 190, new BigDecimal("25000.00"));

        // Assertions
        assertTrue(result);
    }

    @Test
    public void shouldNotCreateCarWhenStatusUpdateFails() {
        // Arrange
        when(carRequestDao.findById(REQUEST_ID)).thenReturn(Optional.of(pendingRequest()));
        when(carRequestDao.updateStatus(REQUEST_ID, CarRequestService.STATUS_PENDING,
                CarRequestService.STATUS_APPROVED)).thenReturn(false);

        // Exercise
        final boolean result = carRequestService.approvePendingRequest(REQUEST_ID, BRAND_ID, "Corolla",
                BODY_TYPE_ID, 2024, "desc",
                List.of(new CarImagePayload(CONTENT_TYPE, IMAGE_BYTES)),
                "GASOLINE", 130, 6, "MANUAL", new BigDecimal("6.5"), 190, new BigDecimal("25000.00"));

        // Assertions
        assertFalse(result);
    }

    @Test
    public void shouldRejectPendingRequestThroughDao() {
        // Arrange
        when(carRequestDao.findById(REQUEST_ID)).thenReturn(Optional.of(pendingRequest()));
        when(carRequestDao.updateStatus(REQUEST_ID, CarRequestService.STATUS_PENDING,
                CarRequestService.STATUS_REJECTED)).thenReturn(true);

        // Exercise
        final boolean result = carRequestService.rejectPendingRequest(REQUEST_ID);

        // Assertions
        assertTrue(result);
    }
}
