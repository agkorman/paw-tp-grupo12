package ar.edu.itba.paw.model;

import java.util.Set;

public final class ReviewOwnershipStatus {

    public static final String CURRENT_OWNER = "current_owner";
    public static final String PREVIOUS_OWNER = "previous_owner";

    private static final String LEGACY_CURRENT_OWNER = "Propietario actual";
    private static final String LEGACY_PREVIOUS_OWNER = "Ex propietario";
    private static final Set<String> ALLOWED = Set.of(CURRENT_OWNER, PREVIOUS_OWNER);

    private ReviewOwnershipStatus() {
    }

    public static boolean isValid(final String value) {
        return value == null || value.isEmpty() || ALLOWED.contains(value);
    }

    public static String normalize(final String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }
        if (LEGACY_CURRENT_OWNER.equals(value)) {
            return CURRENT_OWNER;
        }
        if (LEGACY_PREVIOUS_OWNER.equals(value)) {
            return PREVIOUS_OWNER;
        }
        return value;
    }
}
