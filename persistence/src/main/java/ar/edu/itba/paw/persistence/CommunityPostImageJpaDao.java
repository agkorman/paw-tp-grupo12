package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.CommunityPost;
import ar.edu.itba.paw.model.CommunityPostImage;
import ar.edu.itba.paw.model.ImagePayload;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

@Repository
public class CommunityPostImageJpaDao implements CommunityPostImageDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommunityPostImageJpaDao.class);

    @PersistenceContext
    private EntityManager em;

    @Override
    public List<CommunityPostImage> findAllByPostId(final long postId) {
        final List<?> rawRows = em.createQuery(
                        "SELECT i.imageId, i.post.id, i.displayOrder, i.contentType, i.updatedAt "
                                + "FROM CommunityPostImage i WHERE i.post.id = :postId "
                                + "ORDER BY i.displayOrder ASC, i.imageId ASC")
                .setParameter("postId", postId)
                .getResultList();
        final CommunityPost postRef = em.getReference(CommunityPost.class, postId);
        final List<CommunityPostImage> result = new ArrayList<>();
        for (final Object element : rawRows) {
            final Object[] row = (Object[]) element;
            final CommunityPostImage image = new CommunityPostImage();
            image.setImageId(((Number) row[0]).longValue());
            image.setPost(postRef);
            image.setDisplayOrder(((Number) row[2]).intValue());
            image.setContentType((String) row[3]);
            image.setImageData(null);
            image.setUpdatedAt((java.time.LocalDateTime) row[4]);
            result.add(image);
        }
        return result;
    }

    @Override
    public List<CommunityPostImage> findAllByPostIds(final Collection<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Collections.emptyList();
        }
        final List<?> rawRows = em.createQuery(
                        "SELECT i.imageId, i.post.id, i.displayOrder, i.contentType, i.updatedAt "
                                + "FROM CommunityPostImage i WHERE i.post.id IN :postIds "
                                + "ORDER BY i.post.id ASC, i.displayOrder ASC, i.imageId ASC")
                .setParameter("postIds", postIds)
                .getResultList();
        final List<CommunityPostImage> result = new ArrayList<>();
        for (final Object element : rawRows) {
            final Object[] row = (Object[]) element;
            final CommunityPostImage image = new CommunityPostImage();
            image.setImageId(((Number) row[0]).longValue());
            image.setPost(em.getReference(CommunityPost.class, ((Number) row[1]).longValue()));
            image.setDisplayOrder(((Number) row[2]).intValue());
            image.setContentType((String) row[3]);
            image.setImageData(null);
            image.setUpdatedAt((java.time.LocalDateTime) row[4]);
            result.add(image);
        }
        return result;
    }

    @Override
    public Optional<CommunityPostImage> findByPostIdAndImageId(final long postId, final long imageId) {
        return em.createQuery(
                        "SELECT i FROM CommunityPostImage i WHERE i.post.id = :postId AND i.imageId = :imageId",
                        CommunityPostImage.class)
                .setParameter("postId", postId)
                .setParameter("imageId", imageId)
                .getResultList()
                .stream()
                .findFirst();
    }

    @Override
    public void replaceAll(final long postId, final List<ImagePayload> images) {
        em.createQuery("DELETE FROM CommunityPostImage i WHERE i.post.id = :postId")
                .setParameter("postId", postId)
                .executeUpdate();
        if (images == null || images.isEmpty()) {
            LOGGER.info("cleared image gallery for community post id={}", postId);
            return;
        }
        final CommunityPost postRef = em.getReference(CommunityPost.class, postId);
        for (int i = 0; i < images.size(); i++) {
            final ImagePayload payload = images.get(i);
            final CommunityPostImage image = new CommunityPostImage();
            image.setPost(postRef);
            image.setDisplayOrder(i);
            image.setContentType(payload.getContentType());
            image.setImageData(payload.getImageData());
            em.persist(image);
        }
        LOGGER.info("replaced image gallery for community post id={} imageCount={}", postId, images.size());
    }
}
