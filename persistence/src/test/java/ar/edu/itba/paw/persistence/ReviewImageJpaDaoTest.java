package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.ImagePayload;
import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.model.ReviewImage;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReviewImageJpaDaoTest extends AbstractPersistenceTest {

    @Test
    public void shouldInsertReviewImagesInOrderWhenNoExisting() {
        // Arrange
        final Review review = createReview("insert-order");
        final List<ImagePayload> payloads = List.of(
                new ImagePayload("image/png", new byte[]{1, 2}),
                new ImagePayload("image/jpeg", new byte[]{3, 4})
        );

        // Exercise
        reviewImageDao.replaceAll(review.getId(), payloads);

        // Assertions
        flushAndClear();
        assertEquals(2, countRows("SELECT COUNT(*) FROM review_images WHERE review_id = ?", review.getId()));
        assertEquals("image/png", jdbcTemplate.queryForObject(
                "SELECT content_type FROM review_images WHERE review_id = ? AND display_order = 0",
                String.class, review.getId()
        ));
        assertArrayEquals(new byte[]{1, 2}, jdbcTemplate.queryForObject(
                "SELECT image_data FROM review_images WHERE review_id = ? AND display_order = 0",
                byte[].class, review.getId()
        ));
        assertEquals("image/jpeg", jdbcTemplate.queryForObject(
                "SELECT content_type FROM review_images WHERE review_id = ? AND display_order = 1",
                String.class, review.getId()
        ));
        assertArrayEquals(new byte[]{3, 4}, jdbcTemplate.queryForObject(
                "SELECT image_data FROM review_images WHERE review_id = ? AND display_order = 1",
                byte[].class, review.getId()
        ));
    }

    @Test
    public void shouldReplaceExistingGalleryWithNewImages() {
        // Arrange
        final Review review = createReview("replace");
        jdbcTemplate.update(
                "INSERT INTO review_images (review_id, display_order, content_type, image_data) VALUES (?, ?, ?, ?)",
                review.getId(), 0, "image/png", new byte[]{1}
        );

        // Exercise
        reviewImageDao.replaceAll(review.getId(), List.of(new ImagePayload("image/jpeg", new byte[]{9})));

        // Assertions
        flushAndClear();
        assertEquals(1, countRows("SELECT COUNT(*) FROM review_images WHERE review_id = ?", review.getId()));
        assertEquals(0, countRows(
                "SELECT COUNT(*) FROM review_images WHERE review_id = ? AND content_type = ?",
                review.getId(), "image/png"
        ));
        assertEquals("image/jpeg", jdbcTemplate.queryForObject(
                "SELECT content_type FROM review_images WHERE review_id = ? AND display_order = 0",
                String.class, review.getId()
        ));
        assertArrayEquals(new byte[]{9}, jdbcTemplate.queryForObject(
                "SELECT image_data FROM review_images WHERE review_id = ? AND display_order = 0",
                byte[].class, review.getId()
        ));
    }

    @Test
    public void shouldClearReviewImagesWhenReplacingWithEmptyGallery() {
        // Arrange
        final Review review = createReview("clear");
        jdbcTemplate.update(
                "INSERT INTO review_images (review_id, display_order, content_type, image_data) VALUES (?, ?, ?, ?)",
                review.getId(), 0, "image/png", new byte[]{1}
        );

        // Exercise
        reviewImageDao.replaceAll(review.getId(), List.of());

        // Assertions
        flushAndClear();
        assertEquals(0, countRows("SELECT COUNT(*) FROM review_images WHERE review_id = ?", review.getId()));
    }

    @Test
    public void shouldReturnMetadataWithoutBytesForFindAllByReviewId() {
        // Arrange
        final Review review = createReview("metadata");
        jdbcTemplate.update(
                "INSERT INTO review_images (review_id, display_order, content_type, image_data) VALUES (?, ?, ?, ?)",
                review.getId(), 1, "image/jpeg", new byte[]{2}
        );
        jdbcTemplate.update(
                "INSERT INTO review_images (review_id, display_order, content_type, image_data) VALUES (?, ?, ?, ?)",
                review.getId(), 0, "image/png", new byte[]{1}
        );

        // Exercise
        final List<ReviewImage> result = reviewImageDao.findAllByReviewId(review.getId());

        // Assertions
        assertEquals(2, result.size());
        assertEquals("image/png", result.get(0).getContentType());
        assertEquals(0, result.get(0).getDisplayOrder());
        assertNull(result.get(0).getImageData());
        assertEquals("image/jpeg", result.get(1).getContentType());
        assertEquals(1, result.get(1).getDisplayOrder());
        assertNull(result.get(1).getImageData());
    }

    @Test
    public void shouldReturnBytesForFindByReviewIdAndImageId() {
        // Arrange
        final Review review = createReview("bytes");
        jdbcTemplate.update(
                "INSERT INTO review_images (review_id, display_order, content_type, image_data) VALUES (?, ?, ?, ?)",
                review.getId(), 0, "image/png", new byte[]{7, 8}
        );
        final long imageId = jdbcTemplate.queryForObject(
                "SELECT image_id FROM review_images WHERE review_id = ? AND display_order = ?",
                Long.class, review.getId(), 0
        );

        // Exercise
        final Optional<ReviewImage> result = reviewImageDao.findByReviewIdAndImageId(review.getId(), imageId);

        // Assertions
        assertTrue(result.isPresent());
        assertEquals(imageId, result.get().getImageId());
        assertEquals("image/png", result.get().getContentType());
        assertArrayEquals(new byte[]{7, 8}, result.get().getImageData());
    }

    @Test
    public void shouldReturnEmptyWhenImageIdMismatchesReview() {
        // Arrange
        final Review review = createReview("mismatch");
        jdbcTemplate.update(
                "INSERT INTO review_images (review_id, display_order, content_type, image_data) VALUES (?, ?, ?, ?)",
                review.getId(), 0, "image/png", new byte[]{1}
        );
        final long wrongImageId = 999999L;

        // Exercise
        final Optional<ReviewImage> result = reviewImageDao.findByReviewIdAndImageId(review.getId(), wrongImageId);

        // Assertions
        assertFalse(result.isPresent());
    }
}
