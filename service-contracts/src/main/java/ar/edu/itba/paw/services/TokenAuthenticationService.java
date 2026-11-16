package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.TokenAuthenticationResult;

public interface TokenAuthenticationService {
    TokenAuthenticationResult authenticate(String email, String password, boolean rememberMe);
    TokenAuthenticationResult renew(String refreshToken);
}
