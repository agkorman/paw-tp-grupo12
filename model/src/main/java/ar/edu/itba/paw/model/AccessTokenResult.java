package ar.edu.itba.paw.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;

public final class AccessTokenResult implements Serializable {

    public enum Status { VALID, EXPIRED, INVALID }

    private final Status status;
    private final AccessTokenIdentity identity;

    private AccessTokenResult(final Status status, final AccessTokenIdentity identity) {
        this.status = status;
        this.identity = identity;
    }

    public static AccessTokenResult valid(final AccessTokenIdentity identity) {
        return new AccessTokenResult(Status.VALID, Objects.requireNonNull(identity));
    }

    public static AccessTokenResult rejected(final Status status) {
        if (Objects.requireNonNull(status) == Status.VALID) {
            throw new IllegalArgumentException("A valid result requires an identity");
        }
        return new AccessTokenResult(status, null);
    }

    public Status getStatus() { return status; }
    public Optional<AccessTokenIdentity> getIdentity() { return Optional.ofNullable(identity); }
}
