package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.BodyType;
import ar.edu.itba.paw.persistence.BodyTypeDao;
import ar.edu.itba.paw.persistence.CarDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BodyTypeServiceImpl implements BodyTypeService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BodyTypeServiceImpl.class);

    private final BodyTypeDao bodyTypeDao;
    private final CarDao carDao;

    @Autowired
    public BodyTypeServiceImpl(final BodyTypeDao bodyTypeDao, final CarDao carDao) {
        this.bodyTypeDao = bodyTypeDao;
        this.carDao = carDao;
    }

    @Override
    @Transactional(readOnly = true)
    public List<BodyType> findAll() {
        return bodyTypeDao.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BodyType> findById(final long id) {
        return bodyTypeDao.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BodyType> findByName(final String name) {
        return bodyTypeDao.findByName(name);
    }

    @Override
    @Transactional
    public BodyType createBodyType(final String name) {
        final String normalized = StringUtils.normalizeRequired(name, "Body type name is required.");
        final BodyType bodyType = bodyTypeDao.create(normalized);
        LOGGER.info("created body type id={} name={}", bodyType.getId(), normalized);
        return bodyType;
    }

    @Override
    @Transactional
    public Optional<BodyType> updateBodyType(final long id, final String name) {
        final String normalized = StringUtils.normalizeRequired(name, "Body type name is required.");
        final Optional<BodyType> result = bodyTypeDao.update(id, normalized);
        result.ifPresent(bt -> LOGGER.info("updated body type id={} name={}", id, normalized));
        return result;
    }

    @Override
    @Transactional
    public boolean deleteBodyType(final long id) {
        if (bodyTypeDao.findById(id).isEmpty()) {
            LOGGER.warn("delete body type rejected: not found id={}", id);
            return false;
        }
        if (carDao.countByBodyTypeId(id) > 0) {
            LOGGER.warn("delete body type rejected: cars still reference body type id={}", id);
            return false;
        }
        final boolean deleted = bodyTypeDao.delete(id);
        if (deleted) {
            LOGGER.info("deleted body type id={}", id);
        }
        return deleted;
    }
}
