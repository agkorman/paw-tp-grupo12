package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.BodyType;
import ar.edu.itba.paw.model.Brand;
import ar.edu.itba.paw.model.CarImagePayload;
import ar.edu.itba.paw.model.CarRequest;
import ar.edu.itba.paw.model.CarRequestImage;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Pagination;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.persistence.exception.PersistenceOperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class CarRequestJpaDao implements CarRequestDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(CarRequestJpaDao.class);

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<CarRequest> findById(final long id) {
        return Optional.ofNullable(em.find(CarRequest.class, id));
    }

    @Override
    public List<CarRequest> findByStatus(final String status) {
        return em.createQuery(
                "SELECT r FROM CarRequest r WHERE r.status = :status ORDER BY r.createdAt DESC, r.id DESC",
                CarRequest.class)
                .setParameter("status", status)
                .getResultList();
    }

    @Override
    public Page<CarRequest> findByStatus(final String status, final int page) {
        final int normalizedPage = Pagination.normalizePage(page);
        final int pageSize = Pagination.REQUESTS_PAGE_SIZE;

        final long totalItems = countByStatus(status);
        if (totalItems == 0L) {
            return Page.empty(Pagination.DEFAULT_PAGE, pageSize);
        }

        final int effectivePage = Pagination.clampPage(normalizedPage, totalItems, pageSize);
        final List<CarRequest> items = em.createQuery(
                "SELECT r FROM CarRequest r WHERE r.status = :status ORDER BY r.createdAt DESC, r.id DESC",
                CarRequest.class)
                .setParameter("status", status)
                .setFirstResult((int) Pagination.offsetFor(effectivePage, pageSize))
                .setMaxResults(pageSize)
                .getResultList();

        return new Page<>(items, effectivePage, pageSize, totalItems);
    }

    @Override
    public long countByStatus(final String status) {
        return em.createQuery(
                "SELECT COUNT(r) FROM CarRequest r WHERE r.status = :status", Long.class)
                .setParameter("status", status)
                .getSingleResult();
    }

    @Override
    public CarRequest create(final long submittedByUserId, final String submitterEmail, final long brandId,
                             final long bodyTypeId, final Integer year, final String model, final String description,
                             final String imageContentType, final byte[] imageData, final String status,
                             final String fuelType, final Integer horsepower, final Integer airbagCount,
                             final String transmission, final BigDecimal fuelConsumption, final Integer maxSpeedKmh,
                             final BigDecimal priceUsd) {
        final CarRequest request = new CarRequest();
        request.setSubmittedByUser(em.getReference(User.class, submittedByUserId));
        request.setSubmitterEmail(submitterEmail);
        request.setBrand(em.getReference(Brand.class, brandId));
        request.setBodyType(em.getReference(BodyType.class, bodyTypeId));
        request.setYear(year);
        request.setModel(model);
        request.setDescription(description);
        request.setImageContentType(imageContentType);
        request.setImageData(imageData);
        request.setStatus(status);
        request.setFuelType(fuelType);
        request.setHorsepower(horsepower);
        request.setAirbagCount(airbagCount);
        request.setTransmission(transmission);
        request.setFuelConsumption(fuelConsumption);
        request.setMaxSpeedKmh(maxSpeedKmh);
        request.setPriceUsd(priceUsd);
        em.persist(request);
        LOGGER.info("created car request id={} userId={} model={} status={}", request.getId(), submittedByUserId, model, status);
        return request;
    }

    @Override
    public List<CarRequestImage> findImagesByRequestId(final long requestId) {
        try {
            final List<?> rawRows = em.createQuery(
                    "SELECT i.imageId, i.request.id, i.displayOrder, i.contentType, i.updatedAt " +
                    "FROM CarRequestImage i WHERE i.request.id = :requestId " +
                    "ORDER BY i.displayOrder ASC, i.imageId ASC")
                    .setParameter("requestId", requestId)
                    .getResultList();

            final CarRequest requestRef = em.getReference(CarRequest.class, requestId);
            return rawRows.stream().map(element -> {
                final Object[] r = (Object[]) element;
                final CarRequestImage img = new CarRequestImage();
                img.setImageId(((Number) r[0]).longValue());
                img.setRequest(requestRef);
                img.setDisplayOrder(((Number) r[2]).intValue());
                img.setContentType((String) r[3]);
                img.setImageData(null);
                img.setUpdatedAt((java.time.LocalDateTime) r[4]);
                return img;
            }).collect(Collectors.toList());
        } catch (final Exception e) {
            LOGGER.error("failed to fetch image metadata for car request id={}", requestId, e);
            throw new PersistenceOperationException("fetch image metadata for car request " + requestId, e);
        }
    }

    @Override
    public Optional<CarRequestImage> findImageByRequestIdAndImageId(final long requestId, final long imageId) {
        try {
            final List<CarRequestImage> results = em.createQuery(
                    "SELECT i FROM CarRequestImage i WHERE i.request.id = :reqId AND i.imageId = :imgId",
                    CarRequestImage.class)
                    .setParameter("reqId", requestId)
                    .setParameter("imgId", imageId)
                    .getResultList();
            return results.stream().findFirst();
        } catch (final Exception e) {
            LOGGER.error("failed to fetch image id={} for car request id={}", imageId, requestId, e);
            throw new PersistenceOperationException("fetch image " + imageId + " for car request " + requestId, e);
        }
    }

    @Override
    public void replaceImages(final long requestId, final List<CarImagePayload> images) {
        try {
            em.createQuery("DELETE FROM CarRequestImage i WHERE i.request.id = :requestId")
                    .setParameter("requestId", requestId)
                    .executeUpdate();

            final CarRequest requestRef = em.getReference(CarRequest.class, requestId);
            for (int i = 0; i < images.size(); i++) {
                final CarImagePayload payload = images.get(i);
                final CarRequestImage img = new CarRequestImage();
                img.setRequest(requestRef);
                img.setDisplayOrder(i);
                img.setContentType(payload.getContentType());
                img.setImageData(payload.getImageData());
                em.persist(img);
            }
            LOGGER.info("replaced image gallery for car request id={} imageCount={}", requestId, images.size());
        } catch (final Exception e) {
            LOGGER.error("failed to replace image gallery for car request id={}", requestId, e);
            throw new PersistenceOperationException("replace image gallery for car request " + requestId, e);
        }
    }

    @Override
    public boolean updateStatus(final long id, final String expectedStatus, final String newStatus) {
        final int rows = em.createQuery(
                "UPDATE CarRequest r SET r.status = :new WHERE r.id = :id AND r.status = :expected")
                .setParameter("new", newStatus)
                .setParameter("id", id)
                .setParameter("expected", expectedStatus)
                .executeUpdate();
        if (rows > 0) {
            LOGGER.info("updated car request id={} status {}->{}", id, expectedStatus, newStatus);
        } else {
            LOGGER.warn("car request status update affected 0 rows id={} expectedStatus={} newStatus={}", id, expectedStatus, newStatus);
        }
        return rows > 0;
    }

    @Override
    public int bindRequestsToUserByEmail(final long userId, final String email) {
        final int bound = em.createQuery(
                "UPDATE CarRequest r SET r.submittedByUser = :user " +
                "WHERE r.submittedByUser IS NULL " +
                "AND r.submitterEmail IS NOT NULL AND LOWER(TRIM(r.submitterEmail)) = LOWER(:email)")
                .setParameter("user", em.getReference(User.class, userId))
                .setParameter("email", email)
                .executeUpdate();
        if (bound > 0) {
            LOGGER.info("bound {} pending car requests to user id={} email={}", bound, userId, email);
        }
        return bound;
    }
}
