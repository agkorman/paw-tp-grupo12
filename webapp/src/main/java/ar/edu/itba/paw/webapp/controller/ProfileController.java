package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.model.ReviewStats;
import ar.edu.itba.paw.services.CarService;
import ar.edu.itba.paw.services.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
public class ProfileController {

    private static final String PROFILE_NAME = "Julian Rossi";
    private static final String PROFILE_EMAIL = "julian.rossi@lapostaautos.com";
    private static final long FOLLOWING_COUNT = 9;
    private static final long FOLLOWER_COUNT = 152;

    private final ReviewService reviewService;
    private final CarService carService;

    @Autowired
    public ProfileController(final ReviewService reviewService, final CarService carService) {
        this.reviewService = reviewService;
        this.carService = carService;
    }

    @RequestMapping(value = "/profile", method = RequestMethod.GET)
    public ModelAndView profile() {
        final List<Car> cars = carService.getAllCars();
        final Map<Long, Car> carsById = cars
                .stream()
                .collect(Collectors.toMap(Car::getId, Function.identity()));
        final List<Car> favoriteCars = cars.stream()
                .filter(car -> car.getId() % 2 == 0)
                .limit(4)
                .toList();
        final Map<Long, ReviewStats> reviewStatsByCarId = reviewService.getReviewStatsByCarIds(
                        favoriteCars.stream().map(Car::getId).toList()
                )
                .stream()
                .collect(Collectors.toMap(ReviewStats::getCarId, Function.identity()));
        final List<ProfileReviewCard> reviews = reviewService.getAllReviews()
                .stream()
                .map(review -> new ProfileReviewCard(review, carsById.get(review.getCarId()), isDemoLiked(review), demoLikeCount(review)))
                .toList();
        final List<ProfileReviewCard> likedReviews = reviews.stream()
                .filter(ProfileReviewCard::getLiked)
                .toList();

        final ModelAndView mav = new ModelAndView("profile.jsp");
        mav.addObject("profile", new ProfileData(PROFILE_NAME, PROFILE_EMAIL, reviews.size(),
                FOLLOWING_COUNT, FOLLOWER_COUNT));
        mav.addObject("profileReviews", reviews);
        mav.addObject("favoriteCars", favoriteCars);
        mav.addObject("reviewStatsByCarId", reviewStatsByCarId);
        mav.addObject("likedReviews", likedReviews);
        mav.addObject("followingUsers", demoFollowingUsers());
        mav.addObject("followerUsers", demoFollowerUsers());
        mav.addObject("ownProfile", true);
        mav.addObject("followingProfile", false);
        return mav;
    }

    private boolean isDemoLiked(final Review review) {
        return review.getId() % 2 == 0;
    }

    private long demoLikeCount(final Review review) {
        return review.getId() + 3;
    }

    private List<ProfileConnection> demoFollowingUsers() {
        return List.of(
                new ProfileConnection("usuario1", "U1", true),
                new ProfileConnection("usuario2", "U2", true),
                new ProfileConnection("usuario3", "U3", true),
                new ProfileConnection("usuario4", "U4", true),
                new ProfileConnection("usuario5", "U5", true),
                new ProfileConnection("usuario6", "U6", false),
                new ProfileConnection("usuario7", "U7", true),
                new ProfileConnection("usuario8", "U8", false),
                new ProfileConnection("usuario9", "U9", true),
                new ProfileConnection("usuario10", "U10", true),
                new ProfileConnection("usuario11", "U11", false),
                new ProfileConnection("usuario12", "U12", true)
        );
    }

    private List<ProfileConnection> demoFollowerUsers() {
        return List.of(
                new ProfileConnection("usuario13", "U13", false),
                new ProfileConnection("usuario14", "U14", true),
                new ProfileConnection("usuario15", "U15", false),
                new ProfileConnection("usuario16", "U16", true),
                new ProfileConnection("usuario17", "U17", false),
                new ProfileConnection("usuario18", "U18", true),
                new ProfileConnection("usuario19", "U19", false),
                new ProfileConnection("usuario20", "U20", false),
                new ProfileConnection("usuario21", "U21", true),
                new ProfileConnection("usuario22", "U22", false),
                new ProfileConnection("usuario23", "U23", true),
                new ProfileConnection("usuario24", "U24", false)
        );
    }

    public static final class ProfileData {
        private final String name;
        private final String email;
        private final int reviewCount;
        private final long followingCount;
        private final long followerCount;

        private ProfileData(final String name, final String email, final int reviewCount,
                            final long followingCount, final long followerCount) {
            this.name = name;
            this.email = email;
            this.reviewCount = reviewCount;
            this.followingCount = followingCount;
            this.followerCount = followerCount;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
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

        private ProfileReviewCard(final Review review, final Car car, final boolean liked, final long likeCount) {
            this.review = review;
            this.car = car;
            this.liked = liked;
            this.likeCount = likeCount;
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
    }

    public static final class ProfileConnection {
        private final String username;
        private final String initials;
        private final boolean following;

        private ProfileConnection(final String username, final String initials, final boolean following) {
            this.username = username;
            this.initials = initials;
            this.following = following;
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
    }
}
