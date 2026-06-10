package ar.edu.itba.paw.webapp.config;

import ar.edu.itba.paw.webapp.exception.WebAppConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.support.ResourceBundleMessageSource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.JpaVendorAdapter;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.validation.Validator;
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;
import org.springframework.web.multipart.support.StandardServletMultipartResolver;
import org.springframework.web.servlet.view.InternalResourceViewResolver;
import org.springframework.web.servlet.view.JstlView;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Properties;
import java.util.concurrent.Executor;

@EnableWebMvc
@EnableAsync
@EnableScheduling
@EnableTransactionManagement
@ComponentScan({
        "ar.edu.itba.paw.webapp.controller",
        "ar.edu.itba.paw.webapp.auth",
        "ar.edu.itba.paw.webapp.validation",
        "ar.edu.itba.paw.webapp.util",
        "ar.edu.itba.paw.services",
        "ar.edu.itba.paw.persistence"
})
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final Logger LOGGER = LoggerFactory.getLogger(WebConfig.class);

    private static final String DB_PROPERTIES_RESOURCE = "db.properties";
    private static final String MAIL_PROPERTIES_RESOURCE = "mail.properties";

    /**
     * Default when {@code APP_BASE_URL} and {@code app.baseUrl} are unset (local {@code mvn jetty:run}).
     */
    private static final String DEFAULT_LOCAL_APP_BASE_URL = "http://localhost:8080";

    @Override
    public void addResourceHandlers(final ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/favicon.ico").addResourceLocations("/");
        registry.addResourceHandler("/css/**").addResourceLocations("/css/");
        registry.addResourceHandler("/js/**").addResourceLocations("/js/");
    }

    @Override
    public void addInterceptors(final InterceptorRegistry registry) {
        registry.addInterceptor(userLocaleInterceptor());
        registry.addInterceptor(localeChangeInterceptor());
    }

    @Bean
    public UserLocaleInterceptor userLocaleInterceptor() {
        return new UserLocaleInterceptor(localeResolver());
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
    public MessageSource messageSource() {
        final ResourceBundleMessageSource messageSource = new ResourceBundleMessageSource();
        messageSource.setBasename("messages");
        messageSource.setDefaultEncoding("UTF-8");
        messageSource.setFallbackToSystemLocale(false);
        return messageSource;
    }

    @Bean
    public LocaleResolver localeResolver() {
        return new SessionLocaleResolver() {
            @Override
            protected Locale determineDefaultLocale(final HttpServletRequest request) {
                return request.getLocale();
            }
        };
    }

    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        final LocaleChangeInterceptor interceptor = new LocaleChangeInterceptor();
        interceptor.setParamName("lang");
        interceptor.setIgnoreInvalidLocale(true);
        return interceptor;
    }

    @Bean
    public LocalValidatorFactoryBean validator(final MessageSource messageSource) {
        final LocalValidatorFactoryBean validator = new LocalValidatorFactoryBean();
        validator.setValidationMessageSource(messageSource);
        return validator;
    }

    @Override
    public Validator getValidator() {
        return validator(messageSource());
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
    // dataSourceInitializer must run schema.sql before Hibernate validate checks column existence
    @DependsOn("dataSourceInitializer")
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(final DataSource dataSource) {
        final LocalContainerEntityManagerFactoryBean factoryBean = new LocalContainerEntityManagerFactoryBean();
        factoryBean.setPackagesToScan("ar.edu.itba.paw.model");
        factoryBean.setDataSource(dataSource);
        final JpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        factoryBean.setJpaVendorAdapter(vendorAdapter);
        final Properties properties = new Properties();
        properties.setProperty("hibernate.hbm2ddl.auto", "validate");
        properties.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQL92Dialect");
        properties.setProperty("hibernate.show_sql", "false");
        properties.setProperty("hibernate.format_sql", "false");
        factoryBean.setJpaProperties(properties);
        return factoryBean;
    }

    @Bean
    public JpaTransactionManager transactionManager(final EntityManagerFactory emf) {
        return new JpaTransactionManager(emf);
    }

    @Bean
    public PersistenceExceptionTranslationPostProcessor exceptionTranslation() {
        return new PersistenceExceptionTranslationPostProcessor();
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

        throw new WebAppConfigurationException(
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
        final Properties properties = loadPropertiesIfExists(DB_PROPERTIES_RESOURCE);
        if (properties == null) {
            return null;
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
    public JavaMailSender mailSender() {
        final MailConfig config = resolveMailConfig();
        final JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(config.getHost());
        sender.setPort(config.getPort());
        sender.setUsername(config.getUsername());
        sender.setPassword(config.getPassword());
        final Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");
        return sender;
    }

    @Bean(name = "appBaseUrl")
    public String appBaseUrl() {
        final String envValue = normalizeBaseUrl(System.getenv("APP_BASE_URL"));
        if (envValue != null) {
            return envValue;
        }

        final Properties properties = loadPropertiesIfExists(MAIL_PROPERTIES_RESOURCE);
        if (properties != null) {
            final String propValue = normalizeBaseUrl(properties.getProperty("app.baseUrl"));
            if (propValue != null) {
                return propValue;
            }
        }

        LOGGER.warn(
                "APP_BASE_URL / app.baseUrl not set; using local default {} for links in outbound mail. "
                        + "Set APP_BASE_URL (or app.baseUrl in classpath mail.properties) for non-local deployments.",
                DEFAULT_LOCAL_APP_BASE_URL
        );
        return DEFAULT_LOCAL_APP_BASE_URL;
    }

    private String normalizeBaseUrl(final String value) {
        final String normalized = normalize(value);
        if (normalized == null) {
            return null;
        }
        if (normalized.endsWith("/") && !normalized.endsWith("://")) {
            return normalized.substring(0, normalized.length() - 1);
        }
        return normalized;
    }

    @Bean(name = "mailTaskExecutor")
    public Executor mailTaskExecutor() {
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(2);
        // Sized to absorb the per-user fan-out of the weekly digest without saturating.
        executor.setQueueCapacity(500);
        executor.setThreadNamePrefix("mail-");
        // Email must never block the HTTP response (every EmailService method is @Async).
        // If the executor is ever saturated, log and drop the task on the executor side -
        // do NOT run it on the caller (that would block the request thread). Dropping is
        // visible via WARN instead of a silent RejectedExecutionException.
        executor.setRejectedExecutionHandler((task, poolExecutor) ->
            LOGGER.warn("mail task rejected: mailTaskExecutor saturated, dropping email task"));
        // On shutdown, finish queued/in-flight emails instead of discarding them.
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }

    private MailConfig resolveMailConfig() {
        final MailConfig envConfig = loadMailConfigFromEnvironment();
        if (envConfig != null) {
            return envConfig;
        }

        final Properties properties = loadPropertiesIfExists(MAIL_PROPERTIES_RESOURCE);
        if (properties != null) {
            return new MailConfig(
                    normalize(properties.getProperty("mail.host")),
                    Integer.parseInt(properties.getProperty("mail.port", "587").trim()),
                    normalize(properties.getProperty("mail.username")),
                    normalize(properties.getProperty("mail.password"))
            );
        }

        throw new WebAppConfigurationException(
                "Mail configuration not found. Set MAIL_HOST, MAIL_PORT, MAIL_USERNAME, MAIL_PASSWORD "
                        + "or provide a classpath mail.properties file."
        );
    }

    private MailConfig loadMailConfigFromEnvironment() {
        final String host = normalize(System.getenv("MAIL_HOST"));
        final String portStr = normalize(System.getenv("MAIL_PORT"));
        final String username = normalize(System.getenv("MAIL_USERNAME"));
        final String password = normalize(System.getenv("MAIL_PASSWORD"));
        if (host != null && portStr != null && username != null && password != null) {
            return new MailConfig(host, Integer.parseInt(portStr), username, password);
        }
        return null;
    }

    private Properties loadPropertiesIfExists(final String resourceName) {
        final ClassPathResource resource = new ClassPathResource(resourceName);
        if (!resource.exists()) {
            return null;
        }

        final Properties properties = new Properties();
        try (InputStream inputStream = resource.getInputStream()) {
            properties.load(inputStream);
        } catch (final IOException e) {
            LOGGER.error("failed to load classpath properties resource={}", resourceName, e);
            throw new WebAppConfigurationException("Failed to load " + resourceName, e);
        }
        return properties;
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

    private static final class MailConfig {
        private final String host;
        private final int port;
        private final String username;
        private final String password;

        private MailConfig(final String host, final int port, final String username, final String password) {
            this.host = host;
            this.port = port;
            this.username = username;
            this.password = password;
        }

        private String getHost() { return host; }
        private int getPort() { return port; }
        private String getUsername() { return username; }
        private String getPassword() { return password; }
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
