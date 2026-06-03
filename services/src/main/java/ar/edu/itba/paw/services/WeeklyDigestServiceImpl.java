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
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
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

        final Map<Long, List<EmailService.ReviewActivityItem>> reviewActivityByUser =
                buildReviewActivityByUser(since);
        final Map<Long, List<EmailService.FavoriteActivityItem>> favoriteActivityByUser =
                buildFavoriteActivityByUser(since);

        for (final User user : users) {
            final List<EmailService.ReviewActivityItem> reviewActivities =
                    reviewActivityByUser.getOrDefault(user.getId(), Collections.emptyList());
            final List<EmailService.FavoriteActivityItem> favoriteActivities =
                    favoriteActivityByUser.getOrDefault(user.getId(), Collections.emptyList());
            emailService.sendWeeklyUserDigest(user.getEmail(), user.getUsername(), reviewActivities, favoriteActivities);
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
        final long pendingCount = carRequestService.countCarRequestsByStatus(CarRequestService.STATUS_PENDING);
        emailService.sendWeeklyModeratorDigest(moderatorRecipients, pendingCount);
    }

    private Map<Long, List<EmailService.ReviewActivityItem>> buildReviewActivityByUser(final LocalDateTime since) {
        final Map<Long, Long> likesPerReview = reviewLikeService.countNewLikesPerReviewSince(since);
        final Map<Long, Long> repliesPerReview = reviewReplyService.countNewRepliesPerReviewSince(since);
        final Set<Long> activeReviewIds = new HashSet<>();
        activeReviewIds.addAll(likesPerReview.keySet());
        activeReviewIds.addAll(repliesPerReview.keySet());
        if (activeReviewIds.isEmpty()) {
            return Collections.emptyMap();
        }
        final List<Review> reviews = reviewService.getReviewsByIds(activeReviewIds);
        final Set<Long> carIds = reviews.stream()
                .map(Review::getCarId)
                .collect(Collectors.toSet());
        final Map<Long, String> carNamesById = carService.getCarsByIds(carIds)
                .stream()
                .collect(Collectors.toMap(Car::getId, car -> car.getBrandName() + " " + car.getModel()));

        final Map<Long, List<EmailService.ReviewActivityItem>> activityByUser = new HashMap<>();
        for (final Review review : reviews) {
            final Long ownerId = review.getUserId();
            if (ownerId == null) {
                continue;
            }
            final EmailService.ReviewActivityItem item = new EmailService.ReviewActivityItem(
                    review.getTitle(),
                    carNamesById.getOrDefault(review.getCarId(), "un auto"),
                    likesPerReview.getOrDefault(review.getId(), 0L),
                    repliesPerReview.getOrDefault(review.getId(), 0L)
            );
            activityByUser.computeIfAbsent(ownerId, key -> new ArrayList<>()).add(item);
        }
        return activityByUser;
    }

    private Map<Long, List<EmailService.FavoriteActivityItem>> buildFavoriteActivityByUser(final LocalDateTime since) {
        final Map<Long, List<Long>> favoritesByUser = carFavoriteService.findAllFavoriteCarIdsByUser();
        if (favoritesByUser.isEmpty()) {
            return Collections.emptyMap();
        }
        final Set<Long> allFavoriteCarIds = favoritesByUser.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toSet());
        final Map<Long, String> carNamesById = carService.getCarsByIds(allFavoriteCarIds)
                .stream()
                .collect(Collectors.toMap(Car::getId, car -> car.getBrandName() + " " + car.getModel()));
        final Map<Long, Long> newReviewsPerCar = reviewService.countNewReviewsByCarIds(allFavoriteCarIds, since);

        final Map<Long, List<EmailService.FavoriteActivityItem>> activityByUser = new HashMap<>();
        for (final Map.Entry<Long, List<Long>> entry : favoritesByUser.entrySet()) {
            final List<EmailService.FavoriteActivityItem> items = entry.getValue().stream()
                    .filter(carId -> newReviewsPerCar.getOrDefault(carId, 0L) > 0)
                    .map(carId -> new EmailService.FavoriteActivityItem(
                            carNamesById.getOrDefault(carId, "un auto"),
                            newReviewsPerCar.get(carId)
                    ))
                    .collect(Collectors.toList());
            if (!items.isEmpty()) {
                activityByUser.put(entry.getKey(), items);
            }
        }
        return activityByUser;
    }
}
