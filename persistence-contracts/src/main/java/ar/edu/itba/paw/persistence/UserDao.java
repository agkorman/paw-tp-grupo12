package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.EmailRecipient;
import ar.edu.itba.paw.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserDao {
    Optional<User> findById(long id);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    User create(String username, String email, String password, String role);
    boolean updateUsername(long userId, String username);
    boolean updateRole(long userId, String role);
    boolean updatePreferredLocale(long userId, String preferredLocale);
    List<EmailRecipient> findEmailRecipientsByRoles(Collection<String> roles);
    List<User> findAll();
}
