package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.services.AdminRequestService;
import ar.edu.itba.paw.services.BodyTypeRequestService;
import ar.edu.itba.paw.services.BrandRequestService;
import ar.edu.itba.paw.services.exception.PendingAdminRequestExistsException;
import ar.edu.itba.paw.webapp.auth.AuthenticatedUser;
import ar.edu.itba.paw.webapp.form.AdminRoleRequestForm;
import ar.edu.itba.paw.webapp.form.CatalogNameRequestForm;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import ar.edu.itba.paw.webapp.util.LogSanitizer;

import javax.validation.Valid;
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
    public ModelAndView requestBrand(@Valid @ModelAttribute("catalogNameRequestForm") final CatalogNameRequestForm form,
                                     final BindingResult errors,
                                     @RequestHeader(value = "Referer", required = false) final String referer,
                                     @AuthenticationPrincipal final AuthenticatedUser currentUser) {
        if (currentUser == null) {
            return new ModelAndView("redirect:/login");
        }
        if (errors.hasErrors()) {
            LOGGER.warn("brand request rejected: validation errors userId={} errorCount={}",
                    currentUser.getId(), errors.getErrorCount());
            return redirectBack(referer);
        }
        brandRequestService.createPendingRequest(currentUser.getId(), currentUser.getEmail(), form.getName(), form.getComments());
        LOGGER.info("user id={} submitted brand request name={}", currentUser.getId(), LogSanitizer.forLog(form.getName(), LogSanitizer.MAX_LOG_NAME_CODE_POINTS));
        return redirectToCatalog("brand");
    }

    @RequestMapping(value = "/body-type-requests", method = RequestMethod.POST)
    public ModelAndView requestBodyType(@Valid @ModelAttribute("catalogNameRequestForm") final CatalogNameRequestForm form,
                                        final BindingResult errors,
                                        @RequestHeader(value = "Referer", required = false) final String referer,
                                        @AuthenticationPrincipal final AuthenticatedUser currentUser) {
        if (currentUser == null) {
            return new ModelAndView("redirect:/login");
        }
        if (errors.hasErrors()) {
            LOGGER.warn("body type request rejected: validation errors userId={} errorCount={}",
                    currentUser.getId(), errors.getErrorCount());
            return redirectBack(referer);
        }
        bodyTypeRequestService.createPendingRequest(currentUser.getId(), currentUser.getEmail(), form.getName(), form.getComments());
        LOGGER.info("user id={} submitted body type request name={}", currentUser.getId(), LogSanitizer.forLog(form.getName(), LogSanitizer.MAX_LOG_NAME_CODE_POINTS));
        return redirectToCatalog("body-type");
    }

    @RequestMapping(value = "/admin-requests", method = RequestMethod.POST)
    public ModelAndView requestAdmin(@Valid @ModelAttribute("adminRoleRequestForm") final AdminRoleRequestForm form,
                                     final BindingResult errors,
                                     @RequestHeader(value = "Referer", required = false) final String referer,
                                     @AuthenticationPrincipal final AuthenticatedUser currentUser) {
        if (currentUser == null) {
            return new ModelAndView("redirect:/login");
        }
        if (errors.hasErrors()) {
            LOGGER.warn("admin request rejected: validation errors userId={} errorCount={}",
                    currentUser.getId(), errors.getErrorCount());
            return redirectBack(referer);
        }
        try {
            adminRequestService.createPendingRequest(currentUser.getId(), currentUser.getEmail(),
                    form.getMotivation(), form.getBio(), form.getJustification());
        } catch (final PendingAdminRequestExistsException e) {
            LOGGER.warn("admin request rejected: already has pending userId={}", currentUser.getId());
            return redirectBack(referer);
        }
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
            final String path = ControllerUtils.stripCurrentContextPath(uri.getRawPath());
            if (path == null || path.isBlank() || path.startsWith("//")) {
                return new ModelAndView(withSubmittedRedirect(fallback, submitted));
            }
            final String query = withSubmitted(uri.getRawQuery(), submitted);
            return new ModelAndView("redirect:" + path + (query == null ? "" : "?" + query));
        } catch (final IllegalArgumentException e) {
            LOGGER.warn("invalid referer URI for redirect, falling back referer={}", LogSanitizer.forLog(referer, LogSanitizer.MAX_LOG_URL_CODE_POINTS), e);
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
