package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.webapp.exception.ForbiddenException;
import ar.edu.itba.paw.webapp.exception.ResourceNotFoundException;
import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.ModelAndView;

import static org.junit.Assert.assertEquals;

public class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    public void resourceNotFoundForwardsToNotFoundErrorPage() {
        final ModelAndView mav = handler.handleResourceNotFound(new ResourceNotFoundException());

        assertEquals("forward:/error/404", mav.getViewName());
        assertEquals(HttpStatus.NOT_FOUND, mav.getStatus());
    }

    @Test
    public void forbiddenForwardsToForbiddenErrorPage() {
        final ModelAndView mav = handler.handleForbidden(new ForbiddenException());

        assertEquals("forward:/error/403", mav.getViewName());
        assertEquals(HttpStatus.FORBIDDEN, mav.getStatus());
    }

    @Test
    public void uploadSizeErrorReturnsBadRequestMessage() {
        final ResponseEntity<String> response =
                handler.handleMaxUploadSizeExceeded(new MaxUploadSizeExceededException(50L));

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals(GlobalExceptionHandler.MAX_UPLOAD_SIZE_MESSAGE, response.getBody());
    }
}
