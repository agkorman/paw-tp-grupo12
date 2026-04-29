package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.BodyType;

import java.util.List;
import java.util.Optional;

public interface BodyTypeDao {
    List<BodyType> findAll();

    Optional<BodyType> findById(long id);

    Optional<BodyType> findByName(String name);

    BodyType create(String name);

    Optional<BodyType> update(long id, String name);

    boolean delete(long id);
}
