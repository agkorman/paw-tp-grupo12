package ar.edu.itba.paw.webapp.config;

import ar.edu.itba.paw.webapp.api.common.ApiSecurityErrorHandler;
import ar.edu.itba.paw.webapp.auth.ApiAuthenticationFilter;
import ar.edu.itba.paw.services.AccessTokenService;
import ar.edu.itba.paw.services.TokenAuthenticationService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.savedrequest.NullRequestCache;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.authentication.AnonymousAuthenticationFilter;
import org.springframework.security.web.context.NullSecurityContextRepository;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

/** Header-only authentication for Jersey. Browser cookies never establish an API identity. */
@Configuration
public class ApiSecurityConfig {

    @Bean
    public ApiSecurityErrorHandler apiSecurityErrorHandler() {
        return new ApiSecurityErrorHandler();
    }

    @Bean
    public ApiAuthenticationFilter apiAuthenticationFilter(final TokenAuthenticationService authentication,
                                                           final AccessTokenService accessTokens,
                                                           final ApiSecurityErrorHandler errors) {
        return new ApiAuthenticationFilter(authentication, accessTokens, errors);
    }

    @Bean
    @Order(1)
    public SecurityFilterChain apiSecurityFilterChain(final HttpSecurity http,
                                                       final ApiSecurityErrorHandler errorHandler,
                                                       final ApiAuthenticationFilter authenticationFilter) throws Exception {
        http.securityMatcher(antMatcher("/api/**"))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(antMatcher(HttpMethod.GET, "/api"), antMatcher(HttpMethod.GET, "/api/")).permitAll()
                        .requestMatchers(antMatcher(HttpMethod.GET, "/api/communities/*")).permitAll()
                        .requestMatchers(antMatcher(HttpMethod.POST, "/api/users"),
                                antMatcher(HttpMethod.GET, "/api/users/*")).permitAll()
                        // SessionResource verifies refresh credentials itself to revoke without rotation.
                        // Missing credentials and attempts against other sessions are rejected there.
                        .requestMatchers(antMatcher(HttpMethod.DELETE, "/api/users/*/sessions/*")).permitAll()
                        .requestMatchers(antMatcher(HttpMethod.PATCH, "/api/users/*"),
                                antMatcher(HttpMethod.GET, "/api/users/*/sessions/*")).authenticated()
                        .anyRequest().denyAll())
                .requestCache(cache -> cache.requestCache(new NullRequestCache()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .securityContext(context -> context.securityContextRepository(new NullSecurityContextRepository()))
                .csrf(csrf -> csrf.disable())
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .rememberMe(remember -> remember.disable())
                .logout(logout -> logout.disable())
                .addFilterBefore(authenticationFilter, AnonymousAuthenticationFilter.class)
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(errorHandler)
                        .accessDeniedHandler(errorHandler));
        return http.build();
    }
}
