package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.Review;
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
        final Map<Long, Car> carsById = carService.getAllCars()
                .stream()
                .collect(Collectors.toMap(Car::getId, Function.identity()));
        final List<ProfileReviewCard> reviews = reviewService.getAllReviews()
                .stream()
                .map(review -> new ProfileReviewCard(review, carsById.get(review.getCarId())))
                .toList();

        final ModelAndView mav = new ModelAndView("profile.jsp");
        mav.addObject("profile", new ProfileData(PROFILE_NAME, PROFILE_EMAIL, reviews.size(),
                FOLLOWING_COUNT, FOLLOWER_COUNT));
        mav.addObject("profileReviews", reviews);
        mav.addObject("ownProfile", true);
        mav.addObject("followingProfile", false);
        return mav;
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

        private ProfileReviewCard(final Review review, final Car car) {
            this.review = review;
            this.car = car;
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
    }
}
