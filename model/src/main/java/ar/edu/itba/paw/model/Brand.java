package ar.edu.itba.paw.model;

import java.io.Serializable;
import java.time.LocalDateTime;

public class Brand implements Serializable {

    private long id;
    private String name;
    private LocalDateTime createdAt;

    public Brand() {}

    public Brand(long id, String name, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.createdAt = createdAt;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
