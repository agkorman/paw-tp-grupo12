package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Brand;
import ar.edu.itba.paw.persistence.BrandDao;
import ar.edu.itba.paw.persistence.CarDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BrandServiceImpl implements BrandService {

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
        return brandDao.create(normalized);
    }

    @Override
    @Transactional
    public Optional<Brand> updateBrand(final long id, final String name) {
        final String normalized = StringUtils.normalizeRequired(name, "Brand name is required.");
        return brandDao.update(id, normalized);
    }

    @Override
    @Transactional
    public boolean deleteBrand(final long id) {
        if (brandDao.findById(id).isEmpty()) {
            return false;
        }
        if (carDao.countByBrandId(id) > 0) {
            return false;
        }
        return brandDao.delete(id);
    }
}
