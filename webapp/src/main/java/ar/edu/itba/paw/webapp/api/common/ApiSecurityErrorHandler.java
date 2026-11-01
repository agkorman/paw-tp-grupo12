package ar.edu.itba.paw.webapp.api.common;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;

/** Handles failures raised before a request reaches Jersey. */
public class ApiSecurityErrorHandler implements AuthenticationEntryPoint, AccessDeniedHandler {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void commence(final HttpServletRequest request, final HttpServletResponse response,
                         final AuthenticationException exception) throws IOException {
        write(response, HttpServletResponse.SC_UNAUTHORIZED,
                new ApiError("AUTHENTICATION_REQUIRED", "Authentication is required"));
    }

    @Override
    public void handle(final HttpServletRequest request, final HttpServletResponse response,
                       final AccessDeniedException exception) throws IOException {
        write(response, HttpServletResponse.SC_FORBIDDEN,
                new ApiError("ACCESS_DENIED", "Access is denied"));
    }

    private void write(final HttpServletResponse response, final int status, final ApiError error)
            throws IOException {
        response.setStatus(status);
        response.setContentType(ApiMediaTypes.ERROR_V1);
        response.setHeader("Cache-Control", "no-store");
        response.setHeader("Vary", "Accept");
        objectMapper.writeValue(response.getOutputStream(), error);
    }
}
