package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.BodyType;
import ar.edu.itba.paw.model.Brand;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CatalogJdbcDaoTest extends AbstractPersistenceTest {

    @Test
    public void shouldCreateBrandAndPersistName() {
        // Arrange
        final String brandName = "Persisted Brand";

        // Exercise
        final Brand result = brandDao.insertAndFetch(brandName);

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
        final BodyType result = bodyTypeDao.insertAndFetch(bodyTypeName);

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
}
