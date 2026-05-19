package ar.edu.itba.paw.webapp.config;

import ar.edu.itba.paw.services.UserService;
import ar.edu.itba.paw.webapp.auth.AuthenticatedUser;
import ar.edu.itba.paw.webapp.auth.LoginRedirectUtils;
import ar.edu.itba.paw.webapp.auth.PawUserDetailsService;
import ar.edu.itba.paw.webapp.util.LogSanitizer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;
import java.util.Optional;

import static org.springframework.security.web.util.matcher.AntPathRequestMatcher.antMatcher;

@Configuration
@EnableWebSecurity
public class WebAuthConfig {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebAuthConfig.class);

    private static final String REMEMBER_ME_KEY_ENV = "AUTH_REMEMBER_ME_KEY";
    private static final String LOCAL_REMEMBER_ME_KEY = "local-development-remember-me-key-change-me";

    @Bean
    public SecurityFilterChain securityFilterChain(final HttpSecurity http,
                                                   final UserDetailsService userDetailsService,
                                                   final LocaleResolver localeResolver) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(antMatcher("/css/**"), antMatcher("/js/**"), antMatcher("/favicon.ico"))
                            .permitAll()
                        .requestMatchers(antMatcher("/error/**"))
                            .permitAll()
                        .requestMatchers(
                                antMatcher(HttpMethod.GET, "/"),
                                antMatcher(HttpMethod.GET, "/cars"),
                                antMatcher(HttpMethod.GET, "/cars/recommend"),
                                antMatcher(HttpMethod.GET, "/cars/recommend/results"),
                                antMatcher(HttpMethod.GET, "/activity"),
                                antMatcher(HttpMethod.GET, "/reviews/car/*"),
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
                        .requestMatchers(
                                antMatcher(HttpMethod.POST, "/car-image"),
                                antMatcher(HttpMethod.POST, "/cars/*/image"))
                            .hasRole("ADMIN")
                        .requestMatchers(antMatcher(HttpMethod.POST, "/reviews/*/hide"))
                            .hasRole("ADMIN")
                        .requestMatchers(
                                antMatcher(HttpMethod.GET, "/cars/new"),
                                antMatcher(HttpMethod.GET, "/reviews/new"),
                                antMatcher(HttpMethod.GET, "/reviews/*/edit"),
                                antMatcher(HttpMethod.GET, "/profile"),
                                antMatcher(HttpMethod.POST, "/cars"),
                                antMatcher(HttpMethod.POST, "/reviews/*"),
                                antMatcher(HttpMethod.POST, "/reviews/*/delete"),
                                antMatcher(HttpMethod.POST, "/reviews"),
                                antMatcher(HttpMethod.POST, "/reviews/*/like"),
                                antMatcher(HttpMethod.POST, "/reviews/*/replies"),
                                antMatcher(HttpMethod.POST, "/reviews/replies/*/like"),
                                antMatcher(HttpMethod.POST, "/logout"),
                                antMatcher(HttpMethod.POST, "/profile"),
                                antMatcher(HttpMethod.POST, "/profile/language"),
                                antMatcher(HttpMethod.POST, "/cars/*/favorite"),
                                antMatcher(HttpMethod.POST, "/profiles/*/follow"),
                                antMatcher(HttpMethod.POST, "/brand-requests"),
                                antMatcher(HttpMethod.POST, "/body-type-requests"),
                                antMatcher(HttpMethod.POST, "/admin-requests"))
                            .authenticated()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/", false)
                        .successHandler(loginSuccessHandler(localeResolver))
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
            LOGGER.warn("AUTH_REMEMBER_ME_KEY not configured; falling back to local development key");
            return LOCAL_REMEMBER_ME_KEY;
        }
        return configuredKey.trim();
    }

    private AuthenticationSuccessHandler loginSuccessHandler(final LocaleResolver localeResolver) {
        final SavedRequestAwareAuthenticationSuccessHandler savedRequestHandler =
                new SavedRequestAwareAuthenticationSuccessHandler();
        final HttpSessionRequestCache requestCache = new HttpSessionRequestCache();
        savedRequestHandler.setDefaultTargetUrl("/");

        return (request, response, authentication) -> {
            LOGGER.info("login success user={}",
                    authentication == null
                            ? "<unknown>"
                            : LogSanitizer.forLog(authentication.getName(), LogSanitizer.MAX_LOG_EMAIL_CODE_POINTS));
            if (authentication != null && authentication.getPrincipal() instanceof AuthenticatedUser authenticatedUser) {
                localeResolver.setLocale(
                        request,
                        response,
                        Locale.forLanguageTag(authenticatedUser.getPreferredLocale())
                );
            }
            final Optional<String> redirect = LoginRedirectUtils.safeRedirect(
                    request.getParameter(LoginRedirectUtils.REDIRECT_PARAM),
                    request.getContextPath()
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
            LOGGER.warn("login failure email={} reason={}",
                    LogSanitizer.forLog(request.getParameter("email"), LogSanitizer.MAX_LOG_EMAIL_CODE_POINTS),
                    exception == null ? "<unknown>" : exception.getClass().getSimpleName());
            String target = "/login?error";
            final String redirect = request.getParameter(LoginRedirectUtils.REDIRECT_PARAM);
            final Optional<String> safeRedirect = LoginRedirectUtils.safeRedirect(redirect, request.getContextPath());
            if (safeRedirect.isPresent()) {
                target = LoginRedirectUtils.appendQueryParam(target, LoginRedirectUtils.REDIRECT_PARAM, safeRedirect.get());
            }
            final String intent = request.getParameter(LoginRedirectUtils.INTENT_PARAM);
            if (LoginRedirectUtils.safeIntent(intent).isPresent()) {
                target = LoginRedirectUtils.appendQueryParam(target, LoginRedirectUtils.INTENT_PARAM, intent);
            }
            response.sendRedirect(request.getContextPath() + target);
        };
    }
}
