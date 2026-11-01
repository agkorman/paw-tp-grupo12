package ar.edu.itba.paw.webapp.api;

import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

/** Jersey uses the existing root Spring context through jersey-spring5. */
public class ApiApplication extends ResourceConfig {

    public ApiApplication() {
        packages("ar.edu.itba.paw.webapp.api");
        register(JacksonFeature.class);
        // Return API errors as JSON instead of forwarding to the WAR's HTML error pages.
        property(ServerProperties.RESPONSE_SET_STATUS_OVER_SEND_ERROR, true);
    }
}
