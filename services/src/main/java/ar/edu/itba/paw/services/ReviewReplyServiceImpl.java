package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.ReviewReply;
import ar.edu.itba.paw.persistence.ReviewDao;
import ar.edu.itba.paw.persistence.ReviewReplyDao;
import ar.edu.itba.paw.persistence.UserDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ReviewReplyServiceImpl implements ReviewReplyService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ReviewReplyServiceImpl.class);

    public static final int MAX_BODY_LENGTH = 1000;

    private final ReviewReplyDao reviewReplyDao;
    private final ReviewDao reviewDao;
    private final UserDao userDao;

    @Autowired
    public ReviewReplyServiceImpl(final ReviewReplyDao reviewReplyDao, final ReviewDao reviewDao,
                                  final UserDao userDao) {
        this.reviewReplyDao = reviewReplyDao;
        this.reviewDao = reviewDao;
        this.userDao = userDao;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReviewReply> getReplyById(final long id) {
        try {
            return reviewReplyDao.findById(id);
        } catch (final RuntimeException ignored) {
            return Optional.empty();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewReply> getRepliesByIds(final Collection<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return reviewReplyDao.findByIds(ids);
        } catch (final RuntimeException ignored) {
            return Collections.emptyList();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewReply> getRepliesByReview(final long reviewId) {
        try {
            return reviewReplyDao.findByReviewId(reviewId);
        } catch (final RuntimeException ignored) {
            return Collections.emptyList();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, List<ReviewReply>> getRepliesByReviewIds(final Collection<Long> reviewIds) {
        if (reviewIds == null || reviewIds.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            return reviewReplyDao.findByReviewIds(reviewIds)
                    .stream()
                    .collect(Collectors.groupingBy(ReviewReply::getReviewId));
        } catch (final RuntimeException ignored) {
            return Collections.emptyMap();
        }
    }

    @Override
    @Transactional
    public ReviewReply createReply(final long reviewId, final long userId, final String body) {
        if (reviewDao.findById(reviewId).isEmpty()) {
            LOGGER.warn("create reply rejected: review not found id={}", reviewId);
            throw new IllegalArgumentException("Review not found.");
        }
        if (userDao.findById(userId).isEmpty()) {
            LOGGER.warn("create reply rejected: user not found id={}", userId);
            throw new IllegalArgumentException("User not found.");
        }

        final String normalizedBody = StringUtils.normalize(body);
        if (normalizedBody == null) {
            LOGGER.warn("create reply rejected: empty body reviewId={} userId={}", reviewId, userId);
            throw new IllegalArgumentException("Reply body is required.");
        }
        if (normalizedBody.length() > MAX_BODY_LENGTH) {
            LOGGER.warn("create reply rejected: body too long length={} reviewId={} userId={}",
                    normalizedBody.length(), reviewId, userId);
            throw new IllegalArgumentException("Reply body is too long.");
        }

        try {
            return reviewReplyDao.create(reviewId, userId, normalizedBody);
        } catch (final RuntimeException e) {
            LOGGER.error("failed to create review reply reviewId={} userId={}", reviewId, userId, e);
            throw new IllegalStateException("Failed to create review reply.", e);
        }
    }

    @Override
    @Transactional
    public boolean deleteReply(final long id, final long userId) {
        final ReviewReply reply = reviewReplyDao.findById(id)
                .orElseThrow(() -> {
                    LOGGER.warn("delete reply rejected: not found id={}", id);
                    return new IllegalArgumentException("Reply not found.");
                });
        if (reply.getUserId() != userId) {
            LOGGER.warn("delete reply rejected: ownership mismatch id={} requestingUserId={} ownerId={}",
                    id, userId, reply.getUserId());
            throw new IllegalArgumentException("Reply does not belong to the user.");
        }
        try {
            final boolean deleted = reviewReplyDao.delete(id);
            if (deleted) {
                LOGGER.info("deleted reply id={} userId={}", id, userId);
            }
            return deleted;
        } catch (final RuntimeException e) {
            LOGGER.error("failed to delete review reply id={} userId={}", id, userId, e);
            throw new IllegalStateException("Failed to delete review reply.", e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public Map<Long, Long> countNewRepliesPerReview(final long userId, final LocalDateTime since) {
        if (since == null) {
            return Collections.emptyMap();
        }
        try {
            return reviewReplyDao.countNewRepliesPerReview(userId, since);
        } catch (final RuntimeException ignored) {
            return Collections.emptyMap();
        }
    }

}
