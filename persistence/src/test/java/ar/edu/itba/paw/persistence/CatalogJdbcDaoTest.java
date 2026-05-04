package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.BodyType;
import ar.edu.itba.paw.model.Brand;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CatalogJdbcDaoTest extends AbstractPersistenceTest {

    @Test
    public void shouldCreateBrandAndPersistName() {
        // Arrange
        final String brandName = "Persisted Brand";

        // Exercise
        final Brand result = brandDao.create(brandName);

        // Assertions
        assertEquals(brandName, result.getName());
        assertEquals(1, countRows("SELECT COUNT(*) FROM brands WHERE brand_id = ?", result.getId()));
        assertEquals(brandName, jdbcTemplate.queryForObject(
                "SELECT name FROM brands WHERE brand_id = ?", String.class, result.getId()
        ));
    }

    @Test
    public void shouldCreateBodyTypeAndPersistName() {
        // Arrange
        final String bodyTypeName = "Persisted Body";

        // Exercise
        final BodyType result = bodyTypeDao.create(bodyTypeName);

        // Assertions
        assertEquals(bodyTypeName, result.getName());
        assertEquals(1, countRows("SELECT COUNT(*) FROM body_types WHERE body_type_id = ?", result.getId()));
        assertEquals(bodyTypeName, jdbcTemplate.queryForObject(
                "SELECT name FROM body_types WHERE body_type_id = ?", String.class, result.getId()
        ));
    }

    @Test
    public void shouldFindBrandByNameIgnoringCaseWhenBrandExists() {
        // Arrange
        final Brand created = insertBrand("Toyota");

        // Exercise
        final Optional<Brand> result = brandDao.findByName("toyota");

        // Assertions
        assertTrue(result.isPresent());
        assertEquals(created.getId(), result.get().getId());
        assertEquals("Toyota", result.get().getName());
    }

    @Test
    public void shouldReturnBrandsOrderedByNameWhenFindingAll() {
        // Arrange
        insertBrand("Volvo");
        insertBrand("Audi");

        // Exercise
        final List<Brand> result = brandDao.findAll();

        // Assertions
        assertEquals(2, result.size());
        assertEquals("Audi", result.get(0).getName());
        assertEquals("Volvo", result.get(1).getName());
    }

    @Test
    public void shouldUpdateBrandNameWhenBrandExists() {
        // Arrange
        final Brand created = insertBrand("Old Brand");

        // Exercise
        final Optional<Brand> result = brandDao.update(created.getId(), "New Brand");

        // Assertions
        assertTrue(result.isPresent());
        assertEquals("New Brand", result.get().getName());
        assertEquals("New Brand", jdbcTemplate.queryForObject(
                "SELECT name FROM brands WHERE brand_id = ?", String.class, created.getId()
        ));
    }

    @Test
    public void shouldUpdateBodyTypeNameWhenBodyTypeExists() {
        // Arrange
        final BodyType created = insertBodyType("Old Body");

        // Exercise
        final Optional<BodyType> result = bodyTypeDao.update(created.getId(), "New Body");

        // Assertions
        assertTrue(result.isPresent());
        assertEquals("New Body", result.get().getName());
        assertEquals("New Body", jdbcTemplate.queryForObject(
                "SELECT name FROM body_types WHERE body_type_id = ?", String.class, created.getId()
        ));
    }

    @Test
    public void shouldDeleteBrandWhenBrandExists() {
        // Arrange
        final Brand created = insertBrand("Disposable Brand");

        // Exercise
        final boolean result = brandDao.delete(created.getId());

        // Assertions
        assertTrue(result);
        assertEquals(0, countRows("SELECT COUNT(*) FROM brands WHERE brand_id = ?", created.getId()));
    }

    @Test
    public void shouldDeleteBodyTypeWhenBodyTypeExists() {
        // Arrange
        final BodyType created = insertBodyType("Coupe");

        // Exercise
        final boolean result = bodyTypeDao.delete(created.getId());

        // Assertions
        assertTrue(result);
        assertEquals(0, countRows(
                "SELECT COUNT(*) FROM body_types WHERE body_type_id = ?", created.getId()
        ));
    }

    @Test
    public void shouldReturnEmptyWhenBodyTypeDoesNotExist() {
        // Arrange
        insertBodyType("Sedan");

        // Exercise
        final Optional<BodyType> result = bodyTypeDao.findByName("Hatchback");

        // Assertions
        assertFalse(result.isPresent());
    }

    @Test
    public void shouldReturnEmptyWhenFindBrandByNameHasNoMatch() {
        // Arrange
        insertBrand("Honda");

        // Exercise
        final Optional<Brand> result = brandDao.findByName("Ferrari");

        // Assertions
        assertFalse(result.isPresent());
    }

    @Test
    public void shouldReturnEmptyWhenUpdatingMissingBrand() {
        // Arrange
        final long missingId = 9999L;

        // Exercise
        final Optional<Brand> result = brandDao.update(missingId, "Ghost Brand");

        // Assertions
        assertFalse(result.isPresent());
        assertEquals(0, countRows("SELECT COUNT(*) FROM brands WHERE brand_id = ?", missingId));
    }

    @Test
    public void shouldReturnEmptyWhenUpdatingMissingBodyType() {
        // Arrange
        final long missingId = 9999L;

        // Exercise
        final Optional<BodyType> result = bodyTypeDao.update(missingId, "Ghost Body");

        // Assertions
        assertFalse(result.isPresent());
        assertEquals(0, countRows("SELECT COUNT(*) FROM body_types WHERE body_type_id = ?", missingId));
    }

    @Test
    public void shouldReturnFalseWhenDeletingMissingBrand() {
        // Arrange
        final long missingId = 9999L;

        // Exercise
        final boolean result = brandDao.delete(missingId);

        // Assertions
        assertFalse(result);
        assertEquals(0, countRows("SELECT COUNT(*) FROM brands WHERE brand_id = ?", missingId));
    }

    @Test
    public void shouldReturnFalseWhenDeletingMissingBodyType() {
        // Arrange
        final long missingId = 9999L;

        // Exercise
        final boolean result = bodyTypeDao.delete(missingId);

        // Assertions
        assertFalse(result);
        assertEquals(0, countRows("SELECT COUNT(*) FROM body_types WHERE body_type_id = ?", missingId));
    }

    @Test
    public void shouldRejectDuplicateBrandName() {
        // Arrange
        insertBrand("Duplicate Brand");

        // Exercise
        assertThrows(DataIntegrityViolationException.class, () -> brandDao.create("Duplicate Brand"));

        // Assertions
        assertEquals(1, countRows("SELECT COUNT(*) FROM brands WHERE name = ?", "Duplicate Brand"));
    }

    @Test
    public void shouldRejectDuplicateBodyTypeName() {
        // Arrange
        insertBodyType("Duplicate Body");

        // Exercise
        assertThrows(DataIntegrityViolationException.class, () -> bodyTypeDao.create("Duplicate Body"));

        // Assertions
        assertEquals(1, countRows("SELECT COUNT(*) FROM body_types WHERE name = ?", "Duplicate Body"));
    }

    @Test
    public void shouldRejectBrandDeleteWhenReferencedByCar() {
        // Arrange
        final Brand brand = insertBrand("Referenced Brand");
        final long bodyTypeId = insertBodyType("Ref Body For Brand").getId();
        insertCar(brand.getId(), brand.getName(), "Ref Model", bodyTypeId, "Ref Body For Brand",
                2026, "Description", "combustion", 200, 6, "automatic",
                new java.math.BigDecimal("8.0"), 220, new java.math.BigDecimal("30000.00"));

        // Exercise
        assertThrows(DataIntegrityViolationException.class, () -> brandDao.delete(brand.getId()));

        // Assertions
        assertEquals(1, countRows("SELECT COUNT(*) FROM brands WHERE brand_id = ?", brand.getId()));
    }

    @Test
    public void shouldRejectBodyTypeDeleteWhenReferencedByCar() {
        // Arrange
        final long brandId = insertBrand("Ref Brand For Body").getId();
        final BodyType bodyType = insertBodyType("Referenced Body");
        insertCar(brandId, "Ref Brand For Body", "Ref Model Body", bodyType.getId(), bodyType.getName(),
                2026, "Description", "combustion", 200, 6, "automatic",
                new java.math.BigDecimal("8.0"), 220, new java.math.BigDecimal("30000.00"));

        // Exercise
        assertThrows(DataIntegrityViolationException.class, () -> bodyTypeDao.delete(bodyType.getId()));

        // Assertions
        assertEquals(1, countRows("SELECT COUNT(*) FROM body_types WHERE body_type_id = ?", bodyType.getId()));
    }
}
