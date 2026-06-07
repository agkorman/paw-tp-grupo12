package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.CommunityPost;
import ar.edu.itba.paw.model.CommunityPostImage;
import ar.edu.itba.paw.model.ImageMetadata;
import ar.edu.itba.paw.model.ImagePayload;
import ar.edu.itba.paw.model.StoredImagePayload;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
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
    public List<ImageMetadata> findAllByPostId(final long postId) {
        return em.createQuery(
                        "SELECT new ar.edu.itba.paw.model.ImageMetadata("
                        + "i.imageId, i.post.id, i.displayOrder, i.contentType, i.updatedAt) "
                                + "FROM CommunityPostImage i WHERE i.post.id = :postId "
                                + "ORDER BY i.displayOrder ASC, i.imageId ASC",
                        ImageMetadata.class)
                .setParameter("postId", postId)
                .getResultList();
    }

    @Override
    public List<ImageMetadata> findAllByPostIds(final Collection<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Collections.emptyList();
        }
        return em.createQuery(
                        "SELECT new ar.edu.itba.paw.model.ImageMetadata("
                        + "i.imageId, i.post.id, i.displayOrder, i.contentType, i.updatedAt) "
                                + "FROM CommunityPostImage i WHERE i.post.id IN :postIds "
                                + "ORDER BY i.post.id ASC, i.displayOrder ASC, i.imageId ASC",
                        ImageMetadata.class)
                .setParameter("postIds", postIds)
                .getResultList();
    }

    @Override
    public Optional<StoredImagePayload> findByPostIdAndImageId(final long postId, final long imageId) {
        return em.createQuery(
                        "SELECT new ar.edu.itba.paw.model.StoredImagePayload("
                        + "i.imageId, i.post.id, i.displayOrder, i.contentType, i.imageData, i.updatedAt) "
                        + "FROM CommunityPostImage i WHERE i.post.id = :postId AND i.imageId = :imageId",
                        StoredImagePayload.class)
                .setParameter("postId", postId)
                .setParameter("imageId", imageId)
                .getResultList()
                .stream()
                .findFirst();
    }

    @Override
    public List<StoredImagePayload> findByPostIdAndImageIdsWithData(final long postId, final Collection<Long> imageIds) {
        if (imageIds == null) {
            return List.of();
        }
        final List<Long> ids = imageIds.stream().filter(Objects::nonNull).distinct().collect(Collectors.toList());
        if (ids.isEmpty()) {
            return List.of();
        }
        return em.createQuery(
                        "SELECT new ar.edu.itba.paw.model.StoredImagePayload("
                                + "i.imageId, i.post.id, i.displayOrder, i.contentType, i.imageData, i.updatedAt) "
                                + "FROM CommunityPostImage i WHERE i.post.id = :postId AND i.imageId IN :imageIds "
                                + "ORDER BY i.displayOrder ASC, i.imageId ASC",
                        StoredImagePayload.class)
                .setParameter("postId", postId)
                .setParameter("imageIds", ids)
                .getResultList();
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
