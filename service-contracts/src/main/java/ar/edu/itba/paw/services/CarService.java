package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Car;
import java.util.List;

public interface CarService {

    Car create(int moderatorId, String brand, String model, String generation, String description, String imageUrl);

    Car findById(int carId);

    List<Car> findAll();

    List<Car> filterByBrand(String brand);

    List<Car> filterByModel(String model);

    List<Car> filterByGeneration(String generation);

    List<Car> filterByModerator(int moderatorId);

    List<Car> search(String keyword);
}