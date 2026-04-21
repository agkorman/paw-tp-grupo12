package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.model.ReviewReply;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.services.CarService;
import ar.edu.itba.paw.services.ReviewLikeService;
import ar.edu.itba.paw.services.ReviewReplyService;
import ar.edu.itba.paw.services.ReviewService;
import ar.edu.itba.paw.services.UserFollowService;
import ar.edu.itba.paw.services.UserService;
import ar.edu.itba.paw.webapp.auth.AuthenticatedUser;
import ar.edu.itba.paw.webapp.form.ReviewForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
public class ProfileController {

    private final ReviewService reviewService;
    private final ReviewLikeService reviewLikeService;
    private final ReviewReplyService reviewReplyService;
    private final CarService carService;
    private final UserService userService;
    private final UserFollowService userFollowService;

    @Autowired
    public ProfileController(final ReviewService reviewService, final ReviewLikeService reviewLikeService,
                             final ReviewReplyService reviewReplyService,
                             final CarService carService,
                             final UserService userService, final UserFollowService userFollowService) {
        this.reviewService = reviewService;
        this.reviewLikeService = reviewLikeService;
        this.reviewReplyService = reviewReplyService;
        this.carService = carService;
        this.userService = userService;
        this.userFollowService = userFollowService;
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
            throw new ProfileNotFoundException();
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
                .orElseThrow(ProfileNotFoundException::new);
        final Long currentUserId = currentUser == null ? null : currentUser.getId();
        final boolean ownProfile = currentUserId != null && currentUserId == profileUser.getId();

        final List<Car> cars = carService.getAllCars();
        final Map<Long, Car> carsById = cars
                .stream()
                .collect(Collectors.toMap(Car::getId, Function.identity()));
        final List<Review> profileUserReviews = reviewService.getReviewsByUser(profileUser.getId());
        final List<Long> profileReviewIds = profileUserReviews.stream()
                .map(Review::getId)
                .toList();
        final Map<Long, Long> profileReviewLikeCounts = reviewLikeService.countReviewLikesByReviewIds(profileReviewIds);
        final Set<Long> currentUserLikedReviewIds = currentUserId == null
                ? Set.of()
                : reviewLikeService.getLikedReviewIds(profileReviewIds, currentUserId);
        final List<ProfileReviewCard> reviews = profileUserReviews
                .stream()
                .map(review -> toProfileReviewCard(
                        review,
                        carsById,
                        profileReviewLikeCounts,
                        currentUserLikedReviewIds,
                        currentUserId
                ))
                .toList();
        final List<Review> likedReviews = reviewLikeService.getLikedReviewIdsByUser(profileUser.getId())
                .stream()
                .map(reviewService::getReviewById)
                .flatMap(Optional::stream)
                .toList();
        final List<Long> likedReviewIds = likedReviews.stream()
                .map(Review::getId)
                .toList();
        final Map<Long, Long> likedReviewLikeCounts = reviewLikeService.countReviewLikesByReviewIds(likedReviewIds);
        final Set<Long> likedByCurrentUserInLikedReviews = currentUserId == null
                ? Set.of()
                : reviewLikeService.getLikedReviewIds(likedReviewIds, currentUserId);
        final List<ProfileReviewCard> likedReviewCards = likedReviews
                .stream()
                .map(review -> toProfileReviewCard(
                        review,
                        carsById,
                        likedReviewLikeCounts,
                        likedByCurrentUserInLikedReviews,
                        currentUserId
                ))
                .toList();
        final List<ReviewReply> likedReplies = reviewLikeService.getLikedReplyIdsByUser(profileUser.getId())
                .stream()
                .map(reviewReplyService::getReplyById)
                .flatMap(Optional::stream)
                .toList();
        final Map<Long, Review> parentReviewsById = likedReplies.stream()
                .map(ReviewReply::getReviewId)
                .distinct()
                .map(reviewService::getReviewById)
                .flatMap(Optional::stream)
                .collect(Collectors.toMap(Review::getId, Function.identity()));
        final List<Long> likedReplyIds = likedReplies.stream()
                .map(ReviewReply::getId)
                .toList();
        final Map<Long, Long> likedReplyLikeCounts = reviewLikeService.countReplyLikesByReplyIds(likedReplyIds);
        final Set<Long> likedByCurrentUserInLikedReplies = currentUserId == null
                ? Set.of()
                : reviewLikeService.getLikedReplyIds(likedReplyIds, currentUserId);
        final List<ProfileLikedReplyCard> likedReplyCards = likedReplies
                .stream()
                .map(reply -> toProfileLikedReplyCard(
                        reply,
                        parentReviewsById.get(reply.getReviewId()),
                        carsById,
                        likedReplyLikeCounts,
                        likedByCurrentUserInLikedReplies
                ))
                .toList();
        final boolean followingProfile = currentUserId != null
                && !ownProfile
                && userFollowService.isFollowing(currentUserId, profileUser.getId());

        final ModelAndView mav = new ModelAndView("profile.jsp");
        mav.addObject("profile", new ProfileData(
                profileUser.getId(),
                displayName(profileUser),
                profileUser.getEmail(),
                initials(profileUser),
                reviews.size(),
                userFollowService.countFollowing(profileUser.getId()),
                userFollowService.countFollowers(profileUser.getId())
        ));
        mav.addObject("profileReviews", reviews);
        mav.addObject("favoriteCars", List.of());
        mav.addObject("reviewStatsByCarId", Map.of());
        mav.addObject("likedReviews", likedReviewCards);
        mav.addObject("likedReplies", likedReplyCards);
        mav.addObject("likedActivityCount", likedReviewCards.size() + likedReplyCards.size());
        mav.addObject("followingUsers", toConnections(userFollowService.getFollowing(profileUser.getId()), currentUserId));
        mav.addObject("followerUsers", toConnections(userFollowService.getFollowers(profileUser.getId()), currentUserId));
        mav.addObject("ownProfile", ownProfile);
        mav.addObject("followingProfile", followingProfile);
        mav.addObject("reviewForm", new ReviewForm());
        return mav;
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

    @ResponseStatus(HttpStatus.NOT_FOUND)
    private static final class ProfileNotFoundException extends RuntimeException {
    }
}
