package ar.edu.itba.paw.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class BodyType implements Serializable {

    private long id;
    private String name;
    private LocalDateTime createdAt;

    public BodyType() {}

    public BodyType(final long id, final String name, final LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
    }

    public long getId() {
        return id;
    }

    public void setId(final long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(final LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
