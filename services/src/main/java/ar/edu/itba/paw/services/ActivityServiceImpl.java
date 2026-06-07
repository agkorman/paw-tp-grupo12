package ar.edu.itba.paw.services;

import ar.edu.itba.paw.model.ActivityFeedCriteria;
import ar.edu.itba.paw.model.ActivityFeedItem;
import ar.edu.itba.paw.model.ActivityFeedPermissions;
import ar.edu.itba.paw.model.ActivityFeedReference;
import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.CommunityPost;
import ar.edu.itba.paw.model.ImageMetadata;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Pagination;
import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.persistence.ActivityDao;
import ar.edu.itba.paw.persistence.CarDao;
import ar.edu.itba.paw.persistence.CommunityDao;
import ar.edu.itba.paw.persistence.CommunityPostImageDao;
import ar.edu.itba.paw.persistence.ReviewDao;
import ar.edu.itba.paw.persistence.ReviewImageDao;
import ar.edu.itba.paw.persistence.ReviewLikeDao;
import ar.edu.itba.paw.persistence.ReviewReplyDao;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ActivityServiceImpl implements ActivityService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActivityServiceImpl.class);
    private static final String COMMUNITY_MODERATOR_ROLE = "moderator";

    private final ActivityDao activityDao;
    private final ReviewDao reviewDao;
    private final ReviewImageDao reviewImageDao;
    private final ReviewLikeDao reviewLikeDao;
    private final ReviewReplyDao reviewReplyDao;
    private final CommunityDao communityDao;
    private final CommunityPostImageDao communityPostImageDao;
    private final CarDao carDao;

    @Autowired
    public ActivityServiceImpl(final ActivityDao activityDao,
                               final ReviewDao reviewDao,
                               final ReviewImageDao reviewImageDao,
                               final ReviewLikeDao reviewLikeDao,
                               final ReviewReplyDao reviewReplyDao,
                               final CommunityDao communityDao,
                               final CommunityPostImageDao communityPostImageDao,
                               final CarDao carDao) {
        this.activityDao = activityDao;
        this.reviewDao = reviewDao;
        this.reviewImageDao = reviewImageDao;
        this.reviewLikeDao = reviewLikeDao;
        this.reviewReplyDao = reviewReplyDao;
        this.communityDao = communityDao;
        this.communityPostImageDao = communityPostImageDao;
        this.carDao = carDao;
    }

    @Override
    public Page<ActivityFeedItem> getActivityFeed(final ActivityFeedCriteria criteria) {
        LOGGER.debug("loading activity feed type={} timeframe={} sort={} page={}",
                criteria.getType(), criteria.getTimeframe(), criteria.getSort(), criteria.getPage());
        final Page<ActivityFeedReference> refsPage = activityDao.findFeed(criteria);
        if (refsPage.isEmpty()) {
            LOGGER.debug("loaded empty activity feed page={}", refsPage.getPageNumber());
            return Page.empty(refsPage.getPageNumber(), Pagination.ACTIVITY_PAGE_SIZE);
        }

        final List<Long> reviewIds = refsPage.getItems().stream()
                .filter(ActivityFeedReference::isReview)
                .map(ActivityFeedReference::getItemId)
                .toList();
        final List<Long> communityPostIds = refsPage.getItems().stream()
                .filter(ActivityFeedReference::isCommunityPost)
                .map(ActivityFeedReference::getItemId)
                .toList();

        final Map<Long, Review> reviewsById = (reviewIds.isEmpty() ? Collections.<Review>emptyList() : reviewDao.findByIds(reviewIds)).stream()
                .collect(Collectors.toMap(Review::getId, review -> review, (left, right) -> left, LinkedHashMap::new));
        final Map<Long, CommunityPost> postsById = (communityPostIds.isEmpty()
                ? Collections.<CommunityPost>emptyList()
                : communityDao.findPostsByIds(communityPostIds)).stream()
                .collect(Collectors.toMap(CommunityPost::getId, post -> post, (left, right) -> left, LinkedHashMap::new));
        final Map<Long, Car> carsById = loadCarsById(reviewsById.values());
        final Map<Long, Integer> reviewPagesById = reviewIds.isEmpty()
                ? Collections.emptyMap()
                : reviewDao.findDefaultPagesByReviewIds(reviewIds);
        final Map<Long, List<ImageMetadata>> reviewImagesById = loadReviewImagesById(reviewIds);
        final Map<Long, List<ImageMetadata>> postImagesById = loadCommunityPostImagesById(communityPostIds);
        final Map<Long, Long> commentCountsByPostId = communityPostIds.isEmpty()
                ? Collections.emptyMap()
                : communityDao.countCommentsByPostIds(communityPostIds);
        final Map<Long, Long> helpfulCountsByPostId = communityPostIds.isEmpty()
                ? Collections.emptyMap()
                : communityDao.countHelpfulReactionsByPostIds(communityPostIds);
        final Map<Long, Long> reviewLikeCountsById = reviewIds.isEmpty()
                ? Collections.emptyMap()
                : reviewLikeDao.countReviewLikesByReviewIds(reviewIds);
        final Map<Long, Long> reviewReplyCountsById = reviewIds.isEmpty()
                ? Collections.emptyMap()
                : reviewReplyDao.countRepliesByReviewIds(reviewIds);

        final List<ActivityFeedItem> items = new ArrayList<>();
        for (final ActivityFeedReference ref : refsPage.getItems()) {
            if (ref.isReview()) {
                final Review review = reviewsById.get(ref.getItemId());
                if (review == null) {
                    continue;
                }
                items.add(ActivityFeedItem.reviewItem(
                        review,
                        reviewLikeCountsById.getOrDefault(review.getId(), 0L),
                        reviewReplyCountsById.getOrDefault(review.getId(), 0L),
                        carsById.get(review.getCarId()),
                        reviewPagesById.getOrDefault(review.getId(), Pagination.DEFAULT_PAGE),
                        reviewImagesById.getOrDefault(review.getId(), Collections.emptyList())
                ));
                continue;
            }

            final CommunityPost post = postsById.get(ref.getItemId());
            if (post == null) {
                continue;
            }
            items.add(ActivityFeedItem.communityPostItem(
                    post,
                    helpfulCountsByPostId.getOrDefault(post.getId(), 0L),
                    commentCountsByPostId.getOrDefault(post.getId(), 0L),
                    postImagesById.getOrDefault(post.getId(), Collections.emptyList())
            ));
        }

        LOGGER.debug("loaded activity feed page={} itemCount={} totalItems={}",
                refsPage.getPageNumber(), items.size(), refsPage.getTotalItems());
        return new Page<>(items, refsPage.getPageNumber(), refsPage.getPageSize(), refsPage.getTotalItems());
    }

    @Override
    public Map<ActivityFeedReference, ActivityFeedPermissions> getActivityFeedPermissions(
            final Collection<ActivityFeedItem> items,
            final Long viewerUserId,
            final boolean viewerAdmin) {
        if (items == null || items.isEmpty() || (viewerUserId == null && !viewerAdmin)) {
            return Collections.emptyMap();
        }

        final List<Long> communityIds = items.stream()
                .filter(ActivityFeedItem::isCommunityPost)
                .map(item -> item.getCommunityPost().getCommunityId())
                .distinct()
                .toList();
        final Map<Long, String> communityRolesById = viewerUserId == null || communityIds.isEmpty()
                ? Collections.emptyMap()
                : communityDao.findMembershipRoles(viewerUserId, communityIds);

        final Map<ActivityFeedReference, ActivityFeedPermissions> permissionsByReference = new LinkedHashMap<>();
        for (final ActivityFeedItem item : items) {
            if (item.isReview()) {
                permissionsByReference.put(item.getReference(), reviewPermissions(item, viewerUserId, viewerAdmin));
                continue;
            }
            permissionsByReference.put(
                    item.getReference(),
                    communityPostPermissions(item, viewerUserId, viewerAdmin, communityRolesById)
            );
        }
        return permissionsByReference;
    }

    private ActivityFeedPermissions reviewPermissions(final ActivityFeedItem item,
                                                      final Long viewerUserId,
                                                      final boolean viewerAdmin) {
        final boolean ownedByViewer = viewerUserId != null
                && item.getReview().getUserId() != null
                && item.getReview().getUserId().equals(viewerUserId);
        return new ActivityFeedPermissions(
                item.getReference(),
                ownedByViewer,
                ownedByViewer,
                viewerAdmin && !ownedByViewer
        );
    }

    private ActivityFeedPermissions communityPostPermissions(final ActivityFeedItem item,
                                                             final Long viewerUserId,
                                                             final boolean viewerAdmin,
                                                             final Map<Long, String> communityRolesById) {
        final long communityId = item.getCommunityPost().getCommunityId();
        final boolean ownedByViewer = viewerUserId != null && item.getCommunityPost().getAuthorUserId() == viewerUserId;
        final boolean viewerCommunityModerator = COMMUNITY_MODERATOR_ROLE.equals(communityRolesById.get(communityId));
        return new ActivityFeedPermissions(
                item.getReference(),
                ownedByViewer,
                ownedByViewer,
                (viewerAdmin || viewerCommunityModerator) && !ownedByViewer
        );
    }

    private Map<Long, Car> loadCarsById(final Collection<Review> reviews) {
        final List<Long> carIds = reviews.stream()
                .map(Review::getCarId)
                .distinct()
                .toList();
        if (carIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return carDao.findByIds(carIds).stream()
                .collect(Collectors.toMap(Car::getId, car -> car, (left, right) -> left, LinkedHashMap::new));
    }

    private Map<Long, List<ImageMetadata>> loadReviewImagesById(final Collection<Long> reviewIds) {
        if (reviewIds == null || reviewIds.isEmpty()) {
            return Collections.emptyMap();
        }
        final Map<Long, List<ImageMetadata>> result = new LinkedHashMap<>();
        for (final ImageMetadata image : reviewImageDao.findAllByReviewIds(reviewIds)) {
            result.computeIfAbsent(image.getOwnerId(), ignored -> new ArrayList<>()).add(image);
        }
        return result;
    }

    private Map<Long, List<ImageMetadata>> loadCommunityPostImagesById(final Collection<Long> postIds) {
        if (postIds == null || postIds.isEmpty()) {
            return Collections.emptyMap();
        }
        final Map<Long, List<ImageMetadata>> result = new LinkedHashMap<>();
        for (final ImageMetadata image : communityPostImageDao.findAllByPostIds(postIds)) {
            result.computeIfAbsent(image.getOwnerId(), ignored -> new ArrayList<>()).add(image);
        }
        return result;
    }
}
