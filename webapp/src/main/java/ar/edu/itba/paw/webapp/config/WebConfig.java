package ar.edu.itba.paw.webapp.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.SimpleDriverDataSource;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
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
import java.util.concurrent.Executor;

@EnableWebMvc
@EnableAsync
@EnableTransactionManagement
@ComponentScan({ "ar.edu.itba.paw.webapp.controller", "ar.edu.itba.paw.services", "ar.edu.itba.paw.persistence" })
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private static final String DB_PROPERTIES_RESOURCE = "db.properties";
    private static final String MAIL_PROPERTIES_RESOURCE = "mail.properties";

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

    @Bean(name = "mailTaskExecutor")
    public Executor mailTaskExecutor() {
        final ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(2);
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("mail-");
        executor.initialize();
        return executor;
    }

    private MailConfig resolveMailConfig() {
        final String host = normalize(System.getenv("MAIL_HOST"));
        final String portStr = normalize(System.getenv("MAIL_PORT"));
        final String username = normalize(System.getenv("MAIL_USERNAME"));
        final String password = normalize(System.getenv("MAIL_PASSWORD"));
        if (host != null && portStr != null && username != null && password != null) {
            return new MailConfig(host, Integer.parseInt(portStr), username, password);
        }

        final ClassPathResource resource = new ClassPathResource(MAIL_PROPERTIES_RESOURCE);
        if (resource.exists()) {
            final Properties properties = new Properties();
            try (InputStream is = resource.getInputStream()) {
                properties.load(is);
            } catch (final IOException e) {
                throw new IllegalStateException("Failed to load " + MAIL_PROPERTIES_RESOURCE, e);
            }
            return new MailConfig(
                    properties.getProperty("mail.host"),
                    Integer.parseInt(properties.getProperty("mail.port", "587")),
                    properties.getProperty("mail.username"),
                    properties.getProperty("mail.password")
            );
        }

        throw new IllegalStateException(
                "Mail configuration not found. Set MAIL_HOST, MAIL_PORT, MAIL_USERNAME, MAIL_PASSWORD "
                        + "or provide a classpath mail.properties file."
        );
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
