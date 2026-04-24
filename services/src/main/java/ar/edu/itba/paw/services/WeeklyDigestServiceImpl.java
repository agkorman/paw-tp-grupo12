package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
public class WeeklyDigestServiceImpl implements WeeklyDigestService {

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

        sendModeratorDigest();

        for (final User user : userService.getAllUsers()) {
            sendUserDigest(user, since);
        }
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
        final Map<Long, Long> likesPerReview = reviewLikeService.countNewLikesPerReview(user.getId(), since);
        final Map<Long, Long> repliesPerReview = reviewReplyService.countNewRepliesPerReview(user.getId(), since);

        final Set<Long> activeReviewIds = new HashSet<>();
        activeReviewIds.addAll(likesPerReview.keySet());
        activeReviewIds.addAll(repliesPerReview.keySet());

        final List<EmailService.ReviewActivityItem> reviewActivities = new ArrayList<>();
        for (final long reviewId : activeReviewIds) {
            final Review review = reviewService.getReviewById(reviewId).orElse(null);
            if (review == null) {
                continue;
            }
            final Car car = carService.getCarById(review.getCarId()).orElse(null);
            final String carName = car != null ? car.getBrandName() + " " + car.getModel() : "un auto";
            reviewActivities.add(new EmailService.ReviewActivityItem(
                    review.getTitle(),
                    carName,
                    likesPerReview.getOrDefault(reviewId, 0L),
                    repliesPerReview.getOrDefault(reviewId, 0L)
            ));
        }

        final List<EmailService.FavoriteActivityItem> favoriteActivities = new ArrayList<>();
        for (final long carId : carFavoriteService.findFavoriteCarIdsByUser(user.getId())) {
            final long newReviews = reviewService.getReviewsByCar(carId).stream()
                    .filter(r -> r.getCreatedAt() != null && r.getCreatedAt().isAfter(since))
                    .count();
            if (newReviews == 0) {
                continue;
            }
            final Car car = carService.getCarById(carId).orElse(null);
            final String carName = car != null ? car.getBrandName() + " " + car.getModel() : "un auto";
            favoriteActivities.add(new EmailService.FavoriteActivityItem(carName, newReviews));
        }

        emailService.sendWeeklyUserDigest(user.getEmail(), user.getUsername(), reviewActivities, favoriteActivities);
    }
}
