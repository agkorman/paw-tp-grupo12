package ar.edu.itba.paw.model;

import java.io.Serializable;
import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

@Entity
@Table(name = "auth_sessions")
public class AuthSession implements Serializable {

    public enum RevocationReason { LOGOUT, REFRESH_REUSE }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "session_id")
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "expires_at")
    private Instant expiresAt;

    @Column(name = "remember_me")
    private boolean rememberMe;

    @Column(name = "revoked_at")
    private Instant revokedAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "revocation_reason")
    private RevocationReason revocationReason;

    protected AuthSession() { }

    public AuthSession(final User user, final Instant createdAt, final Instant expiresAt,
                       final boolean rememberMe) {
        this();
        this.user = user;
        this.createdAt = createdAt;
        this.expiresAt = expiresAt;
        this.rememberMe = rememberMe;
    }

    public long getId() { return id; }
    public void setId(final long id) { this.id = id; }
    public User getUser() { return user; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getExpiresAt() { return expiresAt; }
    public boolean isRememberMe() { return rememberMe; }
    public Instant getRevokedAt() { return revokedAt; }
    public void setRevokedAt(final Instant revokedAt) { this.revokedAt = revokedAt; }
    public RevocationReason getRevocationReason() { return revocationReason; }
    public void setRevocationReason(final RevocationReason reason) { this.revocationReason = reason; }
}
