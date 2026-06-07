package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.CarImage;
import ar.edu.itba.paw.model.ImageMetadata;
import ar.edu.itba.paw.model.ImagePayload;
import ar.edu.itba.paw.model.StoredImagePayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class CarImageJpaDao implements CarImageDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(CarImageJpaDao.class);

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<StoredImagePayload> findFirstByCarIdWithData(final long carId) {
        return em.createQuery(
                        "SELECT new ar.edu.itba.paw.model.StoredImagePayload("
                        + "i.imageId, i.car.id, i.displayOrder, i.contentType, i.imageData, i.updatedAt) "
                        + "FROM CarImage i WHERE i.car.id = :carId ORDER BY i.displayOrder ASC, i.imageId ASC",
                        StoredImagePayload.class)
                .setParameter("carId", carId)
                .setMaxResults(1)
                .getResultList()
                .stream()
                .findFirst();
    }

    @Override
    public List<ImageMetadata> findAllByCarId(final long carId) {
        return em.createQuery(
                        "SELECT new ar.edu.itba.paw.model.ImageMetadata("
                        + "i.imageId, i.car.id, i.displayOrder, i.contentType, i.updatedAt) "
                        + "FROM CarImage i WHERE i.car.id = :carId ORDER BY i.displayOrder ASC, i.imageId ASC",
                        ImageMetadata.class)
                .setParameter("carId", carId)
                .getResultList();
    }

    @Override
    public List<StoredImagePayload> findAllByCarIdWithData(final long carId) {
        return em.createQuery(
                        "SELECT new ar.edu.itba.paw.model.StoredImagePayload("
                        + "i.imageId, i.car.id, i.displayOrder, i.contentType, i.imageData, i.updatedAt) "
                        + "FROM CarImage i WHERE i.car.id = :carId ORDER BY i.displayOrder ASC, i.imageId ASC",
                        StoredImagePayload.class)
                .setParameter("carId", carId)
                .getResultList();
    }

    @Override
    public List<StoredImagePayload> findByCarIdAndImageIdsWithData(final long carId, final Collection<Long> imageIds) {
        if (imageIds == null) {
            return List.of();
        }
        final List<Long> ids = imageIds.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
        if (ids.isEmpty()) {
            return List.of();
        }
        return em.createQuery(
                        "SELECT new ar.edu.itba.paw.model.StoredImagePayload("
                        + "i.imageId, i.car.id, i.displayOrder, i.contentType, i.imageData, i.updatedAt) "
                        + "FROM CarImage i WHERE i.car.id = :carId AND i.imageId IN :imageIds "
                        + "ORDER BY i.displayOrder ASC, i.imageId ASC",
                        StoredImagePayload.class)
                .setParameter("carId", carId)
                .setParameter("imageIds", ids)
                .getResultList();
    }

    @Override
    public Optional<StoredImagePayload> findByCarIdAndImageId(final long carId, final long imageId) {
        return em.createQuery(
                        "SELECT new ar.edu.itba.paw.model.StoredImagePayload("
                        + "i.imageId, i.car.id, i.displayOrder, i.contentType, i.imageData, i.updatedAt) "
                        + "FROM CarImage i WHERE i.car.id = :carId AND i.imageId = :imageId",
                        StoredImagePayload.class)
                .setParameter("carId", carId)
                .setParameter("imageId", imageId)
                .getResultList()
                .stream()
                .findFirst();
    }

    @Override
    public void saveOrReplace(final long carId, final String contentType, final byte[] imageData) {
        replaceAll(carId, List.of(new ImagePayload(contentType, imageData)));
    }

    @Override
    public void replaceAll(final long carId, final List<ImagePayload> images) {
        em.createQuery("DELETE FROM CarImage i WHERE i.car.id = :carId")
                .setParameter("carId", carId)
                .executeUpdate();
        final Car carRef = em.getReference(Car.class, carId);
        for (int i = 0; i < images.size(); i++) {
            final ImagePayload payload = images.get(i);
            final CarImage img = new CarImage(carRef, i, payload.getContentType(), payload.getImageData());
            em.persist(img);
        }
        LOGGER.info("replaced image gallery for car id={} imageCount={}", carId, images.size());
    }

}
