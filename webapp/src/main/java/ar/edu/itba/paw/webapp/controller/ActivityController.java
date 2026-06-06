package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.ActivityFeedCriteria;
import ar.edu.itba.paw.model.ActivityFeedItem;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.ReviewImage;
import ar.edu.itba.paw.services.ActivityService;
import ar.edu.itba.paw.services.CommunityService;
import ar.edu.itba.paw.services.ReviewLikeService;
import ar.edu.itba.paw.webapp.auth.AuthenticatedUser;
import ar.edu.itba.paw.webapp.controller.support.RelativeTimeFormatter;
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

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class ActivityController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActivityController.class);

    private final ActivityService activityService;
    private final ReviewLikeService reviewLikeService;
    private final CommunityService communityService;
    private final RelativeTimeFormatter relativeTimeFormatter;

    @Autowired
    public ActivityController(final ActivityService activityService,
                              final ReviewLikeService reviewLikeService,
                              final CommunityService communityService,
                              final RelativeTimeFormatter relativeTimeFormatter) {
        this.activityService = activityService;
        this.reviewLikeService = reviewLikeService;
        this.communityService = communityService;
        this.relativeTimeFormatter = relativeTimeFormatter;
    }

    @InitBinder
    public void initBinder(final WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
    }

    @RequestMapping(value = "/activity", method = RequestMethod.GET)
    public ModelAndView activity(
                                 @ModelAttribute("activityCriteria") final ActivityFeedCriteria criteria,
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
        final List<ActivityCardView> cards = items.stream()
                .map(item -> toActivityCard(item, likedReviewIds, helpfulPostIds, authenticated))
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
                                            final boolean authenticated) {
        if (item.isReview()) {
            final String carName = item.getCar() != null
                    ? item.getCar().getBrandName() + " " + item.getCar().getModel()
                    : null;
            final long reviewId = item.getReview().getId();
            return new ActivityCardView(
                    reviewHref(item),
                    userHref(item.getReview().getUserId()),
                    resolveReviewAuthorName(item),
                    relativeTimeFormatter.format(item.getReview().getCreatedAt()),
                    item.getReview().getTitle(),
                    item.getReview().getBody(),
                    toReviewImageUrls(reviewId, item.getReviewImages()),
                    "review.image.alt",
                    "activity.card.context.review",
                    carName,
                    item.getCar() != null ? "/reviews/car/" + item.getCar().getId() : null,
                    "/reviews/" + reviewId + "/like",
                    likedReviewIds.contains(reviewId),
                    item.getReviewLikeCount(),
                    reviewId,
                    authenticated,
                    "activity-review-" + reviewId,
                    "activity.card.metric.comments",
                    Long.toString(item.getReviewReplyCount())
            );
        }

        final long postId = item.getCommunityPost().getId();
        final String communitySlug = item.getCommunityPost().getCommunity().getSlug();
        final String postSlug = item.getCommunityPost().getSlug();
        return new ActivityCardView(
                "/communities/" + communitySlug + "/posts/" + postSlug,
                userHref(item.getCommunityPost().getAuthorUserId()),
                item.getCommunityPost().getAuthorUsername(),
                relativeTimeFormatter.format(item.getCommunityPost().getCreatedAt()),
                item.getCommunityPost().getTitle(),
                item.getCommunityPost().getBody(),
                toCommunityPostImageUrls(item),
                "communities.post.image.alt",
                "activity.card.context.community",
                item.getCommunityPost().getCommunity().getName(),
                "/communities/" + communitySlug,
                "/communities/" + communitySlug + "/posts/" + postSlug + "/helpful",
                helpfulPostIds.contains(postId),
                item.getHelpfulCount(),
                postId,
                authenticated,
                "activity-post-" + postId,
                "activity.card.metric.comments",
                Long.toString(item.getCommentCount())
        );
    }

    private String reviewHref(final ActivityFeedItem item) {
        final StringBuilder builder = new StringBuilder("/reviews/car/")
                .append(item.getReview().getCarId());
        if (item.getReviewPage() > 1) {
            builder.append("?page=").append(item.getReviewPage());
        }
        builder.append("#review-").append(item.getReview().getId());
        return builder.toString();
    }

    private String userHref(final Long userId) {
        return userId == null ? null : "/users/" + userId;
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

    private List<String> toReviewImageUrls(final long reviewId, final List<ReviewImage> images) {
        if (images == null || images.isEmpty()) {
            return Collections.emptyList();
        }
        return images.stream()
                .map(image -> "/reviews/" + reviewId + "/images/" + image.getImageId())
                .collect(Collectors.toList());
    }

    private List<String> toCommunityPostImageUrls(final ActivityFeedItem item) {
        if (item.getCommunityPostImages() == null || item.getCommunityPostImages().isEmpty()) {
            return Collections.emptyList();
        }
        return item.getCommunityPostImages().stream()
                .map(image -> "/communities/" + item.getCommunityPost().getCommunity().getSlug()
                        + "/posts/" + item.getCommunityPost().getSlug() + "/images/" + image.getImageId())
                .collect(Collectors.toList());
    }

    public static final class ActivityCardView {
        private final String href;
        private final String authorHref;
        private final String authorName;
        private final String timeText;
        private final String title;
        private final String body;
        private final List<String> imageUrls;
        private final String imageAltKey;
        private final String contextLabelKey;
        private final String contextValue;
        private final String contextHref;
        private final String likeAction;
        private final boolean liked;
        private final long likeCount;
        private final long likeEntityId;
        private final boolean authenticated;
        private final String cardAnchorId;
        private final String secondaryMetricKey;
        private final String secondaryMetricValue;

        private ActivityCardView(final String href,
                                 final String authorHref,
                                 final String authorName,
                                 final String timeText,
                                 final String title,
                                 final String body,
                                 final List<String> imageUrls,
                                 final String imageAltKey,
                                 final String contextLabelKey,
                                 final String contextValue,
                                 final String contextHref,
                                 final String likeAction,
                                 final boolean liked,
                                 final long likeCount,
                                 final long likeEntityId,
                                 final boolean authenticated,
                                 final String cardAnchorId,
                                 final String secondaryMetricKey,
                                 final String secondaryMetricValue) {
            this.href = href;
            this.authorHref = authorHref;
            this.authorName = authorName;
            this.timeText = timeText;
            this.title = title;
            this.body = body;
            this.imageUrls = imageUrls;
            this.imageAltKey = imageAltKey;
            this.contextLabelKey = contextLabelKey;
            this.contextValue = contextValue;
            this.contextHref = contextHref;
            this.likeAction = likeAction;
            this.liked = liked;
            this.likeCount = likeCount;
            this.likeEntityId = likeEntityId;
            this.authenticated = authenticated;
            this.cardAnchorId = cardAnchorId;
            this.secondaryMetricKey = secondaryMetricKey;
            this.secondaryMetricValue = secondaryMetricValue;
        }

        public String getHref() { return href; }
        public String getAuthorHref() { return authorHref; }
        public String getAuthorName() { return authorName; }
        public String getTimeText() { return timeText; }
        public String getTitle() { return title; }
        public String getBody() { return body; }
        public List<String> getImageUrls() { return imageUrls; }
        public String getImageAltKey() { return imageAltKey; }
        public String getContextLabelKey() { return contextLabelKey; }
        public String getContextValue() { return contextValue; }
        public String getContextHref() { return contextHref; }
        public String getLikeAction() { return likeAction; }
        public boolean isLiked() { return liked; }
        public long getLikeCount() { return likeCount; }
        public long getLikeEntityId() { return likeEntityId; }
        public boolean isAuthenticated() { return authenticated; }
        public String getCardAnchorId() { return cardAnchorId; }
        public String getSecondaryMetricKey() { return secondaryMetricKey; }
        public String getSecondaryMetricValue() { return secondaryMetricValue; }
    }
}
