package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.BodyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public class BodyTypeJpaDao implements BodyTypeDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(BodyTypeJpaDao.class);

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<BodyType> findAll() {
        return em.createQuery("SELECT b FROM BodyType b ORDER BY b.name", BodyType.class)
                .getResultList();
    }

    @Override
    public Optional<BodyType> findById(final long id) {
        return Optional.ofNullable(em.find(BodyType.class, id));
    }

    @Override
    public Optional<BodyType> findByName(final String name) {
        return em.createQuery("SELECT b FROM BodyType b WHERE LOWER(b.name) = LOWER(:name)", BodyType.class)
                .setParameter("name", name)
                .getResultList()
                .stream().findFirst();
    }

    @Override
    public BodyType create(final String name) {
        final BodyType bodyType = new BodyType();
        bodyType.setName(name);
        bodyType.setCreatedAt(LocalDateTime.now());
        em.persist(bodyType);
        LOGGER.info("created body type id={} name={}", bodyType.getId(), name);
        return bodyType;
    }

    @Override
    public Optional<BodyType> update(final long id, final String name) {
        final BodyType bodyType = em.find(BodyType.class, id);
        if (bodyType == null) {
            LOGGER.warn("body type update affected 0 rows id={}", id);
            return Optional.empty();
        }
        bodyType.setName(name);
        LOGGER.info("updated body type id={} name={}", id, name);
        return Optional.of(bodyType);
    }

    @Override
    public boolean delete(final long id) {
        final BodyType bodyType = em.find(BodyType.class, id);
        if (bodyType == null) {
            LOGGER.warn("body type delete affected 0 rows id={}", id);
            return false;
        }
        em.remove(bodyType);
        LOGGER.info("deleted body type id={}", id);
        return true;
    }
}
