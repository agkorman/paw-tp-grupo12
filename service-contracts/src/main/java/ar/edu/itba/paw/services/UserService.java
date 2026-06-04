package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.EmailRecipient;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.User;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserService {
    Optional<User> getUserById(long id);
    List<User> getUsersByIds(Collection<Long> ids);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    User createUser(String username, String email, String rawPassword);
    User updateUsername(long userId, String username);
    User updatePreferredLocale(long userId, String preferredLocale);
    boolean updateRole(long userId, String role);
    List<EmailRecipient> getModeratorEmailRecipients();
    List<User> getAllUsers();
    Page<User> searchUsers(String query, int page);
}
