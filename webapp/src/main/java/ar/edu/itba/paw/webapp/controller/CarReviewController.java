package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.services.CarService;
import ar.edu.itba.paw.services.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Controller
public class CarReviewController {

    private final CarService carService;
    private final ReviewService reviewService;

    @Autowired
    public CarReviewController(final CarService carService, final ReviewService reviewService) {
        this.carService = carService;
        this.reviewService = reviewService;
    }

    @RequestMapping(value = "/reviews", method = RequestMethod.GET)
    public ModelAndView reviewForm(@RequestParam(value = "carId", required = false) final Long carId) {
        if (carId == null) {
            return new ModelAndView("redirect:/cars");
        }
        return carReviewForCarId(carId, null);
    }

    private ModelAndView carReviewForCarId(final long carId, final String error) {
        final List<Car> cars = carService.getAllCars();
        Car selectedCar = null;
        for (Car car : cars) {
            if (car.getId() == carId) {
                selectedCar = car;
                break;
            }
        }

        if (selectedCar == null) {
            return new ModelAndView("redirect:/cars");
        }

        final List<Review> reviews = reviewService.getReviewsByCar(selectedCar.getId());
        final ModelAndView mav = new ModelAndView("car-review.jsp");
        mav.addObject("selectedCar", selectedCar);
        mav.addObject("reviews", reviews);
        mav.addObject("averageRating", calculateAverageRating(reviews));
        if (error != null) {
            mav.addObject("error", error);
        }
        return mav;
    }

    private BigDecimal calculateAverageRating(final List<Review> reviews) {
        if (reviews.isEmpty()) {
            return null;
        }

        BigDecimal sum = BigDecimal.ZERO;
        for (Review review : reviews) {
            if (review.getRating() != null) {
                sum = sum.add(review.getRating());
            }
        }

        return sum.divide(BigDecimal.valueOf(reviews.size()), 1, RoundingMode.HALF_UP);
    }

    @RequestMapping(value = "/reviews", method = RequestMethod.POST)
    public ModelAndView createReview(@RequestParam("userId") final long userId,
                                     @RequestParam("carId") final long carId,
                                     @RequestParam("rating") final BigDecimal rating,
                                     @RequestParam("title") final String title,
                                     @RequestParam("body") final String body,
                                     @RequestParam(value = "ownershipStatus", required = false) final String ownershipStatus,
                                     @RequestParam(value = "modelYear", required = false) final Integer modelYear,
                                     @RequestParam(value = "mileageKm", required = false) final Integer mileageKm,
                                     @RequestParam(value = "wouldRecommend", required = false) final Boolean wouldRecommend) {

        // Server-side validation to prevent DB constraint violations
        // Validate rating: must be between 0 and 5 inclusive
        if (rating == null || rating.compareTo(BigDecimal.ZERO) < 0 || rating.compareTo(BigDecimal.valueOf(5)) > 0) {
            return carReviewForCarId(carId, "Rating must be between 0 and 5.");
        }

        // Validate ownershipStatus length according to DB schema (e.g., VARCHAR(20))
        if (ownershipStatus != null && ownershipStatus.length() > 20) {
            return carReviewForCarId(carId, "Ownership status must be at most 20 characters long.");
        }
        reviewService.createReview(userId, carId, rating, title, body, ownershipStatus, modelYear, mileageKm, wouldRecommend);
        return new ModelAndView("redirect:/reviews?carId=" + carId);
    }
}