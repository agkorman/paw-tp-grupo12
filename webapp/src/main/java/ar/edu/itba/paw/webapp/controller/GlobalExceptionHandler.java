package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.webapp.exception.ForbiddenException;
import ar.edu.itba.paw.webapp.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;

@ControllerAdvice
public class GlobalExceptionHandler {

    static final String MAX_UPLOAD_SIZE_MESSAGE =
            "Cada imagen no debe superar los 10 MB y la carga total no debe superar los 50 MB.";

    @ExceptionHandler(ResourceNotFoundException.class)
    public ModelAndView handleResourceNotFound(final ResourceNotFoundException ignored) {
        return forwardToErrorPage(HttpStatus.NOT_FOUND, "/error/404");
    }

    @ExceptionHandler(ForbiddenException.class)
    public ModelAndView handleForbidden(final ForbiddenException ignored) {
        return forwardToErrorPage(HttpStatus.FORBIDDEN, "/error/403");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<String> handleMaxUploadSizeExceeded(final MaxUploadSizeExceededException ignored) {
        return ResponseEntity.badRequest().body(MAX_UPLOAD_SIZE_MESSAGE);
    }

    private ModelAndView forwardToErrorPage(final HttpStatus status, final String errorPath) {
        final ModelAndView mav = new ModelAndView("forward:" + errorPath);
        mav.setStatus(status);
        return mav;
    }
}
