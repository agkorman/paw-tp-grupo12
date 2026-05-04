package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.BodyType;

import java.util.List;
import java.util.Optional;

public interface BodyTypeService {
    List<BodyType> findAll();
    Optional<BodyType> findById(long id);
    Optional<BodyType> findByName(String name);
    boolean existsByName(String name);
    BodyType createBodyType(String name);
    Optional<BodyType> updateBodyType(long id, String name);
    boolean deleteBodyType(long id);
}
