package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.services.CarService;
import ar.edu.itba.paw.services.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.math.BigDecimal;
import java.util.List;

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
    public ModelAndView reviewForm(@RequestParam(value = "carId", required = false) final Long carId) {
        final ModelAndView mav = new ModelAndView("reviews.jsp");
        final List<Car> cars = carService.getAllCars();
        Long selectedCarId = null;
        if (carId != null) {
            for (Car car : cars) {
                if (car.getId() == carId) {
                    selectedCarId = carId;
                    break;
                }
            }
        }
        mav.addObject("cars", cars);
        mav.addObject("selectedCarId", selectedCarId);
        mav.addObject("selectionLocked", selectedCarId != null);
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
        reviewService.createReview(userId, carId, rating, title, body, ownershipStatus, modelYear, mileageKm, wouldRecommend);
        return new ModelAndView("redirect:/reviews");
    }
}