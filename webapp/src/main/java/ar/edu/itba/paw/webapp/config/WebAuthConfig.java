package ar.edu.itba.paw.webapp.config;

import ar.edu.itba.paw.services.UserService;
import ar.edu.itba.paw.webapp.auth.PawUserDetailsService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

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
                        .requestMatchers(
                                antMatcher(HttpMethod.GET, "/"),
                                antMatcher(HttpMethod.GET, "/cars"),
                                antMatcher(HttpMethod.GET, "/cars/content"),
                                antMatcher(HttpMethod.GET, "/reviews"),
                                antMatcher(HttpMethod.GET, "/reviews/feed"),
                                antMatcher(HttpMethod.GET, "/car-image"),
                                antMatcher(HttpMethod.GET, "/cars/*/image"),
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
                                antMatcher(HttpMethod.POST, "/cars"),
                                antMatcher(HttpMethod.POST, "/reviews"),
                                antMatcher(HttpMethod.POST, "/logout"),
                                antMatcher(HttpMethod.POST, "/car-image"),
                                antMatcher(HttpMethod.POST, "/cars/*/image"))
                            .authenticated()
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/login")
                        .usernameParameter("email")
                        .passwordParameter("password")
                        .defaultSuccessUrl("/", false)
                        .failureUrl("/login?error")
                        .permitAll())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID", "remember-me")
                        .permitAll())
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

    private String rememberMeKey() {
        final String configuredKey = System.getenv(REMEMBER_ME_KEY_ENV);
        if (configuredKey == null || configuredKey.trim().isEmpty()) {
            return LOCAL_REMEMBER_ME_KEY;
        }
        return configuredKey.trim();
    }
}
