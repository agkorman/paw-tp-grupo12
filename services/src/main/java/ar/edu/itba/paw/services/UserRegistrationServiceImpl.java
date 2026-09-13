package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserRegistrationServiceImpl implements UserRegistrationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserRegistrationServiceImpl.class);

    private final UserService userService;
    private final ReviewService reviewService;
    private final CarRequestService carRequestService;

    @Autowired
    public UserRegistrationServiceImpl(final UserService userService, final ReviewService reviewService,
                                       final CarRequestService carRequestService) {
        this.userService = userService;
        this.reviewService = reviewService;
        this.carRequestService = carRequestService;
    }

    @Override
    @Transactional
    public User register(final String username, final String email, final String rawPassword) {
        final User user = userService.createUser(username, email, rawPassword);
        reviewService.claimPreRegistrationReviews(user.getId(), user.getEmail());
        carRequestService.claimPreRegistrationRequests(user.getId(), user.getEmail());
        LOGGER.info("registered user id={} username={} role={}", user.getId(), user.getUsername(), user.getRole());
        return user;
    }
}
