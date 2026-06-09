package ar.edu.itba.paw.model;

import java.io.Serializable;
import java.util.function.Supplier;

public final class CommunityActionResult implements Serializable {

    private static final CommunityActionResult NOT_FOUND = new CommunityActionResult(false, false);
    private static final CommunityActionResult PERFORMED_FALSE = new CommunityActionResult(true, false);
    private static final CommunityActionResult PERFORMED_TRUE = new CommunityActionResult(true, true);

    private final boolean found;
    private final boolean value;

    private CommunityActionResult(final boolean found, final boolean value) {
        this.found = found;
        this.value = value;
    }

    public static CommunityActionResult notFound() {
        return NOT_FOUND;
    }

    public static CommunityActionResult performed(final boolean value) {
        return value ? PERFORMED_TRUE : PERFORMED_FALSE;
    }

    public boolean isFound() {
        return found;
    }

    public boolean getValue() {
        if (!found) {
            throw new IllegalStateException("Community action result is not available.");
        }
        return value;
    }

    public boolean orElseThrow(final Supplier<? extends RuntimeException> exceptionSupplier) {
        if (!found) {
            throw exceptionSupplier.get();
        }
        return value;
    }
}
