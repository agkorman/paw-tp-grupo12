package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.BodyType;

import java.util.List;
import java.util.Optional;

public interface BodyTypeService {
    List<BodyType> findAll();
    Optional<BodyType> findByName(String name);
}
