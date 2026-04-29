package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Brand;

import java.util.List;
import java.util.Optional;

public interface BrandService {
    List<Brand> findAll();
    Optional<Brand> findById(long id);
    Optional<Brand> findByName(String name);
    Brand createBrand(String name);
    Optional<Brand> updateBrand(long id, String name);
    boolean deleteBrand(long id);
}
