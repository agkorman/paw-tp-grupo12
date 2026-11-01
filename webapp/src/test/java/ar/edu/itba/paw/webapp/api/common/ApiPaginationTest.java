package ar.edu.itba.paw.webapp.api.common;

import ar.edu.itba.paw.model.Page;
import org.junit.jupiter.api.Test;

import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApiPaginationTest {

    @Test
    void addsFirstNextLastAndPreservesExistingResponseFields() {
        // Arrange
        final UriInfo uriInfo = request("https://outside.example/app/api/communities?tag=a&tag=b&page=1&sort=recent");
        final Page<String> page = new Page<>(List.of("item"), 1, 5, 11L);
        final Response.ResponseBuilder builder = Response.status(202).entity("payload").type("text/plain")
                .header("X-Other", "keep").header("X-Total-Count", "old-count");
        // Exercise
        final Response.ResponseBuilder result = ApiPagination.headers(builder, page, uriInfo);
        // Assertions
        assertSame(builder, result);
        final Response response = result.build();
        assertEquals(202, response.getStatus());
        assertEquals("payload", response.getEntity());
        assertEquals("text/plain", response.getMediaType().toString());
        assertEquals("keep", response.getHeaderString("X-Other"));
        assertEquals("11", response.getHeaderString("X-Total-Count"));
        assertEquals(List.of("11"), response.getStringHeaders().get("X-Total-Count"));
        assertEquals(List.of(
                "</app/api/communities?tag=a&tag=b&sort=recent&page=1>; rel=\"first\"",
                "</app/api/communities?tag=a&tag=b&sort=recent&page=2>; rel=\"next\"",
                "</app/api/communities?tag=a&tag=b&sort=recent&page=3>; rel=\"last\""),
                response.getStringHeaders().get("Link"));
    }

    @Test
    void usesLastExistingPageForOutOfRangePreviousAndEmitsNoNext() {
        // Arrange
        final UriInfo uriInfo = request("https://host.example/community?page=99&filter=x");
        final Page<String> page = new Page<>(List.of(), 99, 4, 9L);
        // Exercise
        final Response result = ApiPagination.headers(Response.ok(), page, uriInfo).build();
        // Assertions
        assertEquals(List.of(
                "</community?filter=x&page=1>; rel=\"first\"",
                "</community?filter=x&page=3>; rel=\"prev\"",
                "</community?filter=x&page=3>; rel=\"last\""), result.getStringHeaders().get("Link"));
    }

    @Test
    void lastPageHasPreviousButNoNext() {
        // Arrange
        final UriInfo uriInfo = request("https://host.example/community?page=3");
        final Page<String> page = new Page<>(List.of("item"), 3, 4, 9L);
        // Exercise
        final Response result = ApiPagination.headers(Response.ok(), page, uriInfo).build();
        // Assertions
        assertEquals(List.of(
                "</community?page=1>; rel=\"first\"",
                "</community?page=2>; rel=\"prev\"",
                "</community?page=3>; rel=\"last\""), result.getStringHeaders().get("Link"));
    }

    @Test
    void emptyAndLongMaxTotalsUseSharedPageArithmeticWithoutNavigationForEmpty() {
        // Arrange
        final UriInfo uriInfo = request("https://host.example/community?page=1");
        final Page<String> empty = Page.empty(1, 4);
        final Page<String> huge = new Page<>(List.of(), 1, 1, Long.MAX_VALUE);
        // Exercise
        final Response emptyResponse = ApiPagination.headers(Response.ok(), empty, uriInfo).build();
        final Response hugeResponse = ApiPagination.headers(Response.ok(), huge, uriInfo).build();
        // Assertions
        assertEquals("0", emptyResponse.getHeaderString("X-Total-Count"));
        assertEquals(null, emptyResponse.getHeaderString("Link"));
        assertEquals("9223372036854775807", hugeResponse.getHeaderString("X-Total-Count"));
        assertEquals("</community?page=2>; rel=\"next\"", hugeResponse.getStringHeaders().get("Link").get(1));
        assertEquals("</community?page=2147483647>; rel=\"last\"", hugeResponse.getStringHeaders().get("Link").get(2));
    }

    @Test
    void keepsEscapedFilterBytesOutOfLinkHeaderControlCharacters() {
        // Arrange
        final UriInfo uriInfo = request("https://host.example/community?%70age=8&filter=%3C%3E%2C%0D%0A%2B%25&page=2#section");
        final Page<String> page = new Page<>(List.of(), 2, 1, 3L);
        // Exercise
        final Response result = ApiPagination.headers(Response.ok(), page, uriInfo).build();
        // Assertions
        assertEquals(List.of(
                "</community?filter=%3C%3E%2C%0D%0A%2B%25&page=1>; rel=\"first\"",
                "</community?filter=%3C%3E%2C%0D%0A%2B%25&page=1>; rel=\"prev\"",
                "</community?filter=%3C%3E%2C%0D%0A%2B%25&page=3>; rel=\"next\"",
                "</community?filter=%3C%3E%2C%0D%0A%2B%25&page=3>; rel=\"last\""),
                result.getStringHeaders().get("Link"));
    }

    @Test
    void rejectsInvalidPageMetadata() {
        // Arrange
        final UriInfo uriInfo = request("https://host.example/community");
        final Page<String> badPageNumber = new Page<>(List.of(), 0, 5, 1L);
        final Page<String> badPageSize = new Page<>(List.of(), 1, 0, 1L);
        final Page<String> negativeTotal = new Page<>(List.of(), 1, 5, -1L);
        // Exercise / Assertions
        assertThrows(IllegalArgumentException.class, () -> ApiPagination.headers(Response.ok(), badPageNumber, uriInfo));
        assertThrows(IllegalArgumentException.class, () -> ApiPagination.headers(Response.ok(), badPageSize, uriInfo));
        assertThrows(IllegalArgumentException.class, () -> ApiPagination.headers(Response.ok(), negativeTotal, uriInfo));
    }

    private static UriInfo request(final String uri) {
        final UriInfo uriInfo = mock(UriInfo.class);
        when(uriInfo.getRequestUriBuilder()).thenReturn(UriBuilder.fromUri(uri));
        return uriInfo;
    }
}
