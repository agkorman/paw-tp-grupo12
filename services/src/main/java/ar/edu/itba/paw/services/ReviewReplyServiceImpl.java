package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.model.ReviewReply;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.persistence.ReviewDao;
import ar.edu.itba.paw.persistence.ReviewReplyDao;
import ar.edu.itba.paw.persistence.UserDao;
import ar.edu.itba.paw.services.exception.InvalidServiceInputException;
import ar.edu.itba.paw.services.exception.ReviewNotFoundException;
import ar.edu.itba.paw.services.exception.ReviewReplyNotFoundException;
import ar.edu.itba.paw.services.exception.ReviewReplyOwnershipException;
import ar.edu.itba.paw.services.exception.ServiceOperationException;
import ar.edu.itba.paw.services.exception.UserNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
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
    private final CarService carService;
    private final EmailService emailService;

    @Autowired
    public ReviewReplyServiceImpl(final ReviewReplyDao reviewReplyDao, final ReviewDao reviewDao,
                                  final UserDao userDao, final CarService carService,
                                  final EmailService emailService) {
        this.reviewReplyDao = reviewReplyDao;
        this.reviewDao = reviewDao;
        this.userDao = userDao;
        this.carService = carService;
        this.emailService = emailService;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ReviewReply> getReplyById(final long id) {
        try {
            return reviewReplyDao.findById(id);
        } catch (final DataAccessException e) {
            LOGGER.warn("get reply by id failed id={}", id, e);
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
        } catch (final DataAccessException e) {
            LOGGER.warn("get replies by ids failed count={}", ids.size(), e);
            return Collections.emptyList();
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<ReviewReply> getRepliesByReview(final long reviewId) {
        try {
            return reviewReplyDao.findByReviewId(reviewId);
        } catch (final DataAccessException e) {
            LOGGER.warn("get replies by review failed reviewId={}", reviewId, e);
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
        } catch (final DataAccessException e) {
            LOGGER.warn("get replies by review ids failed count={}", reviewIds.size(), e);
            return Collections.emptyMap();
        }
    }

    @Override
    @Transactional
    public ReviewReply createReply(final long reviewId, final long userId, final String body) {
        try {
            if (reviewDao.findById(reviewId).isEmpty()) {
                LOGGER.warn("create reply rejected: review not found id={}", reviewId);
                throw new ReviewNotFoundException(reviewId);
            }
            if (userDao.findById(userId).isEmpty()) {
                LOGGER.warn("create reply rejected: user not found id={}", userId);
                throw new UserNotFoundException(userId);
            }

            final String normalizedBody = StringUtils.normalize(body);
            if (normalizedBody == null) {
                LOGGER.warn("create reply rejected: empty body reviewId={} userId={}", reviewId, userId);
                throw new InvalidServiceInputException("Reply body is required.");
            }
            if (normalizedBody.length() > MAX_BODY_LENGTH) {
                LOGGER.warn("create reply rejected: body too long length={} reviewId={} userId={}",
                        normalizedBody.length(), reviewId, userId);
                throw new InvalidServiceInputException("Reply body is too long.");
            }

            return reviewReplyDao.create(reviewId, userId, normalizedBody);
        } catch (final DataAccessException e) {
            LOGGER.error("failed to create review reply reviewId={} userId={}", reviewId, userId, e);
            throw new ServiceOperationException("Failed to create review reply.", e);
        }
    }

    @Override
    @Transactional
    public boolean updateReply(final long id, final long userId, final String body) {
        try {
            final ReviewReply reply = reviewReplyDao.findById(id)
                    .orElseThrow(() -> {
                        LOGGER.warn("update reply rejected: not found id={}", id);
                        return new ReviewReplyNotFoundException(id);
                    });
            if (reply.getUserId() != userId) {
                LOGGER.warn("update reply rejected: ownership mismatch id={} requestingUserId={} ownerId={}",
                        id, userId, reply.getUserId());
                throw new ReviewReplyOwnershipException(id, userId);
            }

            final String normalizedBody = StringUtils.normalize(body);
            if (normalizedBody == null) {
                LOGGER.warn("update reply rejected: empty body id={} userId={}", id, userId);
                throw new InvalidServiceInputException("Reply body is required.");
            }
            if (normalizedBody.length() > MAX_BODY_LENGTH) {
                LOGGER.warn("update reply rejected: body too long length={} id={} userId={}",
                        normalizedBody.length(), id, userId);
                throw new InvalidServiceInputException("Reply body is too long.");
            }

            final boolean updated = reviewReplyDao.update(id, normalizedBody);
            if (updated) {
                LOGGER.info("updated reply id={} userId={}", id, userId);
            }
            return updated;
        } catch (final DataAccessException e) {
            LOGGER.error("failed to update review reply id={} userId={}", id, userId, e);
            throw new ServiceOperationException("Failed to update review reply.", e);
        }
    }

    @Override
    @Transactional
    public boolean deleteReply(final long id, final long userId) {
        try {
            final ReviewReply reply = reviewReplyDao.findById(id)
                    .orElseThrow(() -> {
                        LOGGER.warn("delete reply rejected: not found id={}", id);
                        return new ReviewReplyNotFoundException(id);
                    });
            if (reply.getUserId() != userId) {
                LOGGER.warn("delete reply rejected: ownership mismatch id={} requestingUserId={} ownerId={}",
                        id, userId, reply.getUserId());
                throw new ReviewReplyOwnershipException(id, userId);
            }

            final boolean deleted = reviewReplyDao.delete(id);
            if (deleted) {
                LOGGER.info("deleted reply id={} userId={}", id, userId);
            }
            return deleted;
        } catch (final DataAccessException e) {
            LOGGER.error("failed to delete review reply id={} userId={}", id, userId, e);
            throw new ServiceOperationException("Failed to delete review reply.", e);
        }
    }

    @Override
    @Transactional
    public boolean hideReply(final long replyId, final String reason) {
        try {
            final ReviewReply reply = reviewReplyDao.findById(replyId).orElse(null);
            if (reply == null) {
                return false;
            }
            final Review review = reviewDao.findById(reply.getReviewId()).orElse(null);
            if (review == null) {
                return false;
            }
            final String carName = resolveCarDisplayName(review.getCarId());
            final String recipientEmail = userDao.findById(reply.getUserId())
                    .map(User::getEmail)
                    .map(ReviewReplyServiceImpl::normalizeEmail)
                    .orElse(null);
            final boolean deleted = reviewReplyDao.delete(replyId);
            if (!deleted) {
                return false;
            }
            LOGGER.info("deleted reply id={} (hidden by moderator)", replyId);
            if (recipientEmail != null) {
                emailService.sendReviewHiddenNotification(recipientEmail, review.getTitle(), carName, reason);
            }
            return true;
        } catch (final DataAccessException e) {
            LOGGER.error("failed to hide review reply id={}", replyId, e);
            throw new ServiceOperationException("Failed to hide review reply.", e);
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
        } catch (final DataAccessException e) {
            LOGGER.warn("count new replies per review failed userId={}", userId, e);
            return Collections.emptyMap();
        }
    }

    private String resolveCarDisplayName(final long carId) {
        return carService.getCarById(carId)
                .map(ReviewReplyServiceImpl::formatCarDisplayName)
                .orElse(null);
    }

    private static String formatCarDisplayName(final Car car) {
        final String brand = car.getBrandName();
        final String model = car.getModel();
        if (brand == null && model == null) {
            return null;
        }
        if (brand == null) {
            return model;
        }
        if (model == null) {
            return brand;
        }
        return brand + " " + model;
    }

    private static String normalizeEmail(final String email) {
        if (email == null || email.isBlank()) {
            return null;
        }
        final String trimmed = email.trim().toLowerCase(Locale.ROOT);
        return trimmed.isEmpty() ? null : trimmed;
    }

}
