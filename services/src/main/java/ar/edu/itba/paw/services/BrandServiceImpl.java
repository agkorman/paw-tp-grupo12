package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.Brand;
import ar.edu.itba.paw.persistence.BrandDao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class BrandServiceImpl implements BrandService {

    private final BrandDao brandDao;

    @Autowired
    public BrandServiceImpl(final BrandDao brandDao) {
        this.brandDao = brandDao;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Brand> findAll() {
        return brandDao.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Brand> findByName(final String name) {
        return brandDao.findByName(name);
    }
}
