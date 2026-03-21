package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Car;
import java.util.List;

public interface CarDao {
    List<Car> findAll();
}
