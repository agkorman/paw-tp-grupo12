package ar.edu.itba.paw.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

/** A rejection is a normal return so that replay-triggered revocation can commit. */
public final class RefreshResult implements Serializable {

    public enum Status { ISSUED, INVALID, EXPIRED, REVOKED, REUSED }

    private final Status status;
    private final SessionCredentials credentials;

    private RefreshResult(final Status status, final SessionCredentials credentials) {
        this.status = status;
        this.credentials = credentials;
    }

    public static RefreshResult issued(final SessionCredentials credentials) {
        return new RefreshResult(Status.ISSUED, Objects.requireNonNull(credentials));
    }

    public static RefreshResult rejected(final Status status) {
        if (Objects.requireNonNull(status) == Status.ISSUED) {
            throw new IllegalArgumentException("An issued result requires credentials");
        }
        return new RefreshResult(status, null);
    }

    public Status getStatus() { return status; }
    public Optional<SessionCredentials> getCredentials() { return Optional.ofNullable(credentials); }
}
