package ar.edu.itba.paw.webapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.NullSecurityContextRepository;
import org.springframework.security.web.savedrequest.NullRequestCache;
import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

/** Public MVC reads remain during migration. Authentication is exclusively in the API chain. */
@Configuration
@EnableWebSecurity
@Import(ApiSecurityConfig.class)
public class WebAuthConfig {
    @Bean
    public SecurityFilterChain securityFilterChain(final HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(antMatcher(org.springframework.http.HttpMethod.GET, "/app/**"), antMatcher(org.springframework.http.HttpMethod.HEAD, "/app/**")).permitAll()
                        .requestMatchers(antMatcher("/css/**"), antMatcher("/js/**"), antMatcher("/favicon.ico"))
                            .permitAll()
                        .requestMatchers(antMatcher("/error/**"))
                            .permitAll()
                        .requestMatchers(
                                antMatcher(HttpMethod.GET, "/communities/new"),
                                antMatcher(HttpMethod.GET, "/communities/*/edit"),
                                antMatcher(HttpMethod.POST, "/communities/*/edit"),
                                antMatcher(HttpMethod.POST, "/communities/*/delete"),
                                antMatcher(HttpMethod.GET, "/communities/*/submit"),
                                antMatcher(HttpMethod.GET, "/communities/*/posts/*/edit"),
                                antMatcher(HttpMethod.POST, "/communities"),
                                antMatcher(HttpMethod.POST, "/communities/*/join"),
                                antMatcher(HttpMethod.POST, "/communities/*/posts"),
                                antMatcher(HttpMethod.POST, "/communities/*/posts/*/edit"),
                                antMatcher(HttpMethod.POST, "/communities/*/posts/*/helpful"),
                                antMatcher(HttpMethod.POST, "/communities/*/posts/*/comments/*/helpful"),
                                antMatcher(HttpMethod.POST, "/communities/*/posts/*/comments"),
                                antMatcher(HttpMethod.POST, "/communities/*/posts/*/comments/*/update"),
                                antMatcher(HttpMethod.GET, "/communities/*/members"),
                                antMatcher(HttpMethod.POST, "/communities/*/members/*/kick"),
                                antMatcher(HttpMethod.POST, "/communities/*/members/*/promote"),
                                antMatcher(HttpMethod.POST, "/communities/*/members/*/transfer"),
                                antMatcher(HttpMethod.POST, "/communities/*/posts/*/hide"),
                                antMatcher(HttpMethod.POST, "/communities/*/posts/*/comments/*/hide"),
                                antMatcher(HttpMethod.POST, "/communities/*/posts/*/delete"),
                                antMatcher(HttpMethod.POST, "/communities/*/posts/*/comments/*/delete"),
                                antMatcher(HttpMethod.GET, "/reviews/new"))
                            .authenticated()
                        .requestMatchers(
                                antMatcher(HttpMethod.GET, "/"),
                                antMatcher(HttpMethod.GET, "/cars"),
                                antMatcher(HttpMethod.GET, "/cars/recommend"),
                                antMatcher(HttpMethod.GET, "/cars/recommend/results"),
                                antMatcher(HttpMethod.GET, "/activity"),
                                antMatcher(HttpMethod.GET, "/communities"),
                                antMatcher(HttpMethod.GET, "/communities/*"),
                                antMatcher(HttpMethod.GET, "/communities/*/posts/*"),
                                antMatcher(HttpMethod.GET, "/communities/*/posts/*/images/*"),
                                antMatcher(HttpMethod.GET, "/reviews/car/*"),
                                antMatcher(HttpMethod.GET, "/reviews/*"),
                                antMatcher(HttpMethod.GET, "/car-image"),
                                antMatcher(HttpMethod.GET, "/cars/*/image"),
                                antMatcher(HttpMethod.GET, "/cars/*/images/*"),
                                antMatcher(HttpMethod.GET, "/users/*"),
                                antMatcher(HttpMethod.GET, "/reviews/*/images/*"),
                                antMatcher(HttpMethod.GET, "/login"),
                                antMatcher(HttpMethod.GET, "/register"))
                            .permitAll()
                        .requestMatchers(antMatcher("/admin/**"))
                            .hasRole("ADMIN")
                        .requestMatchers(
                                antMatcher(HttpMethod.POST, "/car-image"),
                                antMatcher(HttpMethod.POST, "/cars/*/image"))
                            .hasRole("ADMIN")
                        .requestMatchers(
                                antMatcher(HttpMethod.POST, "/reviews/*/hide"),
                                antMatcher(HttpMethod.POST, "/reviews/replies/*/hide"))
                            .hasRole("ADMIN")
                        .requestMatchers(
                                antMatcher(HttpMethod.GET, "/cars/new"),
                                antMatcher(HttpMethod.GET, "/reviews/new"),
                                antMatcher(HttpMethod.GET, "/reviews/*/edit"),
                                antMatcher(HttpMethod.GET, "/reviews/*/repost"),
                                antMatcher(HttpMethod.POST, "/reviews/*/repost"),
                                antMatcher(HttpMethod.GET, "/user"),
                                antMatcher(HttpMethod.POST, "/cars"),
                                antMatcher(HttpMethod.POST, "/reviews/*"),
                                antMatcher(HttpMethod.POST, "/reviews/*/delete"),
                                antMatcher(HttpMethod.POST, "/reviews"),
                                antMatcher(HttpMethod.POST, "/reviews/*/like"),
                                antMatcher(HttpMethod.POST, "/reviews/*/replies"),
                                antMatcher(HttpMethod.POST, "/reviews/replies/*/delete"),
                                antMatcher(HttpMethod.POST, "/reviews/replies/*/update"),
                                antMatcher(HttpMethod.POST, "/reviews/replies/*/like"),
                                antMatcher(HttpMethod.POST, "/logout"),
                                antMatcher(HttpMethod.POST, "/user"),
                                antMatcher(HttpMethod.POST, "/user/language"),
                                antMatcher(HttpMethod.POST, "/cars/*/favorite"),
                                antMatcher(HttpMethod.POST, "/users/*/follow"),
                                antMatcher(HttpMethod.POST, "/brand-requests"),
                                antMatcher(HttpMethod.POST, "/body-type-requests"),
                                antMatcher(HttpMethod.POST, "/admin-requests"))
                            .authenticated()
                        .anyRequest().authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .securityContext(context -> context.securityContextRepository(new NullSecurityContextRepository()))
                .requestCache(cache -> cache.requestCache(new NullRequestCache()))
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .rememberMe(remember -> remember.disable())
                .logout(logout -> logout.disable())
                .csrf(csrf -> csrf.disable())
                .exceptionHandling(exceptions -> exceptions.authenticationEntryPoint((request, response, error) -> {
                    if ("GET".equals(request.getMethod()) || "HEAD".equals(request.getMethod())) {
                        response.sendRedirect(request.getContextPath() + "/app/login");
                    } else {
                        response.sendError(401);
                    }
                }));
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() { return new BCryptPasswordEncoder(); }
}
