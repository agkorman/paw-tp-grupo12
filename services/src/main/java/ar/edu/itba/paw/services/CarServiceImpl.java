package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.CarImage;
import ar.edu.itba.paw.model.CarImagePayload;
import ar.edu.itba.paw.persistence.BodyTypeDao;
import ar.edu.itba.paw.persistence.BrandDao;
import ar.edu.itba.paw.persistence.CarDao;
import ar.edu.itba.paw.persistence.CarImageDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class CarServiceImpl implements CarService {

    private final CarDao carDao;
    private final CarImageDao carImageDao;
    private final CarRequestService carRequestService;
    private final BrandDao brandDao;
    private final BodyTypeDao bodyTypeDao;

    @Autowired
    public CarServiceImpl(final CarDao carDao, final CarImageDao carImageDao,
                          final CarRequestService carRequestService,
                          final BrandDao brandDao, final BodyTypeDao bodyTypeDao) {
        this.carDao = carDao;
        this.carImageDao = carImageDao;
        this.carRequestService = carRequestService;
        this.brandDao = brandDao;
        this.bodyTypeDao = bodyTypeDao;
    }

    @Override
    public List<Car> getAllCars() {
        return carDao.findAll();
    }

    @Override
    public Optional<Car> getCarById(final long id) {
        return carDao.findById(id);
    }

    @Override
    public List<Car> getCarsByBodyType(final String bodyType) {
        return bodyTypeDao.findByName(bodyType)
                .map(bt -> carDao.findByBodyTypeId(bt.getId()))
                .orElse(Collections.emptyList());
    }

    @Override
    public List<Car> getCarsByBrand(final String brand) {
        return brandDao.findByName(brand)
                .map(b -> carDao.findByBrandId(b.getId()))
                .orElse(Collections.emptyList());
    }

    @Override
    public List<Car> getCarsByBrandAndBodyType(final String brand, final String bodyType) {
        return brandDao.findByName(brand)
                .flatMap(b -> bodyTypeDao.findByName(bodyType)
                        .map(bt -> carDao.findByBrandIdAndBodyTypeId(b.getId(), bt.getId())))
                .orElse(Collections.emptyList());
    }

    @Override
    public List<Car> searchCars(final String query, final String brand, final String bodyType) {
        final Long brandId = brand != null
                ? brandDao.findByName(brand).map(br -> br.getId()).orElse(null)
                : null;
        if (brand != null && brandId == null) {
            return Collections.emptyList();
        }

        final Long bodyTypeId = bodyType != null
                ? bodyTypeDao.findByName(bodyType).map(bt -> bt.getId()).orElse(null)
                : null;
        if (bodyType != null && bodyTypeId == null) {
            return Collections.emptyList();
        }

        if (query == null || query.trim().isEmpty()) {
            if (brandId != null && bodyTypeId != null) return carDao.findByBrandIdAndBodyTypeId(brandId, bodyTypeId);
            if (brandId != null)                        return carDao.findByBrandId(brandId);
            if (bodyTypeId != null)                     return carDao.findByBodyTypeId(bodyTypeId);
            return carDao.findAll();
        }

        return carDao.search(query, brandId, bodyTypeId);
    }

    @Override
    public Optional<CarImage> getCarImageByCarId(final long carId) {
        return carImageDao.findByCarId(carId);
    }

    @Override
    public List<CarImage> getCarImagesByCarId(final long carId) {
        return carImageDao.findAllByCarId(carId);
    }

    @Override
    public Optional<CarImage> getCarImageById(final long carId, final long imageId) {
        return carImageDao.findByCarIdAndImageId(carId, imageId);
    }

    @Override
    @Transactional
    public void saveCarImage(final long carId, final String contentType, final byte[] imageData) {
        carImageDao.saveOrReplace(carId, contentType, imageData);
    }

    @Override
    @Transactional
    public void saveCarImages(final long carId, final List<CarImagePayload> images) {
        carImageDao.replaceAll(carId, normalizeImages(images));
    }

    @Override
    @Transactional
    public Car createCar(final long brandId, final String model, final long bodyTypeId,
                         final long submittedByUserId,
                         final Optional<String> description, final Optional<String> imageContentType,
                         final Optional<byte[]> imageData) {
        final List<CarImagePayload> images;
        final boolean hasImageContentType = imageContentType.isPresent();
        final boolean hasImageData = imageData.isPresent();
        if (hasImageContentType != hasImageData) {
            throw new IllegalArgumentException("Image metadata and payload must be provided together.");
        }
        if (hasImageContentType) {
            images = List.of(new CarImagePayload(imageContentType.orElseThrow(), imageData.orElseThrow()));
        } else {
            images = Collections.emptyList();
        }
        return createCar(brandId, model, bodyTypeId, submittedByUserId, description, images);
    }

    @Override
    @Transactional
    public Car createCar(final long brandId, final String model, final long bodyTypeId,
                         final long submittedByUserId,
                         final Optional<String> description, final List<CarImagePayload> images) {
        final String normalizedDescription = description
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .orElseThrow(() -> new IllegalArgumentException("Description is required for car creation."));
        final List<CarImagePayload> normalizedImages = normalizeImages(images);
        if (normalizedImages.isEmpty()) {
            throw new IllegalArgumentException("At least one image is required for car creation.");
        }

        try {
            final Car createdCar = carDao.create(brandId, model, bodyTypeId, normalizedDescription);
            carImageDao.replaceAll(createdCar.getId(), normalizedImages);

            carRequestService.createPendingRequest(
                    submittedByUserId,
                    brandId,
                    bodyTypeId,
                    model,
                    normalizedDescription,
                    normalizedImages
            );

            return carDao.findById(createdCar.getId()).orElse(createdCar);
        } catch (final RuntimeException e) {
            throw new IllegalStateException("Failed to create car with image gallery.", e);
        }
    }

    private List<CarImagePayload> normalizeImages(final List<CarImagePayload> images) {
        if (images == null) {
            return Collections.emptyList();
        }
        for (final CarImagePayload image : images) {
            if (image == null || image.getContentType() == null || image.getContentType().isBlank()
                    || image.getImageData() == null || image.getImageData().length == 0) {
                throw new IllegalArgumentException("Image metadata and payload must be provided together.");
            }
        }
        return List.copyOf(images);
    }
}
