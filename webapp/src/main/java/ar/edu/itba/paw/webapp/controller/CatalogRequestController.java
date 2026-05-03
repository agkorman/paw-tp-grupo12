package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.services.AdminRequestService;
import ar.edu.itba.paw.services.BodyTypeRequestService;
import ar.edu.itba.paw.services.BrandRequestService;
import ar.edu.itba.paw.webapp.auth.AuthenticatedUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    private static final Logger LOGGER = LoggerFactory.getLogger(CatalogRequestController.class);

    private final BrandRequestService brandRequestService;
    private final BodyTypeRequestService bodyTypeRequestService;
    private final AdminRequestService adminRequestService;

    @Autowired
    public CatalogRequestController(final BrandRequestService brandRequestService,
                                    final BodyTypeRequestService bodyTypeRequestService,
                                    final AdminRequestService adminRequestService) {
        this.brandRequestService = brandRequestService;
        this.bodyTypeRequestService = bodyTypeRequestService;
        this.adminRequestService = adminRequestService;
    }

    @InitBinder
    public void initBinder(final WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @RequestMapping(value = "/brand-requests", method = RequestMethod.POST)
    public ModelAndView requestBrand(@RequestParam(value = "name", required = false) final String name,
                                     @RequestParam(value = "comments", required = false) final String comments,
                                     @RequestHeader(value = "Referer", required = false) final String referer,
                                     @AuthenticationPrincipal final AuthenticatedUser currentUser) {
        if (currentUser == null) {
            return new ModelAndView("redirect:/login");
        }
        if (name == null || name.isBlank()) {
            LOGGER.warn("brand request rejected: blank name userId={}", currentUser.getId());
            return redirectBack(referer);
        }
        brandRequestService.createPendingRequest(currentUser.getId(), currentUser.getEmail(), name, comments);
        LOGGER.info("user id={} submitted brand request name={}", currentUser.getId(), name);
        return redirectToCatalog("brand");
    }

    @RequestMapping(value = "/body-type-requests", method = RequestMethod.POST)
    public ModelAndView requestBodyType(@RequestParam(value = "name", required = false) final String name,
                                        @RequestParam(value = "comments", required = false) final String comments,
                                        @RequestHeader(value = "Referer", required = false) final String referer,
                                        @AuthenticationPrincipal final AuthenticatedUser currentUser) {
        if (currentUser == null) {
            return new ModelAndView("redirect:/login");
        }
        if (name == null || name.isBlank()) {
            LOGGER.warn("body type request rejected: blank name userId={}", currentUser.getId());
            return redirectBack(referer);
        }
        bodyTypeRequestService.createPendingRequest(currentUser.getId(), currentUser.getEmail(), name, comments);
        LOGGER.info("user id={} submitted body type request name={}", currentUser.getId(), name);
        return redirectToCatalog("body-type");
    }

    @RequestMapping(value = "/admin-requests", method = RequestMethod.POST)
    public ModelAndView requestAdmin(@RequestParam(value = "motivation", required = false) final String motivation,
                                     @RequestParam(value = "bio", required = false) final String bio,
                                     @RequestParam(value = "justification", required = false) final String justification,
                                     @RequestHeader(value = "Referer", required = false) final String referer,
                                     @AuthenticationPrincipal final AuthenticatedUser currentUser) {
        if (currentUser == null) {
            return new ModelAndView("redirect:/login");
        }
        if (motivation == null || motivation.isBlank()
                || bio == null || bio.isBlank()
                || justification == null || justification.isBlank()) {
            LOGGER.warn("admin request rejected: missing fields userId={}", currentUser.getId());
            return redirectBack(referer);
        }
        if (adminRequestService.hasPendingRequest(currentUser.getId())) {
            LOGGER.warn("admin request rejected: already has pending userId={}", currentUser.getId());
            return redirectBack(referer);
        }
        adminRequestService.createPendingRequest(currentUser.getId(), currentUser.getEmail(),
                motivation, bio, justification);
        LOGGER.info("user id={} submitted admin moderator request", currentUser.getId());
        return redirectBack(referer, "moderator");
    }

    private ModelAndView redirectBack(final String referer) {
        return redirectBack(referer, null);
    }

    private ModelAndView redirectToCatalog(final String submitted) {
        return new ModelAndView(withSubmittedRedirect("redirect:/cars", submitted));
    }

    private ModelAndView redirectBack(final String referer, final String submitted) {
        final String fallback = "redirect:/cars";
        if (referer == null || referer.isBlank()) {
            return new ModelAndView(withSubmittedRedirect(fallback, submitted));
        }
        try {
            final URI uri = URI.create(referer);
            final String path = uri.getRawPath();
            if (path == null || path.isBlank() || path.startsWith("//")) {
                return new ModelAndView(withSubmittedRedirect(fallback, submitted));
            }
            final String query = withSubmitted(uri.getRawQuery(), submitted);
            return new ModelAndView("redirect:" + path + (query == null ? "" : "?" + query));
        } catch (final IllegalArgumentException e) {
            LOGGER.warn("invalid referer URI for redirect, falling back referer={}", referer, e);
            return new ModelAndView(withSubmittedRedirect(fallback, submitted));
        }
    }

    private String withSubmitted(final String rawQuery, final String submitted) {
        if (submitted == null || submitted.isBlank()) {
            return rawQuery;
        }
        final StringBuilder query = new StringBuilder();
        if (rawQuery != null && !rawQuery.isBlank()) {
            final String[] params = rawQuery.split("&");
            for (final String param : params) {
                if (param.isBlank() || param.equals("submitted") || param.startsWith("submitted=")) {
                    continue;
                }
                if (query.length() > 0) {
                    query.append('&');
                }
                query.append(param);
            }
        }
        if (query.length() > 0) {
            query.append('&');
        }
        query.append("submitted=").append(submitted);
        return query.toString();
    }

    private String withSubmittedRedirect(final String redirectView, final String submitted) {
        if (submitted == null || submitted.isBlank()) {
            return redirectView;
        }
        return redirectView + (redirectView.contains("?") ? "&" : "?") + "submitted=" + submitted;
    }
}
