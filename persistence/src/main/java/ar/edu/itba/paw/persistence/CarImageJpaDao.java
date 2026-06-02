package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.CarImage;
import ar.edu.itba.paw.model.ImagePayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Repository
public class CarImageJpaDao implements CarImageDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(CarImageJpaDao.class);

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<CarImage> findByCarId(final long carId) {
        return em.createQuery(
                        "SELECT i FROM CarImage i WHERE i.car.id = :carId ORDER BY i.displayOrder ASC, i.imageId ASC",
                        CarImage.class)
                .setParameter("carId", carId)
                .setMaxResults(1)
                .getResultList()
                .stream().findFirst();
    }

    @Override
    public List<CarImage> findAllByCarId(final long carId) {
        final List<?> rawRows = em.createQuery(
                        "SELECT i.imageId, i.car.id, i.displayOrder, i.contentType, i.updatedAt "
                        + "FROM CarImage i WHERE i.car.id = :carId ORDER BY i.displayOrder ASC, i.imageId ASC")
                .setParameter("carId", carId)
                .getResultList();
        final Car carRef = em.getReference(Car.class, carId);
        final List<CarImage> result = new ArrayList<>();
        for (final Object element : rawRows) {
            final Object[] row = (Object[]) element;
            final CarImage image = new CarImage(carRef, ((Number) row[2]).intValue(), (String) row[3], null);
            image.setImageId(((Number) row[0]).longValue());
            image.setUpdatedAt((java.time.LocalDateTime) row[4]);
            result.add(image);
        }
        return result;
    }

    @Override
    public List<CarImage> findAllByCarIdWithData(final long carId) {
        return em.createQuery(
                        "SELECT i FROM CarImage i WHERE i.car.id = :carId ORDER BY i.displayOrder ASC, i.imageId ASC",
                        CarImage.class)
                .setParameter("carId", carId)
                .getResultList();
    }

    @Override
    public Optional<CarImage> findByCarIdAndImageId(final long carId, final long imageId) {
        return em.createQuery(
                        "SELECT i FROM CarImage i WHERE i.car.id = :carId AND i.imageId = :imageId",
                        CarImage.class)
                .setParameter("carId", carId)
                .setParameter("imageId", imageId)
                .getResultList()
                .stream().findFirst();
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
