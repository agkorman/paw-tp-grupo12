package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.CarImage;
import ar.edu.itba.paw.persistence.BodyTypeDao;
import ar.edu.itba.paw.persistence.BrandDao;
import ar.edu.itba.paw.persistence.CarDao;
import ar.edu.itba.paw.persistence.CarImageDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class CarServiceImpl implements CarService {

    private final CarDao carDao;
    private final CarImageDao carImageDao;
    private final BrandDao brandDao;
    private final BodyTypeDao bodyTypeDao;

    @Autowired
    public CarServiceImpl(final CarDao carDao, final CarImageDao carImageDao,
                          final BrandDao brandDao, final BodyTypeDao bodyTypeDao) {
        this.carDao = carDao;
        this.carImageDao = carImageDao;
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
    public Optional<CarImage> getCarImageByCarId(final long carId) {
        return carImageDao.findByCarId(carId);
    }

    @Override
    public void saveCarImage(final long carId, final String contentType, final byte[] imageData) {
        carImageDao.saveOrReplace(carId, contentType, imageData);
    }
}
