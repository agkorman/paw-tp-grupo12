package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.model.ReviewReply;
import ar.edu.itba.paw.model.User;
import org.junit.jupiter.api.Test;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ReviewInteractionJdbcDaoTest extends AbstractPersistenceTest {

    @Test
    public void shouldCreateReplyAndFindItByReviewId() {
        // Arrange
        final Review review = createReview("reply");
        final User author = createUser("reply-author");

        // Exercise
        final ReviewReply result = reviewReplyDao.create(review.getId(), author.getId(), "Reply body");

        // Assertions
        assertEquals("Reply body", result.getBody());
        assertEquals(author.getId(), result.getUserId());
        assertEquals(1, countRows(
                "SELECT COUNT(*) FROM review_replies WHERE reply_id = ? AND review_id = ? AND user_id = ?",
                result.getId(), review.getId(), author.getId()
        ));
        assertEquals("Reply body", jdbcTemplate.queryForObject(
                "SELECT body FROM review_replies WHERE reply_id = ?", String.class, result.getId()
        ));
    }

    @Test
    public void shouldDeleteReplyAndRemoveItFromReviewReplies() {
        // Arrange
        final Review review = createReview("delete-reply");
        final User author = createUser("delete-reply-author");
        final ReviewReply reply = insertReviewReply(review.getId(), author.getId(), author.getUsername(), "Reply body");

        // Exercise
        final boolean result = reviewReplyDao.delete(reply.getId());

        // Assertions
        assertTrue(result);
        assertEquals(0, countRows("SELECT COUNT(*) FROM review_replies WHERE reply_id = ?", reply.getId()));
        assertEquals(0, countRows("SELECT COUNT(*) FROM review_replies WHERE review_id = ?", review.getId()));
    }

    @Test
    public void shouldLikeReviewWhenNotPreviouslyLiked() {
        // Arrange
        final Review review = createReview("like-review");
        final User liker = createUser("like-review-user");

        // Exercise
        final boolean result = reviewLikeDao.likeReview(review.getId(), liker.getId());

        // Assertions
        assertTrue(result);
        assertEquals(1, countRows(
                "SELECT COUNT(*) FROM review_likes WHERE review_id = ? AND user_id = ?",
                review.getId(), liker.getId()
        ));
    }

    @Test
    public void shouldNotLikeReviewAgainWhenAlreadyLiked() {
        // Arrange
        final Review review = createReview("like-review-again");
        final User liker = createUser("like-review-again-user");
        jdbcTemplate.update("INSERT INTO review_likes (review_id, user_id) VALUES (?, ?)", review.getId(), liker.getId());

        // Exercise
        final boolean result = reviewLikeDao.likeReview(review.getId(), liker.getId());

        // Assertions
        assertFalse(result);
        assertEquals(1, countRows(
                "SELECT COUNT(*) FROM review_likes WHERE review_id = ? AND user_id = ?",
                review.getId(), liker.getId()
        ));
    }

    @Test
    public void shouldUnlikeReviewWhenPreviouslyLiked() {
        // Arrange
        final Review review = createReview("unlike-review");
        final User liker = createUser("unlike-review-user");
        jdbcTemplate.update("INSERT INTO review_likes (review_id, user_id) VALUES (?, ?)", review.getId(), liker.getId());

        // Exercise
        final boolean result = reviewLikeDao.unlikeReview(review.getId(), liker.getId());

        // Assertions
        assertTrue(result);
        assertEquals(0, countRows(
                "SELECT COUNT(*) FROM review_likes WHERE review_id = ? AND user_id = ?",
                review.getId(), liker.getId()
        ));
    }

    @Test
    public void shouldLikeReplyWhenNotPreviouslyLiked() {
        // Arrange
        final Review review = createReview("like-reply");
        final User author = createUser("like-reply-author");
        final User liker = createUser("like-reply-user");
        final ReviewReply reply = insertReviewReply(review.getId(), author.getId(), author.getUsername(), "Reply body");

        // Exercise
        final boolean result = reviewLikeDao.likeReply(reply.getId(), liker.getId());

        // Assertions
        assertTrue(result);
        assertEquals(1, countRows(
                "SELECT COUNT(*) FROM review_reply_likes WHERE reply_id = ? AND user_id = ?",
                reply.getId(), liker.getId()
        ));
    }

    @Test
    public void shouldNotLikeReplyAgainWhenAlreadyLiked() {
        // Arrange
        final Review review = createReview("like-reply-again");
        final User author = createUser("like-reply-again-author");
        final User liker = createUser("like-reply-again-user");
        final ReviewReply reply = insertReviewReply(review.getId(), author.getId(), author.getUsername(), "Reply body");
        jdbcTemplate.update("INSERT INTO review_reply_likes (reply_id, user_id) VALUES (?, ?)", reply.getId(), liker.getId());

        // Exercise
        final boolean result = reviewLikeDao.likeReply(reply.getId(), liker.getId());

        // Assertions
        assertFalse(result);
        assertEquals(1, countRows(
                "SELECT COUNT(*) FROM review_reply_likes WHERE reply_id = ? AND user_id = ?",
                reply.getId(), liker.getId()
        ));
    }

    @Test
    public void shouldUnlikeReplyWhenPreviouslyLiked() {
        // Arrange
        final Review review = createReview("unlike-reply");
        final User author = createUser("unlike-reply-author");
        final User liker = createUser("unlike-reply-user");
        final ReviewReply reply = insertReviewReply(review.getId(), author.getId(), author.getUsername(), "Reply body");
        jdbcTemplate.update("INSERT INTO review_reply_likes (reply_id, user_id) VALUES (?, ?)", reply.getId(), liker.getId());

        // Exercise
        final boolean result = reviewLikeDao.unlikeReply(reply.getId(), liker.getId());

        // Assertions
        assertTrue(result);
        assertEquals(0, countRows(
                "SELECT COUNT(*) FROM review_reply_likes WHERE reply_id = ? AND user_id = ?",
                reply.getId(), liker.getId()
        ));
    }

    @Test
    public void shouldReplaceReviewTagAssignmentsAndCountTagsForCar() {
        // Arrange
        final Review review = createReview("tags");
        final short positive = createReviewTag("positive-tag", "positive", "comfort");
        final short negative = createReviewTag("negative-tag", "negative", "comfort");

        // Exercise
        reviewTagDao.replaceAssignments(review.getId(), List.of(positive, negative, positive));

        // Assertions
        assertEquals(2, countRows("SELECT COUNT(*) FROM review_tag_assignments WHERE review_id = ?", review.getId()));
        assertEquals(1, countRows(
                "SELECT COUNT(*) FROM review_tag_assignments WHERE review_id = ? AND tag_id = ?",
                review.getId(), positive
        ));
        assertEquals(1, countRows(
                "SELECT COUNT(*) FROM review_tag_assignments WHERE review_id = ? AND tag_id = ?",
                review.getId(), negative
        ));
    }

    @Test
    public void shouldReturnFalseWhenDeletingMissingReply() {
        // Arrange
        final long missingId = 9999L;

        // Exercise
        final boolean result = reviewReplyDao.delete(missingId);

        // Assertions
        assertFalse(result);
        assertEquals(0, countRows("SELECT COUNT(*) FROM review_replies WHERE reply_id = ?", missingId));
    }

    @Test
    public void shouldReturnFalseWhenUnlikingNonLikedReview() {
        // Arrange
        final Review review = createReview("unlike-missing-review");
        final User user = createUser("unlike-missing-review-user");

        // Exercise
        final boolean result = reviewLikeDao.unlikeReview(review.getId(), user.getId());

        // Assertions
        assertFalse(result);
        assertEquals(0, countRows(
                "SELECT COUNT(*) FROM review_likes WHERE review_id = ? AND user_id = ?",
                review.getId(), user.getId()
        ));
    }

    @Test
    public void shouldReturnFalseWhenUnlikingNonLikedReply() {
        // Arrange
        final Review review = createReview("unlike-missing-reply");
        final User author = createUser("unlike-missing-reply-author");
        final User liker = createUser("unlike-missing-reply-user");
        final ReviewReply reply = insertReviewReply(review.getId(), author.getId(), author.getUsername(), "Reply body");

        // Exercise
        final boolean result = reviewLikeDao.unlikeReply(reply.getId(), liker.getId());

        // Assertions
        assertFalse(result);
        assertEquals(0, countRows(
                "SELECT COUNT(*) FROM review_reply_likes WHERE reply_id = ? AND user_id = ?",
                reply.getId(), liker.getId()
        ));
    }

    @Test
    public void shouldClearTagAssignmentsWhenReplacingWithEmptyList() {
        // Arrange
        final Review review = createReview("tags-clear");
        final short existing = createReviewTag("clear-tag", "positive", "comfort");
        jdbcTemplate.update(
                "INSERT INTO review_tag_assignments (review_id, tag_id) VALUES (?, ?)",
                review.getId(), existing
        );

        // Exercise
        reviewTagDao.replaceAssignments(review.getId(), List.of());

        // Assertions
        assertEquals(0, countRows("SELECT COUNT(*) FROM review_tag_assignments WHERE review_id = ?", review.getId()));
    }

    @Test
    public void shouldReplaceReviewTagAssignmentsAndRemovePreviousTags() {
        // Arrange
        final Review review = createReview("tags-replace");
        final short previous = createReviewTag("previous-tag", "neutral", "comfort");
        final short replacement = createReviewTag("replacement-tag", "positive", "comfort");
        jdbcTemplate.update(
                "INSERT INTO review_tag_assignments (review_id, tag_id) VALUES (?, ?)",
                review.getId(), previous
        );

        // Exercise
        reviewTagDao.replaceAssignments(review.getId(), List.of(replacement));

        // Assertions
        assertEquals(1, countRows("SELECT COUNT(*) FROM review_tag_assignments WHERE review_id = ?", review.getId()));
        assertEquals(0, countRows(
                "SELECT COUNT(*) FROM review_tag_assignments WHERE review_id = ? AND tag_id = ?",
                review.getId(), previous
        ));
        assertEquals(1, countRows(
                "SELECT COUNT(*) FROM review_tag_assignments WHERE review_id = ? AND tag_id = ?",
                review.getId(), replacement
        ));
    }
}
