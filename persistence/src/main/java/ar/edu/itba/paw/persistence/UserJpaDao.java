package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.EmailRecipient;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Pagination;
import ar.edu.itba.paw.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class UserJpaDao implements UserDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserJpaDao.class);

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<User> findById(long id) {
        return Optional.ofNullable(em.find(User.class, id));
    }

    @Override
    public Optional<User> findByEmail(String email) {
        return em.createQuery(
                        "SELECT u FROM User u WHERE LOWER(u.email) = LOWER(:email)", User.class)
                .setParameter("email", email)
                .getResultList()
                .stream().findFirst();
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return em.createQuery(
                        "SELECT u FROM User u WHERE LOWER(u.username) = LOWER(:username)", User.class)
                .setParameter("username", username)
                .getResultList()
                .stream().findFirst();
    }

    @Override
    public User create(String username, String email, String password, String role) {
        final User user = new User(username, email, password, role, "es");
        em.persist(user);
        LOGGER.info("created user id={} email={} role={}", user.getId(), email, role);
        return user;
    }

    @Override
    public boolean updateUsername(long userId, String username) {
        final User user = em.find(User.class, userId);
        if (user == null) {
            return false;
        }
        user.setUsername(username);
        return true;
    }

    @Override
    public boolean updateRole(long userId, String role) {
        final User user = em.find(User.class, userId);
        if (user == null) {
            LOGGER.warn("user role update affected 0 rows id={}", userId);
            return false;
        }
        user.setRole(role);
        LOGGER.info("updated user role id={} role={}", userId, role);
        return true;
    }

    @Override
    public boolean updatePreferredLocale(long userId, String preferredLocale) {
        final User user = em.find(User.class, userId);
        if (user == null) {
            LOGGER.warn("user preferred locale update affected 0 rows id={}", userId);
            return false;
        }
        user.setPreferredLocale(preferredLocale);
        LOGGER.info("updated user preferred locale id={} locale={}", userId, preferredLocale);
        return true;
    }

    @Override
    public List<User> findAll() {
        return em.createQuery("SELECT u FROM User u ORDER BY u.id", User.class)
                .getResultList();
    }

    @Override
    public List<EmailRecipient> findEmailRecipientsByRoles(Collection<String> roles) {
        final List<String> normalizedRoles = normalizeRoles(roles);
        if (normalizedRoles.isEmpty()) {
            return Collections.emptyList();
        }
        return em.createQuery(
                        "SELECT NEW ar.edu.itba.paw.model.EmailRecipient(u.email, u.preferredLocale) " +
                        "FROM User u WHERE LOWER(u.role) IN :roles ORDER BY u.email",
                        EmailRecipient.class)
                .setParameter("roles", normalizedRoles)
                .getResultList();
    }

    @Override
    public Page<User> searchByQuery(final String query, final int page, final int pageSize) {
        if (query == null || query.isBlank()) {
            return Page.empty(Pagination.DEFAULT_PAGE, pageSize);
        }
        final String escaped = query.toLowerCase(Locale.ROOT)
                .replace("\\", "\\\\").replace("%", "\\%").replace("_", "\\_");
        final String likeQ = "%" + escaped + "%";

        final Number total = (Number) em.createNativeQuery(
                "SELECT COUNT(*) FROM users " +
                "WHERE LOWER(username) LIKE ? ESCAPE '\\' OR LOWER(email) LIKE ? ESCAPE '\\'")
                .setParameter(1, likeQ)
                .setParameter(2, likeQ)
                .getSingleResult();
        final long totalItems = total == null ? 0L : total.longValue();
        if (totalItems == 0L) {
            return Page.empty(Pagination.DEFAULT_PAGE, pageSize);
        }

        final int clampedPage = Pagination.clampPage(Pagination.normalizePage(page), totalItems, pageSize);
        final long offset = Pagination.offsetFor(clampedPage, pageSize);

        @SuppressWarnings("unchecked")
        final List<Number> idRows = em.createNativeQuery(
                "SELECT user_id FROM users " +
                "WHERE LOWER(username) LIKE ? ESCAPE '\\' OR LOWER(email) LIKE ? ESCAPE '\\' " +
                "ORDER BY username ASC, user_id ASC LIMIT ? OFFSET ?")
                .setParameter(1, likeQ)
                .setParameter(2, likeQ)
                .setParameter(3, pageSize)
                .setParameter(4, offset)
                .getResultList();
        if (idRows.isEmpty()) {
            return Page.empty(clampedPage, pageSize);
        }
        final List<Long> ids = idRows.stream().map(Number::longValue).collect(Collectors.toList());

        final List<User> users = em.createQuery(
                "SELECT u FROM User u WHERE u.id IN :ids ORDER BY u.username ASC, u.id ASC", User.class)
                .setParameter("ids", ids)
                .getResultList();
        return new Page<>(users, clampedPage, pageSize, totalItems);
    }

    private List<String> normalizeRoles(Collection<String> roles) {
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
