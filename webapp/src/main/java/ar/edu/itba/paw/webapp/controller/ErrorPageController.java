package ar.edu.itba.paw.webapp.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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

    @RequestMapping("/error/400")
    public ModelAndView badRequest(final HttpServletRequest request, final HttpServletResponse response) {
        return errorPage(request, response, 400,
                "Pedido inválido",
                "El formulario o la URL que enviaste no son válidos. Revisá los campos e intentá de nuevo.");
    }

    @RequestMapping("/error/403")
    public ModelAndView forbidden(final HttpServletRequest request, final HttpServletResponse response) {
        return errorPage(request, response, 403,
                "Acceso restringido",
                "No tenés permisos para entrar a esta sección.");
    }

    @RequestMapping("/error/404")
    public ModelAndView notFound(final HttpServletRequest request, final HttpServletResponse response) {
        return errorPage(request, response, 404,
                "No encontrado",
                "El recurso que buscás no existe o fue movido.");
    }

    @RequestMapping("/error/405")
    public ModelAndView methodNotAllowed(final HttpServletRequest request, final HttpServletResponse response) {
        return errorPage(request, response, 405,
                "Acción no permitida",
                "La acción que intentaste realizar no está disponible para esta URL.");
    }

    @RequestMapping("/error/413")
    public ModelAndView payloadTooLarge(final HttpServletRequest request, final HttpServletResponse response) {
        return errorPage(request, response, 413,
                "Archivo demasiado grande",
                "El contenido que intentaste subir supera el tamaño permitido.");
    }

    @RequestMapping("/error/415")
    public ModelAndView unsupportedMediaType(final HttpServletRequest request, final HttpServletResponse response) {
        return errorPage(request, response, 415,
                "Formato no compatible",
                "El tipo de contenido enviado no es compatible con esta acción.");
    }

    @RequestMapping("/error/500")
    public ModelAndView serverError(final HttpServletRequest request, final HttpServletResponse response) {
        return errorPage(request, response, 500,
                "Algo salió mal",
                "Tuvimos un problema inesperado. Intentá de nuevo en unos instantes.");
    }

    private ModelAndView errorPage(final HttpServletRequest request, final HttpServletResponse response,
                                   final int fallbackStatus,
                                   final String title, final String description) {
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
        mav.addObject("title", title);
        mav.addObject("description", description);
        return mav;
    }
}
