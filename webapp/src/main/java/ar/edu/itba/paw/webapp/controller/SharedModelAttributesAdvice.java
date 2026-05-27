package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.BodyType;
import ar.edu.itba.paw.model.Brand;
import ar.edu.itba.paw.model.ReviewTag;
import ar.edu.itba.paw.services.BodyTypeService;
import ar.edu.itba.paw.services.BrandService;
import ar.edu.itba.paw.services.ReviewTagService;
import ar.edu.itba.paw.webapp.form.CarForm;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.util.List;
import java.util.Map;

@ControllerAdvice(assignableTypes = {
        AdminController.class,
        CarController.class,
        CarReviewController.class,
        RecommendationController.class
})
public class SharedModelAttributesAdvice {

    private final BrandService brandService;
    private final BodyTypeService bodyTypeService;
    private final ReviewTagService reviewTagService;

    @Autowired
    public SharedModelAttributesAdvice(final BrandService brandService, final BodyTypeService bodyTypeService,
                                       final ReviewTagService reviewTagService) {
        this.brandService = brandService;
        this.bodyTypeService = bodyTypeService;
        this.reviewTagService = reviewTagService;
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

    @ModelAttribute("reviewTagsBySentiment")
    public Map<String, List<ReviewTag>> reviewTagsBySentiment() {
        return reviewTagService.getAllGroupedBySentiment();
    }

    @ModelAttribute("reviewTagsByCode")
    public Map<String, ReviewTag> reviewTagsByCode() {
        return reviewTagService.getAllByCode();
    }
}
