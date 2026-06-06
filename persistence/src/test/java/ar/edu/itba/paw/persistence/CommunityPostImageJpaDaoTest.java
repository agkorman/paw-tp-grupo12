package ar.edu.itba.paw.persistence;

import ar.edu.itba.paw.model.Community;
import ar.edu.itba.paw.model.CommunityPost;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.model.ImagePayload;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CommunityPostImageJpaDaoTest extends AbstractPersistenceTest {

    @Test
    public void shouldInsertCommunityPostImagesInOrderWhenNoExisting() {
        // Arrange
        final CommunityPost post = createCommunityPost("insert-order");
        final List<ImagePayload> payloads = List.of(
                new ImagePayload("image/png", new byte[]{1, 2}),
                new ImagePayload("image/jpeg", new byte[]{3, 4})
        );

        // Exercise
        communityPostImageDao.replaceAll(post.getId(), payloads);

        // Assertions
        flushAndClear();
        assertEquals(2, countRows("SELECT COUNT(*) FROM community_post_images WHERE post_id = ?", post.getId()));
        assertEquals("image/png", jdbcTemplate.queryForObject(
                "SELECT content_type FROM community_post_images WHERE post_id = ? AND display_order = 0",
                String.class, post.getId()
        ));
        assertArrayEquals(new byte[]{1, 2}, jdbcTemplate.queryForObject(
                "SELECT image_data FROM community_post_images WHERE post_id = ? AND display_order = 0",
                byte[].class, post.getId()
        ));
        assertEquals("image/jpeg", jdbcTemplate.queryForObject(
                "SELECT content_type FROM community_post_images WHERE post_id = ? AND display_order = 1",
                String.class, post.getId()
        ));
    }

    @Test
    public void shouldReplaceExistingCommunityPostGalleryWithNewImages() {
        // Arrange
        final CommunityPost post = createCommunityPost("replace");
        jdbcTemplate.update(
                "INSERT INTO community_post_images (post_id, display_order, content_type, image_data) VALUES (?, ?, ?, ?)",
                post.getId(), 0, "image/png", new byte[]{1}
        );

        // Exercise
        communityPostImageDao.replaceAll(post.getId(), List.of(new ImagePayload("image/jpeg", new byte[]{9})));

        // Assertions
        flushAndClear();
        assertEquals(1, countRows("SELECT COUNT(*) FROM community_post_images WHERE post_id = ?", post.getId()));
        assertEquals(0, countRows(
                "SELECT COUNT(*) FROM community_post_images WHERE post_id = ? AND content_type = ?",
                post.getId(), "image/png"
        ));
    }

    @Test
    public void shouldReturnMetadataWithoutBytesForFindAllByPostId() {
        // Arrange
        final CommunityPost post = createCommunityPost("metadata");
        jdbcTemplate.update(
                "INSERT INTO community_post_images (post_id, display_order, content_type, image_data) VALUES (?, ?, ?, ?)",
                post.getId(), 1, "image/jpeg", new byte[]{2}
        );
        jdbcTemplate.update(
                "INSERT INTO community_post_images (post_id, display_order, content_type, image_data) VALUES (?, ?, ?, ?)",
                post.getId(), 0, "image/png", new byte[]{1}
        );

        // Exercise
        final List<ar.edu.itba.paw.model.CommunityPostImage> result = communityPostImageDao.findAllByPostId(post.getId());

        // Assertions
        assertEquals(2, result.size());
        assertEquals("image/png", result.get(0).getContentType());
        assertEquals(0, result.get(0).getDisplayOrder());
        assertNull(result.get(0).getImageData());
    }

    @Test
    public void shouldReturnBytesForFindByPostIdAndImageId() {
        // Arrange
        final CommunityPost post = createCommunityPost("bytes");
        jdbcTemplate.update(
                "INSERT INTO community_post_images (post_id, display_order, content_type, image_data) VALUES (?, ?, ?, ?)",
                post.getId(), 0, "image/png", new byte[]{7, 8}
        );
        final long imageId = jdbcTemplate.queryForObject(
                "SELECT image_id FROM community_post_images WHERE post_id = ? AND display_order = 0",
                Long.class, post.getId()
        );

        // Exercise
        final Optional<ar.edu.itba.paw.model.CommunityPostImage> result =
                communityPostImageDao.findByPostIdAndImageId(post.getId(), imageId);

        // Assertions
        assertTrue(result.isPresent());
        assertEquals("image/png", result.get().getContentType());
        assertArrayEquals(new byte[]{7, 8}, result.get().getImageData());
    }

    @Test
    public void shouldReturnEmptyWhenImageDoesNotBelongToPost() {
        // Arrange
        final CommunityPost post = createCommunityPost("mismatch");

        // Exercise
        final Optional<ar.edu.itba.paw.model.CommunityPostImage> result =
                communityPostImageDao.findByPostIdAndImageId(post.getId(), 999999L);

        // Assertions
        assertFalse(result.isPresent());
    }

    private CommunityPost createCommunityPost(final String suffix) {
        final User user = createUser("community-post-" + suffix);
        final Community community = createCommunity("community-post-" + suffix, user.getId());
        jdbcTemplate.update(
                "INSERT INTO community_memberships (community_id, user_id, role) VALUES (?, ?, ?)",
                community.getId(), user.getId(), "moderator"
        );
        jdbcTemplate.update(
                "INSERT INTO community_posts (community_id, author_user_id, slug, title, body) VALUES (?, ?, ?, ?, ?)",
                community.getId(), user.getId(), "post-" + suffix, "Title " + suffix, "Body " + suffix
        );
        final Long postId = jdbcTemplate.queryForObject(
                "SELECT post_id FROM community_posts WHERE community_id = ? AND slug = ?",
                Long.class, community.getId(), "post-" + suffix
        );
        final CommunityPost post = new CommunityPost();
        post.setId(postId);
        post.setCommunity(community);
        post.setAuthor(user);
        post.setSlug("post-" + suffix);
        post.setTitle("Title " + suffix);
        post.setBody("Body " + suffix);
        return post;
    }

    private Community createCommunity(final String suffix, final long ownerUserId) {
        jdbcTemplate.update(
                "INSERT INTO communities (slug, name, description, created_by_user_id) VALUES (?, ?, ?, ?)",
                suffix, "Community " + suffix, "Description " + suffix, ownerUserId
        );
        final Long communityId = jdbcTemplate.queryForObject(
                "SELECT community_id FROM communities WHERE slug = ?",
                Long.class, suffix
        );
        final Community community = new Community(
                suffix,
                "Community " + suffix,
                "Description " + suffix
        );
        community.setId(communityId);
        return community;
    }
}
