package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.CarImagePayload;
import ar.edu.itba.paw.model.CarRequest;
import ar.edu.itba.paw.model.CarRequestImage;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Pagination;
import ar.edu.itba.paw.persistence.exception.PersistenceOperationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
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
        final long offset = Pagination.offsetFor(effectivePage, pageSize);

        final List<?> ids = em.createNativeQuery(
                "SELECT car_request_id FROM car_requests WHERE status = ? " +
                "ORDER BY created_at DESC, car_request_id DESC LIMIT ? OFFSET ?")
                .setParameter(1, status)
                .setParameter(2, pageSize)
                .setParameter(3, offset)
                .getResultList();

        if (ids.isEmpty()) {
            return Page.empty(effectivePage, pageSize);
        }

        final List<Long> longIds = ids.stream().map(r -> ((Number) r).longValue()).collect(Collectors.toList());
        final List<CarRequest> items = em.createQuery(
                "SELECT r FROM CarRequest r WHERE r.id IN :ids ORDER BY r.createdAt DESC, r.id DESC",
                CarRequest.class)
                .setParameter("ids", longIds)
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
        request.setSubmittedByUserId(submittedByUserId);
        request.setSubmitterEmail(submitterEmail);
        request.setBrandId(brandId);
        request.setBodyTypeId(bodyTypeId);
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
            final List<?> rawRows = em.createNativeQuery(
                    "SELECT image_id, car_request_id, display_order, content_type, updated_at " +
                    "FROM car_request_images WHERE car_request_id = ? " +
                    "ORDER BY display_order ASC, image_id ASC")
                    .setParameter(1, requestId)
                    .getResultList();

            return rawRows.stream().map(element -> {
                final Object[] r = (Object[]) element;
                final CarRequestImage img = new CarRequestImage();
                img.setImageId(((Number) r[0]).longValue());
                img.setRequestId(((Number) r[1]).longValue());
                img.setDisplayOrder(((Number) r[2]).intValue());
                img.setContentType((String) r[3]);
                img.setImageData(null);
                img.setUpdatedAt(r[4] instanceof Timestamp ? ((Timestamp) r[4]).toLocalDateTime()
                        : r[4] instanceof LocalDateTime ? (LocalDateTime) r[4] : null);
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
                    "SELECT i FROM CarRequestImage i WHERE i.requestId = :reqId AND i.imageId = :imgId",
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
            em.createNativeQuery("DELETE FROM car_request_images WHERE car_request_id = ?")
                    .setParameter(1, requestId)
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
        final int bound = em.createNativeQuery(
                "UPDATE car_requests SET submitted_by_user_id = ? " +
                "WHERE submitted_by_user_id IS NULL " +
                "AND submitter_email IS NOT NULL AND LOWER(BTRIM(submitter_email)) = LOWER(?)")
                .setParameter(1, userId)
                .setParameter(2, email)
                .executeUpdate();
        if (bound > 0) {
            LOGGER.info("bound {} pending car requests to user id={} email={}", bound, userId, email);
        }
        return bound;
    }
}
