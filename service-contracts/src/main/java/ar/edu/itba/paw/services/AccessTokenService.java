package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.AccessTokenResult;
import ar.edu.itba.paw.model.SessionCredentials;
import java.util.Set;

public interface AccessTokenService {

    /** Roles must come from the current user in the database, never a previous JWT or request input. */
    String issue(SessionCredentials session, Set<String> currentRoles);

    /** Validates locally, without reading users or sessions from the database. */
    AccessTokenResult validate(String token);
}
