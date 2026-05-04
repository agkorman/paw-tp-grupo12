package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.CarImage;
import ar.edu.itba.paw.model.CarImagePayload;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ImageJdbcDaoTest extends AbstractPersistenceTest {

    @Test
    public void shouldReplaceCarImagesAndReturnCoverByDisplayOrder() {
        // Arrange
        final Car car = createCar("images");
        jdbcTemplate.update(
                "INSERT INTO car_images (car_id, display_order, content_type, image_data) VALUES (?, ?, ?, ?)",
                car.getId(), 0, "image/png", new byte[]{1, 2}
        );
        jdbcTemplate.update(
                "INSERT INTO car_images (car_id, display_order, content_type, image_data) VALUES (?, ?, ?, ?)",
                car.getId(), 1, "image/jpeg", new byte[]{3, 4}
        );

        // Exercise
        final Optional<CarImage> result = carImageDao.findByCarId(car.getId());

        // Assertions
        assertTrue(result.isPresent());
        assertEquals(2, countRows("SELECT COUNT(*) FROM car_images WHERE car_id = ?", car.getId()));
        assertEquals("image/png", jdbcTemplate.queryForObject(
                "SELECT content_type FROM car_images WHERE car_id = ? AND display_order = 0",
                String.class, car.getId()
        ));
        assertArrayEquals(new byte[]{1, 2}, jdbcTemplate.queryForObject(
                "SELECT image_data FROM car_images WHERE car_id = ? AND display_order = 0",
                byte[].class, car.getId()
        ));
        assertEquals(0, result.get().getDisplayOrder());
        assertEquals("image/png", result.get().getContentType());
        assertArrayEquals(new byte[]{1, 2}, result.get().getImageData());
    }

    @Test
    public void shouldReplaceExistingGalleryWithNewImages() {
        // Arrange
        final Car car = createCar("replace-images");
        jdbcTemplate.update(
                "INSERT INTO car_images (car_id, display_order, content_type, image_data) VALUES (?, ?, ?, ?)",
                car.getId(), 0, "image/png", new byte[]{1}
        );

        // Exercise
        carImageDao.replaceAll(car.getId(), List.of(new CarImagePayload("image/jpeg", new byte[]{9})));

        // Assertions
        assertEquals(1, countRows("SELECT COUNT(*) FROM car_images WHERE car_id = ?", car.getId()));
        assertEquals(0, countRows(
                "SELECT COUNT(*) FROM car_images WHERE car_id = ? AND content_type = ?",
                car.getId(), "image/png"
        ));
        assertEquals("image/jpeg", jdbcTemplate.queryForObject(
                "SELECT content_type FROM car_images WHERE car_id = ? AND display_order = 0",
                String.class, car.getId()
        ));
        assertArrayEquals(new byte[]{9}, jdbcTemplate.queryForObject(
                "SELECT image_data FROM car_images WHERE car_id = ? AND display_order = 0",
                byte[].class, car.getId()
        ));
    }

    @Test
    public void shouldClearCarImagesWhenReplacingWithEmptyGallery() {
        // Arrange
        final Car car = createCar("clear-images");
        jdbcTemplate.update(
                "INSERT INTO car_images (car_id, display_order, content_type, image_data) VALUES (?, ?, ?, ?)",
                car.getId(), 0, "image/png", new byte[]{1}
        );

        // Exercise
        carImageDao.replaceAll(car.getId(), List.of());

        // Assertions
        assertEquals(0, countRows("SELECT COUNT(*) FROM car_images WHERE car_id = ?", car.getId()));
    }

    @Test
    public void shouldReturnEmptyWhenCarHasNoImages() {
        // Arrange
        final Car car = createCar("no-images");

        // Exercise
        final Optional<CarImage> result = carImageDao.findByCarId(car.getId());

        // Assertions
        assertFalse(result.isPresent());
    }

    @Test
    public void shouldReturnEmptyWhenImageDoesNotExistForCar() {
        // Arrange
        final Car car = createCar("wrong-image-id");
        jdbcTemplate.update(
                "INSERT INTO car_images (car_id, display_order, content_type, image_data) VALUES (?, ?, ?, ?)",
                car.getId(), 0, "image/png", new byte[]{1}
        );
        final long wrongImageId = 9999L;

        // Exercise
        final Optional<CarImage> result = carImageDao.findByCarIdAndImageId(car.getId(), wrongImageId);

        // Assertions
        assertFalse(result.isPresent());
    }

    @Test
    public void shouldFindCarImageByCarAndImageId() {
        // Arrange
        final Car car = createCar("image-id");
        jdbcTemplate.update(
                "INSERT INTO car_images (car_id, display_order, content_type, image_data) VALUES (?, ?, ?, ?)",
                car.getId(), 0, "image/png", new byte[]{7, 8}
        );
        final long imageId = jdbcTemplate.queryForObject(
                "SELECT image_id FROM car_images WHERE car_id = ? AND display_order = ?",
                Long.class, car.getId(), 0
        );

        // Exercise
        final Optional<CarImage> result = carImageDao.findByCarIdAndImageId(car.getId(), imageId);

        // Assertions
        assertTrue(result.isPresent());
        assertEquals(imageId, result.get().getImageId());
        assertArrayEquals(new byte[]{7, 8}, result.get().getImageData());
    }
}
