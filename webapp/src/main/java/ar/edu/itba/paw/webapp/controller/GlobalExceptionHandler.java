package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.services.exception.CannotModerateCreatorException;
import ar.edu.itba.paw.services.exception.CommunityContentOwnershipException;
import ar.edu.itba.paw.services.exception.CommunityCreatorCannotLeaveException;
import ar.edu.itba.paw.services.exception.CommunityMembershipRequiredException;
import ar.edu.itba.paw.services.exception.CommunityModeratorRequiredException;
import ar.edu.itba.paw.services.exception.CommunityOwnerRequiredException;
import ar.edu.itba.paw.services.exception.InvalidReviewTagSelectionException;
import ar.edu.itba.paw.services.exception.InvalidServiceInputException;
import ar.edu.itba.paw.services.exception.CarNotFoundException;
import ar.edu.itba.paw.services.exception.InvalidImagePayloadException;
import ar.edu.itba.paw.services.exception.ReviewNotFoundException;
import ar.edu.itba.paw.services.exception.ReviewReplyNotFoundException;
import ar.edu.itba.paw.services.exception.ReviewOwnershipException;
import ar.edu.itba.paw.services.exception.ReviewReplyOwnershipException;
import ar.edu.itba.paw.services.exception.SelfFollowException;
import ar.edu.itba.paw.services.exception.PendingAdminRequestExistsException;
import ar.edu.itba.paw.services.exception.ServiceOperationException;
import ar.edu.itba.paw.services.exception.UserNotFoundException;
import ar.edu.itba.paw.persistence.exception.PersistenceOperationException;
import ar.edu.itba.paw.webapp.exception.ForbiddenException;
import ar.edu.itba.paw.webapp.exception.ResourceNotFoundException;
import ar.edu.itba.paw.webapp.exception.UploadedImageReadException;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
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

    private final MessageSource messageSource;

    public GlobalExceptionHandler(final MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @ExceptionHandler({
            ResourceNotFoundException.class,
            CarNotFoundException.class,
            ReviewNotFoundException.class,
            ReviewReplyNotFoundException.class,
            UserNotFoundException.class
    })
    public ModelAndView handleResourceNotFound(final RuntimeException e,
                                               final HttpServletRequest request) {
        LOGGER.warn("resource not found uri={} type={}",
                LogSanitizer.forLog(request.getRequestURI(), LogSanitizer.MAX_LOG_URL_CODE_POINTS),
                e.getClass().getSimpleName());
        return forwardToErrorPage(HttpStatus.NOT_FOUND, "/error/404");
    }

    @ExceptionHandler({
            ForbiddenException.class,
            ReviewOwnershipException.class,
            ReviewReplyOwnershipException.class,
            CommunityMembershipRequiredException.class,
            CommunityModeratorRequiredException.class,
            CannotModerateCreatorException.class,
            CommunityContentOwnershipException.class,
            CommunityCreatorCannotLeaveException.class,
            CommunityOwnerRequiredException.class
    })
    public ModelAndView handleForbidden(final RuntimeException e,
                                        final HttpServletRequest request) {
        LOGGER.warn("access forbidden uri={} type={}",
                LogSanitizer.forLog(request.getRequestURI(), LogSanitizer.MAX_LOG_URL_CODE_POINTS),
                e.getClass().getSimpleName());
        return forwardToErrorPage(HttpStatus.FORBIDDEN, "/error/403");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<String> handleMaxUploadSizeExceeded(final MaxUploadSizeExceededException e,
                                                              final HttpServletRequest request) {
        LOGGER.warn("upload exceeded max size uri={} type={}",
                LogSanitizer.forLog(request.getRequestURI(), LogSanitizer.MAX_LOG_URL_CODE_POINTS),
                e.getClass().getSimpleName());
        return ResponseEntity.badRequest().body(message("upload.maxSize"));
    }

    @ExceptionHandler(InvalidReviewTagSelectionException.class)
    public ResponseEntity<String> handleInvalidReviewTagSelection(final InvalidReviewTagSelectionException e) {
        LOGGER.warn("invalid review tag selection type={}", e.getClass().getSimpleName());
        return ResponseEntity.badRequest().body(e.getMessage());
    }

    @ExceptionHandler({
            InvalidImagePayloadException.class,
            InvalidServiceInputException.class,
            SelfFollowException.class,
            PendingAdminRequestExistsException.class
    })
    public ModelAndView handleInvalidServiceInput(final RuntimeException e,
                                                  final HttpServletRequest request) {
        LOGGER.warn("invalid service input uri={} type={}",
                LogSanitizer.forLog(request.getRequestURI(), LogSanitizer.MAX_LOG_URL_CODE_POINTS),
                e.getClass().getSimpleName());
        return forwardToErrorPage(HttpStatus.BAD_REQUEST, "/error/400");
    }

    @ExceptionHandler({
            ServiceOperationException.class,
            PersistenceOperationException.class,
            UploadedImageReadException.class
    })
    public ModelAndView handleServiceOperationFailure(final RuntimeException e,
                                                      final HttpServletRequest request) {
        LOGGER.error("operation failed uri={}", LogSanitizer.forLog(request.getRequestURI(), LogSanitizer.MAX_LOG_URL_CODE_POINTS), e);
        return forwardToErrorPage(HttpStatus.INTERNAL_SERVER_ERROR, "/error/500");
    }

    private ModelAndView forwardToErrorPage(final HttpStatus status, final String errorPath) {
        final ModelAndView mav = new ModelAndView("forward:" + errorPath);
        mav.setStatus(status);
        return mav;
    }

    private String message(final String code) {
        return messageSource.getMessage(code, null, LocaleContextHolder.getLocale());
    }
}
