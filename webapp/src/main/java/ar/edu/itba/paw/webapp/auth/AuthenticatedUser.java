package ar.edu.itba.paw.webapp.auth;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

public class AuthenticatedUser implements UserDetails {

    private final long id;
    private final String username;
    private final String email;
    private final String password;
    private final String preferredLocale;
    private final Collection<? extends GrantedAuthority> authorities;

    public AuthenticatedUser(final long id, final String username, final String email, final String password,
                             final Collection<? extends GrantedAuthority> authorities) {
        this(id, username, email, password, "es", authorities);
    }

    public AuthenticatedUser(final long id, final String username, final String email, final String password,
                             final String preferredLocale,
                             final Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.password = password;
        this.preferredLocale = preferredLocale;
        this.authorities = authorities;
    }

    public long getId() {
        return id;
    }

    public String getDisplayName() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public String getPreferredLocale() {
        return preferredLocale;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}
