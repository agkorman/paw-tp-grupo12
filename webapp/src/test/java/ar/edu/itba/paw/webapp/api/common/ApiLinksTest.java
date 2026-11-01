package ar.edu.itba.paw.webapp.api.common;

import org.junit.jupiter.api.Test;

import javax.ws.rs.Path;
import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ApiLinksTest {

    @Path("widgets")
    static class WidgetResource { }

    @Path("wídgets")
    static class UnicodeWidgetResource { }

    @Test
    void resourceBuildsLocalContextPathAndEncodesTemplateValuesAsPathSegments() {
        // Arrange
        final UriInfo uriInfo = mock(UriInfo.class);
        when(uriInfo.getBaseUriBuilder()).thenReturn(UriBuilder.fromUri("https://untrusted.example/app/api/"));
        final String value = "á +/%/<>,";
        // Exercise
        final ApiLink result = ApiLinks.resource(uriInfo, WidgetResource.class, "{id}/detail", value);
        // Assertions
        assertEquals("/app/api/widgets/%C3%A1%20%2B%2F%25%2F%3C%3E%2C/detail", result.getHref());
    }

    @Test
    void resourceReturnsAsciiPathAndRejectsNullTemplateValues() {
        // Arrange
        final UriInfo uriInfo = mock(UriInfo.class);
        when(uriInfo.getBaseUriBuilder()).thenReturn(UriBuilder.fromUri("https://untrusted.example/app/café/api/"));
        // Exercise
        final ApiLink result = ApiLinks.resource(uriInfo, UnicodeWidgetResource.class, "{id}", "á");
        // Assertions
        assertEquals("/app/caf%C3%A9/api/w%C3%ADdgets/%C3%A1", result.getHref());
        assertThrows(NullPointerException.class,
                () -> ApiLinks.resource(uriInfo, WidgetResource.class, "{id}", (Object) null));
    }

    @Test
    void pagePreservesContextAndRepeatedFiltersWhileReplacingPageAndDroppingAuthorityAndFragment() {
        // Arrange
        final UriInfo uriInfo = mock(UriInfo.class);
        when(uriInfo.getRequestUriBuilder()).thenReturn(UriBuilder.fromUri(
                "https://external.example/app/api/widgets?tag=a&tag=b&page=2&%70age=9&sort=name%20asc&token=%2B%25#details"));
        // Exercise
        final URI result = ApiLinks.page(uriInfo, 3);
        // Assertions
        assertEquals("/app/api/widgets?tag=a&tag=b&sort=name%20asc&token=%2B%25&page=3", result.toASCIIString());
    }

    @Test
    void pageRejectsInvalidPageAndAmbiguousDoubleSlashPath() {
        // Arrange
        final UriInfo validPath = mock(UriInfo.class);
        when(validPath.getRequestUriBuilder()).thenReturn(UriBuilder.fromUri("https://host.example/path"));
        final UriInfo unsafePath = mock(UriInfo.class);
        when(unsafePath.getRequestUriBuilder()).thenReturn(UriBuilder.fromUri("https://host.example//evil/path"));
        // Exercise / Assertions
        assertThrows(IllegalArgumentException.class, () -> ApiLinks.page(validPath, 0));
        assertThrows(IllegalArgumentException.class, () -> ApiLinks.page(unsafePath, 1));
    }
}
