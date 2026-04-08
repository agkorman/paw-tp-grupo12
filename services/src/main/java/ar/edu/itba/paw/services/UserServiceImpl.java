package ar.edu.itba.paw.services;

import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Override
    public Object createUser(final String email) {
        return email;
    }

    @Override
    public long getUserId() {
        return 1L;
    }

    @Override
    public List<String> getModeratorsEmails() {
        return Collections.singletonList("jnolascodecarles@itba.edu.ar");
    }
}
