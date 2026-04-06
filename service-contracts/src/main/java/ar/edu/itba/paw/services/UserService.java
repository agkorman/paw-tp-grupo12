package ar.edu.itba.paw.services;

import java.util.List;

public interface UserService {
    Object createUser(final String email);
    long getUserId();
    List<String> getModeratorsEmails();
}
