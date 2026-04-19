package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.persistence.CarRequestDao;
import ar.edu.itba.paw.persistence.ReviewDao;
import ar.edu.itba.paw.persistence.UserDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    private static final String DEFAULT_ROLE = "user";
    private static final List<String> MODERATOR_EMAIL_ROLES = Arrays.asList("moderator", "admin");

    private final UserDao userDao;
    private final ReviewDao reviewDao;
    private final CarRequestDao carRequestDao;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public UserServiceImpl(final UserDao userDao, final ReviewDao reviewDao, final CarRequestDao carRequestDao,
                           final PasswordEncoder passwordEncoder) {
        this.userDao = userDao;
        this.reviewDao = reviewDao;
        this.carRequestDao = carRequestDao;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public Optional<User> findByEmail(final String email) {
        final String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail == null) {
            return Optional.empty();
        }
        return userDao.findByEmail(normalizedEmail);
    }

    @Override
    public Optional<User> findByUsername(final String username) {
        final String normalizedUsername = normalize(username);
        if (normalizedUsername == null) {
            return Optional.empty();
        }
        return userDao.findByUsername(normalizedUsername);
    }

    @Override
    @Transactional
    public User createUser(final String username, final String email, final String rawPassword) {
        final String normalizedUsername = normalize(username);
        final String normalizedEmail = normalizeEmail(email);
        if (normalizedUsername == null) {
            throw new IllegalArgumentException("Username is required.");
        }
        if (normalizedEmail == null) {
            throw new IllegalArgumentException("Email is required.");
        }
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new IllegalArgumentException("Password is required.");
        }
        if (userDao.findByUsername(normalizedUsername).isPresent()) {
            throw new IllegalArgumentException("Username is already registered.");
        }
        if (userDao.findByEmail(normalizedEmail).isPresent()) {
            throw new IllegalArgumentException("Email is already registered.");
        }

        final User user = userDao.create(
                normalizedUsername,
                normalizedEmail,
                passwordEncoder.encode(rawPassword),
                DEFAULT_ROLE
        );
        reviewDao.bindReviewsToUserByEmail(user.getId(), normalizedEmail);
        carRequestDao.bindRequestsToUserByEmail(user.getId(), normalizedEmail);
        return user;
    }

    @Override
    public List<String> getModeratorsEmails() {
        return userDao.findEmailsByRoles(MODERATOR_EMAIL_ROLES);
    }

    private String normalizeEmail(final String value) {
        final String normalized = normalize(value);
        return normalized == null ? null : normalized.toLowerCase(Locale.ROOT);
    }

    private String normalize(final String value) {
        if (value == null) {
            return null;
        }
        final String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
