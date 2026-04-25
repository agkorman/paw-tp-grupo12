package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.BodyType;
import ar.edu.itba.paw.persistence.BodyTypeDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BodyTypeServiceImpl implements BodyTypeService {

    private final BodyTypeDao bodyTypeDao;

    @Autowired
    public BodyTypeServiceImpl(final BodyTypeDao bodyTypeDao) {
        this.bodyTypeDao = bodyTypeDao;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BodyType> findAll() {
        return bodyTypeDao.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BodyType> findByName(final String name) {
        return bodyTypeDao.findByName(name);
    }
}
