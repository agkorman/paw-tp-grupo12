package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.EmailRecipient;
import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class WeeklyDigestServiceImpl implements WeeklyDigestService {

    private static final Logger LOGGER = LoggerFactory.getLogger(WeeklyDigestServiceImpl.class);

    private static final ZoneId DIGEST_ZONE = ZoneId.of("America/Argentina/Buenos_Aires");

    private final UserService userService;
    private final CarService carService;
    private final ReviewService reviewService;
    private final ReviewLikeService reviewLikeService;
    private final ReviewReplyService reviewReplyService;
    private final CarFavoriteService carFavoriteService;
    private final CarRequestService carRequestService;
    private final EmailService emailService;

    @Autowired
    public WeeklyDigestServiceImpl(final UserService userService, final CarService carService,
                                   final ReviewService reviewService, final ReviewLikeService reviewLikeService,
                                   final ReviewReplyService reviewReplyService,
                                   final CarFavoriteService carFavoriteService,
                                   final CarRequestService carRequestService,
                                   final EmailService emailService) {
        this.userService = userService;
        this.carService = carService;
        this.reviewService = reviewService;
        this.reviewLikeService = reviewLikeService;
        this.reviewReplyService = reviewReplyService;
        this.carFavoriteService = carFavoriteService;
        this.carRequestService = carRequestService;
        this.emailService = emailService;
    }

    @Override
    @Scheduled(cron = "0 0 23 * * SUN", zone = "America/Argentina/Buenos_Aires")
    public void sendWeeklyDigest() {
        final LocalDateTime since = LocalDateTime.now(DIGEST_ZONE).minusDays(7);
        LOGGER.info("starting weekly digest since={}", since);

        sendModeratorDigest();

        final List<User> users = userService.getAllUsers();
        for (final User user : users) {
            sendUserDigest(user, since);
        }
        LOGGER.info("weekly digest completed userCount={}", users.size());
    }

    private void sendModeratorDigest() {
        final List<EmailRecipient> moderatorRecipients;
        try {
            moderatorRecipients = userService.getModeratorEmailRecipients();
        } catch (final RuntimeException e) {
            LOGGER.warn("failed to load moderator email recipients for weekly digest", e);
            return;
        }
        if (moderatorRecipients.isEmpty()) {
            return;
        }
        final int pendingCount = carRequestService.getCarRequestsByStatus(CarRequestService.STATUS_PENDING).size();
        emailService.sendWeeklyModeratorDigest(moderatorRecipients, pendingCount);
    }

    private void sendUserDigest(final User user, final LocalDateTime since) {
        final List<EmailService.ReviewActivityItem> reviewActivities = buildReviewActivities(user.getId(), since);
        final List<EmailService.FavoriteActivityItem> favoriteActivities = buildFavoriteActivities(user.getId(), since);
        emailService.sendWeeklyUserDigest(user.getEmail(), user.getUsername(), reviewActivities, favoriteActivities);
    }

    private List<EmailService.ReviewActivityItem> buildReviewActivities(final long userId, final LocalDateTime since) {
        final Map<Long, Long> likesPerReview = reviewLikeService.countNewLikesPerReview(userId, since);
        final Map<Long, Long> repliesPerReview = reviewReplyService.countNewRepliesPerReview(userId, since);
        final Set<Long> activeReviewIds = new HashSet<>();
        activeReviewIds.addAll(likesPerReview.keySet());
        activeReviewIds.addAll(repliesPerReview.keySet());
        if (activeReviewIds.isEmpty()) {
            return Collections.emptyList();
        }
        final Map<Long, Review> reviewsById = reviewService.getReviewsByIds(activeReviewIds)
                .stream()
                .collect(Collectors.toMap(Review::getId, r -> r));
        final Set<Long> carIds = reviewsById.values().stream()
                .map(Review::getCarId)
                .collect(Collectors.toSet());
        final Map<Long, String> carNamesById = carService.getCarsByIds(carIds)
                .stream()
                .collect(Collectors.toMap(Car::getId, car -> car.getBrandName() + " " + car.getModel()));
        return activeReviewIds.stream()
                .filter(reviewsById::containsKey)
                .map(reviewId -> {
                    final Review review = reviewsById.get(reviewId);
                    return new EmailService.ReviewActivityItem(
                            review.getTitle(),
                            carNamesById.getOrDefault(review.getCarId(), "un auto"),
                            likesPerReview.getOrDefault(reviewId, 0L),
                            repliesPerReview.getOrDefault(reviewId, 0L)
                    );
                })
                .collect(Collectors.toList());
    }

    private List<EmailService.FavoriteActivityItem> buildFavoriteActivities(final long userId,
                                                                            final LocalDateTime since) {
        final List<Long> favoriteCarIds = carFavoriteService.findFavoriteCarIdsByUser(userId);
        if (favoriteCarIds.isEmpty()) {
            return Collections.emptyList();
        }
        final Map<Long, String> carNamesById = carService.getCarsByIds(favoriteCarIds)
                .stream()
                .collect(Collectors.toMap(Car::getId, car -> car.getBrandName() + " " + car.getModel()));
        final Map<Long, Long> newReviewsPerCar = reviewService.getReviewsByCarIds(favoriteCarIds)
                .stream()
                .filter(review -> review.getCreatedAt() != null && review.getCreatedAt().isAfter(since))
                .collect(Collectors.groupingBy(Review::getCarId, Collectors.counting()));
        return favoriteCarIds.stream()
                .filter(carId -> newReviewsPerCar.getOrDefault(carId, 0L) > 0)
                .map(carId -> new EmailService.FavoriteActivityItem(
                        carNamesById.getOrDefault(carId, "un auto"),
                        newReviewsPerCar.get(carId)
                ))
                .collect(Collectors.toList());
    }
}
