package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.AdminRequest;
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
public class AdminRequestJpaDao implements AdminRequestDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(AdminRequestJpaDao.class);

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<AdminRequest> findById(final long id) {
        return Optional.ofNullable(em.find(AdminRequest.class, id));
    }

    @Override
    public List<AdminRequest> findByStatus(final String status) {
        return em.createQuery(
                "SELECT a FROM AdminRequest a WHERE a.status = :status ORDER BY a.createdAt DESC, a.id DESC",
                AdminRequest.class)
                .setParameter("status", status)
                .getResultList();
    }

    @Override
    public Page<AdminRequest> findByStatus(final String status, final int page) {
        final int normalizedPage = Pagination.normalizePage(page);
        final int pageSize = Pagination.REQUESTS_PAGE_SIZE;

        final long totalItems = countByStatus(status);
        if (totalItems == 0L) {
            return Page.empty(Pagination.DEFAULT_PAGE, pageSize);
        }

        final int effectivePage = Pagination.clampPage(normalizedPage, totalItems, pageSize);
        final long offset = Pagination.offsetFor(effectivePage, pageSize);

        final List<?> ids = em.createNativeQuery(
                "SELECT admin_request_id FROM admin_requests WHERE status = ? " +
                "ORDER BY created_at DESC, admin_request_id DESC LIMIT ? OFFSET ?")
                .setParameter(1, status)
                .setParameter(2, pageSize)
                .setParameter(3, offset)
                .getResultList();

        if (ids.isEmpty()) {
            return Page.empty(effectivePage, pageSize);
        }

        final List<Long> longIds = ids.stream().map(r -> ((Number) r).longValue()).collect(Collectors.toList());
        final List<AdminRequest> items = em.createQuery(
                "SELECT a FROM AdminRequest a WHERE a.id IN :ids ORDER BY a.createdAt DESC, a.id DESC",
                AdminRequest.class)
                .setParameter("ids", longIds)
                .getResultList();

        return new Page<>(items, effectivePage, pageSize, totalItems);
    }

    @Override
    public long countByStatus(final String status) {
        return em.createQuery(
                "SELECT COUNT(a) FROM AdminRequest a WHERE a.status = :status", Long.class)
                .setParameter("status", status)
                .getSingleResult();
    }

    @Override
    public boolean existsPendingByUser(final long userId) {
        return em.createQuery(
                "SELECT COUNT(a) FROM AdminRequest a WHERE a.submittedByUserId = :userId AND a.status = :status", Long.class)
                .setParameter("userId", userId)
                .setParameter("status", "pending")
                .getSingleResult() > 0L;
    }

    @Override
    public AdminRequest create(final long submittedByUserId, final String submitterEmail,
                               final String motivation, final String bio, final String justification,
                               final String status) {
        final AdminRequest request = new AdminRequest();
        request.setSubmittedByUserId(submittedByUserId);
        request.setSubmitterEmail(submitterEmail);
        request.setMotivation(motivation);
        request.setBio(bio);
        request.setJustification(justification);
        request.setStatus(status);
        em.persist(request);
        LOGGER.info("created admin request id={} userId={} status={}", request.getId(), submittedByUserId, status);
        return request;
    }

    @Override
    public boolean updateStatus(final long id, final String expectedStatus, final String newStatus) {
        final int rows = em.createQuery(
                "UPDATE AdminRequest a SET a.status = :new WHERE a.id = :id AND a.status = :expected")
                .setParameter("new", newStatus)
                .setParameter("id", id)
                .setParameter("expected", expectedStatus)
                .executeUpdate();
        if (rows > 0) {
            LOGGER.info("updated admin request id={} status {}->{}", id, expectedStatus, newStatus);
        } else {
            LOGGER.warn("admin request status update affected 0 rows id={} expectedStatus={} newStatus={}", id, expectedStatus, newStatus);
        }
        return rows > 0;
    }
}
