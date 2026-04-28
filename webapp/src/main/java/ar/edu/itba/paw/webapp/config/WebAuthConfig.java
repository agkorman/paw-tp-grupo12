package ar.edu.itba.paw.webapp.config;

import ar.edu.itba.paw.services.UserService;
import ar.edu.itba.paw.webapp.auth.LoginRedirectUtils;
import ar.edu.itba.paw.webapp.auth.PawUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.SavedRequestAwareAuthenticationSuccessHandler;
import org.springframework.security.web.savedrequest.HttpSessionRequestCache;
import org.springframework.security.web.savedrequest.SavedRequest;

import java.util.Optional;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Configuration
@EnableWebSecurity
public class WebAuthConfig {

    private static final String REMEMBER_ME_KEY_ENV = "AUTH_REMEMBER_ME_KEY";
    private static final String LOCAL_REMEMBER_ME_KEY = "local-development-remember-me-key-change-me";

    @Bean
    public SecurityFilterChain securityFilterChain(final HttpSecurity http,
                                                   final UserDetailsService userDetailsService) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(antMatcher("/css/**"), antMatcher("/js/**"), antMatcher("/favicon.ico"))
                            .permitAll()
                        .requestMatchers(antMatcher("/error/**"))
                            .permitAll()
                        .requestMatchers(
                                antMatcher(HttpMethod.GET, "/"),
                                antMatcher(HttpMethod.GET, "/cars"),
                                antMatcher(HttpMethod.GET, "/cars/content"),
                                antMatcher(HttpMethod.GET, "/reviews"),
                                antMatcher(HttpMethod.GET, "/reviews/feed"),
                                antMatcher(HttpMethod.GET, "/car-image"),
                                antMatcher(HttpMethod.GET, "/cars/*/image"),
                                antMatcher(HttpMethod.GET, "/cars/*/images/*"),
                                antMatcher(HttpMethod.GET, "/profiles/*"),
                                antMatcher(HttpMethod.GET, "/login"),
                                antMatcher(HttpMethod.GET, "/register"))
                            .permitAll()
                        .requestMatchers(antMatcher(HttpMethod.POST, "/register"))
                            .permitAll()
                        .requestMatchers(antMatcher("/admin/**"))
                            .hasRole("ADMIN")
                        .requestMatchers(antMatcher("/moderation/**"))
                            .hasAnyRole("MODERATOR", "ADMIN")
                        .requestMatchers(
                                antMatcher(HttpMethod.POST, "/car-image"),
                                antMatcher(HttpMethod.POST, "/cars/*/image"))
                            .hasRole("ADMIN")
                        .requestMatchers(
                                antMatcher(HttpMethod.POST, "/cars"),
                                antMatcher(HttpMethod.POST, "/reviews"),
                                antMatcher(HttpMethod.POST, "/reviews/*/like"),
                                antMatcher(HttpMethod.POST, "/reviews/*/replies"),
                                antMatcher(HttpMethod.POST, "/reviews/replies/*/like"),
                                antMatcher(HttpMethod.POST, "/logout"),
                                antMatcher(HttpMethod.POST, "/cars/*/favorite"),
                                antMatcher(HttpMethod.POST, "/profiles/*/follow"),
                                antMatcher(HttpMethod.POST, "/brand-requests"),
                                antMatcher(HttpMethod.POST, "/body-type-requests"))
                            .authenticated()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/", false)
                        .successHandler(loginSuccessHandler())
                        .failureHandler(loginFailureHandler())
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID", "remember-me")
                        .permitAll())
                .exceptionHandling(exceptions -> exceptions
                        .accessDeniedPage("/error/403"))
                .rememberMe(rememberMe -> rememberMe
                        .rememberMeParameter("remember-me")
                        .key(rememberMeKey())
                        .userDetailsService(userDetailsService));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(final UserService userService) {
        return new PawUserDetailsService(userService);
    }

    @Bean
    public AuthenticationManager authenticationManager(final AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    private String rememberMeKey() {
        final String configuredKey = System.getenv(REMEMBER_ME_KEY_ENV);
        if (configuredKey == null || configuredKey.trim().isEmpty()) {
            return LOCAL_REMEMBER_ME_KEY;
        }
        return configuredKey.trim();
    }

    private AuthenticationSuccessHandler loginSuccessHandler() {
        final SavedRequestAwareAuthenticationSuccessHandler savedRequestHandler =
                new SavedRequestAwareAuthenticationSuccessHandler();
        final HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
        savedRequestHandler.setDefaultTargetUrl("/");

        return (request, response, authentication) -> {
            final Optional<String> redirect = LoginRedirectUtils.safeRedirect(
                    request.getParameter(LoginRedirectUtils.REDIRECT_PARAM)
            );
            if (redirect.isEmpty()) {
                final SavedRequest savedRequest = requestCache.getRequest(request, response);
                if (savedRequest != null && !HttpMethod.GET.name().equalsIgnoreCase(savedRequest.getMethod())) {
                    requestCache.removeRequest(request, response);
                    response.sendRedirect(request.getContextPath() + "/");
                    return;
                }
                savedRequestHandler.onAuthenticationSuccess(request, response, authentication);
                return;
            }

            final String target = LoginRedirectUtils.appendIntent(
                    redirect.get(),
                    request.getParameter(LoginRedirectUtils.INTENT_PARAM)
            );
            response.sendRedirect(request.getContextPath() + target);
        };
    }

    private AuthenticationFailureHandler loginFailureHandler() {
        return (request, response, exception) -> {
            String target = "/login?error";
            final String redirect = request.getParameter(LoginRedirectUtils.REDIRECT_PARAM);
            if (LoginRedirectUtils.safeRedirect(redirect).isPresent()) {
                target = LoginRedirectUtils.appendQueryParam(target, LoginRedirectUtils.REDIRECT_PARAM, redirect);
            }
            final String intent = request.getParameter(LoginRedirectUtils.INTENT_PARAM);
            if (LoginRedirectUtils.safeIntent(intent).isPresent()) {
                target = LoginRedirectUtils.appendQueryParam(target, LoginRedirectUtils.INTENT_PARAM, intent);
            }
            response.sendRedirect(request.getContextPath() + target);
        };
    }
}
