package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.BodyType;
import ar.edu.itba.paw.model.Brand;
import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.CarImagePayload;
import ar.edu.itba.paw.model.CarRequest;
import ar.edu.itba.paw.persistence.BodyTypeDao;
import ar.edu.itba.paw.persistence.BrandDao;
import ar.edu.itba.paw.persistence.CarDao;
import ar.edu.itba.paw.persistence.CarImageDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CarServiceImplTest {

    private static final long CAR_ID = 5L;
    private static final long BRAND_ID = 1L;
    private static final long BODY_TYPE_ID = 2L;
    private static final byte[] IMAGE_BYTES = new byte[]{1, 2, 3};

    @Mock
    private CarDao carDao;
    @Mock
    private CarImageDao carImageDao;
    @Mock
    private CarRequestService carRequestService;
    @Mock
    private BrandDao brandDao;
    @Mock
    private BodyTypeDao bodyTypeDao;
    @Mock
    private EmailService emailService;

    @InjectMocks
    private CarServiceImpl carService;

    private static Car car() {
        return TestModels.car(CAR_ID, BRAND_ID, "Toyota", "Corolla", BODY_TYPE_ID, 2024, "sedan",
                "desc", LocalDateTime.now(), false, "GASOLINE", 100, 6, "MANUAL",
                new BigDecimal("6.0"), 180, new BigDecimal("20000.00"));
    }

    @Test
    public void shouldDeleteCarAndItsReviewsWhenCarExists() {
        // Arrange
        when(carDao.findById(CAR_ID)).thenReturn(Optional.of(car()));
        when(carDao.delete(CAR_ID)).thenReturn(true);

        // Exercise
        final boolean result = carService.deleteCar(CAR_ID);

        // Assertions
        assertTrue(result);
    }

    @Test
    public void shouldNotDeleteCarWhenItDoesNotExist() {
        // Arrange
        when(carDao.findById(CAR_ID)).thenReturn(Optional.empty());

        // Exercise
        final boolean result = carService.deleteCar(CAR_ID);

        // Assertions
        assertFalse(result);
    }

    @Test
    public void shouldRejectUpdateCarWhenModelIsBlank() {
        // Arrange
        final String blankModel = "  ";

        // Exercise
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> carService.updateCar(CAR_ID, BRAND_ID, blankModel, BODY_TYPE_ID, 2024, "desc",
                        Optional.empty(), Optional.empty(),
                        "GASOLINE", 100, 6, "MANUAL", new BigDecimal("6.0"), 180, new BigDecimal("20000.00")));

        // Assertions
        assertEquals("Model is required for car update.", ex.getMessage());
    }

    @Test
    public void shouldRejectUpdateCarWhenDescriptionIsBlank() {
        // Arrange
        final String blankDescription = "  ";

        // Exercise
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> carService.updateCar(CAR_ID, BRAND_ID, "Corolla", BODY_TYPE_ID, 2024, blankDescription,
                        Optional.empty(), Optional.empty(),
                        "GASOLINE", 100, 6, "MANUAL", new BigDecimal("6.0"), 180, new BigDecimal("20000.00")));

        // Assertions
        assertEquals("Description is required for car update.", ex.getMessage());
    }

    @Test
    public void shouldRejectUpdateCarWithMismatchedImagePair() {
        // Arrange
        final Optional<String> contentType = Optional.of("image/png");
        final Optional<byte[]> imageData = Optional.empty();

        // Exercise
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> carService.updateCar(CAR_ID, BRAND_ID, "Corolla", BODY_TYPE_ID, 2024, "desc",
                        contentType, imageData,
                        "GASOLINE", 100, 6, "MANUAL", new BigDecimal("6.0"), 180, new BigDecimal("20000.00")));

        // Assertions
        assertEquals("Image metadata and payload must be provided together.", ex.getMessage());
    }

    @Test
    public void shouldUpdateCarWhenInputsAreValid() {
        // Arrange
        when(carDao.update(anyLong(), anyLong(), any(), anyLong(), any(), any(),
                any(), any(), any(), any(), any(), any(), any())).thenReturn(Optional.of(car()));

        // Exercise
        final Optional<Car> result = carService.updateCar(CAR_ID, BRAND_ID, "  Corolla  ", BODY_TYPE_ID, 2024,
                "  desc  ", Optional.empty(), Optional.empty(),
                "GASOLINE", 100, 6, "MANUAL", new BigDecimal("6.0"), 180, new BigDecimal("20000.00"));

        // Assertions
        assertTrue(result.isPresent());
        assertEquals(CAR_ID, result.get().getId());
    }

    @Test
    public void shouldRejectRequestCarCreationWithBlankDescription() {
        // Arrange
        final List<CarImagePayload> images = List.of(new CarImagePayload("image/png", IMAGE_BYTES));

        // Exercise
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> carService.requestCarCreation(BRAND_ID, "Corolla", BODY_TYPE_ID, 2024, 1L, "u@x.com",
                        Optional.of("   "), images, "GASOLINE", 100, 6, "MANUAL",
                        new BigDecimal("6.0"), 180, new BigDecimal("20000.00")));

        // Assertions
        assertEquals("Description is required for car creation.", ex.getMessage());
    }

    @Test
    public void shouldRejectRequestCarCreationWhenNoImagesProvided() {
        // Arrange
        final List<CarImagePayload> images = List.of();

        // Exercise
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> carService.requestCarCreation(BRAND_ID, "Corolla", BODY_TYPE_ID, 2024, 1L, "u@x.com",
                        Optional.of("desc"), images, "GASOLINE", 100, 6, "MANUAL",
                        new BigDecimal("6.0"), 180, new BigDecimal("20000.00")));

        // Assertions
        assertEquals("At least one image is required for car creation.", ex.getMessage());
    }

    @Test
    public void shouldDelegateRequestCarCreationToCarRequestService() {
        // Arrange
        final List<CarImagePayload> images = List.of(new CarImagePayload("image/png", IMAGE_BYTES));
        final CarRequest createdRequest = TestModels.carRequest(99L, 1L, "u@x.com", BRAND_ID, BODY_TYPE_ID, 2024, "Corolla",
                "desc", "image/png", IMAGE_BYTES, CarRequestService.STATUS_PENDING, LocalDateTime.now(),
                "GASOLINE", 100, 6, "MANUAL", new BigDecimal("6.0"), 180, new BigDecimal("20000.00"));
        when(carRequestService.createPendingRequest(anyLong(), any(), anyLong(), anyLong(), any(), any(), any(),
                any(), any(), any(), any(), any(), any(), any(), any())).thenReturn(createdRequest);
        when(brandDao.findById(BRAND_ID)).thenReturn(Optional.of(TestModels.brand(BRAND_ID, "Toyota", LocalDateTime.now())));
        when(bodyTypeDao.findById(BODY_TYPE_ID)).thenReturn(Optional.of(TestModels.bodyType(BODY_TYPE_ID, "sedan", LocalDateTime.now())));

        // Exercise
        final CarRequest result = carService.requestCarCreation(BRAND_ID, "Corolla", BODY_TYPE_ID, 2024, 1L, "u@x.com",
                Optional.of("desc"), images, "GASOLINE", 100, 6, "MANUAL",
                new BigDecimal("6.0"), 180, new BigDecimal("20000.00"));

        // Assertions
        assertEquals(99L, result.getId());
        assertEquals("Corolla", result.getModel());
        verify(emailService).sendNewCarRequestNotification(createdRequest, "Toyota", "sedan");
    }

    @Test
    public void shouldReturnEmptyListForBrandBodyTypeWhenBrandUnknown() {
        // Arrange
        when(brandDao.findByName("Unknown")).thenReturn(Optional.empty());

        // Exercise
        final List<Car> result = carService.getCarsByBrandAndBodyType("Unknown", "sedan");

        // Assertions
        assertTrue(result.isEmpty());
    }

    @Test
    public void shouldReturnEmptyListForBrandBodyTypeWhenBodyTypeUnknown() {
        // Arrange
        when(brandDao.findByName("Toyota")).thenReturn(Optional.of(TestModels.brand(1L, "Toyota", LocalDateTime.now())));
        when(bodyTypeDao.findByName("Unknown")).thenReturn(Optional.empty());

        // Exercise
        final List<Car> result = carService.getCarsByBrandAndBodyType("Toyota", "Unknown");

        // Assertions
        assertTrue(result.isEmpty());
    }

    @Test
    public void shouldDelegateBrandBodyTypeLookupToCarDaoWhenBothExist() {
        // Arrange
        when(brandDao.findByName("Toyota")).thenReturn(Optional.of(TestModels.brand(1L, "Toyota", LocalDateTime.now())));
        when(bodyTypeDao.findByName("sedan")).thenReturn(Optional.of(TestModels.bodyType(2L, "sedan", LocalDateTime.now())));
        when(carDao.findByBrandIdAndBodyTypeId(1L, 2L)).thenReturn(List.of(car()));

        // Exercise
        final List<Car> result = carService.getCarsByBrandAndBodyType("Toyota", "sedan");

        // Assertions
        assertEquals(1, result.size());
        assertEquals(CAR_ID, result.get(0).getId());
    }

    @Test
    public void shouldReturnTopRatedCarsWhenCountMeetsLimit() {
        // Arrange
        final int limit = 2;
        when(carDao.findTopRated(limit)).thenReturn(List.of(car(), car()));

        // Exercise
        final List<Car> result = carService.getFeaturedCars(limit);

        // Assertions
        assertEquals(2, result.size());
    }

    @Test
    public void shouldFillWithRecentlyAddedCarsWhenTopRatedBelowLimit() {
        // Arrange
        final int limit = 3;
        final Car recent = TestModels.car(6L, BRAND_ID, "Honda", "Civic", BODY_TYPE_ID, 2023, "sedan",
                "desc", LocalDateTime.now(), false, "GASOLINE", 90, 4, "MANUAL",
                new BigDecimal("5.0"), 160, new BigDecimal("15000.00"));
        when(carDao.findTopRated(limit)).thenReturn(List.of(car()));
        when(carDao.findRecentlyAdded(anyInt(), anyCollection())).thenReturn(List.of(recent, recent));

        // Exercise
        final List<Car> result = carService.getFeaturedCars(limit);

        // Assertions
        assertEquals(3, result.size());
        assertEquals(CAR_ID, result.get(0).getId());
        assertEquals(6L, result.get(1).getId());
    }

    @Test
    public void shouldReturnFalseForDuplicateWhenModelIsBlank() {
        // Arrange
        final String blankModel = "   ";

        // Exercise
        final boolean result = carService.existsDuplicateCar("Toyota", "sedan", blankModel, 2024, 0L);

        // Assertions
        assertFalse(result);
    }

    @Test
    public void shouldDetectDuplicateCarInCatalog() {
        // Arrange
        when(brandDao.findByName("Toyota")).thenReturn(Optional.of(TestModels.brand(BRAND_ID, "Toyota", LocalDateTime.now())));
        when(bodyTypeDao.findByName("sedan")).thenReturn(Optional.of(TestModels.bodyType(BODY_TYPE_ID, "sedan", LocalDateTime.now())));
        when(carDao.findByBrandIdAndBodyTypeId(BRAND_ID, BODY_TYPE_ID)).thenReturn(List.of(car()));

        // Exercise
        final boolean result = carService.existsDuplicateCar("Toyota", "sedan", "  Corolla  ", 2024, 0L);

        // Assertions
        assertTrue(result);
    }

    @Test
    public void shouldNotFlagIgnoredCarAsDuplicate() {
        // Arrange
        when(brandDao.findByName("Toyota")).thenReturn(Optional.of(TestModels.brand(BRAND_ID, "Toyota", LocalDateTime.now())));
        when(bodyTypeDao.findByName("sedan")).thenReturn(Optional.of(TestModels.bodyType(BODY_TYPE_ID, "sedan", LocalDateTime.now())));
        when(carDao.findByBrandIdAndBodyTypeId(BRAND_ID, BODY_TYPE_ID)).thenReturn(List.of(car()));

        // Exercise
        final boolean result = carService.existsDuplicateCar("Toyota", "sedan", "Corolla", 2024, CAR_ID);

        // Assertions
        assertFalse(result);
    }

    @Test
    public void shouldNotFlagCarWithDifferentYear() {
        // Arrange
        when(brandDao.findByName("Toyota")).thenReturn(Optional.of(TestModels.brand(BRAND_ID, "Toyota", LocalDateTime.now())));
        when(bodyTypeDao.findByName("sedan")).thenReturn(Optional.of(TestModels.bodyType(BODY_TYPE_ID, "sedan", LocalDateTime.now())));
        when(carDao.findByBrandIdAndBodyTypeId(BRAND_ID, BODY_TYPE_ID)).thenReturn(List.of(car()));

        // Exercise
        final boolean result = carService.existsDuplicateCar("Toyota", "sedan", "Corolla", 2025, 0L);

        // Assertions
        assertFalse(result);
    }

    @Test
    public void shouldRejectAppendCarImagesWithInvalidPayload() {
        // Arrange
        final List<CarImagePayload> images = List.of(new CarImagePayload("image/png", new byte[0]));

        // Exercise
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> carService.appendCarImages(CAR_ID, images));

        // Assertions
        assertEquals("Image metadata and payload must be provided together.", ex.getMessage());
    }

    @Test
    public void shouldNormalizeElectricOnlySearchByRemovingFuelConsumptionFilter() {
        // Arrange
        final ar.edu.itba.paw.model.CarSearchCriteria criteria = new ar.edu.itba.paw.model.CarSearchCriteria();
        criteria.setFuelTypes(List.of("electric"));
        criteria.setFuelConsumptionMax(new BigDecimal("15.0"));
        final ar.edu.itba.paw.model.Page<Car> expectedPage = new ar.edu.itba.paw.model.Page<>(List.of(), 1, 10, 0);
        when(carDao.findByCriteria(any())).thenReturn(expectedPage);

        // Exercise
        carService.searchCars(criteria);

        // Assertions
        assertTrue(criteria.isElectricOnly());
        assertTrue(criteria.getFuelConsumptionMax() == null);
    }
}
