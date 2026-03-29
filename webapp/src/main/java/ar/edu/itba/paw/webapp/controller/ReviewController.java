package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.services.CarService;
import ar.edu.itba.paw.services.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;

@Controller
public class ReviewController {

    private final CarService carService;
    private final ReviewService reviewService;

    @Autowired
    public ReviewController(final CarService carService, final ReviewService reviewService) {
        this.carService = carService;
        this.reviewService = reviewService;
    }

    @RequestMapping(value = "/reviews", method = RequestMethod.GET)
    public ModelAndView reviewForm() {
        final ModelAndView mav = new ModelAndView("reviews.jsp");
        mav.addObject("cars", carService.getAllCars());
        mav.addObject("reviews", reviewService.getAllReviews());
        return mav;
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
            final ModelAndView mav = new ModelAndView("reviews.jsp");
            mav.addObject("cars", carService.getAllCars());
            mav.addObject("reviews", reviewService.getAllReviews());
            mav.addObject("error", "Rating must be between 0 and 5.");
            return mav;
        }

        // Validate ownershipStatus length according to DB schema (e.g., VARCHAR(20))
        if (ownershipStatus != null && ownershipStatus.length() > 20) {
            final ModelAndView mav = new ModelAndView("reviews.jsp");
            mav.addObject("cars", carService.getAllCars());
            mav.addObject("reviews", reviewService.getAllReviews());
            mav.addObject("error", "Ownership status must be at most 20 characters long.");
            return mav;
        }
        reviewService.createReview(userId, carId, rating, title, body, ownershipStatus, modelYear, mileageKm, wouldRecommend);
        return new ModelAndView("redirect:/reviews");
    }
}