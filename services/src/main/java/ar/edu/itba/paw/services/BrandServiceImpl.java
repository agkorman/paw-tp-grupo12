package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Brand;
import ar.edu.itba.paw.persistence.BrandDao;
import ar.edu.itba.paw.persistence.CarDao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BrandServiceImpl implements BrandService {

    private static final Logger LOGGER = LoggerFactory.getLogger(BrandServiceImpl.class);

    private final BrandDao brandDao;
    private final CarDao carDao;

    @Autowired
    public BrandServiceImpl(final BrandDao brandDao, final CarDao carDao) {
        this.brandDao = brandDao;
        this.carDao = carDao;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Brand> findAll() {
        return brandDao.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Brand> findById(final long id) {
        return brandDao.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Brand> findByName(final String name) {
        return brandDao.findByName(name);
    }

    @Override
    @Transactional
    public Brand createBrand(final String name) {
        final String normalized = StringUtils.normalizeRequired(name, "Brand name is required.");
        final Brand brand = brandDao.create(normalized);
        LOGGER.info("created brand id={} name={}", brand.getId(), normalized);
        return brand;
    }

    @Override
    @Transactional
    public Optional<Brand> updateBrand(final long id, final String name) {
        final String normalized = StringUtils.normalizeRequired(name, "Brand name is required.");
        final Optional<Brand> result = brandDao.update(id, normalized);
        result.ifPresent(b -> LOGGER.info("updated brand id={} name={}", id, normalized));
        return result;
    }

    @Override
    @Transactional
    public boolean deleteBrand(final long id) {
        if (brandDao.findById(id).isEmpty()) {
            LOGGER.warn("delete brand rejected: not found id={}", id);
            return false;
        }
        if (carDao.countByBrandId(id) > 0) {
            LOGGER.warn("delete brand rejected: cars still reference brand id={}", id);
            return false;
        }
        final boolean deleted = brandDao.delete(id);
        if (deleted) {
            LOGGER.info("deleted brand id={}", id);
        }
        return deleted;
    }
}
