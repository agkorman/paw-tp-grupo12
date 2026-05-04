package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.BodyType;
import ar.edu.itba.paw.persistence.BodyTypeDao;
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
public class BodyTypeServiceImplTest {

    private static final long BODY_TYPE_ID = 7L;

    @Mock
    private BodyTypeDao bodyTypeDao;
    @Mock
    private CarDao carDao;

    @InjectMocks
    private BodyTypeServiceImpl bodyTypeService;

    private static BodyType bodyType() {
        return new BodyType(BODY_TYPE_ID, "Sedan", LocalDateTime.now());
    }

    @Test
    public void shouldCreateBodyTypeWithTrimmedName() {
        // Arrange
        final BodyType created = bodyType();
        when(bodyTypeDao.insertAndFetch("Sedan")).thenReturn(created);

        // Exercise
        final BodyType result = bodyTypeService.createBodyType("  Sedan  ");

        // Assertions
        assertEquals(BODY_TYPE_ID, result.getId());
        assertEquals("Sedan", result.getName());
    }

    @Test
    public void shouldRejectCreatingBodyTypeWithBlankName() {
        // Arrange
        final String blankName = "   ";

        // Exercise
        final IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> bodyTypeService.createBodyType(blankName));

        // Assertions
        assertEquals("Body type name is required.", ex.getMessage());
    }

    @Test
    public void shouldUpdateBodyTypeWithTrimmedName() {
        // Arrange
        when(bodyTypeDao.update(BODY_TYPE_ID, "Sedan")).thenReturn(Optional.of(bodyType()));

        // Exercise
        final Optional<BodyType> result = bodyTypeService.updateBodyType(BODY_TYPE_ID, " Sedan ");

        // Assertions
        assertTrue(result.isPresent());
        assertEquals("Sedan", result.get().getName());
    }

    @Test
    public void shouldNotDeleteBodyTypeWhenItHasAssociatedCars() {
        // Arrange
        when(bodyTypeDao.findById(BODY_TYPE_ID)).thenReturn(Optional.of(bodyType()));
        when(carDao.countByBodyTypeId(BODY_TYPE_ID)).thenReturn(2L);

        // Exercise
        final boolean result = bodyTypeService.deleteBodyType(BODY_TYPE_ID);

        // Assertions
        assertFalse(result);
    }

    @Test
    public void shouldDeleteBodyTypeWhenItExistsAndHasNoCars() {
        // Arrange
        when(bodyTypeDao.findById(BODY_TYPE_ID)).thenReturn(Optional.of(bodyType()));
        when(carDao.countByBodyTypeId(BODY_TYPE_ID)).thenReturn(0L);
        when(bodyTypeDao.delete(BODY_TYPE_ID)).thenReturn(true);

        // Exercise
        final boolean result = bodyTypeService.deleteBodyType(BODY_TYPE_ID);

        // Assertions
        assertTrue(result);
    }
}
