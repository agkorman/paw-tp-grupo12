package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.CarImage;
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
    public List<Car> searchCars(final String query) {
        if (query == null || query.trim().isEmpty()) {
            return getAllCars();
        }
        return carDao.search(query);
    }

    @Override
    public Optional<CarImage> getCarImageByCarId(final long carId) {
        return carImageDao.findByCarId(carId);
    }

    @Override
    public void saveCarImage(final long carId, final String contentType, final byte[] imageData) {
        carImageDao.saveOrReplace(carId, contentType, imageData);
    }

    @Override
    @Transactional
    public Car createCar(final long brandId, final String model, final long bodyTypeId,
                         final String submitterEmail,
                         final Optional<String> description, final Optional<String> imageContentType,
                         final Optional<byte[]> imageData) {
        final String normalizedDescription = description
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .orElseThrow(() -> new IllegalArgumentException("Description is required for car creation."));

        final Car createdCar = carDao.create(
                brandId,
                model,
                bodyTypeId,
                normalizedDescription,
                null
        );

        final boolean hasImageContentType = imageContentType.isPresent();
        final boolean hasImageData = imageData.isPresent();
        if (hasImageContentType != hasImageData) {
            throw new IllegalArgumentException("Image metadata and payload must be provided together.");
        }
        if (hasImageContentType) {
            carImageDao.saveOrReplace(createdCar.getId(), imageContentType.orElseThrow(), imageData.orElseThrow());
        }

        carRequestService.createPendingRequest(
                null,
                submitterEmail,
                brandId,
                bodyTypeId,
                model,
                normalizedDescription,
                imageContentType,
                imageData
        );

        return carDao.findById(createdCar.getId()).orElse(createdCar);
    }
}
