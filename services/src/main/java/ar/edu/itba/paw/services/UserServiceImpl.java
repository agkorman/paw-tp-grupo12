package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.persistence.CarRequestDao;
import ar.edu.itba.paw.persistence.ReviewDao;
import ar.edu.itba.paw.persistence.UserDao;
import ar.edu.itba.paw.services.exception.EmailAlreadyExistsException;
import ar.edu.itba.paw.services.exception.InvalidServiceInputException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ar.edu.itba.paw.services.exception.UserNotFoundException;
import ar.edu.itba.paw.services.exception.UsernameAlreadyExistsException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserServiceImpl.class);
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
    public Optional<User> getUserById(final long id) {
        return userDao.findById(id);
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
        final String normalizedUsername = StringUtils.normalize(username);
        if (normalizedUsername == null) {
            return Optional.empty();
        }
        return userDao.findByUsername(normalizedUsername);
    }

    @Override
    @Transactional
    public User createUser(final String username, final String email, final String rawPassword) {
        final String normalizedUsername = StringUtils.normalize(username);
        final String normalizedEmail = normalizeEmail(email);
        if (normalizedUsername == null) {
            throw new InvalidServiceInputException("Username is required.");
        }
        if (normalizedEmail == null) {
            throw new InvalidServiceInputException("Email is required.");
        }
        if (rawPassword == null || rawPassword.isEmpty()) {
            throw new InvalidServiceInputException("Password is required.");
        }
        if (userDao.findByUsername(normalizedUsername).isPresent()) {
            throw new UsernameAlreadyExistsException(normalizedUsername);
        }
        if (userDao.findByEmail(normalizedEmail).isPresent()) {
            throw new EmailAlreadyExistsException(normalizedEmail);
        }

        final User user = userDao.create(
                normalizedUsername,
                normalizedEmail,
                passwordEncoder.encode(rawPassword),
                DEFAULT_ROLE
        );
        reviewDao.bindReviewsToUserByEmail(user.getId(), normalizedEmail);
        carRequestDao.bindRequestsToUserByEmail(user.getId(), normalizedEmail);
        LOGGER.info("Created user id={} username={} role={}", user.getId(), normalizedUsername, DEFAULT_ROLE);
        return user;
    }

    @Override
    @Transactional
    public User updateUsername(final long userId, final String username) {
        final String normalizedUsername = StringUtils.normalize(username);
        if (normalizedUsername == null) {
            throw new InvalidServiceInputException("Username is required.");
        }
        final Optional<User> existing = userDao.findByUsername(normalizedUsername);
        if (existing.isPresent() && existing.get().getId() != userId) {
            throw new UsernameAlreadyExistsException(normalizedUsername);
        }
        if (!userDao.updateUsername(userId, normalizedUsername)) {
            throw new UserNotFoundException(userId);
        }
        return userDao.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
    }

    @Override
    @Transactional
    public boolean updateRole(final long userId, final String role) {
        final String normalizedRole = StringUtils.normalize(role);
        if (normalizedRole == null) {
            throw new InvalidServiceInputException("Role is required.");
        }
        return userDao.updateRole(userId, normalizedRole.toLowerCase(Locale.ROOT));
    }

    @Override
    public List<String> getModeratorsEmails() {
        return userDao.findEmailsByRoles(MODERATOR_EMAIL_ROLES);
    }

    @Override
    public List<User> getAllUsers() {
        return userDao.findAll();
    }

    private String normalizeEmail(final String value) {
        final String normalized = StringUtils.normalize(value);
        return normalized == null ? null : normalized.toLowerCase(Locale.ROOT);
    }
}
