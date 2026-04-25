package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.BodyType;
import ar.edu.itba.paw.model.Brand;
import ar.edu.itba.paw.services.BodyTypeService;
import ar.edu.itba.paw.services.BrandService;
import ar.edu.itba.paw.webapp.form.CarForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;

@ControllerAdvice(assignableTypes = {
        AdminController.class,
        CarController.class,
        CarReviewController.class
})
public class SharedModelAttributesAdvice {

    private final BrandService brandService;
    private final BodyTypeService bodyTypeService;

    @Autowired
    public SharedModelAttributesAdvice(final BrandService brandService, final BodyTypeService bodyTypeService) {
        this.brandService = brandService;
        this.bodyTypeService = bodyTypeService;
    }

    @ModelAttribute("brands")
    public List<Brand> brands() {
        return brandService.findAll();
    }

    @ModelAttribute("bodyTypes")
    public List<BodyType> bodyTypes() {
        return bodyTypeService.findAll();
    }

    @ModelAttribute("carForm")
    public CarForm carForm() {
        return new CarForm();
    }
}
