package ar.edu.itba.paw.webapp.config;

import ar.edu.itba.paw.webapp.api.common.ApiSecurityErrorHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.savedrequest.NullRequestCache;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

/** Initial API boundary; P2 will integrate token authentication into this chain. */
@Configuration
public class ApiSecurityConfig {

    @Bean
    public ApiSecurityErrorHandler apiSecurityErrorHandler() {
        return new ApiSecurityErrorHandler();
    }

    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(final HttpSecurity http,
                                                       final ApiSecurityErrorHandler errorHandler) throws Exception {
        http.securityMatcher(antMatcher("/api/**"))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(antMatcher(HttpMethod.GET, "/api/communities/*")).permitAll()
                        .anyRequest().denyAll())
                .requestCache(cache -> cache.requestCache(new NullRequestCache()))
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(errorHandler)
                        .accessDeniedHandler(errorHandler));
        // Keep CSRF protection while session cookies can authenticate requests.
        return http.build();
    }
}
