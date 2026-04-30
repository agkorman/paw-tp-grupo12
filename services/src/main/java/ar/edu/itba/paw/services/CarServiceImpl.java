package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.CarImage;
import ar.edu.itba.paw.model.CarImagePayload;
import ar.edu.itba.paw.model.CarSearchCriteria;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.persistence.BodyTypeDao;
import ar.edu.itba.paw.persistence.BrandDao;
import ar.edu.itba.paw.persistence.CarDao;
import ar.edu.itba.paw.persistence.CarImageDao;
import ar.edu.itba.paw.persistence.ReviewDao;
import ar.edu.itba.paw.services.utils.ImagePayloadUtils;
import ar.edu.itba.paw.services.utils.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class CarServiceImpl implements CarService {

    private final CarDao carDao;
    private final CarImageDao carImageDao;
    private final ReviewDao reviewDao;
    private final BrandDao brandDao;
    private final BodyTypeDao bodyTypeDao;

    @Autowired
    public CarServiceImpl(final CarDao carDao, final CarImageDao carImageDao,
                          final ReviewDao reviewDao,
                          final BrandDao brandDao, final BodyTypeDao bodyTypeDao) {
        this.carDao = carDao;
        this.carImageDao = carImageDao;
        this.reviewDao = reviewDao;
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
    public List<Car> getCarsByIds(final Collection<Long> ids) {
        return carDao.findByIds(ids);
    }

    @Override
    public List<Car> getCarsByBrandAndBodyType(final String brand, final String bodyType) {
        return brandDao.findByName(brand)
                .flatMap(b -> bodyTypeDao.findByName(bodyType)
                        .map(bt -> carDao.findByBrandIdAndBodyTypeId(b.getId(), bt.getId())))
                .orElse(Collections.emptyList());
    }

    @Override
    public Page<Car> searchCars(final CarSearchCriteria criteria) {
        return carDao.findByCriteria(criteria);
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
    public void saveCarImages(final long carId, final List<CarImagePayload> images) {
        carImageDao.replaceAll(carId, ImagePayloadUtils.normalizeImages(images));
    }

    @Override
    @Transactional
    public Optional<Car> updateCar(final long id, final long brandId, final String model,
                                   final long bodyTypeId, final Integer year, final String description,
                                   final Optional<String> imageContentType,
                                   final Optional<byte[]> imageData,
                                   final String fuelType, final Integer horsepower,
                                   final Integer airbagCount, final String transmission,
                                   final BigDecimal fuelConsumption, final Integer maxSpeedKmh,
                                   final BigDecimal priceUsd) {
        final String normalizedModel = StringUtils.normalizeRequired(model, "Model is required for car update.");
        final String normalizedDescription = StringUtils.normalizeRequired(description, "Description is required for car update.");
        validateImagePair(imageContentType, imageData);

        final Optional<Car> updated = carDao.update(id, brandId, normalizedModel, bodyTypeId, year, normalizedDescription,
                fuelType, horsepower, airbagCount, transmission, fuelConsumption, maxSpeedKmh, priceUsd);
        if (updated.isPresent() && imageContentType.isPresent()) {
            carImageDao.saveOrReplace(id, imageContentType.get(), imageData.orElseThrow());
        }
        return updated;
    }

    @Override
    @Transactional
    public boolean deleteCar(final long id) {
        if (carDao.findById(id).isEmpty()) {
            return false;
        }
        reviewDao.deleteByCarId(id);
        return carDao.delete(id);
    }

    private void validateImagePair(final Optional<String> imageContentType, final Optional<byte[]> imageData) {
        final boolean hasImageContentType = imageContentType.isPresent();
        final boolean hasImageData = imageData.isPresent();
        if (hasImageContentType != hasImageData) {
            throw new IllegalArgumentException("Image metadata and payload must be provided together.");
        }
    }
}
