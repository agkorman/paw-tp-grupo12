package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.BodyType;
import ar.edu.itba.paw.model.Brand;
import ar.edu.itba.paw.persistence.BodyTypeDao;
import ar.edu.itba.paw.persistence.BrandDao;
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

    private final BrandDao brandDao;
    private final BodyTypeDao bodyTypeDao;

    @Autowired
    public SharedModelAttributesAdvice(final BrandDao brandDao, final BodyTypeDao bodyTypeDao) {
        this.brandDao = brandDao;
        this.bodyTypeDao = bodyTypeDao;
    }

    @ModelAttribute("brands")
    public List<Brand> brands() {
        return brandDao.findAll();
    }

    @ModelAttribute("bodyTypes")
    public List<BodyType> bodyTypes() {
        return bodyTypeDao.findAll();
    }

    @ModelAttribute("carForm")
    public CarForm carForm() {
        return new CarForm();
    }
}
