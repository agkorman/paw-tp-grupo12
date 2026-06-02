package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Brand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;

@Repository
public class BrandJpaDao implements BrandDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(BrandJpaDao.class);

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<Brand> findAll() {
        return em.createQuery("SELECT b FROM Brand b ORDER BY b.name", Brand.class)
                .getResultList();
    }

    @Override
    public Optional<Brand> findById(final long id) {
        return Optional.ofNullable(em.find(Brand.class, id));
    }

    @Override
    public Optional<Brand> findByName(final String name) {
        return em.createQuery("SELECT b FROM Brand b WHERE LOWER(b.name) = LOWER(:name)", Brand.class)
                .setParameter("name", name)
                .getResultList()
                .stream().findFirst();
    }

    @Override
    public Brand create(final String name) {
        final Brand brand = new Brand(name);
        em.persist(brand);
        LOGGER.info("created brand id={} name={}", brand.getId(), name);
        return brand;
    }

    @Override
    public Optional<Brand> update(final long id, final String name) {
        final Brand brand = em.find(Brand.class, id);
        if (brand == null) {
            LOGGER.warn("brand update affected 0 rows id={}", id);
            return Optional.empty();
        }
        brand.setName(name);
        LOGGER.info("updated brand id={} name={}", id, name);
        return Optional.of(brand);
    }

    @Override
    public boolean delete(final long id) {
        final Brand brand = em.find(Brand.class, id);
        if (brand == null) {
            LOGGER.warn("brand delete affected 0 rows id={}", id);
            return false;
        }
        em.remove(brand);
        LOGGER.info("deleted brand id={}", id);
        return true;
    }
}
