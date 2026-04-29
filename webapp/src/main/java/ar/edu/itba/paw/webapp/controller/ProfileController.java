package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.model.ReviewReply;
import ar.edu.itba.paw.model.ReviewStats;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.services.CarFavoriteService;
import ar.edu.itba.paw.services.CarService;
import ar.edu.itba.paw.services.ReviewLikeService;
import ar.edu.itba.paw.services.ReviewReplyService;
import ar.edu.itba.paw.services.ReviewService;
import ar.edu.itba.paw.services.UserFollowService;
import ar.edu.itba.paw.services.AdminRequestService;
import ar.edu.itba.paw.services.UserService;
import ar.edu.itba.paw.webapp.auth.AuthenticatedUser;
import ar.edu.itba.paw.webapp.exception.ResourceNotFoundException;
import ar.edu.itba.paw.webapp.form.ReviewForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
public class ProfileController {

    private final ReviewService reviewService;
    private final ReviewLikeService reviewLikeService;
    private final ReviewReplyService reviewReplyService;
    private final CarService carService;
    private final CarFavoriteService carFavoriteService;
    private final UserService userService;
    private final UserFollowService userFollowService;
    private final AdminRequestService adminRequestService;

    @Autowired
    public ProfileController(final ReviewService reviewService, final ReviewLikeService reviewLikeService,
                             final ReviewReplyService reviewReplyService,
                             final CarService carService,
                             final CarFavoriteService carFavoriteService,
                             final UserService userService, final UserFollowService userFollowService,
                             final AdminRequestService adminRequestService) {
        this.reviewService = reviewService;
        this.reviewLikeService = reviewLikeService;
        this.reviewReplyService = reviewReplyService;
        this.carService = carService;
        this.carFavoriteService = carFavoriteService;
        this.userService = userService;
        this.userFollowService = userFollowService;
        this.adminRequestService = adminRequestService;
    }

    @RequestMapping(value = "/profile", method = RequestMethod.GET)
    public ModelAndView ownProfile(@AuthenticationPrincipal final AuthenticatedUser currentUser) {
        if (currentUser == null) {
            return new ModelAndView("redirect:/login");
        }
        return profile(currentUser.getId(), currentUser);
    }

    @RequestMapping(value = "/profiles/{userId}", method = RequestMethod.GET)
    public ModelAndView publicProfile(@PathVariable("userId") final long userId,
                                      @AuthenticationPrincipal final AuthenticatedUser currentUser) {
        return profile(userId, currentUser);
    }

    @RequestMapping(value = "/profiles/{userId}/follow", method = RequestMethod.POST)
    public ModelAndView toggleFollow(@PathVariable("userId") final long userId,
                                     @AuthenticationPrincipal final AuthenticatedUser currentUser) {
        if (currentUser == null) {
            return new ModelAndView("redirect:/login");
        }
        if (currentUser.getId() == userId) {
            return new ModelAndView("redirect:/profile");
        }
        if (userService.getUserById(userId).isEmpty()) {
            throw new ResourceNotFoundException();
        }

        if (userFollowService.isFollowing(currentUser.getId(), userId)) {
            userFollowService.unfollowUser(currentUser.getId(), userId);
        } else {
            userFollowService.followUser(currentUser.getId(), userId);
        }
        return new ModelAndView("redirect:/profiles/" + userId);
    }

    private ModelAndView profile(final long profileUserId, final AuthenticatedUser currentUser) {
        final User profileUser = userService.getUserById(profileUserId)
                .orElseThrow(ResourceNotFoundException::new);
        final Long currentUserId = currentUser == null ? null : currentUser.getId();
        final boolean ownProfile = currentUserId != null && currentUserId == profileUser.getId();

        final List<Review> userReviews = reviewService.getReviewsByUser(profileUser.getId());
        final Map<Long, Car> carsById = reviewedCarsById(userReviews);
        final List<ProfileReviewCard> reviews = buildProfileReviewCards(userReviews, carsById, currentUserId);
        final List<ProfileReviewCard> likedReviewCards = buildLikedReviewCards(profileUser.getId(), carsById, currentUserId);
        final List<ProfileLikedReplyCard> likedReplyCards = buildLikedReplyCards(profileUser.getId(), carsById, currentUserId);
        final boolean followingProfile = currentUserId != null
                && !ownProfile
                && userFollowService.isFollowing(currentUserId, profileUser.getId());
        final List<Car> favoriteCars = carFavoriteService.getFavoriteCars(profileUser.getId());
        final Map<Long, ReviewStats> reviewStatsByCarId = reviewStatsByCarId(favoriteCars);

        final ModelAndView mav = new ModelAndView("profile.jsp");
        mav.addObject("profile", toProfileData(profileUser, reviews.size()));
        mav.addObject("profileReviews", reviews);
        mav.addObject("likedReviews", likedReviewCards);
        mav.addObject("likedReplies", likedReplyCards);
        mav.addObject("likedActivityCount", likedReviewCards.size() + likedReplyCards.size());
        mav.addObject("favoriteCars", favoriteCars);
        mav.addObject("reviewStatsByCarId", reviewStatsByCarId);
        mav.addObject("followingUsers", toConnections(userFollowService.getFollowing(profileUser.getId()), currentUserId));
        mav.addObject("followerUsers", toConnections(userFollowService.getFollowers(profileUser.getId()), currentUserId));
        mav.addObject("ownProfile", ownProfile);
        mav.addObject("followingProfile", followingProfile);
        mav.addObject("reviewForm", new ReviewForm());
        mav.addObject("canRequestModerator", canRequestModerator(ownProfile, profileUser));
        return mav;
    }

    private boolean canRequestModerator(final boolean ownProfile, final User profileUser) {
        if (!ownProfile) {
            return false;
        }
        final String role = profileUser.getRole();
        if (role != null && !"user".equalsIgnoreCase(role.trim())) {
            return false;
        }
        return !adminRequestService.hasPendingRequest(profileUser.getId());
    }

    private Map<Long, Car> reviewedCarsById(final List<Review> reviews) {
        final Set<Long> reviewedCarIds = reviews.stream()
                .map(Review::getCarId)
                .collect(Collectors.toSet());
        return carService.getCarsByIds(reviewedCarIds).stream()
                .collect(Collectors.toMap(Car::getId, Function.identity()));
    }

    private List<ProfileReviewCard> buildProfileReviewCards(final List<Review> reviews,
                                                            final Map<Long, Car> carsById,
                                                            final Long currentUserId) {
        final List<Long> reviewIds = reviews.stream()
                .map(Review::getId)
                .toList();
        final Map<Long, Long> likeCounts = reviewLikeService.countReviewLikesByReviewIds(reviewIds);
        final Set<Long> likedByCurrentUser = likedReviewIds(reviewIds, currentUserId);
        return reviews.stream()
                .map(review -> toProfileReviewCard(review, carsById, likeCounts, likedByCurrentUser, currentUserId))
                .toList();
    }

    private List<ProfileReviewCard> buildLikedReviewCards(final long profileUserId,
                                                          final Map<Long, Car> carsById,
                                                          final Long currentUserId) {
        final List<Review> likedReviews = orderedExistingReviews(reviewLikeService.getLikedReviewIdsByUser(profileUserId));
        final List<Long> likedReviewIds = likedReviews.stream()
                .map(Review::getId)
                .toList();
        final Map<Long, Long> likeCounts = reviewLikeService.countReviewLikesByReviewIds(likedReviewIds);
        final Set<Long> likedByCurrentUser = likedReviewIds(likedReviewIds, currentUserId);
        return likedReviews.stream()
                .map(review -> toProfileReviewCard(review, carsById, likeCounts, likedByCurrentUser, currentUserId))
                .toList();
    }

    private List<Review> orderedExistingReviews(final List<Long> reviewIds) {
        final Map<Long, Review> reviewsById = reviewService.getReviewsByIds(reviewIds).stream()
                .collect(Collectors.toMap(Review::getId, Function.identity(), (left, right) -> left));
        return reviewIds.stream()
                .map(reviewsById::get)
                .filter(Objects::nonNull)
                .toList();
    }

    private List<ProfileLikedReplyCard> buildLikedReplyCards(final long profileUserId,
                                                             final Map<Long, Car> carsById,
                                                             final Long currentUserId) {
        final List<ReviewReply> likedReplies = orderedExistingReplies(reviewLikeService.getLikedReplyIdsByUser(profileUserId));
        final Map<Long, Review> parentReviewsById = parentReviewsById(likedReplies);
        final List<Long> likedReplyIds = likedReplies.stream()
                .map(ReviewReply::getId)
                .toList();
        final Map<Long, Long> likeCounts = reviewLikeService.countReplyLikesByReplyIds(likedReplyIds);
        final Set<Long> likedByCurrentUser = currentUserId == null
                ? Set.of()
                : reviewLikeService.getLikedReplyIds(likedReplyIds, currentUserId);
        return likedReplies.stream()
                .map(reply -> toProfileLikedReplyCard(
                        reply,
                        parentReviewsById.get(reply.getReviewId()),
                        carsById,
                        likeCounts,
                        likedByCurrentUser
                ))
                .toList();
    }

    private List<ReviewReply> orderedExistingReplies(final List<Long> replyIds) {
        final Map<Long, ReviewReply> repliesById = reviewReplyService.getRepliesByIds(replyIds).stream()
                .collect(Collectors.toMap(ReviewReply::getId, Function.identity(), (left, right) -> left));
        return replyIds.stream()
                .map(repliesById::get)
                .filter(Objects::nonNull)
                .toList();
    }

    private Map<Long, Review> parentReviewsById(final List<ReviewReply> replies) {
        final List<Long> parentReviewIds = replies.stream()
                .map(ReviewReply::getReviewId)
                .distinct()
                .toList();
        return reviewService.getReviewsByIds(parentReviewIds).stream()
                .collect(Collectors.toMap(Review::getId, Function.identity(), (left, right) -> left));
    }

    private Set<Long> likedReviewIds(final List<Long> reviewIds, final Long currentUserId) {
        return currentUserId == null ? Set.of() : reviewLikeService.getLikedReviewIds(reviewIds, currentUserId);
    }

    private Map<Long, ReviewStats> reviewStatsByCarId(final List<Car> favoriteCars) {
        if (favoriteCars.isEmpty()) {
            return Map.of();
        }
        return reviewService.getReviewStatsByCarIds(favoriteCars.stream().map(Car::getId).toList()).stream()
                .collect(Collectors.toMap(ReviewStats::getCarId, Function.identity()));
    }

    private ProfileData toProfileData(final User user, final int reviewCount) {
        return new ProfileData(
                user.getId(),
                displayName(user),
                user.getEmail(),
                initials(user),
                reviewCount,
                userFollowService.countFollowing(user.getId()),
                userFollowService.countFollowers(user.getId())
        );
    }

    private ProfileReviewCard toProfileReviewCard(final Review review, final Map<Long, Car> carsById,
                                                  final Map<Long, Long> likeCounts,
                                                  final Set<Long> likedByCurrentUser,
                                                  final Long currentUserId) {
        return new ProfileReviewCard(
                review,
                carsById.get(review.getCarId()),
                likedByCurrentUser.contains(review.getId()),
                likeCounts.getOrDefault(review.getId(), 0L),
                isOwnedByCurrentUser(review, currentUserId)
        );
    }

    private ProfileLikedReplyCard toProfileLikedReplyCard(final ReviewReply reply, final Review parentReview,
                                                          final Map<Long, Car> carsById,
                                                          final Map<Long, Long> likeCounts,
                                                          final Set<Long> likedByCurrentUser) {
        final Car car = parentReview == null ? null : carsById.get(parentReview.getCarId());
        return new ProfileLikedReplyCard(
                reply,
                parentReview,
                car,
                likedByCurrentUser.contains(reply.getId()),
                likeCounts.getOrDefault(reply.getId(), 0L)
        );
    }

    private List<ProfileConnection> toConnections(final List<User> users, final Long currentUserId) {
        return users.stream()
                .map(user -> toConnection(user, currentUserId))
                .toList();
    }

    private ProfileConnection toConnection(final User user, final Long currentUserId) {
        final boolean currentUser = currentUserId != null && currentUserId == user.getId();
        final boolean following = currentUserId != null
                && !currentUser
                && userFollowService.isFollowing(currentUserId, user.getId());
        return new ProfileConnection(user.getId(), displayName(user), initials(user), following, !currentUser);
    }

    private boolean isOwnedByCurrentUser(final Review review, final Long currentUserId) {
        return currentUserId != null && review.getUserId() != null && review.getUserId().equals(currentUserId);
    }

    private String displayName(final User user) {
        if (user.getUsername() != null && !user.getUsername().trim().isEmpty()) {
            return user.getUsername().trim();
        }
        return user.getEmail();
    }

    private String initials(final User user) {
        final String value = displayName(user);
        if (value == null || value.trim().isEmpty()) {
            return "?";
        }
        final String[] parts = value.trim().split("\\s+");
        if (parts.length > 1) {
            return (parts[0].substring(0, 1) + parts[1].substring(0, 1)).toUpperCase(Locale.ROOT);
        }
        return value.substring(0, Math.min(2, value.length())).toUpperCase(Locale.ROOT);
    }

    public static final class ProfileData {
        private final long id;
        private final String name;
        private final String email;
        private final String initials;
        private final int reviewCount;
        private final long followingCount;
        private final long followerCount;

        private ProfileData(final long id, final String name, final String email, final String initials,
                            final int reviewCount,
                            final long followingCount, final long followerCount) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.initials = initials;
            this.reviewCount = reviewCount;
            this.followingCount = followingCount;
            this.followerCount = followerCount;
        }

        public long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }

        public String getInitials() {
            return initials;
        }

        public int getReviewCount() {
            return reviewCount;
        }

        public long getFollowingCount() {
            return followingCount;
        }

        public long getFollowerCount() {
            return followerCount;
        }
    }

    public static final class ProfileReviewCard {
        private final Review review;
        private final Car car;
        private final boolean liked;
        private final long likeCount;
        private final boolean ownedByCurrentUser;

        private ProfileReviewCard(final Review review, final Car car, final boolean liked, final long likeCount,
                                  final boolean ownedByCurrentUser) {
            this.review = review;
            this.car = car;
            this.liked = liked;
            this.likeCount = likeCount;
            this.ownedByCurrentUser = ownedByCurrentUser;
        }

        public Review getReview() {
            return review;
        }

        public Car getCar() {
            return car;
        }

        public String getCarName() {
            if (car == null) {
                return "Auto no disponible";
            }
            return car.getBrandName() + " " + car.getModel();
        }

        public boolean getHasCarImage() {
            return car != null && car.getHasImage();
        }

        public boolean getLiked() {
            return liked;
        }

        public long getLikeCount() {
            return likeCount;
        }

        public boolean getOwnedByCurrentUser() {
            return ownedByCurrentUser;
        }
    }

    public static final class ProfileLikedReplyCard {
        private final ReviewReply reply;
        private final Review parentReview;
        private final Car car;
        private final boolean liked;
        private final long likeCount;

        private ProfileLikedReplyCard(final ReviewReply reply, final Review parentReview, final Car car,
                                      final boolean liked, final long likeCount) {
            this.reply = reply;
            this.parentReview = parentReview;
            this.car = car;
            this.liked = liked;
            this.likeCount = likeCount;
        }

        public ReviewReply getReply() {
            return reply;
        }

        public Review getParentReview() {
            return parentReview;
        }

        public Car getCar() {
            return car;
        }

        public String getCarName() {
            if (car == null) {
                return "Auto no disponible";
            }
            return car.getBrandName() + " " + car.getModel();
        }

        public long getCarId() {
            return parentReview == null ? 0 : parentReview.getCarId();
        }

        public String getParentReviewTitle() {
            return parentReview == null ? "Review no disponible" : parentReview.getTitle();
        }

        public boolean getLiked() {
            return liked;
        }

        public long getLikeCount() {
            return likeCount;
        }
    }

    public static final class ProfileConnection {
        private final long id;
        private final String username;
        private final String initials;
        private final boolean following;
        private final boolean followable;

        private ProfileConnection(final long id, final String username, final String initials,
                                  final boolean following, final boolean followable) {
            this.id = id;
            this.username = username;
            this.initials = initials;
            this.following = following;
            this.followable = followable;
        }

        public long getId() {
            return id;
        }

        public String getUsername() {
            return username;
        }

        public String getInitials() {
            return initials;
        }

        public boolean getFollowing() {
            return following;
        }

        public boolean getFollowable() {
            return followable;
        }
    }

}
