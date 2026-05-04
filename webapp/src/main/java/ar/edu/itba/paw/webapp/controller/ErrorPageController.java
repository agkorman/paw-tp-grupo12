package ar.edu.itba.paw.webapp.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import ar.edu.itba.paw.webapp.util.LogSanitizer;

@Controller
public class ErrorPageController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ErrorPageController.class);

    private final MessageSource messageSource;

    @Autowired
    public ErrorPageController(final MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @RequestMapping("/error/400")
    public ModelAndView badRequest(final HttpServletRequest request, final HttpServletResponse response) {
        return errorPage(request, response, 400,
                "error.page.400.title",
                "error.page.400.description");
    }

    @RequestMapping("/error/403")
    public ModelAndView forbidden(final HttpServletRequest request, final HttpServletResponse response) {
        return errorPage(request, response, 403,
                "error.page.403.title",
                "error.page.403.description");
    }

    @RequestMapping("/error/404")
    public ModelAndView notFound(final HttpServletRequest request, final HttpServletResponse response) {
        return errorPage(request, response, 404,
                "error.page.404.title",
                "error.page.404.description");
    }

    @RequestMapping("/error/405")
    public ModelAndView methodNotAllowed(final HttpServletRequest request, final HttpServletResponse response) {
        return errorPage(request, response, 405,
                "error.page.405.title",
                "error.page.405.description");
    }

    @RequestMapping("/error/413")
    public ModelAndView payloadTooLarge(final HttpServletRequest request, final HttpServletResponse response) {
        return errorPage(request, response, 413,
                "error.page.413.title",
                "error.page.413.description");
    }

    @RequestMapping("/error/415")
    public ModelAndView unsupportedMediaType(final HttpServletRequest request, final HttpServletResponse response) {
        return errorPage(request, response, 415,
                "error.page.415.title",
                "error.page.415.description");
    }

    @RequestMapping("/error/500")
    public ModelAndView serverError(final HttpServletRequest request, final HttpServletResponse response) {
        return errorPage(request, response, 500,
                "error.page.500.title",
                "error.page.500.description");
    }

    private ModelAndView errorPage(final HttpServletRequest request, final HttpServletResponse response,
                                   final int fallbackStatus,
                                   final String titleCode, final String descriptionCode) {
        final Integer attrStatus = (Integer) request.getAttribute("javax.servlet.error.status_code");
        final int status = attrStatus == null || attrStatus < 400 ? fallbackStatus : attrStatus;

        final String requestUri = (String) request.getAttribute("javax.servlet.error.request_uri");
        if (status >= 500) {
            LOGGER.error("rendering error page status={} uri={}", status, LogSanitizer.forLog(requestUri, LogSanitizer.MAX_LOG_URL_CODE_POINTS));
        } else {
            LOGGER.warn("rendering error page status={} uri={}", status, LogSanitizer.forLog(requestUri, LogSanitizer.MAX_LOG_URL_CODE_POINTS));
        }
        final ModelAndView mav = new ModelAndView("error.jsp");
        response.setStatus(status);
        final HttpStatus httpStatus = HttpStatus.resolve(status);
        if (httpStatus != null) {
            mav.setStatus(httpStatus);
        }
        mav.addObject("statusCode", status);
        mav.addObject("title", resolveMessage(titleCode));
        mav.addObject("description", resolveMessage(descriptionCode));
        return mav;
    }

    private String resolveMessage(final String code) {
        return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }
}
