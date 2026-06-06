package ar.edu.itba.paw.model;

import java.io.Serializable;

public class ActivityFeedPermissions implements Serializable {

    private final ActivityFeedReference reference;
    private final boolean editable;
    private final boolean deletable;
    private final boolean hideable;

    public ActivityFeedPermissions(final ActivityFeedReference reference,
                                   final boolean editable,
                                   final boolean deletable,
                                   final boolean hideable) {
        this.reference = reference;
        this.editable = editable;
        this.deletable = deletable;
        this.hideable = hideable;
    }

    public static ActivityFeedPermissions none(final ActivityFeedReference reference) {
        return new ActivityFeedPermissions(reference, false, false, false);
    }

    public ActivityFeedReference getReference() {
        return reference;
    }

    public boolean isEditable() {
        return editable;
    }

    public boolean isDeletable() {
        return deletable;
    }

    public boolean isHideable() {
        return hideable;
    }
}
