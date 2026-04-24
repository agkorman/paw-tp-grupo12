package ar.edu.itba.paw.webapp.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;

@Controller
public class ErrorPageController {

    @RequestMapping("/error/400")
    public ModelAndView badRequest(final HttpServletRequest request) {
        return errorPage(request, 400,
                "Pedido inválido",
                "El formulario o la URL que enviaste no son válidos. Revisá los campos e intentá de nuevo.");
    }

    @RequestMapping("/error/403")
    public ModelAndView forbidden(final HttpServletRequest request) {
        return errorPage(request, 403,
                "Acceso restringido",
                "No tenés permisos para entrar a esta sección.");
    }

    @RequestMapping("/error/404")
    public ModelAndView notFound(final HttpServletRequest request) {
        return errorPage(request, 404,
                "No encontrado",
                "El recurso que buscás no existe o fue movido.");
    }

    @RequestMapping("/error/405")
    public ModelAndView methodNotAllowed(final HttpServletRequest request) {
        return errorPage(request, 405,
                "Acción no permitida",
                "La acción que intentaste realizar no está disponible para esta URL.");
    }

    @RequestMapping("/error/413")
    public ModelAndView payloadTooLarge(final HttpServletRequest request) {
        return errorPage(request, 413,
                "Archivo demasiado grande",
                "El contenido que intentaste subir supera el tamaño permitido.");
    }

    @RequestMapping("/error/415")
    public ModelAndView unsupportedMediaType(final HttpServletRequest request) {
        return errorPage(request, 415,
                "Formato no compatible",
                "El tipo de contenido enviado no es compatible con esta acción.");
    }

    @RequestMapping("/error/500")
    public ModelAndView serverError(final HttpServletRequest request) {
        return errorPage(request, 500,
                "Algo salió mal",
                "Tuvimos un problema inesperado. Intentá de nuevo en unos instantes.");
    }

    private ModelAndView errorPage(final HttpServletRequest request, final int fallbackStatus,
                                   final String title, final String description) {
        final Integer attrStatus = (Integer) request.getAttribute("javax.servlet.error.status_code");
        final int status = attrStatus == null ? fallbackStatus : attrStatus;

        final ModelAndView mav = new ModelAndView("error.jsp");
        mav.addObject("statusCode", status);
        mav.addObject("title", title);
        mav.addObject("description", description);
        return mav;
    }
}
