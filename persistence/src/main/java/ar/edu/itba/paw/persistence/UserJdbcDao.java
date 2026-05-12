package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.EmailRecipient;
import ar.edu.itba.paw.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

public class UserJdbcDao implements UserDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserJdbcDao.class);

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    private static final RowMapper<User> ROW_MAPPER = (rs, rowNum) -> new User(
            rs.getLong("user_id"),
            rs.getString("username"),
            rs.getString("email"),
            rs.getString("password"),
            rs.getString("role"),
            rs.getString("preferred_locale"),
            rs.getTimestamp("created_at").toLocalDateTime()
    );
    private static final RowMapper<EmailRecipient> EMAIL_RECIPIENT_ROW_MAPPER = (rs, rowNum) ->
            new EmailRecipient(
                    rs.getString("email"),
                    rs.getString("preferred_locale")
            );

    @Autowired
    public UserJdbcDao(final DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("users")
                .usingGeneratedKeyColumns("user_id");
    }

    @Override
    public Optional<User> findById(long id) {
        return jdbcTemplate.query(
                "SELECT user_id, username, email, password, role, preferred_locale, created_at FROM users WHERE user_id = ?",
                ROW_MAPPER, id
        ).stream().findFirst();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jdbcTemplate.query(
                "SELECT user_id, username, email, password, role, preferred_locale, created_at FROM users WHERE LOWER(email) = LOWER(?)",
                ROW_MAPPER, email
        ).stream().findFirst();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jdbcTemplate.query(
                "SELECT user_id, username, email, password, role, preferred_locale, created_at FROM users WHERE LOWER(username) = LOWER(?)",
                ROW_MAPPER, username
        ).stream().findFirst();
    }

    @Override
    public User create(String username, String email, String password, String role) {
        Map<String, Object> params = new HashMap<>();
        params.put("username", username);
        params.put("email", email);
        params.put("password", password);
        params.put("role", role);
        params.put("preferred_locale", "es");
        params.put("created_at", Timestamp.valueOf(LocalDateTime.now()));

        long id = jdbcInsert.executeAndReturnKey(params).longValue();
        LOGGER.info("created user id={} email={} role={}", id, email, role);
        return findById(id).orElseThrow();
    }

    @Override
    public boolean updateUsername(final long userId, final String username) {
        return jdbcTemplate.update(
                "UPDATE users SET username = ? WHERE user_id = ?",
                username,
                userId
        ) > 0;
    }

    @Override
    public boolean updateRole(final long userId, final String role) {
        final boolean updated = jdbcTemplate.update(
                "UPDATE users SET role = ? WHERE user_id = ?",
                role,
                userId
        ) > 0;
        if (updated) {
            LOGGER.info("updated user role id={} role={}", userId, role);
        } else {
            LOGGER.warn("user role update affected 0 rows id={}", userId);
        }
        return updated;
    }

    @Override
    public boolean updatePreferredLocale(final long userId, final String preferredLocale) {
        final boolean updated = jdbcTemplate.update(
                "UPDATE users SET preferred_locale = ? WHERE user_id = ?",
                preferredLocale,
                userId
        ) > 0;
        if (updated) {
            LOGGER.info("updated user preferred locale id={} locale={}", userId, preferredLocale);
        } else {
            LOGGER.warn("user preferred locale update affected 0 rows id={}", userId);
        }
        return updated;
    }

    @Override
    public List<User> findAll() {
        return jdbcTemplate.query(
                "SELECT user_id, username, email, password, role, preferred_locale, created_at FROM users ORDER BY user_id",
                ROW_MAPPER
        );
    }

    @Override
    public List<EmailRecipient> findEmailRecipientsByRoles(final Collection<String> roles) {
        final List<String> normalizedRoles = normalizeRoles(roles);
        if (normalizedRoles.isEmpty()) {
            return Collections.emptyList();
        }

        final String placeholders = normalizedRoles.stream()
                .map(ignored -> "?")
                .collect(Collectors.joining(", "));

        return jdbcTemplate.query(
                "SELECT email, preferred_locale FROM users WHERE LOWER(role) IN (" + placeholders + ") ORDER BY email",
                EMAIL_RECIPIENT_ROW_MAPPER,
                normalizedRoles.toArray()
        );
    }

    private List<String> normalizeRoles(final Collection<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return Collections.emptyList();
        }
        return roles.stream()
                .filter(role -> role != null && !role.trim().isEmpty())
                .map(role -> role.trim().toLowerCase(Locale.ROOT))
                .distinct()
                .collect(Collectors.toList());
    }
}
