package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.services.CarService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class CarController {

    private final CarService carService;

    @Autowired
    public CarController(final CarService carService) {
        this.carService = carService;
    }

    @RequestMapping(value = "/cars", method = RequestMethod.GET)
    public ModelAndView listCars() {
        final ModelAndView mav = new ModelAndView("cars");
        mav.addObject("cars", carService.getAllCars());
        return mav;
    }
}
