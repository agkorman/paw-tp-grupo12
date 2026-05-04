package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Brand;
import ar.edu.itba.paw.persistence.BrandDao;
import ar.edu.itba.paw.persistence.CarDao;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class BrandServiceImplTest {

    private static final long BRAND_ID = 9L;

    @Mock
    private BrandDao brandDao;
    @Mock
    private CarDao carDao;

    @InjectMocks
    private BrandServiceImpl brandService;

    private static Brand brand() {
        return new Brand(BRAND_ID, "Toyota", LocalDateTime.now());
    }

    @Test
    public void shouldCreateBrandWithTrimmedName() {
        // Arrange
        final Brand created = new Brand(BRAND_ID, "Toyota", LocalDateTime.now());
        when(brandDao.insertAndFetch("Toyota")).thenReturn(created);

        // Exercise
        final Brand result = brandService.createBrand("  Toyota  ");

        // Assertions
        assertEquals(BRAND_ID, result.getId());
        assertEquals("Toyota", result.getName());
    }

    @Test
    public void shouldRejectCreatingBrandWithBlankName() {
        // Arrange
        final String blankName = "   ";

        // Exercise
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> brandService.createBrand(blankName));

        // Assertions
        assertEquals("Brand name is required.", ex.getMessage());
    }

    @Test
    public void shouldUpdateBrandWithTrimmedName() {
        // Arrange
        when(brandDao.update(BRAND_ID, "Toyota")).thenReturn(Optional.of(brand()));

        // Exercise
        final Optional<Brand> result = brandService.updateBrand(BRAND_ID, "  Toyota ");

        // Assertions
        assertTrue(result.isPresent());
        assertEquals("Toyota", result.get().getName());
    }

    @Test
    public void shouldRejectUpdatingBrandWithBlankName() {
        // Arrange
        final String blankName = null;

        // Exercise
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> brandService.updateBrand(BRAND_ID, blankName));

        // Assertions
        assertEquals("Brand name is required.", ex.getMessage());
    }

    @Test
    public void shouldNotDeleteBrandWhenItDoesNotExist() {
        // Arrange
        when(brandDao.findById(BRAND_ID)).thenReturn(Optional.empty());

        // Exercise
        final boolean result = brandService.deleteBrand(BRAND_ID);

        // Assertions
        assertFalse(result);
    }

    @Test
    public void shouldNotDeleteBrandWhenItHasAssociatedCars() {
        // Arrange
        when(brandDao.findById(BRAND_ID)).thenReturn(Optional.of(brand()));
        when(carDao.countByBrandId(BRAND_ID)).thenReturn(3L);

        // Exercise
        final boolean result = brandService.deleteBrand(BRAND_ID);

        // Assertions
        assertFalse(result);
    }

    @Test
    public void shouldDeleteBrandWhenItExistsAndHasNoCars() {
        // Arrange
        when(brandDao.findById(BRAND_ID)).thenReturn(Optional.of(brand()));
        when(carDao.countByBrandId(BRAND_ID)).thenReturn(0L);
        when(brandDao.delete(BRAND_ID)).thenReturn(true);

        // Exercise
        final boolean result = brandService.deleteBrand(BRAND_ID);

        // Assertions
        assertTrue(result);
    }
}
