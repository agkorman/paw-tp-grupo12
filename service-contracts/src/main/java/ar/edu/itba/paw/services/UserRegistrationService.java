package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.User;

public interface UserRegistrationService {
    User register(String username, String email, String rawPassword);
}
