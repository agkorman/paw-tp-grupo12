package ar.edu.itba.paw.model;

public class Car {

    private int carId;
    private int moderatorId;
    private String brand;
    private String model;
    private String generation;
    private String description;
    private String imageUrl;

    public Car(int carId, int moderatorId, String brand, String model,
               String generation, String description, String imageUrl) {
        this.carId = carId;
        this.moderatorId = moderatorId;
        this.brand = brand;
        this.model = model;
        this.generation = generation;
        this.description = description;
        this.imageUrl = imageUrl;
    }

    public int getCarId() { return carId; }
    public int getModeratorId() { return moderatorId; }
    public String getBrand() { return brand; }
    public String getModel() { return model; }
    public String getGeneration() { return generation; }
    public String getDescription() { return description; }
    public String getImageUrl() { return imageUrl; }

    // Mepa que no tiene sentido poner setters
    public void setCarId(int carId) { this.carId = carId; }
    public void setModeratorId(int moderatorId) { this.moderatorId = moderatorId; }
    public void setBrand(String brand) { this.brand = brand; }
    public void setModel(String model) { this.model = model; }
    public void setGeneration(String generation) { this.generation = generation; }
    public void setDescription(String description) { this.description = description; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
