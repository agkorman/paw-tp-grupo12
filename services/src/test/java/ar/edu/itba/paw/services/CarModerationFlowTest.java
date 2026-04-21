package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.BodyType;
import ar.edu.itba.paw.model.Brand;
import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.CarImage;
import ar.edu.itba.paw.model.CarRequest;
import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.model.ReviewStats;
import ar.edu.itba.paw.persistence.BodyTypeDao;
import ar.edu.itba.paw.persistence.BrandDao;
import ar.edu.itba.paw.persistence.CarDao;
import ar.edu.itba.paw.persistence.CarImageDao;
import ar.edu.itba.paw.persistence.CarRequestDao;
import ar.edu.itba.paw.persistence.ReviewDao;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class CarModerationFlowTest {

    @Test
    void userCarSubmissionOnlyCreatesPendingRequest() {
        final FakeCarDao carDao = new FakeCarDao();
        final FakeCarImageDao carImageDao = new FakeCarImageDao();
        final FakeCarRequestService carRequestService = new FakeCarRequestService();
        final CarService carService = new CarServiceImpl(
                carDao,
                carImageDao,
                new FakeReviewDao(),
                carRequestService,
                new FakeBrandDao(),
                new FakeBodyTypeDao()
        );

        final CarRequest request = carService.requestCarCreation(
                1L,
                "Supra",
                2L,
                7L,
                Optional.of("Desc"),
                Optional.of("image/png"),
                Optional.of(new byte[] {1, 2, 3})
        );

        assertEquals(CarRequestService.STATUS_PENDING, request.getStatus());
        assertFalse(carDao.created);
        assertFalse(carImageDao.saved);
        assertTrue(carRequestService.created);
    }

    @Test
    void approvingPendingRequestPublishesCarAndImage() {
        final FakeCarRequestDao carRequestDao = new FakeCarRequestDao();
        final FakeCarDao carDao = new FakeCarDao();
        final FakeCarImageDao carImageDao = new FakeCarImageDao();
        final CarRequestService carRequestService = new CarRequestServiceImpl(
                carRequestDao,
                carDao,
                carImageDao
        );
        final byte[] imageData = new byte[] {4, 5, 6};
        final CarRequest request = carRequestDao.create(
                7L,
                1L,
                2L,
                "Supra",
                "Desc",
                "image/png",
                imageData,
                CarRequestService.STATUS_PENDING
        );

        final boolean approved = carRequestService.approvePendingRequest(
                request.getId(),
                9L,
                "GR86",
                8L,
                "Edited desc",
                Optional.empty(),
                Optional.empty()
        );

        assertTrue(approved);
        assertEquals(CarRequestService.STATUS_APPROVED, carRequestDao.request.getStatus());
        assertTrue(carDao.created);
        assertEquals(9L, carDao.createdBrandId);
        assertEquals("GR86", carDao.createdModel);
        assertEquals(8L, carDao.createdBodyTypeId);
        assertEquals("Edited desc", carDao.createdDescription);
        assertTrue(carImageDao.saved);
        assertEquals(100L, carImageDao.savedCarId);
        assertEquals("image/png", carImageDao.savedContentType);
        assertArrayEquals(imageData, carImageDao.savedImageData);
    }

    @Test
    void deletingCarRemovesReviewsBeforeCar() {
        final FakeCarRequestDao carRequestDao = new FakeCarRequestDao();
        final FakeCarDao carDao = new FakeCarDao();
        carDao.existingCar = new Car(20L, 1L, "Toyota", "Supra", 2L, "Coupe", "Desc", LocalDateTime.now());
        final FakeReviewDao reviewDao = new FakeReviewDao();
        final CarService carService = new CarServiceImpl(
                carDao,
                new FakeCarImageDao(),
                reviewDao,
                new CarRequestServiceImpl(carRequestDao, carDao, new FakeCarImageDao()),
                new FakeBrandDao(),
                new FakeBodyTypeDao()
        );

        final boolean deleted = carService.deleteCar(20L);

        assertTrue(deleted);
        assertTrue(reviewDao.deletedByCarId);
        assertTrue(carDao.deleted);
        assertEquals(20L, reviewDao.deletedCarId);
        assertEquals(20L, carDao.deletedCarId);
    }

    @Test
    void updatingCarPersistsEditedValuesAndReplacementImage() {
        final FakeCarDao carDao = new FakeCarDao();
        carDao.existingCar = new Car(20L, 1L, "Toyota", "Supra", 2L, "Coupe", "Desc", LocalDateTime.now());
        final FakeCarImageDao carImageDao = new FakeCarImageDao();
        final CarService carService = new CarServiceImpl(
                carDao,
                carImageDao,
                new FakeReviewDao(),
                new FakeCarRequestService(),
                new FakeBrandDao(),
                new FakeBodyTypeDao()
        );
        final byte[] imageData = new byte[] {9, 8, 7};

        final Optional<Car> updated = carService.updateCar(
                20L,
                3L,
                "  GR86  ",
                4L,
                "  Edited desc  ",
                Optional.of("image/webp"),
                Optional.of(imageData)
        );

        assertTrue(updated.isPresent());
        assertEquals(3L, carDao.existingCar.getBrandId());
        assertEquals("GR86", carDao.existingCar.getModel());
        assertEquals(4L, carDao.existingCar.getBodyTypeId());
        assertEquals("Edited desc", carDao.existingCar.getDescription());
        assertTrue(carImageDao.saved);
        assertEquals(20L, carImageDao.savedCarId);
        assertEquals("image/webp", carImageDao.savedContentType);
        assertArrayEquals(imageData, carImageDao.savedImageData);
    }

    private static final class FakeCarRequestService implements CarRequestService {
        private boolean created;

        @Override
        public Optional<CarRequest> getCarRequestById(final long id) {
            return Optional.empty();
        }

        @Override
        public List<CarRequest> getAllCarRequests() {
            return Collections.emptyList();
        }

        @Override
        public List<CarRequest> getCarRequestsByStatus(final String status) {
            return Collections.emptyList();
        }

        @Override
        public CarRequest createPendingRequest(final long submittedByUserId, final long brandId,
                                               final long bodyTypeId, final String model,
                                               final String description,
                                               final Optional<String> imageContentType,
                                               final Optional<byte[]> imageData) {
            created = true;
            return new CarRequest(
                    10L,
                    submittedByUserId,
                    null,
                    brandId,
                    bodyTypeId,
                    model,
                    description,
                    imageContentType.orElse(null),
                    imageData.orElse(null),
                    STATUS_PENDING,
                    LocalDateTime.now()
            );
        }

        @Override
        public boolean approvePendingRequest(final long id) {
            return false;
        }

        @Override
        public boolean approvePendingRequest(final long id, final long brandId, final String model,
                                             final long bodyTypeId, final String description,
                                             final Optional<String> imageContentType,
                                             final Optional<byte[]> imageData) {
            return false;
        }

        @Override
        public boolean rejectPendingRequest(final long id) {
            return false;
        }
    }

    private static final class FakeCarRequestDao implements CarRequestDao {
        private CarRequest request;

        @Override
        public Optional<CarRequest> findById(final long id) {
            return request != null && request.getId() == id ? Optional.of(request) : Optional.empty();
        }

        @Override
        public List<CarRequest> findAll() {
            return request == null ? Collections.emptyList() : List.of(request);
        }

        @Override
        public List<CarRequest> findByStatus(final String status) {
            return request != null && status.equals(request.getStatus())
                    ? List.of(request)
                    : Collections.emptyList();
        }

        @Override
        public CarRequest create(final long submittedByUserId, final long brandId, final long bodyTypeId,
                                 final String model, final String description, final String imageContentType,
                                 final byte[] imageData, final String status) {
            request = new CarRequest(
                    20L,
                    submittedByUserId,
                    null,
                    brandId,
                    bodyTypeId,
                    model,
                    description,
                    imageContentType,
                    imageData,
                    status,
                    LocalDateTime.now()
            );
            return request;
        }

        @Override
        public boolean updateStatus(final long id, final String expectedStatus, final String newStatus) {
            if (request == null || request.getId() != id || !expectedStatus.equals(request.getStatus())) {
                return false;
            }
            request.setStatus(newStatus);
            return true;
        }

        @Override
        public int bindRequestsToUserByEmail(final long userId, final String email) {
            return 0;
        }
    }

    private static final class FakeCarDao implements CarDao {
        private boolean created;
        private boolean deleted;
        private long deletedCarId;
        private long createdBrandId;
        private String createdModel;
        private long createdBodyTypeId;
        private String createdDescription;
        private Car existingCar;

        @Override
        public List<Car> findAll() {
            return Collections.emptyList();
        }

        @Override
        public Optional<Car> findById(final long id) {
            return existingCar != null && existingCar.getId() == id ? Optional.of(existingCar) : Optional.empty();
        }

        @Override
        public List<Car> findByIds(final Collection<Long> ids) {
            return Collections.emptyList();
        }

        @Override
        public List<Car> findByBrandId(final long brandId) {
            return Collections.emptyList();
        }

        @Override
        public List<Car> findByBodyTypeId(final long bodyTypeId) {
            return Collections.emptyList();
        }

        @Override
        public List<Car> findByBrandIdAndBodyTypeId(final long brandId, final long bodyTypeId) {
            return Collections.emptyList();
        }

        @Override
        public List<Car> search(final String query, final Long brandId, final Long bodyTypeId) {
            return Collections.emptyList();
        }

        @Override
        public Car create(final long brandId, final String model, final long bodyTypeId, final String description) {
            created = true;
            createdBrandId = brandId;
            createdModel = model;
            createdBodyTypeId = bodyTypeId;
            createdDescription = description;
            return new Car(100L, brandId, "Toyota", model, bodyTypeId, "Coupe", description, LocalDateTime.now());
        }

        @Override
        public Optional<Car> update(final long id, final long brandId, final String model, final long bodyTypeId,
                                    final String description) {
            existingCar = new Car(id, brandId, "Toyota", model, bodyTypeId, "Coupe", description, LocalDateTime.now());
            return Optional.of(existingCar);
        }

        @Override
        public boolean delete(final long id) {
            deleted = true;
            deletedCarId = id;
            existingCar = null;
            return true;
        }
    }

    private static final class FakeReviewDao implements ReviewDao {
        private boolean deletedByCarId;
        private long deletedCarId;

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
        public int bindReviewsToUserByEmail(final long userId, final String email) {
            return 0;
        }

        @Override
        public Optional<Review> update(final long id, final long carId, final BigDecimal rating,
                                       final String title, final String body, final String ownershipStatus,
                                       final Integer modelYear, final Integer mileageKm,
                                       final Boolean wouldRecommend) {
            return Optional.empty();
        }

        @Override
        public boolean delete(final long id) {
            return false;
        }

        @Override
        public int deleteByCarId(final long carId) {
            deletedByCarId = true;
            deletedCarId = carId;
            return 1;
        }
    }

    private static final class FakeCarImageDao implements CarImageDao {
        private boolean saved;
        private long savedCarId;
        private String savedContentType;
        private byte[] savedImageData;

        @Override
        public Optional<CarImage> findByCarId(final long carId) {
            return Optional.empty();
        }

        @Override
        public void saveOrReplace(final long carId, final String contentType, final byte[] imageData) {
            saved = true;
            savedCarId = carId;
            savedContentType = contentType;
            savedImageData = imageData;
        }
    }

    private static final class FakeBrandDao implements BrandDao {
        @Override
        public List<Brand> findAll() {
            return Collections.emptyList();
        }

        @Override
        public Optional<Brand> findById(final long id) {
            return Optional.empty();
        }

        @Override
        public Optional<Brand> findByName(final String name) {
            return Optional.empty();
        }

        @Override
        public Brand create(final String name) {
            throw new UnsupportedOperationException();
        }
    }

    private static final class FakeBodyTypeDao implements BodyTypeDao {
        @Override
        public List<BodyType> findAll() {
            return Collections.emptyList();
        }

        @Override
        public Optional<BodyType> findById(final long id) {
            return Optional.empty();
        }

        @Override
        public Optional<BodyType> findByName(final String name) {
            return Optional.empty();
        }

        @Override
        public BodyType create(final String name) {
            throw new UnsupportedOperationException();
        }
    }
}
