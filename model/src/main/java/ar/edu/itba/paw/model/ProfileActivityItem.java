package ar.edu.itba.paw.model;

import java.io.Serializable;

public class ProfileActivityItem implements Serializable {

    public enum ItemType { REVIEW, POST }

    private final ItemType type;
    private final long entityId;

    public ProfileActivityItem(final ItemType type, final long entityId) {
        this.type = type;
        this.entityId = entityId;
    }

    public ItemType getType() {
        return type;
    }

    public long getEntityId() {
        return entityId;
    }
}
