package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.services.exception.InvalidReviewTagSelectionException;
import ar.edu.itba.paw.webapp.exception.ForbiddenException;
import ar.edu.itba.paw.webapp.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import ar.edu.itba.paw.webapp.util.LogSanitizer;

@ControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    static final String MAX_UPLOAD_SIZE_MESSAGE =
            "Cada imagen no debe superar los 10 MB y la carga total no debe superar los 50 MB.";

    @ExceptionHandler(ResourceNotFoundException.class)
    public ModelAndView handleResourceNotFound(final ResourceNotFoundException e,
                                               final HttpServletRequest request) {
        LOGGER.warn("resource not found uri={}", LogSanitizer.forLog(request.getRequestURI(), LogSanitizer.MAX_LOG_URL_CODE_POINTS), e);
        return forwardToErrorPage(HttpStatus.NOT_FOUND, "/error/404");
    }

    @ExceptionHandler(ForbiddenException.class)
    public ModelAndView handleForbidden(final ForbiddenException e,
                                        final HttpServletRequest request) {
        LOGGER.warn("access forbidden uri={}", LogSanitizer.forLog(request.getRequestURI(), LogSanitizer.MAX_LOG_URL_CODE_POINTS), e);
        return forwardToErrorPage(HttpStatus.FORBIDDEN, "/error/403");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<String> handleMaxUploadSizeExceeded(final MaxUploadSizeExceededException e,
                                                              final HttpServletRequest request) {
        LOGGER.warn("upload exceeded max size uri={}", LogSanitizer.forLog(request.getRequestURI(), LogSanitizer.MAX_LOG_URL_CODE_POINTS), e);
        return ResponseEntity.badRequest().body(MAX_UPLOAD_SIZE_MESSAGE);
    }

    @ExceptionHandler(InvalidReviewTagSelectionException.class)
    public ResponseEntity<String> handleInvalidReviewTagSelection(final InvalidReviewTagSelectionException e) {
        LOGGER.warn("invalid review tag selection", e);
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    private ModelAndView forwardToErrorPage(final HttpStatus status, final String errorPath) {
        final ModelAndView mav = new ModelAndView("forward:" + errorPath);
        mav.setStatus(status);
        return mav;
    }
}
