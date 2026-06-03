package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.ActivityFeedItem;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.ReviewImage;
import ar.edu.itba.paw.services.ActivityService;
import ar.edu.itba.paw.webapp.controller.support.RelativeTimeFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ActivityController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ActivityController.class);

    private final ActivityService activityService;
    private final RelativeTimeFormatter relativeTimeFormatter;

    @Autowired
    public ActivityController(final ActivityService activityService,
                              final RelativeTimeFormatter relativeTimeFormatter) {
        this.activityService = activityService;
        this.relativeTimeFormatter = relativeTimeFormatter;
    }

    @RequestMapping(value = "/activity", method = RequestMethod.GET)
    public ModelAndView activity(
                                 @RequestParam(value = "page", defaultValue = "1") final int page) {
        LOGGER.debug("rendering mixed activity feed page={}", page);
        final Page<ActivityFeedItem> activityPage = activityService.getLatestActivityFeed(page);

        final ModelAndView mav = new ModelAndView("activity.jsp");
        mav.addObject("activityCards", toActivityCards(activityPage.getItems()));
        mav.addObject("activityCurrentPage", activityPage.getPageNumber());
        mav.addObject("activityTotalPages", activityPage.getTotalPages());
        return mav;
    }

    private List<ActivityCardView> toActivityCards(final List<ActivityFeedItem> items) {
        return items.stream()
                .map(this::toActivityCard)
                .collect(Collectors.toList());
    }

    private ActivityCardView toActivityCard(final ActivityFeedItem item) {
        if (item.isReview()) {
            final String carName = item.getCar() != null
                    ? item.getCar().getBrandName() + " " + item.getCar().getModel()
                    : null;
            return new ActivityCardView(
                    reviewHref(item),
                    userHref(item.getReview().getUserId()),
                    resolveReviewAuthorName(item),
                    relativeTimeFormatter.format(item.getReview().getCreatedAt()),
                    item.getReview().getTitle(),
                    item.getReview().getBody(),
                    toReviewImageUrls(item.getReview().getId(), item.getReviewImages()),
                    "review.image.alt",
                    "activity.card.context.review",
                    carName,
                    carName != null ? null : "activity.card.car.unknown",
                    carName,
                    null,
                    item.getReview().getRating() + " / 5"
            );
        }

        return new ActivityCardView(
                "/communities/" + item.getCommunityPost().getCommunity().getSlug()
                        + "/posts/" + item.getCommunityPost().getSlug(),
                userHref(item.getCommunityPost().getAuthorUserId()),
                item.getCommunityPost().getAuthorUsername(),
                relativeTimeFormatter.format(item.getCommunityPost().getCreatedAt()),
                item.getCommunityPost().getTitle(),
                item.getCommunityPost().getBody(),
                toCommunityPostImageUrls(item),
                "communities.post.image.alt",
                "activity.card.context.community",
                item.getCommunityPost().getCommunity().getName(),
                "activity.card.metric.helpful",
                Long.toString(item.getHelpfulCount()),
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
        private final String primaryMetricKey;
        private final String primaryMetricValue;
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
                                 final String primaryMetricKey,
                                 final String primaryMetricValue,
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
            this.primaryMetricKey = primaryMetricKey;
            this.primaryMetricValue = primaryMetricValue;
            this.secondaryMetricKey = secondaryMetricKey;
            this.secondaryMetricValue = secondaryMetricValue;
        }

        public String getHref() {
            return href;
        }

        public String getAuthorHref() {
            return authorHref;
        }

        public String getAuthorName() {
            return authorName;
        }

        public String getTimeText() {
            return timeText;
        }

        public String getTitle() {
            return title;
        }

        public String getBody() {
            return body;
        }

        public List<String> getImageUrls() {
            return imageUrls;
        }

        public String getImageAltKey() {
            return imageAltKey;
        }

        public String getContextLabelKey() {
            return contextLabelKey;
        }

        public String getContextValue() {
            return contextValue;
        }

        public String getPrimaryMetricKey() {
            return primaryMetricKey;
        }

        public String getPrimaryMetricValue() {
            return primaryMetricValue;
        }

        public String getSecondaryMetricKey() {
            return secondaryMetricKey;
        }

        public String getSecondaryMetricValue() {
            return secondaryMetricValue;
        }
    }
}
