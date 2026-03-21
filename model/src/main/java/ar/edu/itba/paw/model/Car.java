package ar.edu.itba.paw.model;

import java.io.Serializable;

public class Car implements Serializable {

    private long id;
    private String brand;
    private String model;
    private String generation;
    private String description;
    private String imageUrl;

    public Car() {}

    public Car(long id, String brand, String model, String generation, String description, String imageUrl) {
        this.id = id;
        this.brand = brand;
        this.model = model;
        this.generation = generation;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getBrand() { return brand; }
    public void setBrand(String brand) { this.brand = brand; }

    public String getModel() { return model; }
    public void setModel(String model) { this.model = model; }

    public String getGeneration() { return generation; }
    public void setGeneration(String generation) { this.generation = generation; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
