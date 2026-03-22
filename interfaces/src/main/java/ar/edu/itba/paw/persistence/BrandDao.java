package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Brand;

import java.util.List;
import java.util.Optional;

public interface BrandDao {
    List<Brand> findAll();
    Optional<Brand> findById(long id);
    Optional<Brand> findByName(String name);
    Brand create(String name, String imageUrl);
}
