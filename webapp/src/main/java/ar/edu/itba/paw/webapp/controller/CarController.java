package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.ReviewStats;
import ar.edu.itba.paw.persistence.BodyTypeDao;
import ar.edu.itba.paw.persistence.BrandDao;
import ar.edu.itba.paw.services.CarService;
import ar.edu.itba.paw.services.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Controller
public class CarController {

    private final CarService carService;
    private final BrandDao brandDao;
    private final BodyTypeDao bodyTypeDao;
    private final ReviewService reviewService;

    @Autowired
    public CarController(final CarService carService, final BrandDao brandDao, final BodyTypeDao bodyTypeDao,
                         final ReviewService reviewService) {
        this.carService = carService;
        this.brandDao = brandDao;
        this.bodyTypeDao = bodyTypeDao;
        this.reviewService = reviewService;
    }

    @RequestMapping(value = "/", method = RequestMethod.GET)
    public ModelAndView home() {
        return new ModelAndView("redirect:/cars");
    }

    @RequestMapping(value = "/cars", method = RequestMethod.GET)
    public ModelAndView listCars(
            @RequestParam(value = "brand", required = false) final String brand,
            @RequestParam(value = "bodyType", required = false) final String bodyType) {

        final String brandFilter = blankToNull(brand);
        final String bodyTypeFilter = blankToNull(bodyType);

        final List<Car> cars;
        if (brandFilter != null && bodyTypeFilter != null) {
            cars = carService.getCarsByBrandAndBodyType(brandFilter, bodyTypeFilter);
        } else if (brandFilter != null) {
            cars = carService.getCarsByBrand(brandFilter);
        } else if (bodyTypeFilter != null) {
            cars = carService.getCarsByBodyType(bodyTypeFilter);
        } else {
            cars = carService.getAllCars();
        }

        final Map<Long, ReviewStats> reviewStatsByCarId;
        if (cars.isEmpty()) {
            reviewStatsByCarId = Collections.emptyMap();
        } else {
            reviewStatsByCarId = reviewService.getReviewStatsByCarIds(
                            cars.stream().map(Car::getId).collect(Collectors.toList()))
                    .stream()
                    .collect(Collectors.toMap(ReviewStats::getCarId, Function.identity()));
        }

        final ModelAndView mav = new ModelAndView("cars.jsp");
        mav.addObject("cars", cars);
        mav.addObject("reviewStatsByCarId", reviewStatsByCarId);
        mav.addObject("brands", brandDao.findAll());
        mav.addObject("bodyTypes", bodyTypeDao.findAll());
        mav.addObject("selectedBrand", brandFilter);
        mav.addObject("selectedBodyType", bodyTypeFilter);
        return mav;
    }

    private static String blankToNull(final String s) {
        if (s == null) {
            return null;
        }
        final String t = s.trim();
        return t.isEmpty() ? null : t;
    }
}
