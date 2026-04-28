package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.services.BodyTypeRequestService;
import ar.edu.itba.paw.services.BrandRequestService;
import ar.edu.itba.paw.webapp.auth.AuthenticatedUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.net.URI;

@Controller
public class CatalogRequestController {

    private final BrandRequestService brandRequestService;
    private final BodyTypeRequestService bodyTypeRequestService;

    @Autowired
    public CatalogRequestController(final BrandRequestService brandRequestService,
                                    final BodyTypeRequestService bodyTypeRequestService) {
        this.brandRequestService = brandRequestService;
        this.bodyTypeRequestService = bodyTypeRequestService;
    }

    @InitBinder
    public void initBinder(final WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @RequestMapping(value = "/brand-requests", method = RequestMethod.POST)
    public ModelAndView requestBrand(@RequestParam(value = "name", required = false) final String name,
                                     @RequestHeader(value = "Referer", required = false) final String referer,
                                     @AuthenticationPrincipal final AuthenticatedUser currentUser) {
        if (currentUser == null) {
            return new ModelAndView("redirect:/login");
        }
        if (name == null || name.isBlank()) {
            return redirectBack(referer);
        }
        brandRequestService.createPendingRequest(currentUser.getId(), currentUser.getEmail(), name);
        return redirectBack(referer);
    }

    @RequestMapping(value = "/body-type-requests", method = RequestMethod.POST)
    public ModelAndView requestBodyType(@RequestParam(value = "name", required = false) final String name,
                                        @RequestHeader(value = "Referer", required = false) final String referer,
                                        @AuthenticationPrincipal final AuthenticatedUser currentUser) {
        if (currentUser == null) {
            return new ModelAndView("redirect:/login");
        }
        if (name == null || name.isBlank()) {
            return redirectBack(referer);
        }
        bodyTypeRequestService.createPendingRequest(currentUser.getId(), currentUser.getEmail(), name);
        return redirectBack(referer);
    }

    private ModelAndView redirectBack(final String referer) {
        final String fallback = "redirect:/cars";
        if (referer == null || referer.isBlank()) {
            return new ModelAndView(fallback);
        }
        try {
            final URI uri = URI.create(referer);
            final String path = uri.getRawPath();
            if (path == null || path.isBlank()) {
                return new ModelAndView(fallback);
            }
            final String query = uri.getRawQuery();
            return new ModelAndView("redirect:" + path + (query == null ? "" : "?" + query));
        } catch (final IllegalArgumentException ignored) {
            return new ModelAndView(fallback);
        }
    }
}
