package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.ActivityFeedCriteria;
import ar.edu.itba.paw.model.ActivityFeedItem;
import ar.edu.itba.paw.model.ActivityFeedPermissions;
import ar.edu.itba.paw.model.ActivityFeedReference;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.model.ImageMetadata;
import ar.edu.itba.paw.services.ActivityService;
import ar.edu.itba.paw.services.CommunityService;
import ar.edu.itba.paw.services.ReviewLikeService;
import ar.edu.itba.paw.webapp.auth.AuthenticatedUser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class ActivityController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActivityController.class);

    private final ActivityService activityService;
    private final ReviewLikeService reviewLikeService;
    private final CommunityService communityService;

    @Autowired
    public ActivityController(final ActivityService activityService,
                              final ReviewLikeService reviewLikeService,
                              final CommunityService communityService) {
        this.activityService = activityService;
        this.reviewLikeService = reviewLikeService;
        this.communityService = communityService;
    }

    @InitBinder
    public void initBinder(final WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @RequestMapping(value = "/activity", method = RequestMethod.GET)
    public ModelAndView activity(
                                 @ModelAttribute("activityCriteria") final ActivityFeedCriteria criteria,
                                 final HttpServletRequest request,
                                 @AuthenticationPrincipal final AuthenticatedUser currentUser) {
        if (!criteria.isValid()) {
            LOGGER.warn("activity feed received unrecognized filter values; applying defaults");
        }
        LOGGER.debug("rendering mixed activity feed type={} timeframe={} sort={} page={}",
                criteria.getType(), criteria.getTimeframe(), criteria.getSort(), criteria.getPage());
        final Page<ActivityFeedItem> activityPage = activityService.getActivityFeed(criteria);
        final List<ActivityFeedItem> items = activityPage.getItems();

        final Set<Long> likedReviewIds;
        final Set<Long> helpfulPostIds;
        if (currentUser != null) {
            final List<Long> reviewIds = items.stream()
                    .filter(ActivityFeedItem::isReview)
                    .map(i -> i.getReview().getId())
                    .collect(Collectors.toList());
            final List<Long> postIds = items.stream()
                    .filter(i -> !i.isReview())
                    .map(i -> i.getCommunityPost().getId())
                    .collect(Collectors.toList());
            likedReviewIds = reviewLikeService.getLikedReviewIds(reviewIds, currentUser.getId());
            helpfulPostIds = communityService.getHelpfulPostIds(postIds, currentUser.getId());
        } else {
            likedReviewIds = Collections.emptySet();
            helpfulPostIds = Collections.emptySet();
        }

        final boolean authenticated = currentUser != null;
        final Long currentUserId = currentUser == null ? null : currentUser.getId();
        final boolean admin = request.isUserInRole("ADMIN");
        final Map<ActivityFeedReference, ActivityFeedPermissions> permissionsByReference =
                activityService.getActivityFeedPermissions(items, currentUserId, admin);
        final List<ActivityCardView> cards = items.stream()
                .map(item -> toActivityCard(
                        item,
                        likedReviewIds,
                        helpfulPostIds,
                        authenticated,
                        permissionsByReference.getOrDefault(
                                item.getReference(),
                                ActivityFeedPermissions.none(item.getReference())
                        )
                ))
                .collect(Collectors.toList());

        final ModelAndView mav = new ModelAndView("activity.jsp");
        mav.addObject("activityCards", cards);
        mav.addObject("activityCurrentPage", activityPage.getPageNumber());
        mav.addObject("activityTotalPages", activityPage.getTotalPages());
        mav.addObject("activityCriteria", criteria);
        return mav;
    }

    private ActivityCardView toActivityCard(final ActivityFeedItem item,
                                            final Set<Long> likedReviewIds,
                                            final Set<Long> helpfulPostIds,
                                            final boolean authenticated,
                                            final ActivityFeedPermissions permissions) {
        if (item.isReview()) {
            final String carName = item.getCar() != null
                    ? item.getCar().getBrandName() + " " + item.getCar().getModel()
                    : null;
            final long reviewId = item.getReview().getId();
            return new ActivityCardView(
                    true,
                    item.getReview().getUserId(),
                    resolveReviewAuthorName(item),
                    item.getReview().getCreatedAt(),
                    item.getReview().getTitle(),
                    item.getReview().getBody(),
                    imageIds(item.getReviewImages()),
                    "review.image.alt",
                    "activity.card.context.review",
                    carName,
                    item.getCar() != null ? item.getCar().getId() : null,
                    null,
                    null,
                    likedReviewIds.contains(reviewId),
                    item.getReviewLikeCount(),
                    reviewId,
                    authenticated,
                    "review-" + reviewId,
                    "activity.card.metric.replies",
                    Long.toString(item.getReviewReplyCount()),
                    permissions.isEditable(),
                    permissions.isDeletable(),
                    permissions.isHideable(),
                    authenticated,
                    null,
                    null
            );
        }

        final long postId = item.getCommunityPost().getId();
        final String communitySlug = item.getCommunityPost().getCommunity().getSlug();
        final String postSlug = item.getCommunityPost().getSlug();
        final Review linkedReview = item.getCommunityPost().getLinkedReview();
        final CommunityController.RepostReviewView repostReview = linkedReview != null
                ? buildRepostReviewView(linkedReview) : null;
        return new ActivityCardView(
                false,
                item.getCommunityPost().getAuthorUserId(),
                item.getCommunityPost().getAuthorUsername(),
                item.getCommunityPost().getCreatedAt(),
                item.getCommunityPost().getTitle(),
                item.getCommunityPost().getBody(),
                imageIds(item.getCommunityPostImages()),
                "communities.post.image.alt",
                "activity.card.context.community",
                item.getCommunityPost().getCommunity().getName(),
                null,
                communitySlug,
                postSlug,
                helpfulPostIds.contains(postId),
                item.getHelpfulCount(),
                postId,
                authenticated,
                "post-" + postId,
                "activity.card.metric.replies",
                Long.toString(item.getCommentCount()),
                permissions.isEditable(),
                permissions.isDeletable(),
                permissions.isHideable(),
                false,
                "hideCommunityPostModal",
                repostReview
        );
    }

    private CommunityController.RepostReviewView buildRepostReviewView(final Review review) {
        final String carName = review.getCar() != null
                ? review.getCar().getBrandName() + " " + review.getCar().getModel()
                : null;
        final String authorName = review.getUser() != null ? review.getUser().getUsername() : null;
        return new CommunityController.RepostReviewView(
                review.getId(),
                review.getTitle(),
                review.getBody(),
                review.getRating(),
                carName,
                review.getCarId(),
                authorName
        );
    }

    private String resolveReviewAuthorName(final ActivityFeedItem item) {
        if (item.getReview().getReviewerUsername() != null && !item.getReview().getReviewerUsername().trim().isEmpty()) {
            return item.getReview().getReviewerUsername().trim();
        }
        if (item.getReview().getReviewerEmail() != null && !item.getReview().getReviewerEmail().trim().isEmpty()) {
            return item.getReview().getReviewerEmail().trim();
        }
        return null;
    }

    private List<Long> imageIds(final List<ImageMetadata> images) {
        if (images == null || images.isEmpty()) {
            return Collections.emptyList();
        }
        return images.stream()
                .map(ImageMetadata::getImageId)
                .collect(Collectors.toList());
    }

    public static final class ActivityCardView {
        private final boolean review;
        private final Long authorUserId;
        private final String authorName;
        private final LocalDateTime createdAt;
        private final String title;
        private final String body;
        private final List<Long> imageIds;
        private final String imageAltKey;
        private final String contextLabelKey;
        private final String contextValue;
        private final Long carId;
        private final String communitySlug;
        private final String postSlug;
        private final boolean liked;
        private final long likeCount;
        private final long likeEntityId;
        private final boolean authenticated;
        private final String cardAnchorId;
        private final String secondaryMetricKey;
        private final String secondaryMetricValue;
        private final boolean editable;
        private final boolean deletable;
        private final boolean hideable;
        private final boolean repostable;
        private final String hideModalTarget;
        private final CommunityController.RepostReviewView repostReview;

        private ActivityCardView(final boolean review,
                                 final Long authorUserId,
                                 final String authorName,
                                 final LocalDateTime createdAt,
                                 final String title,
                                 final String body,
                                 final List<Long> imageIds,
                                 final String imageAltKey,
                                 final String contextLabelKey,
                                 final String contextValue,
                                 final Long carId,
                                 final String communitySlug,
                                 final String postSlug,
                                 final boolean liked,
                                 final long likeCount,
                                 final long likeEntityId,
                                 final boolean authenticated,
                                 final String cardAnchorId,
                                 final String secondaryMetricKey,
                                 final String secondaryMetricValue,
                                 final boolean editable,
                                 final boolean deletable,
                                 final boolean hideable,
                                 final boolean repostable,
                                 final String hideModalTarget,
                                 final CommunityController.RepostReviewView repostReview) {
            this.review = review;
            this.authorUserId = authorUserId;
            this.authorName = authorName;
            this.createdAt = createdAt;
            this.title = title;
            this.body = body;
            this.imageIds = imageIds;
            this.imageAltKey = imageAltKey;
            this.contextLabelKey = contextLabelKey;
            this.contextValue = contextValue;
            this.carId = carId;
            this.communitySlug = communitySlug;
            this.postSlug = postSlug;
            this.liked = liked;
            this.likeCount = likeCount;
            this.likeEntityId = likeEntityId;
            this.authenticated = authenticated;
            this.cardAnchorId = cardAnchorId;
            this.secondaryMetricKey = secondaryMetricKey;
            this.secondaryMetricValue = secondaryMetricValue;
            this.editable = editable;
            this.deletable = deletable;
            this.hideable = hideable;
            this.repostable = repostable;
            this.hideModalTarget = hideModalTarget;
            this.repostReview = repostReview;
        }

        public boolean isReview() { return review; }
        public boolean isCommunityPost() { return !review; }
        public Long getAuthorUserId() { return authorUserId; }
        public String getAuthorName() { return authorName; }
        public LocalDateTime getCreatedAt() { return createdAt; }
        public String getTitle() { return title; }
        public String getBody() { return body; }
        public List<Long> getImageIds() { return imageIds; }
        public String getImageAltKey() { return imageAltKey; }
        public String getContextLabelKey() { return contextLabelKey; }
        public String getContextValue() { return contextValue; }
        public Long getCarId() { return carId; }
        public String getCommunitySlug() { return communitySlug; }
        public String getPostSlug() { return postSlug; }
        public boolean isLiked() { return liked; }
        public long getLikeCount() { return likeCount; }
        public long getLikeEntityId() { return likeEntityId; }
        public boolean isAuthenticated() { return authenticated; }
        public String getCardAnchorId() { return cardAnchorId; }
        public String getSecondaryMetricKey() { return secondaryMetricKey; }
        public String getSecondaryMetricValue() { return secondaryMetricValue; }
        public boolean isEditable() { return editable; }
        public boolean isDeletable() { return deletable; }
        public boolean isHideable() { return hideable; }
        public boolean isRepostable() { return repostable; }
        public boolean isActionMenuVisible() { return editable || deletable || hideable || repostable; }
        public String getHideModalTarget() { return hideModalTarget; }
        public CommunityController.RepostReviewView getRepostReview() { return repostReview; }
    }
}
