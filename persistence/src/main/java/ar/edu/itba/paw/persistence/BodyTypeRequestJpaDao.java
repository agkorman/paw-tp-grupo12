package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.BodyTypeRequest;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Pagination;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class BodyTypeRequestJpaDao implements BodyTypeRequestDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(BodyTypeRequestJpaDao.class);

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<BodyTypeRequest> findById(final long id) {
        return Optional.ofNullable(em.find(BodyTypeRequest.class, id));
    }

    @Override
    public List<BodyTypeRequest> findByStatus(final String status) {
        return em.createQuery(
                "SELECT b FROM BodyTypeRequest b WHERE b.status = :status ORDER BY b.createdAt DESC, b.id DESC",
                BodyTypeRequest.class)
                .setParameter("status", status)
                .getResultList();
    }

    @Override
    public Page<BodyTypeRequest> findByStatus(final String status, final int page) {
        final int normalizedPage = Pagination.normalizePage(page);
        final int pageSize = Pagination.REQUESTS_PAGE_SIZE;

        final long totalItems = countByStatus(status);
        if (totalItems == 0L) {
            return Page.empty(Pagination.DEFAULT_PAGE, pageSize);
        }

        final int effectivePage = Pagination.clampPage(normalizedPage, totalItems, pageSize);
        final long offset = Pagination.offsetFor(effectivePage, pageSize);

        final List<?> ids = em.createNativeQuery(
                "SELECT body_type_request_id FROM body_type_requests WHERE status = ? " +
                "ORDER BY created_at DESC, body_type_request_id DESC LIMIT ? OFFSET ?")
                .setParameter(1, status)
                .setParameter(2, pageSize)
                .setParameter(3, offset)
                .getResultList();

        if (ids.isEmpty()) {
            return Page.empty(effectivePage, pageSize);
        }

        final List<Long> longIds = ids.stream().map(r -> ((Number) r).longValue()).collect(Collectors.toList());
        final List<BodyTypeRequest> items = em.createQuery(
                "SELECT b FROM BodyTypeRequest b WHERE b.id IN :ids ORDER BY b.createdAt DESC, b.id DESC",
                BodyTypeRequest.class)
                .setParameter("ids", longIds)
                .getResultList();

        return new Page<>(items, effectivePage, pageSize, totalItems);
    }

    @Override
    public long countByStatus(final String status) {
        return em.createQuery(
                "SELECT COUNT(b) FROM BodyTypeRequest b WHERE b.status = :status", Long.class)
                .setParameter("status", status)
                .getSingleResult();
    }

    @Override
    public BodyTypeRequest create(final Long submittedByUserId, final String submitterEmail,
                                  final String name, final String comments, final String status) {
        final BodyTypeRequest request = new BodyTypeRequest();
        request.setSubmittedByUserId(submittedByUserId);
        request.setSubmitterEmail(submitterEmail);
        request.setName(name);
        request.setComments(comments);
        request.setStatus(status);
        em.persist(request);
        LOGGER.info("created body type request id={} userId={} name={} status={}", request.getId(), submittedByUserId, name, status);
        return request;
    }

    @Override
    public boolean updateStatus(final long id, final String expectedStatus, final String newStatus) {
        final int rows = em.createQuery(
                "UPDATE BodyTypeRequest b SET b.status = :new WHERE b.id = :id AND b.status = :expected")
                .setParameter("new", newStatus)
                .setParameter("id", id)
                .setParameter("expected", expectedStatus)
                .executeUpdate();
        if (rows > 0) {
            LOGGER.info("updated body type request id={} status {}->{}", id, expectedStatus, newStatus);
        } else {
            LOGGER.warn("body type request status update affected 0 rows id={} expectedStatus={} newStatus={}", id, expectedStatus, newStatus);
        }
        return rows > 0;
    }
}
