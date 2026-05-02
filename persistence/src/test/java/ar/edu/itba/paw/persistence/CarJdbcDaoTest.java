package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.CarSearchCriteria;
import ar.edu.itba.paw.model.Page;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CarJdbcDaoTest extends AbstractPersistenceTest {

    @Test
    public void shouldCreateAndFindCarWithJoinedBrandAndBodyType() {
        // Arrange
        final long brandId = brandDao.create("BMW").getId();
        final long bodyTypeId = bodyTypeDao.create("Sedan").getId();

        // Exercise
        final Car result = carDao.create(brandId, "M3", bodyTypeId, 2026, "Sport sedan",
                "combustion", 473, 8, "manual", new BigDecimal("9.9"), 290, new BigDecimal("76900.00"));

        // Assertions
        final Car persisted = carDao.findById(result.getId()).orElseThrow();
        assertEquals("BMW", persisted.getBrandName());
        assertEquals("Sedan", persisted.getBodyType());
        assertEquals("M3", persisted.getModel());
        assertEquals(473, persisted.getHorsepower());
    }

    @Test
    public void shouldFilterCarsByCriteriaAndExcludeNonMatchingCars() {
        // Arrange
        final Car matching = createCar("criteria-match");
        createCar("criteria-miss");
        final CarSearchCriteria criteria = new CarSearchCriteria();
        criteria.setBrand(matching.getBrandName());
        criteria.setFuelTypes(List.of("combustion"));
        criteria.setHorsepowerMin(150);
        criteria.setSortBy("name_asc");

        // Exercise
        final Page<Car> result = carDao.findByCriteria(criteria);

        // Assertions
        assertEquals(1, result.getTotalItems());
        assertEquals(matching.getId(), result.getItems().get(0).getId());
        assertEquals("combustion", result.getItems().get(0).getFuelType());
    }

    @Test
    public void shouldUpdatePersistedCarSpecsWhenCarExists() {
        // Arrange
        final Car created = createCar("update");

        // Exercise
        final Optional<Car> result = carDao.update(created.getId(), created.getBrandId(), "Updated Model",
                created.getBodyTypeId(), 2025, "Updated description", "hybrid", 350, 10, "automatic",
                new BigDecimal("6.7"), 250, new BigDecimal("52000.00"));

        // Assertions
        assertTrue(result.isPresent());
        final Car persisted = carDao.findById(created.getId()).orElseThrow();
        assertEquals("Updated Model", persisted.getModel());
        assertEquals("hybrid", persisted.getFuelType());
        assertEquals(new BigDecimal("52000.00"), persisted.getPriceUsd());
    }

    @Test
    public void shouldDeleteCarAndRemoveItFromCountsWhenCarExists() {
        // Arrange
        final Car created = createCar("delete");

        // Exercise
        final boolean result = carDao.delete(created.getId());

        // Assertions
        assertTrue(result);
        assertFalse(carDao.findById(created.getId()).isPresent());
        assertEquals(0, carDao.countByBrandId(created.getBrandId()));
        assertEquals(0, carDao.countByBodyTypeId(created.getBodyTypeId()));
    }

    @Test
    public void shouldFindOnlyRequestedCarsByIds() {
        // Arrange
        final Car first = createCar("ids-first");
        final Car second = createCar("ids-second");
        createCar("ids-third");

        // Exercise
        final List<Car> result = carDao.findByIds(List.of(second.getId(), first.getId(), first.getId()));

        // Assertions
        assertEquals(2, result.size());
        assertEquals(first.getId(), result.get(0).getId());
        assertEquals(second.getId(), result.get(1).getId());
    }
}
