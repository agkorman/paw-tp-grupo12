package ar.edu.itba.paw.webapp.api.common;

import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.ext.Provider;
import org.springframework.stereotype.Component;

@Component
@Provider
public class ApiResponseFilter implements ContainerResponseFilter {

    @Override
    public void filter(final ContainerRequestContext request, final ContainerResponseContext response) {
        // Conservative initial policy, including responses that will later carry viewer data.
        response.getHeaders().putSingle(HttpHeaders.CACHE_CONTROL, "no-store");
        response.getHeaders().add(HttpHeaders.VARY, HttpHeaders.ACCEPT);
    }
}
