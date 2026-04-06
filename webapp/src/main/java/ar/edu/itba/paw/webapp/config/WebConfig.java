package ar.edu.itba.paw.webapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

import javax.sql.DataSource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

@EnableWebMvc
@EnableTransactionManagement
@ComponentScan({ "ar.edu.itba.paw.webapp.controller", "ar.edu.itba.paw.services", "ar.edu.itba.paw.persistence" })
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final String DB_PROPERTIES_RESOURCE = "db.properties";

    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/favicon.ico").addResourceLocations("/");
        registry.addResourceHandler("/css/**").addResourceLocations("/css/");
        registry.addResourceHandler("/js/**").addResourceLocations("/js/");
    }

    @Bean
    public ViewResolver viewResolver() {
        final InternalResourceViewResolver resolver = new InternalResourceViewResolver();
        resolver.setViewClass(JstlView.class);
        resolver.setPrefix("/WEB-INF/jsp/");
        return resolver;
    }

    @Bean(name = "multipartResolver")
    public StandardServletMultipartResolver multipartResolver() {
        return new StandardServletMultipartResolver();
    }

    @Bean
    public DataSource dataSource() {
        final DbConfig config = resolveDbConfig();
        final SimpleDriverDataSource ds = new SimpleDriverDataSource();
        ds.setDriverClass(org.postgresql.Driver.class);
        ds.setUrl(config.getUrl());
        ds.setUsername(config.getUsername());
        ds.setPassword(config.getPassword());
        return ds;
    }

    @Bean
    public DataSourceTransactionManager transactionManager(final DataSource dataSource) {
        return new DataSourceTransactionManager(dataSource);
    }

    private DbConfig resolveDbConfig() {
        final DbConfig envConfig = loadFromEnvironment();
        if (envConfig != null) {
            return envConfig;
        }

        final DbConfig propertiesConfig = loadFromProperties();
        if (propertiesConfig != null) {
            return propertiesConfig;
        }

        throw new IllegalStateException(
                "Database configuration not found. Set DB_URL, DB_USERNAME and DB_PASSWORD environment variables "
                        + "or provide a classpath db.properties with db.url, db.username and db.password"
        );
    }

    private DbConfig loadFromEnvironment() {
        final String url = normalize(System.getenv("DB_URL"));
        final String username = normalize(System.getenv("DB_USERNAME"));
        final String password = normalize(System.getenv("DB_PASSWORD"));
        if (isConfigured(url, username, password)) {
            return new DbConfig(url, username, password);
        }
        return null;
    }

    private DbConfig loadFromProperties() {
        final ClassPathResource resource = new ClassPathResource(DB_PROPERTIES_RESOURCE);
        if (!resource.exists()) {
            return null;
        }

        final Properties properties = new Properties();
        try (InputStream inputStream = resource.getInputStream()) {
            properties.load(inputStream);
        } catch (final IOException e) {
            throw new IllegalStateException("Failed to load " + DB_PROPERTIES_RESOURCE, e);
        }

        final String url = normalize(properties.getProperty("db.url"));
        final String username = normalize(properties.getProperty("db.username"));
        final String password = normalize(properties.getProperty("db.password"));
        if (isConfigured(url, username, password)) {
            return new DbConfig(url, username, password);
        }
        return null;
    }

    private boolean isConfigured(final String url, final String username, final String password) {
        return isUsableValue(url) && isUsableValue(username) && isUsableValue(password);
    }

    private boolean isUsableValue(final String value) {
        return value != null && !value.contains("CHANGE_ME");
    }

    private String normalize(final String value) {
        if (value == null) {
            return null;
        }
        final String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    @Bean
    public DataSourceInitializer dataSourceInitializer(final DataSource dataSource) {
        final DataSourceInitializer initializer = new DataSourceInitializer();
        initializer.setDataSource(dataSource);
        final ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.addScript(new ClassPathResource("schema.sql"));
        initializer.setDatabasePopulator(populator);
        return initializer;
    }

    private static final class DbConfig {
        private final String url;
        private final String username;
        private final String password;

        private DbConfig(final String url, final String username, final String password) {
            this.url = url;
            this.username = username;
            this.password = password;
        }

        private String getUrl() {
            return url;
        }

        private String getUsername() {
            return username;
        }

        private String getPassword() {
            return password;
        }
    }
}
