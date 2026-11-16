package ar.edu.itba.paw.model;

import java.io.Serializable;
import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

/** Only the digest is persisted; consumed digests remain until their session expires. */
@Entity
@Table(name = "auth_refresh_tokens")
public class AuthRefreshToken implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "refresh_token_id")
    private long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "session_id")
    private AuthSession session;

    @Column(name = "token_hash")
    private String tokenHash;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "consumed_at")
    private Instant consumedAt;

    protected AuthRefreshToken() { }

    public AuthRefreshToken(final AuthSession session, final String tokenHash, final Instant createdAt) {
        this();
        this.session = session;
        this.tokenHash = tokenHash;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public AuthSession getSession() { return session; }
    public String getTokenHash() { return tokenHash; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getConsumedAt() { return consumedAt; }
    public void setConsumedAt(final Instant consumedAt) { this.consumedAt = consumedAt; }
}
