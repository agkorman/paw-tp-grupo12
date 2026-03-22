package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.User;

import java.util.Optional;

public interface UserDao {
    Optional<User> findById(long id);
    Optional<User> findByEmail(String email);
    Optional<User> findByUsername(String username);
    User create(String username, String email, String password, String role);
}
