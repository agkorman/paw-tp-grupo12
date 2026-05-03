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
    public void shouldFindBrandByNameIgnoringCaseWhenBrandExists() {
        // Arrange
        final Brand created = brandDao.create("Toyota");

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
        brandDao.create("Volvo");
        brandDao.create("Audi");

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
        final Brand created = brandDao.create("Old Brand");

        // Exercise
        final Optional<Brand> result = brandDao.update(created.getId(), "New Brand");

        // Assertions
        assertTrue(result.isPresent());
        assertEquals("New Brand", result.get().getName());
        assertEquals("New Brand", brandDao.findById(created.getId()).orElseThrow().getName());
    }

    @Test
    public void shouldDeleteBodyTypeWhenBodyTypeExists() {
        // Arrange
        final BodyType created = bodyTypeDao.create("Coupe");

        // Exercise
        final boolean result = bodyTypeDao.delete(created.getId());

        // Assertions
        assertTrue(result);
        assertFalse(bodyTypeDao.findById(created.getId()).isPresent());
    }

    @Test
    public void shouldReturnEmptyWhenBodyTypeDoesNotExist() {
        // Arrange
        bodyTypeDao.create("Sedan");

        // Exercise
        final Optional<BodyType> result = bodyTypeDao.findByName("Hatchback");

        // Assertions
        assertFalse(result.isPresent());
    }
}
