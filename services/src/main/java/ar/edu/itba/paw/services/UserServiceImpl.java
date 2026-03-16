package ar.edu.itba.paw.services;

import org.springframework.stereotype.Service;

// Dependency injection
// Inversion of control

@Service
public class UserServiceImpl implements UserService{
  public Object createUser(final String email) {
    return email;
  }
}
