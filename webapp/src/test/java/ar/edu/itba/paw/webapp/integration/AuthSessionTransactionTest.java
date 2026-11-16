package ar.edu.itba.paw.webapp.integration;

import ar.edu.itba.paw.model.RefreshResult;
import ar.edu.itba.paw.model.SessionCredentials;
import ar.edu.itba.paw.model.SessionRevocationResult;
import ar.edu.itba.paw.persistence.AuthSessionJpaDao;
import ar.edu.itba.paw.services.AuthSessionService;
import ar.edu.itba.paw.services.AuthSessionServiceImpl;
import ar.edu.itba.paw.services.RefreshTokenCodec;
import java.sql.Connection;
import java.sql.Statement;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.annotation.PersistenceExceptionTranslationPostProcessor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Real service proxies, DAO and committed transactions, outside the unit-test modules.
 * Defaults to an isolated HSQLDB. Set PAW_AUTH_TEST_JDBC_URL/USERNAME/PASSWORD to exercise
 * PostgreSQL; the test creates and drops its own random schema, never the public schema.
 */
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AuthSessionTransactionTest.Config.class)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
class AuthSessionTransactionTest {

    private static final Instant NOW = Instant.parse("2026-11-15T18:00:00Z");

    @Autowired private AuthSessionService service;
    @Autowired private RefreshTokenCodec codec;
    @Autowired private JdbcTemplate jdbc;
    @Autowired private PlatformTransactionManager transactionManager;

    @AfterEach
    void removeTestRows() {
        jdbc.update("DELETE FROM users"); // This DataSource belongs exclusively to this test class.
    }

    @Test
    void sessionCreationStoresHashAndPreservesExistingUser() {
        // Arrange
        final long userId = user();
        // Exercise
        final SessionCredentials result = service.createSession(userId, true);
        // Assertions
        assertEquals(NOW.plus(Duration.ofDays(30)), result.getExpiresAt());
        final String storedHash = jdbc.queryForObject("SELECT token_hash FROM auth_refresh_tokens WHERE session_id = ?",
                String.class, result.getSessionId());
        assertEquals(codec.digest(result.getRefreshToken()).orElseThrow(), storedHash);
        assertNotEquals(result.getRefreshToken(), storedHash);
        assertEquals("existing-password-hash", jdbc.queryForObject("SELECT password FROM users WHERE user_id = ?",
                String.class, userId));
        assertEquals(NOW, jdbc.queryForObject("SELECT created_at FROM auth_sessions WHERE session_id = ?",
                (rs, row) -> rs.getTimestamp(1).toInstant(), result.getSessionId()));
    }

    @Test
    void rotationCommitsHistoryAndKeepsAbsoluteDeadline() {
        // Arrange
        final SessionCredentials initial = service.createSession(user(), false);
        // Exercise
        final RefreshResult result = service.rotate(initial.getRefreshToken());
        // Assertions
        assertEquals(RefreshResult.Status.ISSUED, result.getStatus());
        assertEquals(initial.getExpiresAt(), result.getCredentials().orElseThrow().getExpiresAt());
        assertNotEquals(initial.getRefreshToken(), result.getCredentials().orElseThrow().getRefreshToken());
        assertEquals(2, tokenCount(initial.getSessionId()));
        assertEquals(1, jdbc.queryForObject("SELECT COUNT(*) FROM auth_refresh_tokens WHERE session_id = ? AND consumed_at IS NULL",
                Integer.class, initial.getSessionId()));
        assertEquals(NOW, jdbc.queryForObject("SELECT consumed_at FROM auth_refresh_tokens WHERE token_hash = ?",
                (rs, row) -> rs.getTimestamp(1).toInstant(), codec.digest(initial.getRefreshToken()).orElseThrow()));
    }

    @Test
    void replayRevocationSurvivesCallerRollbackAndDoesNotRevokeOtherSessions() {
        // Arrange
        final long userId = user();
        final SessionCredentials first = service.createSession(userId, false);
        final SessionCredentials other = service.createSession(userId, false);
        final SessionCredentials replacement = service.rotate(first.getRefreshToken()).getCredentials().orElseThrow();
        final TransactionTemplate outer = new TransactionTemplate(transactionManager);
        // Exercise
        final IllegalStateException result = assertThrows(IllegalStateException.class, () -> outer.execute(status -> rejectAfterReplay(first)));
        // Assertions
        assertEquals("caller rejected the request", result.getMessage());
        assertEquals("REFRESH_REUSE", reason(first.getSessionId()));
        assertEquals(RefreshResult.Status.REVOKED, service.rotate(replacement.getRefreshToken()).getStatus());
        assertNull(reason(other.getSessionId()));
        assertEquals(RefreshResult.Status.ISSUED, service.rotate(other.getRefreshToken()).getStatus());
    }

    @Test
    // HSQLDB's FOR UPDATE controls cursor updatability, not PostgreSQL row-lock semantics.
    @EnabledIfEnvironmentVariable(named = "PAW_AUTH_TEST_JDBC_URL", matches = "jdbc:postgresql:.*")
    void concurrentRenewalsCannotBothSucceed() throws Exception {
        // Arrange
        final SessionCredentials initial = service.createSession(user(), false);
        // Exercise
        final List<RefreshResult> results = renewConcurrently(initial.getRefreshToken());
        // Assertions
        assertEquals(Set.of(RefreshResult.Status.ISSUED, RefreshResult.Status.REUSED),
                new java.util.HashSet<>(List.of(results.get(0).getStatus(), results.get(1).getStatus())));
        assertEquals(2, tokenCount(initial.getSessionId()));
        assertEquals("REFRESH_REUSE", reason(initial.getSessionId()));
        final SessionCredentials replacement = results.stream().flatMap(r -> r.getCredentials().stream()).findFirst().orElseThrow();
        assertEquals(RefreshResult.Status.REVOKED, service.rotate(replacement.getRefreshToken()).getStatus());
    }

    @Test
    void failedReplacementRollsBackConsumption() {
        // Arrange
        final SessionCredentials initial = service.createSession(user(), false);
        final String firstHash = codec.digest(initial.getRefreshToken()).orElseThrow();
        jdbc.execute("ALTER TABLE auth_refresh_tokens ADD CONSTRAINT test_only_original_hash CHECK (token_hash = '" + firstHash + "')");
        try {
            // Exercise
            final RuntimeException result = assertThrows(RuntimeException.class, () -> service.rotate(initial.getRefreshToken()));
            // Assertions
            assertNotNull(result.getCause());
            assertEquals(1, tokenCount(initial.getSessionId()));
            assertNull(jdbc.queryForObject("SELECT consumed_at FROM auth_refresh_tokens WHERE session_id = ?",
                    Object.class, initial.getSessionId()));
            assertNull(reason(initial.getSessionId()));
        } finally {
            jdbc.execute("ALTER TABLE auth_refresh_tokens DROP CONSTRAINT test_only_original_hash");
        }
    }

    @Test
    void refreshLogoutCommitsRevocationWithoutIssuingReplacement() {
        // Arrange
        final SessionCredentials initial = service.createSession(user(), false);
        // Exercise
        final SessionRevocationResult result = service.revokeWithRefresh(initial.getUserId(), initial.getSessionId(), initial.getRefreshToken());
        // Assertions
        assertEquals(SessionRevocationResult.REVOKED, result);
        assertEquals("LOGOUT", reason(initial.getSessionId()));
        assertEquals(1, tokenCount(initial.getSessionId()));
        assertEquals(RefreshResult.Status.REVOKED, service.rotate(initial.getRefreshToken()).getStatus());
    }

    @Test
    void cleanupRetainsRevokedHistoryUntilDeadlineAndCascadesAfterwards() {
        // Arrange
        final SessionCredentials expired = service.createSession(user(), false);
        final SessionCredentials unexpired = service.createSession(user(), true);
        service.revoke(unexpired.getUserId(), unexpired.getSessionId(), unexpired.getUserId(), unexpired.getSessionId());
        jdbc.update("UPDATE auth_sessions SET created_at = ?, expires_at = ? WHERE session_id = ?",
                java.sql.Timestamp.from(NOW.minusSeconds(86400)), java.sql.Timestamp.from(NOW), expired.getSessionId());
        // Exercise
        final int result = service.deleteExpiredSessions();
        // Assertions
        assertEquals(1, result);
        assertEquals(0, tokenCount(expired.getSessionId()));
        assertEquals(0, jdbc.queryForObject("SELECT COUNT(*) FROM auth_sessions WHERE session_id = ?", Integer.class, expired.getSessionId()));
        assertEquals(1, tokenCount(unexpired.getSessionId()));
        assertEquals("LOGOUT", reason(unexpired.getSessionId()));
        assertEquals(2, jdbc.queryForObject("SELECT COUNT(*) FROM users", Integer.class));
    }

    @Test
    void refreshForDifferentSessionCannotRevokeEitherChain() {
        // Arrange
        final long userId = user();
        final SessionCredentials first = service.createSession(userId, false);
        final SessionCredentials other = service.createSession(userId, false);
        // Exercise
        final SessionRevocationResult result = service.revokeWithRefresh(userId, other.getSessionId(), first.getRefreshToken());
        // Assertions
        assertEquals(SessionRevocationResult.FORBIDDEN, result);
        assertNull(reason(first.getSessionId()));
        assertNull(reason(other.getSessionId()));
        assertEquals(1, tokenCount(first.getSessionId()));
        assertEquals(1, tokenCount(other.getSessionId()));
    }

    private Object rejectAfterReplay(final SessionCredentials initial) {
        assertEquals(RefreshResult.Status.REUSED, service.rotate(initial.getRefreshToken()).getStatus());
        throw new IllegalStateException("caller rejected the request");
    }

    private List<RefreshResult> renewConcurrently(final String token) throws Exception {
        final var executor = Executors.newFixedThreadPool(2);
        final CountDownLatch ready = new CountDownLatch(2);
        final CountDownLatch start = new CountDownLatch(1);
        final Callable<RefreshResult> task = () -> {
            ready.countDown();
            if (!start.await(10, TimeUnit.SECONDS)) {
                throw new IllegalStateException("concurrent refresh start timed out");
            }
            return service.rotate(token);
        };
        try {
            final var first = executor.submit(task);
            final var second = executor.submit(task);
            assertTrue(ready.await(10, TimeUnit.SECONDS));
            start.countDown();
            return List.of(first.get(20, TimeUnit.SECONDS), second.get(20, TimeUnit.SECONDS));
        } finally {
            start.countDown();
            executor.shutdownNow();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS));
        }
    }

    private long user() {
        final String suffix = UUID.randomUUID().toString();
        jdbc.update("INSERT INTO users (username, email, password) VALUES (?, ?, ?)",
                suffix, suffix + "@example.test", "existing-password-hash");
        return jdbc.queryForObject("SELECT user_id FROM users WHERE username = ?", Long.class, suffix);
    }

    private int tokenCount(final long sessionId) {
        return jdbc.queryForObject("SELECT COUNT(*) FROM auth_refresh_tokens WHERE session_id = ?", Integer.class, sessionId);
    }

    private String reason(final long sessionId) {
        return jdbc.queryForObject("SELECT revocation_reason FROM auth_sessions WHERE session_id = ?", String.class, sessionId);
    }

    @Configuration
    @EnableTransactionManagement
    @Import({AuthSessionJpaDao.class, AuthSessionServiceImpl.class, RefreshTokenCodec.class})
    static class Config {
        @Bean(destroyMethod = "close")
        TestDatabase database() throws Exception { return new TestDatabase(); }

        @Bean
        DataSource dataSource(final TestDatabase database) { return database.source; }

        @Bean
        Clock authClock() { return Clock.fixed(NOW, ZoneOffset.UTC); }

        @Bean
        JdbcTemplate jdbcTemplate(final DataSource source) { return new JdbcTemplate(source); }

        @Bean
        LocalContainerEntityManagerFactoryBean entityManagerFactory(final DataSource source, final TestDatabase database) {
            final var factory = new LocalContainerEntityManagerFactoryBean();
            factory.setDataSource(source);
            factory.setPackagesToScan("ar.edu.itba.paw.model");
            factory.setJpaVendorAdapter(new HibernateJpaVendorAdapter());
            final Properties properties = new Properties();
            properties.setProperty("hibernate.dialect", database.postgres ? "org.hibernate.dialect.PostgreSQL92Dialect" : "org.hibernate.dialect.HSQLDialect");
            properties.setProperty("hibernate.hbm2ddl.auto", "none");
            factory.setJpaProperties(properties);
            return factory;
        }

        @Bean
        PlatformTransactionManager transactionManager(final EntityManagerFactory factory) { return new JpaTransactionManager(factory); }

        @Bean
        static PersistenceExceptionTranslationPostProcessor exceptionTranslation() { return new PersistenceExceptionTranslationPostProcessor(); }
    }

    static class TestDatabase implements AutoCloseable {
        final DriverManagerDataSource source;
        final boolean postgres;
        private final DriverManagerDataSource admin;
        private final String schema = "auth_test_" + UUID.randomUUID().toString().replace("-", "");

        TestDatabase() throws Exception {
            final String url = System.getenv("PAW_AUTH_TEST_JDBC_URL");
            postgres = url != null && !url.isBlank();
            if (postgres) {
                admin = new DriverManagerDataSource(url, System.getenv("PAW_AUTH_TEST_USERNAME"), System.getenv("PAW_AUTH_TEST_PASSWORD"));
                try (Connection connection = admin.getConnection(); Statement statement = connection.createStatement()) {
                    statement.execute("CREATE SCHEMA " + schema);
                }
                source = new DriverManagerDataSource(url + (url.contains("?") ? "&" : "?") + "currentSchema=" + schema,
                        System.getenv("PAW_AUTH_TEST_USERNAME"), System.getenv("PAW_AUTH_TEST_PASSWORD"));
            } else {
                admin = null;
                source = new DriverManagerDataSource("jdbc:hsqldb:mem:" + schema + ";sql.syntax_pgs=true;hsqldb.tx=mvcc", "sa", "");
            }
            new ResourceDatabasePopulator(new ClassPathResource("auth-session-test-schema.sql")).execute(source);
        }

        @Override
        public void close() throws Exception {
            try (Connection connection = (postgres ? admin : source).getConnection(); Statement statement = connection.createStatement()) {
                statement.execute(postgres ? "DROP SCHEMA " + schema + " CASCADE" : "SHUTDOWN");
            }
        }
    }
}
