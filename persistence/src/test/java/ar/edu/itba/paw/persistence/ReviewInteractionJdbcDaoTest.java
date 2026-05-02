package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.model.ReviewReply;
import ar.edu.itba.paw.model.User;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        assertEquals(result.getId(), reviewReplyDao.findByReviewId(review.getId()).get(0).getId());
    }

    @Test
    public void shouldDeleteReplyAndRemoveItFromReviewReplies() {
        // Arrange
        final Review review = createReview("delete-reply");
        final User author = createUser("delete-reply-author");
        final ReviewReply reply = reviewReplyDao.create(review.getId(), author.getId(), "Reply body");

        // Exercise
        final boolean result = reviewReplyDao.delete(reply.getId());

        // Assertions
        assertTrue(result);
        assertFalse(reviewReplyDao.findById(reply.getId()).isPresent());
        assertEquals(0, reviewReplyDao.findByReviewId(review.getId()).size());
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
        assertTrue(reviewLikeDao.isReviewLikedByUser(review.getId(), liker.getId()));
        assertEquals(1, reviewLikeDao.countReviewLikes(review.getId()));
        assertEquals(Set.of(review.getId()), reviewLikeDao.findLikedReviewIds(List.of(review.getId()), liker.getId()));
        assertEquals(List.of(review.getId()), reviewLikeDao.findLikedReviewIdsByUserId(liker.getId()));
    }

    @Test
    public void shouldNotLikeReviewAgainWhenAlreadyLiked() {
        // Arrange
        final Review review = createReview("like-review-again");
        final User liker = createUser("like-review-again-user");
        reviewLikeDao.likeReview(review.getId(), liker.getId());

        // Exercise
        final boolean result = reviewLikeDao.likeReview(review.getId(), liker.getId());

        // Assertions
        assertFalse(result);
        assertEquals(1, reviewLikeDao.countReviewLikes(review.getId()));
    }

    @Test
    public void shouldUnlikeReviewWhenPreviouslyLiked() {
        // Arrange
        final Review review = createReview("unlike-review");
        final User liker = createUser("unlike-review-user");
        reviewLikeDao.likeReview(review.getId(), liker.getId());

        // Exercise
        final boolean result = reviewLikeDao.unlikeReview(review.getId(), liker.getId());

        // Assertions
        assertTrue(result);
        assertFalse(reviewLikeDao.isReviewLikedByUser(review.getId(), liker.getId()));
        assertEquals(0, reviewLikeDao.countReviewLikes(review.getId()));
    }

    @Test
    public void shouldLikeReplyWhenNotPreviouslyLiked() {
        // Arrange
        final Review review = createReview("like-reply");
        final User author = createUser("like-reply-author");
        final User liker = createUser("like-reply-user");
        final ReviewReply reply = reviewReplyDao.create(review.getId(), author.getId(), "Reply body");

        // Exercise
        final boolean result = reviewLikeDao.likeReply(reply.getId(), liker.getId());

        // Assertions
        assertTrue(result);
        assertTrue(reviewLikeDao.isReplyLikedByUser(reply.getId(), liker.getId()));
        assertEquals(1, reviewLikeDao.countReplyLikes(reply.getId()));
        assertEquals(Set.of(reply.getId()), reviewLikeDao.findLikedReplyIds(List.of(reply.getId()), liker.getId()));
        assertEquals(List.of(reply.getId()), reviewLikeDao.findLikedReplyIdsByUserId(liker.getId()));
    }

    @Test
    public void shouldNotLikeReplyAgainWhenAlreadyLiked() {
        // Arrange
        final Review review = createReview("like-reply-again");
        final User author = createUser("like-reply-again-author");
        final User liker = createUser("like-reply-again-user");
        final ReviewReply reply = reviewReplyDao.create(review.getId(), author.getId(), "Reply body");
        reviewLikeDao.likeReply(reply.getId(), liker.getId());

        // Exercise
        final boolean result = reviewLikeDao.likeReply(reply.getId(), liker.getId());

        // Assertions
        assertFalse(result);
        assertEquals(1, reviewLikeDao.countReplyLikes(reply.getId()));
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
        assertEquals(2, reviewTagDao.findByReviewIds(List.of(review.getId())).get(review.getId()).size());
        final Map<Short, Integer> counts = reviewTagDao.getTagCountsForCars(List.of(review.getCarId())).get(review.getCarId());
        assertEquals(1, counts.get(positive));
        assertEquals(1, counts.get(negative));
    }
}
