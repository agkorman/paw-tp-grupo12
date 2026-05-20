package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.CarSearchCriteria;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Pagination;
import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CarDaoTest extends AbstractPersistenceTest {

    @Test
    public void shouldCreateAndFindCarWithJoinedBrandAndBodyType() {
        // Arrange
        final long brandId = insertBrand("BMW").getId();
        final long bodyTypeId = insertBodyType("Sedan").getId();

        // Exercise
        final Car result = carDao.create(brandId, "M3", bodyTypeId, 2026, "Sport sedan",
                "combustion", 473, 8, "manual", new BigDecimal("9.9"), 290, new BigDecimal("76900.00"));

        // Assertions
        assertEquals(1, countRows("SELECT COUNT(*) FROM cars WHERE car_id = ?", result.getId()));
        assertEquals("M3", jdbcTemplate.queryForObject(
                "SELECT model FROM cars WHERE car_id = ?", String.class, result.getId()
        ));
        assertEquals(473, jdbcTemplate.queryForObject(
                "SELECT horsepower FROM cars WHERE car_id = ?", Integer.class, result.getId()
        ));
        assertEquals(new BigDecimal("76900.00"), jdbcTemplate.queryForObject(
                "SELECT price_usd FROM cars WHERE car_id = ?", BigDecimal.class, result.getId()
        ));
        assertEquals("BMW", jdbcTemplate.queryForObject(
                "SELECT b.name FROM cars c JOIN brands b ON c.brand_id = b.brand_id WHERE c.car_id = ?",
                String.class, result.getId()
        ));
        assertEquals("Sedan", jdbcTemplate.queryForObject(
                "SELECT bt.name FROM cars c JOIN body_types bt ON c.body_type_id = bt.body_type_id WHERE c.car_id = ?",
                String.class, result.getId()
        ));
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
    public void shouldFilterCarsByAdvancedCriteriaCombination() {
        // Arrange
        final long brandId = insertBrand("Advanced Brand").getId();
        final long bodyTypeId = insertBodyType("Advanced Body").getId();
        final Car matching = insertCar(brandId, "Advanced Brand", "Advanced Match", bodyTypeId, "Advanced Body",
                2025, "Small city car", "hybrid", 220, 8, "automatic", new BigDecimal("5.5"),
                240, new BigDecimal("42000.00"));
        insertCar(brandId, "Advanced Brand", "Advanced Miss", bodyTypeId, "Advanced Body",
                2021, "Fast auto", "hybrid", 120, 4, "manual", new BigDecimal("9.5"),
                190, new BigDecimal("90000.00"));
        final CarSearchCriteria criteria = new CarSearchCriteria();
        // Search is intentionally naive here because HSQLDB does not support PostgreSQL full-text search.
        criteria.setQ("y");
        criteria.setBrand("Advanced Brand");
        criteria.setBodyType("Advanced Body");
        criteria.setFuelTypes(List.of("hybrid"));
        criteria.setYearMin(2024);
        criteria.setYearMax(2026);
        criteria.setHorsepowerMin(200);
        criteria.setHorsepowerMax(300);
        criteria.setAirbagMin(6);
        criteria.setTransmission("automatic");
        criteria.setFuelConsumptionMax(new BigDecimal("6.0"));
        criteria.setMaxSpeedMin(220);
        criteria.setPriceMin(new BigDecimal("40000.00"));
        criteria.setPriceMax(new BigDecimal("50000.00"));
        criteria.setSortBy("price_asc");

        // Exercise
        final Page<Car> result = carDao.findByCriteria(criteria);

        // Assertions
        assertEquals(1, result.getTotalItems());
        assertEquals(matching.getId(), result.getItems().get(0).getId());
        assertEquals("hybrid", result.getItems().get(0).getFuelType());
        assertEquals("automatic", result.getItems().get(0).getTransmission());
    }

    @Test
    public void shouldPaginateAndClampCarsByCriteria() {
        // Arrange
        final long brandId = insertBrand("Paged Brand").getId();
        final long bodyTypeId = insertBodyType("Paged Body").getId();
        for (int i = 0; i < Pagination.CARS_PAGE_SIZE + 1; i++) {
            insertCar(brandId, "Paged Brand", String.format("Paged Model %02d", i), bodyTypeId, "Paged Body",
                    2026, "Paged description " + i, "combustion", 150 + i, 6, "automatic",
                    new BigDecimal("8.0"), 200 + i, new BigDecimal("30000.00"));
        }
        final CarSearchCriteria criteria = new CarSearchCriteria();
        criteria.setBrand("Paged Brand");
        criteria.setSortBy("name_asc");
        criteria.setPage(999);

        // Exercise
        final Page<Car> result = carDao.findByCriteria(criteria);

        // Assertions
        assertEquals(Pagination.CARS_PAGE_SIZE + 1L, result.getTotalItems());
        assertEquals(2, result.getPageNumber());
        assertEquals(1, result.getItems().size());
        assertEquals("Paged Model 16", result.getItems().get(0).getModel());
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
        assertEquals("Updated Model", result.get().getModel());
        flushAndClear();
        assertEquals("Updated Model", jdbcTemplate.queryForObject(
                "SELECT model FROM cars WHERE car_id = ?", String.class, created.getId()
        ));
        assertEquals("hybrid", jdbcTemplate.queryForObject(
                "SELECT fuel_type FROM cars WHERE car_id = ?", String.class, created.getId()
        ));
        assertEquals(new BigDecimal("52000.00"), jdbcTemplate.queryForObject(
                "SELECT price_usd FROM cars WHERE car_id = ?", BigDecimal.class, created.getId()
        ));
    }

    @Test
    public void shouldDeleteCarAndRemoveItFromCountsWhenCarExists() {
        // Arrange
        final Car created = createCar("delete");
        final User reviewer = createUser("delete-car-review");
        final Review review = insertReview(reviewer.getId(), reviewer.getUsername(), created.getId(),
                new BigDecimal("4.0"), "Delete car review", "Body delete car review", "owner", 2026, 1000, true);

        // Exercise
        final boolean result = carDao.delete(created.getId());
        flushAndClear();

        // Assertions
        assertTrue(result);
        assertEquals(0, countRows("SELECT COUNT(*) FROM cars WHERE car_id = ?", created.getId()));
        assertEquals(0, countRows("SELECT COUNT(*) FROM reviews WHERE review_id = ?", review.getId()));
        assertEquals(0, countRows("SELECT COUNT(*) FROM cars WHERE brand_id = ?", created.getBrandId()));
        assertEquals(0, countRows("SELECT COUNT(*) FROM cars WHERE body_type_id = ?", created.getBodyTypeId()));
        assertEquals(1, countRows("SELECT COUNT(*) FROM brands WHERE brand_id = ?", created.getBrandId()));
        assertEquals(1, countRows("SELECT COUNT(*) FROM body_types WHERE body_type_id = ?", created.getBodyTypeId()));
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

    @Test
    public void shouldReturnEmptyPageWhenCriteriaHasNoMatches() {
        // Arrange
        createCar("no-match");
        final CarSearchCriteria criteria = new CarSearchCriteria();
        criteria.setBrand("Missing Brand");

        // Exercise
        final Page<Car> result = carDao.findByCriteria(criteria);

        // Assertions
        assertEquals(0L, result.getTotalItems());
        assertTrue(result.getItems().isEmpty());
        assertEquals(Pagination.DEFAULT_PAGE, result.getPageNumber());
    }

    @Test
    public void shouldSortCarsByPriceDescending() {
        // Arrange
        final long brandId = insertBrand("Sort Price Brand").getId();
        final long bodyTypeId = insertBodyType("Sort Price Body").getId();
        final Car cheaper = insertCar(brandId, "Sort Price Brand", "Cheaper", bodyTypeId, "Sort Price Body",
                2026, "Cheaper car", "combustion", 150, 6, "automatic", new BigDecimal("8.0"),
                200, new BigDecimal("20000.00"));
        final Car expensive = insertCar(brandId, "Sort Price Brand", "Expensive", bodyTypeId, "Sort Price Body",
                2026, "Expensive car", "combustion", 150, 6, "automatic", new BigDecimal("8.0"),
                200, new BigDecimal("50000.00"));
        final CarSearchCriteria criteria = new CarSearchCriteria();
        criteria.setBrand("Sort Price Brand");
        criteria.setSortBy("price_desc");

        // Exercise
        final Page<Car> result = carDao.findByCriteria(criteria);

        // Assertions
        assertEquals(2L, result.getTotalItems());
        assertEquals(expensive.getId(), result.getItems().get(0).getId());
        assertEquals(cheaper.getId(), result.getItems().get(1).getId());
    }

    @Test
    public void shouldReturnEmptyWhenFindByIdHasNoMatch() {
        // Arrange
        final long missingId = 9999L;

        // Exercise
        final Optional<Car> result = carDao.findById(missingId);

        // Assertions
        assertFalse(result.isPresent());
    }

    @Test
    public void shouldReturnEmptyWhenUpdatingMissingCar() {
        // Arrange
        final long missingId = 9999L;

        // Exercise
        final Optional<Car> result = carDao.update(missingId, 1L, "Ghost Model", 1L, 2026,
                "Description", "combustion", 200, 6, "automatic",
                new BigDecimal("8.0"), 220, new BigDecimal("30000.00"));

        // Assertions
        assertFalse(result.isPresent());
        assertEquals(0, countRows("SELECT COUNT(*) FROM cars WHERE car_id = ?", missingId));
    }

    @Test
    public void shouldReturnFalseWhenDeletingMissingCar() {
        // Arrange
        final long missingId = 9999L;

        // Exercise
        final boolean result = carDao.delete(missingId);

        // Assertions
        assertFalse(result);
        assertEquals(0, countRows("SELECT COUNT(*) FROM cars WHERE car_id = ?", missingId));
    }

    @Test
    public void shouldReturnEmptyListWhenFindByIdsReceivesEmptyCollection() {
        // Arrange
        final List<Long> emptyIds = Collections.emptyList();

        // Exercise
        final List<Car> result = carDao.findByIds(emptyIds);

        // Assertions
        assertTrue(result.isEmpty());
    }

    @Test
    public void shouldRejectDuplicateCarBrandModelBodyTypeYear() {
        // Arrange
        final Car existing = createCar("dup-car");

        // Exercise
        assertThrows(DataIntegrityViolationException.class, () ->
                carDao.create(existing.getBrandId(), existing.getModel(), existing.getBodyTypeId(),
                        existing.getYear(), "Another description", "combustion", 150, 6, "manual",
                        new BigDecimal("7.0"), 200, new BigDecimal("25000.00"))
        );

        // Assertions
        assertEquals(1, countRows("SELECT COUNT(*) FROM cars WHERE brand_id = ? AND model = ? AND body_type_id = ? AND year = ?",
                existing.getBrandId(), existing.getModel(), existing.getBodyTypeId(), existing.getYear()));
    }

    @Test
    public void shouldSortCarsByHorsepowerAscending() {
        // Arrange
        final long brandId = insertBrand("Sort Hp Brand").getId();
        final long bodyTypeId = insertBodyType("Sort Hp Body").getId();
        final Car weak = insertCar(brandId, "Sort Hp Brand", "Weak", bodyTypeId, "Sort Hp Body",
                2026, "Weak car", "combustion", 120, 6, "automatic", new BigDecimal("8.0"),
                200, new BigDecimal("30000.00"));
        final Car strong = insertCar(brandId, "Sort Hp Brand", "Strong", bodyTypeId, "Sort Hp Body",
                2026, "Strong car", "combustion", 400, 6, "automatic", new BigDecimal("8.0"),
                250, new BigDecimal("30000.00"));
        final CarSearchCriteria criteria = new CarSearchCriteria();
        criteria.setBrand("Sort Hp Brand");
        criteria.setSortBy("hp_asc");

        // Exercise
        final Page<Car> result = carDao.findByCriteria(criteria);

        // Assertions
        assertEquals(2L, result.getTotalItems());
        assertEquals(weak.getId(), result.getItems().get(0).getId());
        assertEquals(strong.getId(), result.getItems().get(1).getId());
    }

    @Test
    public void shouldFilterByMultipleFuelTypesAndPaginate() {
        // Arrange
        final long brandId = insertBrand("Multi Fuel Brand").getId();
        final long bodyTypeId = insertBodyType("Multi Fuel Body").getId();
        // 3 matching (hybrid or electric)
        insertCar(brandId, "Multi Fuel Brand", "Hybrid 1", bodyTypeId, "Multi Fuel Body",
                2026, "Desc", "hybrid", 200, 6, "automatic", new BigDecimal("5.0"), 220, new BigDecimal("40000"));
        insertCar(brandId, "Multi Fuel Brand", "Electric 1", bodyTypeId, "Multi Fuel Body",
                2026, "Desc", "electric", 300, 6, "automatic", new BigDecimal("0.0"), 240, new BigDecimal("50000"));
        insertCar(brandId, "Multi Fuel Brand", "Hybrid 2", bodyTypeId, "Multi Fuel Body",
                2026, "Desc", "hybrid", 250, 6, "automatic", new BigDecimal("5.5"), 230, new BigDecimal("45000"));
        // 1 non-matching (combustion)
        insertCar(brandId, "Multi Fuel Brand", "Combustion", bodyTypeId, "Multi Fuel Body",
                2026, "Desc", "combustion", 150, 6, "manual", new BigDecimal("8.0"), 200, new BigDecimal("30000"));

        final CarSearchCriteria criteria = new CarSearchCriteria();
        criteria.setBrand("Multi Fuel Brand");
        criteria.setFuelTypes(List.of("hybrid", "electric"));
        criteria.setSortBy("hp_asc");
        criteria.setPage(1);
        // Page size is 16, so all 3 should be on page 1

        // Exercise
        final Page<Car> result = carDao.findByCriteria(criteria);

        // Assertions
        assertEquals(3L, result.getTotalItems());
        assertEquals(3, result.getItems().size());
        assertEquals("Hybrid 1", result.getItems().get(0).getModel()); // 200 hp
        assertEquals("Hybrid 2", result.getItems().get(1).getModel()); // 250 hp
        assertEquals("Electric 1", result.getItems().get(2).getModel()); // 300 hp
    }

    @Test
    public void shouldCombineMultipleFiltersWithPagination() {
        // Arrange
        final long brandId = insertBrand("Combo Brand").getId();
        final long bodyTypeId = insertBodyType("Combo Body").getId();
        // Insert 5 matching cars
        for (int i = 0; i < 5; i++) {
            insertCar(brandId, "Combo Brand", "Match " + i, bodyTypeId, "Combo Body",
                    2025, "Matching", "hybrid", 200 + i, 8, "automatic", new BigDecimal("5.0"),
                    220, new BigDecimal("40000.00"));
        }
        // Insert 1 non-matching (wrong year)
        insertCar(brandId, "Combo Brand", "Miss Year", bodyTypeId, "Combo Body",
                2020, "Non-matching", "hybrid", 205, 8, "automatic", new BigDecimal("5.0"),
                220, new BigDecimal("40000.00"));

        final CarSearchCriteria criteria = new CarSearchCriteria();
        criteria.setBrand("Combo Brand");
        criteria.setYearMin(2024);
        criteria.setFuelTypes(List.of("hybrid"));
        criteria.setTransmission("automatic");
        criteria.setSortBy("hp_desc");

        // We want to test page 2 with a small page size, but CARS_PAGE_SIZE is 16.
        // Since we can't easily change Pagination.CARS_PAGE_SIZE in tests without side effects,
        // we'll just verify page 1 and the total count, or insert 17 items if we really want to test page 2.
        // Let's insert enough for page 2.
        for (int i = 5; i < 17; i++) {
            insertCar(brandId, "Combo Brand", "Match " + i, bodyTypeId, "Combo Body",
                    2025, "Matching", "hybrid", 200 + i, 8, "automatic", new BigDecimal("5.0"),
                    220, new BigDecimal("40000.00"));
        }

        criteria.setPage(2);

        // Exercise
        final Page<Car> result = carDao.findByCriteria(criteria);

        // Assertions
        assertEquals(17L, result.getTotalItems());
        assertEquals(2, result.getPageNumber());
        assertEquals(1, result.getItems().size());
        // Sorted by hp_desc: Match 16 (216hp) is top, Match 0 (200hp) is bottom.
        // Page 1 has Match 16 to Match 1. Page 2 has Match 0.
        assertEquals("Match 0", result.getItems().get(0).getModel());
    }
}
