package ar.edu.itba.paw.webapp.api;

import ar.edu.itba.paw.webapp.api.common.ApiValidationExceptionMapper;
import javax.inject.Provider;
import javax.ws.rs.Priorities;
import javax.ws.rs.ext.ExceptionMapper;
import org.glassfish.jersey.internal.inject.AbstractBinder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.server.ResourceConfig;
import org.glassfish.jersey.server.ServerProperties;

/** Jersey uses the existing root Spring context through jersey-spring5. */
public class ApiApplication extends ResourceConfig {

    public ApiApplication() {
        packages("ar.edu.itba.paw.webapp.api");
        // Jackson's default mappers expose parser messages and classify response failures as 400.
        register(JacksonFeature.withoutExceptionMappers());
        register(new AbstractBinder() {
            @Override
            protected void configure() {
                // jersey-spring5 does not copy @Priority onto its Spring bean supplier bindings.
                final Provider<ApiValidationExceptionMapper> mapper =
                        createManagedInstanceProvider(ApiValidationExceptionMapper.class);
                bindFactory(mapper::get).to(ExceptionMapper.class).ranked(Priorities.USER - 1000);
            }
        });
        // Return API errors as JSON instead of forwarding to the WAR's HTML error pages.
        property(ServerProperties.RESPONSE_SET_STATUS_OVER_SEND_ERROR, true);
    }
}
