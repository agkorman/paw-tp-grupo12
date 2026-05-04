package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.CarSearchCriteria;
import ar.edu.itba.paw.model.Page;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CarDao {
    List<Car> findAll();

    Optional<Car> findById(long id);

    List<Car> findByIds(Collection<Long> ids);

    List<Car> findByBrandIdAndBodyTypeId(long brandId, long bodyTypeId);

    Page<Car> findByCriteria(CarSearchCriteria criteria);

    Car insertAndFetch(long brandId, String model, long bodyTypeId, Integer year, String description,
               String fuelType, Integer horsepower, Integer airbagCount,
               String transmission, BigDecimal fuelConsumption, Integer maxSpeedKmh, BigDecimal priceUsd);

    Optional<Car> update(long id, long brandId, String model, long bodyTypeId, Integer year, String description,
                         String fuelType, Integer horsepower, Integer airbagCount,
                         String transmission, BigDecimal fuelConsumption, Integer maxSpeedKmh, BigDecimal priceUsd);

    boolean delete(long id);

    long countByBrandId(long brandId);

    long countByBodyTypeId(long bodyTypeId);

    List<Car> findTopRated(int limit);

    List<Car> findRecentlyAdded(int limit, Collection<Long> excludedIds);
}
