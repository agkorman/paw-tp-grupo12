package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.User;

import java.util.List;
import java.util.Optional;

public interface UserService {
    Optional<User> getUserById(long id);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    User createUser(String username, String email, String rawPassword);
    List<String> getModeratorsEmails();
    List<User> getAllUsers();
}
