package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.User;
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

@Repository
public class UserJdbcDao implements UserDao {

    private final JdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    private static final RowMapper<User> ROW_MAPPER = (rs, rowNum) -> new User(
            rs.getLong("user_id"),
            rs.getString("username"),
            rs.getString("email"),
            rs.getString("password"),
            rs.getString("role"),
            rs.getTimestamp("created_at").toLocalDateTime()
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
                "SELECT user_id, username, email, password, role, created_at FROM users WHERE user_id = ?",
                ROW_MAPPER, id
        ).stream().findFirst();
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return jdbcTemplate.query(
                "SELECT user_id, username, email, password, role, created_at FROM users WHERE LOWER(email) = LOWER(?)",
                ROW_MAPPER, email
        ).stream().findFirst();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return jdbcTemplate.query(
                "SELECT user_id, username, email, password, role, created_at FROM users WHERE username = ?",
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
        params.put("created_at", Timestamp.valueOf(LocalDateTime.now()));

        long id = jdbcInsert.executeAndReturnKey(params).longValue();
        return findById(id).orElseThrow();
    }

    @Override
    public List<User> findAll() {
        return jdbcTemplate.query(
                "SELECT user_id, username, email, password, role, created_at FROM users ORDER BY user_id",
                ROW_MAPPER
        );
    }

    @Override
    public List<String> findEmailsByRoles(final Collection<String> roles) {
        if (roles == null || roles.isEmpty()) {
            return Collections.emptyList();
        }

        final List<String> normalizedRoles = roles.stream()
                .filter(role -> role != null && !role.trim().isEmpty())
                .map(role -> role.trim().toLowerCase(Locale.ROOT))
                .distinct()
                .collect(Collectors.toList());
        if (normalizedRoles.isEmpty()) {
            return Collections.emptyList();
        }

        final String placeholders = normalizedRoles.stream()
                .map(ignored -> "?")
                .collect(Collectors.joining(", "));

        return jdbcTemplate.queryForList(
                "SELECT email FROM users WHERE LOWER(role) IN (" + placeholders + ") ORDER BY email",
                String.class,
                normalizedRoles.toArray()
        );
    }
}
