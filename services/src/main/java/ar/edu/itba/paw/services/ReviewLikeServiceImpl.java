package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.persistence.ReviewDao;
import ar.edu.itba.paw.persistence.ReviewLikeDao;
import ar.edu.itba.paw.persistence.ReviewReplyDao;
import ar.edu.itba.paw.persistence.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class ReviewLikeServiceImpl implements ReviewLikeService {

    private final ReviewLikeDao reviewLikeDao;
    private final ReviewDao reviewDao;
    private final ReviewReplyDao reviewReplyDao;
    private final UserDao userDao;

    @Autowired
    public ReviewLikeServiceImpl(final ReviewLikeDao reviewLikeDao, final ReviewDao reviewDao,
                                 final ReviewReplyDao reviewReplyDao, final UserDao userDao) {
        this.reviewLikeDao = reviewLikeDao;
        this.reviewDao = reviewDao;
        this.reviewReplyDao = reviewReplyDao;
        this.userDao = userDao;
    }

    @Override
    @Transactional
    public boolean toggleReviewLike(final long reviewId, final long userId) {
        validateReviewAndUser(reviewId, userId);
        try {
            if (reviewLikeDao.isReviewLikedByUser(reviewId, userId)) {
                reviewLikeDao.unlikeReview(reviewId, userId);
                return false;
            }
            reviewLikeDao.likeReview(reviewId, userId);
            return true;
        } catch (final RuntimeException e) {
            throw new IllegalStateException("Failed to toggle review like.", e);
        }
    }

    @Override
    @Transactional
    public boolean toggleReplyLike(final long replyId, final long userId) {
        validateReplyAndUser(replyId, userId);
        try {
            if (reviewLikeDao.isReplyLikedByUser(replyId, userId)) {
                reviewLikeDao.unlikeReply(replyId, userId);
                return false;
            }
            reviewLikeDao.likeReply(replyId, userId);
            return true;
        } catch (final RuntimeException e) {
            throw new IllegalStateException("Failed to toggle reply like.", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long countReviewLikes(final long reviewId) {
        try {
            return reviewLikeDao.countReviewLikes(reviewId);
        } catch (final RuntimeException ignored) {
            return 0;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Long> countReviewLikesByReviewIds(final Collection<Long> reviewIds) {
        if (reviewIds == null || reviewIds.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            return reviewLikeDao.countReviewLikesByReviewIds(reviewIds);
        } catch (final RuntimeException ignored) {
            return Collections.emptyMap();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Long> getLikedReviewIds(final Collection<Long> reviewIds, final long userId) {
        if (reviewIds == null || reviewIds.isEmpty()) {
            return Collections.emptySet();
        }
        try {
            return reviewLikeDao.findLikedReviewIds(reviewIds, userId);
        } catch (final RuntimeException ignored) {
            return Collections.emptySet();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getLikedReviewIdsByUser(final long userId) {
        try {
            return reviewLikeDao.findLikedReviewIdsByUserId(userId);
        } catch (final RuntimeException ignored) {
            return Collections.emptyList();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Long> getLikedReviewIdsByUser(final long userId, final int page) {
        try {
            return reviewLikeDao.findLikedReviewIdsByUserId(userId, page);
        } catch (final RuntimeException ignored) {
            return Page.empty(page < 1 ? 1 : page, 0);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long countLikedReviewsByUser(final long userId) {
        try {
            return reviewLikeDao.countLikedReviewsByUserId(userId);
        } catch (final RuntimeException ignored) {
            return 0L;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public long countReplyLikes(final long replyId) {
        try {
            return reviewLikeDao.countReplyLikes(replyId);
        } catch (final RuntimeException ignored) {
            return 0;
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Long> countReplyLikesByReplyIds(final Collection<Long> replyIds) {
        if (replyIds == null || replyIds.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            return reviewLikeDao.countReplyLikesByReplyIds(replyIds);
        } catch (final RuntimeException ignored) {
            return Collections.emptyMap();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Set<Long> getLikedReplyIds(final Collection<Long> replyIds, final long userId) {
        if (replyIds == null || replyIds.isEmpty()) {
            return Collections.emptySet();
        }
        try {
            return reviewLikeDao.findLikedReplyIds(replyIds, userId);
        } catch (final RuntimeException ignored) {
            return Collections.emptySet();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<Long> getLikedReplyIdsByUser(final long userId) {
        try {
            return reviewLikeDao.findLikedReplyIdsByUserId(userId);
        } catch (final RuntimeException ignored) {
            return Collections.emptyList();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Long> countNewLikesPerReview(final long userId, final LocalDateTime since) {
        if (since == null) {
            return Collections.emptyMap();
        }
        try {
            return reviewLikeDao.countNewLikesPerReview(userId, since);
        } catch (final RuntimeException ignored) {
            return Collections.emptyMap();
        }
    }

    private void validateReviewAndUser(final long reviewId, final long userId) {
        if (reviewDao.findById(reviewId).isEmpty()) {
            throw new IllegalArgumentException("Review not found.");
        }
        validateUser(userId);
    }

    private void validateReplyAndUser(final long replyId, final long userId) {
        if (reviewReplyDao.findById(replyId).isEmpty()) {
            throw new IllegalArgumentException("Reply not found.");
        }
        validateUser(userId);
    }

    private void validateUser(final long userId) {
        if (userDao.findById(userId).isEmpty()) {
            throw new IllegalArgumentException("User not found.");
        }
    }
}
