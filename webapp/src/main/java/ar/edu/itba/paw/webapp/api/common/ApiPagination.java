package ar.edu.itba.paw.webapp.api.common;

import ar.edu.itba.paw.model.Page;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.net.URI;
import java.util.Objects;

/** Adds pagination metadata to an existing response builder. */
public final class ApiPagination {

    private ApiPagination() { }

    public static Response.ResponseBuilder headers(final Response.ResponseBuilder response,
                                                   final Page<?> page,
                                                   final UriInfo uriInfo) {
        Objects.requireNonNull(response, "response");
        Objects.requireNonNull(page, "page");
        Objects.requireNonNull(uriInfo, "uriInfo");
        if (page.getPageNumber() < 1 || page.getPageSize() <= 0 || page.getTotalItems() < 0L) {
            throw new IllegalArgumentException("invalid page metadata");
        }

        response.header("X-Total-Count", null);
        response.header("X-Total-Count", Long.toString(page.getTotalItems()));
        final int totalPages = page.getTotalPages();
        if (totalPages == 0) {
            return response;
        }

        add(response, uriInfo, "first", 1);
        if (page.getPageNumber() > 1) {
            add(response, uriInfo, "prev", Math.min(page.getPageNumber() - 1, totalPages));
        }
        if (page.getPageNumber() < totalPages) {
            add(response, uriInfo, "next", page.getPageNumber() + 1);
        }
        add(response, uriInfo, "last", totalPages);
        return response;
    }

    private static void add(final Response.ResponseBuilder response, final UriInfo uriInfo,
                            final String relation, final int page) {
        final URI uri = ApiLinks.page(uriInfo, page);
        response.header("Link", "<" + uri.toASCIIString() + ">; rel=\"" + relation + "\"");
    }
}
