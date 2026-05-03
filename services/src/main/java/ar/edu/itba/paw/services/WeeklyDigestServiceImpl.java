package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

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
        final List<String> moderatorEmails = userService.getModeratorsEmails();
        if (moderatorEmails.isEmpty()) {
            return;
        }
        final int pendingCount = carRequestService.getCarRequestsByStatus(CarRequestService.STATUS_PENDING).size();
        emailService.sendWeeklyModeratorDigest(moderatorEmails, pendingCount);
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

        return activeReviewIds.stream()
                .map(reviewId -> toReviewActivityItem(reviewId, likesPerReview, repliesPerReview))
                .flatMap(Optional::stream)
                .toList();
    }

    private Optional<EmailService.ReviewActivityItem> toReviewActivityItem(final long reviewId,
                                                                           final Map<Long, Long> likesPerReview,
                                                                           final Map<Long, Long> repliesPerReview) {
        return reviewService.getReviewById(reviewId)
                .map(review -> new EmailService.ReviewActivityItem(
                        review.getTitle(),
                        carNameOrFallback(review.getCarId()),
                        likesPerReview.getOrDefault(reviewId, 0L),
                        repliesPerReview.getOrDefault(reviewId, 0L)
                ));
    }

    private List<EmailService.FavoriteActivityItem> buildFavoriteActivities(final long userId,
                                                                            final LocalDateTime since) {
        return carFavoriteService.findFavoriteCarIdsByUser(userId).stream()
                .map(carId -> toFavoriteActivityItem(carId, since))
                .flatMap(Optional::stream)
                .toList();
    }

    private Optional<EmailService.FavoriteActivityItem> toFavoriteActivityItem(final long carId,
                                                                               final LocalDateTime since) {
        final long newReviews = reviewService.getReviewsByCar(carId).stream()
                .filter(review -> review.getCreatedAt() != null && review.getCreatedAt().isAfter(since))
                .count();
        if (newReviews == 0) {
            return Optional.empty();
        }
        return Optional.of(new EmailService.FavoriteActivityItem(carNameOrFallback(carId), newReviews));
    }

    private String carNameOrFallback(final long carId) {
        return carService.getCarById(carId)
                .map(car -> car.getBrandName() + " " + car.getModel())
                .orElse("un auto");
    }
}
