package ar.edu.itba.paw.webapp.controller;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.Proxy;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.Assert.assertEquals;

public class ErrorPageControllerTest {

    private final ErrorPageController controller = new ErrorPageController();

    @Test
    public void notFoundPageSetsNotFoundHttpStatus() {
        final AtomicInteger responseStatus = new AtomicInteger();
        final ModelAndView mav = controller.notFound(requestWithoutErrorStatus(), response(responseStatus));

        assertEquals("error.jsp", mav.getViewName());
        assertEquals(HttpStatus.NOT_FOUND, mav.getStatus());
        assertEquals(404, responseStatus.get());
        assertEquals(404, mav.getModel().get("statusCode"));
    }

    @Test
    public void serverErrorUsesServletErrorStatusWhenPresent() {
        final AtomicInteger responseStatus = new AtomicInteger();
        final ModelAndView mav = controller.serverError(requestWithErrorStatus(404), response(responseStatus));

        assertEquals(HttpStatus.NOT_FOUND, mav.getStatus());
        assertEquals(404, responseStatus.get());
        assertEquals(404, mav.getModel().get("statusCode"));
    }

    @Test
    public void notFoundPageIgnoresForwardedSuccessStatus() {
        final AtomicInteger responseStatus = new AtomicInteger();
        final ModelAndView mav = controller.notFound(requestWithErrorStatus(200), response(responseStatus));

        assertEquals(HttpStatus.NOT_FOUND, mav.getStatus());
        assertEquals(404, responseStatus.get());
        assertEquals(404, mav.getModel().get("statusCode"));
    }

    private HttpServletRequest requestWithoutErrorStatus() {
        return requestWithErrorStatus(null);
    }

    private HttpServletRequest requestWithErrorStatus(final Integer status) {
        return (HttpServletRequest) Proxy.newProxyInstance(
                HttpServletRequest.class.getClassLoader(),
                new Class<?>[]{HttpServletRequest.class},
                (proxy, method, args) -> {
                    if ("getAttribute".equals(method.getName())
                            && args != null
                            && args.length == 1
                            && "javax.servlet.error.status_code".equals(args[0])) {
                        return status;
                    }
                    return null;
                });
    }

    private HttpServletResponse response(final AtomicInteger status) {
        return (HttpServletResponse) Proxy.newProxyInstance(
                HttpServletResponse.class.getClassLoader(),
                new Class<?>[]{HttpServletResponse.class},
                (proxy, method, args) -> {
                    if ("setStatus".equals(method.getName()) && args != null && args.length == 1) {
                        status.set((Integer) args[0]);
                    }
                    return null;
                });
    }
}
