package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.BrandRequest;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Pagination;
import ar.edu.itba.paw.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
public class BrandRequestJpaDao implements BrandRequestDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(BrandRequestJpaDao.class);

    @PersistenceContext
    private EntityManager em;

    @Override
    public Optional<BrandRequest> findById(final long id) {
        return Optional.ofNullable(em.find(BrandRequest.class, id));
    }

    @Override
    public Page<BrandRequest> findByStatus(final String status, final int page) {
        final int normalizedPage = Pagination.normalizePage(page);
        final int pageSize = Pagination.REQUESTS_PAGE_SIZE;

        final long totalItems = countByStatus(status);
        if (totalItems == 0L) {
            return Page.empty(Pagination.DEFAULT_PAGE, pageSize);
        }

        final int effectivePage = Pagination.clampPage(normalizedPage, totalItems, pageSize);
        final List<?> ids = em.createNativeQuery(
                "SELECT brand_request_id FROM brand_requests WHERE status = ? " +
                "ORDER BY created_at DESC, brand_request_id DESC LIMIT ? OFFSET ?")
                .setParameter(1, status)
                .setParameter(2, pageSize)
                .setParameter(3, Pagination.offsetFor(effectivePage, pageSize))
                .getResultList();

        if (ids.isEmpty()) {
            return Page.empty(effectivePage, pageSize);
        }

        final List<Long> longIds = ids.stream().map(r -> ((Number) r).longValue()).collect(Collectors.toList());
        final List<BrandRequest> items = em.createQuery(
                "SELECT b FROM BrandRequest b WHERE b.id IN :ids ORDER BY b.createdAt DESC, b.id DESC",
                BrandRequest.class)
                .setParameter("ids", longIds)
                .getResultList();

        return new Page<>(items, effectivePage, pageSize, totalItems);
    }

    @Override
    public long countByStatus(final String status) {
        return em.createQuery(
                "SELECT COUNT(b) FROM BrandRequest b WHERE b.status = :status", Long.class)
                .setParameter("status", status)
                .getSingleResult();
    }

    @Override
    public BrandRequest create(final Long submittedByUserId, final String submitterEmail,
                               final String name, final String comments, final String status) {
        final BrandRequest request = new BrandRequest(name, status);
        if (submittedByUserId != null) {
            request.setSubmittedByUser(em.getReference(User.class, submittedByUserId));
        }
        request.setSubmitterEmail(submitterEmail);
        request.setComments(comments);
        em.persist(request);
        LOGGER.info("created brand request id={} userId={} name={} status={}", request.getId(), submittedByUserId, name, status);
        return request;
    }

    @Override
    public boolean updateStatus(final long id, final String expectedStatus, final String newStatus) {
        final int rows = em.createQuery(
                "UPDATE BrandRequest b SET b.status = :new WHERE b.id = :id AND b.status = :expected")
                .setParameter("new", newStatus)
                .setParameter("id", id)
                .setParameter("expected", expectedStatus)
                .executeUpdate();
        if (rows > 0) {
            LOGGER.info("updated brand request id={} status {}->{}", id, expectedStatus, newStatus);
        } else {
            LOGGER.warn("brand request status update affected 0 rows id={} expectedStatus={} newStatus={}", id, expectedStatus, newStatus);
        }
        return rows > 0;
    }
}
