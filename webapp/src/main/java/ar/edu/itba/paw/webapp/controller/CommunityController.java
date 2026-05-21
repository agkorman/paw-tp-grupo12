package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.Community;
import ar.edu.itba.paw.model.CommunityDetailData;
import ar.edu.itba.paw.model.CommunityHubEntry;
import ar.edu.itba.paw.model.CommunityPost;
import ar.edu.itba.paw.model.CommunityPostComment;
import ar.edu.itba.paw.model.CommunityPostDetailData;
import ar.edu.itba.paw.model.CommunityPostSummary;
import ar.edu.itba.paw.model.CommunityTopic;
import ar.edu.itba.paw.services.CommunityService;
import ar.edu.itba.paw.services.exception.InvalidCommunityTopicSelectionException;
import ar.edu.itba.paw.webapp.auth.AuthenticatedUser;
import ar.edu.itba.paw.webapp.auth.LoginRedirectUtils;
import ar.edu.itba.paw.webapp.controller.support.RelativeTimeFormatter;
import ar.edu.itba.paw.webapp.exception.ResourceNotFoundException;
import ar.edu.itba.paw.webapp.form.CommunityPostCommentForm;
import ar.edu.itba.paw.webapp.form.CommunityForm;
import ar.edu.itba.paw.webapp.form.CommunityPostForm;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class CommunityController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CommunityController.class);

    private final CommunityService communityService;
    private final RelativeTimeFormatter relativeTimeFormatter;

    @Autowired
    public CommunityController(final CommunityService communityService,
                               final RelativeTimeFormatter relativeTimeFormatter) {
        this.communityService = communityService;
        this.relativeTimeFormatter = relativeTimeFormatter;
    }

    @InitBinder
    public void initBinder(final WebDataBinder binder) {
        binder.registerCustomEditor(
            String.class,
            new StringTrimmerEditor(true)
        );
    }

    @ModelAttribute("communityForm")
    public CommunityForm communityForm() {
        return new CommunityForm();
    }

    @ModelAttribute("communityPostForm")
    public CommunityPostForm communityPostForm() {
        return new CommunityPostForm();
    }

    @ModelAttribute("communityPostCommentForm")
    public CommunityPostCommentForm communityPostCommentForm() {
        return new CommunityPostCommentForm();
    }

    @RequestMapping(value = "/communities", method = RequestMethod.GET)
    public ModelAndView communitiesHub(
        @AuthenticationPrincipal final AuthenticatedUser currentUser
    ) {
        final List<CommunityHubEntry> entries = communityService.getCommunityHub(currentUserId(currentUser));
        final ModelAndView mav = new ModelAndView("communities.jsp");
        mav.addObject("communityCards", toCommunityCards(entries));
        return mav;
    }

    @RequestMapping(value = "/communities/new", method = RequestMethod.GET)
    public ModelAndView createCommunity() {
        final ModelAndView mav = new ModelAndView("community-create.jsp");
        populateCreateCommunityPageModel(mav);
        return mav;
    }

    @RequestMapping(value = "/communities", method = RequestMethod.POST)
    public String createCommunity(
        @Valid @ModelAttribute("communityForm") final CommunityForm communityForm,
        final BindingResult errors,
        final Model model,
        @AuthenticationPrincipal final AuthenticatedUser currentUser
    ) {
        if (currentUser == null) {
            return "redirect:/communities/new";
        }

        if (errors.hasErrors()) {
            LOGGER.warn("create community rejected: validation errors userId={} errorCount={}",
                    currentUser.getId(), errors.getErrorCount());
            populateCreateCommunityPageModel(model);
            return "community-create.jsp";
        }

        try {
            final Community community = communityService.createCommunity(
                    currentUser.getId(),
                    communityForm.getName(),
                    communityForm.getDescription(),
                    communityForm.getSelectedTopicIds()
            );
            LOGGER.info("created community slug={} userId={}", community.getSlug(), currentUser.getId());
            return "redirect:/communities/" + community.getSlug();
        } catch (final InvalidCommunityTopicSelectionException e) {
            LOGGER.warn("create community rejected: invalid topic selection userId={} reason={}",
                    currentUser.getId(), e.getReason());
            errors.rejectValue("selectedTopicIds", "validation.community.topics.invalid", e.getMessage());
            populateCreateCommunityPageModel(model);
            return "community-create.jsp";
        }
    }

    @RequestMapping(value = "/communities/{communitySlug}", method = RequestMethod.GET)
    public ModelAndView communityDetail(
        @PathVariable final String communitySlug,
        @AuthenticationPrincipal final AuthenticatedUser currentUser
    ) {
        final CommunityDetailData communityDetail = communityService
            .getCommunityDetail(communitySlug, currentUserId(currentUser))
            .orElseThrow(() -> new ResourceNotFoundException("community not found"));

        final ModelAndView mav = new ModelAndView("community-detail.jsp");
        mav.addObject("pageTitle", communityDetail.getCommunity().getName() + " | La Posta Autos");
        mav.addObject("communityDetail", communityDetail);
        mav.addObject("postCards", toPostCards(communityDetail.getPosts(), communityDetail.getCommunity().getSlug()));
        return mav;
    }

    @RequestMapping(value = "/communities/{communitySlug}/join", method = RequestMethod.POST)
    public String joinCommunity(
        @PathVariable final String communitySlug,
        @AuthenticationPrincipal final AuthenticatedUser currentUser
    ) {
        if (currentUser == null) {
            return "redirect:/communities/" + communitySlug;
        }

        communityService.toggleMembership(communitySlug, currentUser.getId())
            .orElseThrow(() -> new ResourceNotFoundException("community not found"));
        return "redirect:/communities/" + communitySlug;
    }

    @RequestMapping(
        value = "/communities/{communitySlug}/posts/{postSlug}",
        method = RequestMethod.GET
    )
    public ModelAndView communityPostDetail(
        @PathVariable final String communitySlug,
        @PathVariable final String postSlug,
        @AuthenticationPrincipal final AuthenticatedUser currentUser
    ) {
        final CommunityPostDetailData postDetail = communityService
            .getCommunityPostDetail(communitySlug, postSlug, currentUserId(currentUser))
            .orElseThrow(() -> new ResourceNotFoundException("community post not found"));

        final ModelAndView mav = new ModelAndView("community-post-detail.jsp");
        mav.addObject("pageTitle", postDetail.getPost().getTitle() + " | La Posta Autos");
        mav.addObject("postDetail", postDetail);
        mav.addObject("postView", toPostView(postDetail));
        return mav;
    }

    @RequestMapping(
        value = "/communities/{communitySlug}/posts/{postSlug}/helpful",
        method = RequestMethod.POST
    )
    public String togglePostHelpful(
        @PathVariable final String communitySlug,
        @PathVariable final String postSlug,
        final HttpServletRequest request,
        @AuthenticationPrincipal final AuthenticatedUser currentUser
    ) {
        final String defaultRedirect = "/communities/" + communitySlug + "/posts/" + postSlug;
        if (currentUser == null) {
            final String safeRedirect = LoginRedirectUtils.safeRedirect(defaultRedirect, request.getContextPath())
                    .orElse(defaultRedirect);
            return "redirect:/login?redirect=" + safeRedirect;
        }

        communityService.togglePostHelpfulReaction(communitySlug, postSlug, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("community post not found"));
        return "redirect:" + defaultRedirect;
    }

    @RequestMapping(
        value = "/communities/{communitySlug}/posts/{postSlug}/comments",
        method = RequestMethod.POST
    )
    public ModelAndView createCommunityPostComment(
        @PathVariable final String communitySlug,
        @PathVariable final String postSlug,
        @Valid @ModelAttribute("communityPostCommentForm") final CommunityPostCommentForm communityPostCommentForm,
        final BindingResult errors,
        @AuthenticationPrincipal final AuthenticatedUser currentUser
    ) {
        if (currentUser == null) {
            return new ModelAndView("redirect:/login");
        }

        final CommunityPostDetailData postDetail = communityService
                .getCommunityPostDetail(communitySlug, postSlug, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("community post not found"));

        if (errors.hasFieldErrors("body")) {
            LOGGER.warn("create community post comment rejected: validation errors userId={} communitySlug={} postSlug={}",
                    currentUser.getId(), communitySlug, postSlug);
            return communityPostPageWithCommentError(postDetail);
        }

        communityService.createCommunityPostComment(
                        communitySlug,
                        postSlug,
                        currentUser.getId(),
                        communityPostCommentForm.getBody()
                )
                .orElseThrow(() -> new ResourceNotFoundException("community post not found"));
        LOGGER.info("user id={} commented on communitySlug={} postSlug={}",
                currentUser.getId(), communitySlug, postSlug);
        return new ModelAndView("redirect:/communities/" + communitySlug + "/posts/" + postSlug);
    }

    @RequestMapping(
        value = "/communities/{communitySlug}/submit",
        method = RequestMethod.GET
    )
    public ModelAndView createCommunityPost(
        @PathVariable final String communitySlug
    ) {
        final Community community = communityService
            .getCommunityBySlug(communitySlug)
            .orElseThrow(() -> new ResourceNotFoundException("community not found"));
        final ModelAndView mav = new ModelAndView("community-post-form.jsp");
        populateCommunityPostFormModel(mav, community);
        return mav;
    }

    @RequestMapping(
        value = "/communities/{communitySlug}/posts",
        method = RequestMethod.POST
    )
    public String submitCommunityPost(
        @PathVariable final String communitySlug,
        @Valid @ModelAttribute("communityPostForm") final CommunityPostForm communityPostForm,
        final BindingResult errors,
        final Model model,
        @AuthenticationPrincipal final AuthenticatedUser currentUser
    ) {
        if (currentUser == null) {
            return "redirect:/communities/" + communitySlug + "/submit";
        }

        final Community community = communityService
            .getCommunityBySlug(communitySlug)
            .orElseThrow(() -> new ResourceNotFoundException("community not found"));

        if (errors.hasErrors()) {
            LOGGER.warn("create community post rejected: validation errors userId={} communitySlug={} errorCount={}",
                    currentUser.getId(), communitySlug, errors.getErrorCount());
            populateCommunityPostFormModel(model, community);
            return "community-post-form.jsp";
        }

        final CommunityPost createdPost = communityService
            .createCommunityPost(communitySlug, currentUser.getId(),
                    communityPostForm.getTitle(), communityPostForm.getBody())
            .orElseThrow(() -> new ResourceNotFoundException("community not found"));
        LOGGER.info("created community post slug={} communitySlug={} userId={}",
                createdPost.getSlug(), communitySlug, currentUser.getId());
        return "redirect:/communities/" + communitySlug + "/posts/" + createdPost.getSlug();
    }

    private Long currentUserId(final AuthenticatedUser currentUser) {
        return currentUser == null ? null : currentUser.getId();
    }

    private ModelAndView communityPostPageWithCommentError(final CommunityPostDetailData postDetail) {
        final ModelAndView mav = new ModelAndView("community-post-detail.jsp");
        mav.addObject("pageTitle", postDetail.getPost().getTitle() + " | La Posta Autos");
        mav.addObject("postDetail", postDetail);
        mav.addObject("postView", toPostView(postDetail));
        return mav;
    }

    private List<CommunityCardView> toCommunityCards(final List<CommunityHubEntry> entries) {
        final List<CommunityCardView> cards = new ArrayList<>();
        for (final CommunityHubEntry entry : entries) {
            final Community community = entry.getCommunity();
            cards.add(new CommunityCardView(
                "/communities/" + community.getSlug(),
                community.getName(),
                community.getDescription(),
                entry.getMemberCount(),
                entry.getWeeklyPostCount(),
                entry.isJoined()
            ));
        }
        return cards;
    }

    private List<TopicView> toTopicViews(final List<CommunityTopic> topics) {
        final List<TopicView> topicViews = new ArrayList<>();
        for (final CommunityTopic topic : topics) {
            topicViews.add(new TopicView(topic.getId(), topic.getCode(), "communities.topic." + topic.getCode()));
        }
        return topicViews;
    }

    private void populateCreateCommunityPageModel(final ModelAndView mav) {
        mav.addObject("communityTopics", toTopicViews(communityService.getAvailableTopics()));
    }

    private void populateCreateCommunityPageModel(final Model model) {
        model.addAttribute("communityTopics", toTopicViews(communityService.getAvailableTopics()));
    }

    private void populateCommunityPostFormModel(final ModelAndView mav, final Community community) {
        mav.addObject("community", community);
    }

    private void populateCommunityPostFormModel(final Model model, final Community community) {
        model.addAttribute("community", community);
    }

    private List<PostCardView> toPostCards(final List<CommunityPostSummary> postSummaries,
                                           final String communitySlug) {
        final List<PostCardView> cards = new ArrayList<>();
        for (final CommunityPostSummary postSummary : postSummaries) {
            cards.add(new PostCardView(
                "/communities/" + communitySlug + "/posts/" + postSummary.getPost().getSlug(),
                postSummary.getPost().getAuthorUsername(),
                relativeTimeFormatter.format(postSummary.getPost().getCreatedAt()),
                postSummary.getPost().getTitle(),
                postSummary.getPost().getBody(),
                postSummary.getHelpfulCount(),
                postSummary.getCommentCount()
            ));
        }
        return cards;
    }

    private PostDetailView toPostView(final CommunityPostDetailData postDetail) {
        final List<CommentView> comments = new ArrayList<>();
        for (final CommunityPostComment comment : postDetail.getComments()) {
            comments.add(new CommentView(
                comment.getAuthorUsername(),
                relativeTimeFormatter.format(comment.getCreatedAt()),
                comment.getBody(),
                0,
                comment.getUserId() == postDetail.getPost().getAuthorUserId()
            ));
        }

        return new PostDetailView(
            postDetail.getCommunity().getName(),
            "c/" + postDetail.getCommunity().getSlug(),
            postDetail.getPost().getAuthorUsername(),
            relativeTimeFormatter.format(postDetail.getPost().getCreatedAt()),
            postDetail.getPost().getTitle(),
            postDetail.getPost().getBody(),
            postDetail.getHelpfulCount(),
            postDetail.getHelpfulByCurrentUser(),
            postDetail.getCommentCount(),
            comments
        );
    }

    public static final class TopicView {

        private final short id;
        private final String code;
        private final String labelCode;

        private TopicView(final short id, final String code, final String labelCode) {
            this.id = id;
            this.code = code;
            this.labelCode = labelCode;
        }

        public short getId() {
            return id;
        }

        public String getCode() {
            return code;
        }

        public String getLabelCode() {
            return labelCode;
        }
    }

    public static final class CommunityCardView {

        private final String href;
        private final String title;
        private final String description;
        private final long memberCount;
        private final long weeklyPostCount;
        private final boolean joined;

        private CommunityCardView(final String href, final String title,
                                  final String description,
                                  final long memberCount, final long weeklyPostCount,
                                  final boolean joined) {
            this.href = href;
            this.title = title;
            this.description = description;
            this.memberCount = memberCount;
            this.weeklyPostCount = weeklyPostCount;
            this.joined = joined;
        }

        public String getHref() {
            return href;
        }

        public String getTitle() {
            return title;
        }

        public String getDescription() {
            return description;
        }

        public long getMemberCount() {
            return memberCount;
        }

        public long getWeeklyPostCount() {
            return weeklyPostCount;
        }

        public boolean getJoined() {
            return joined;
        }
    }

    public static final class PostCardView {

        private final String href;
        private final String author;
        private final String timeText;
        private final String title;
        private final String body;
        private final long helpfulCount;
        private final long commentCount;

        private PostCardView(final String href, final String author,
                             final String timeText, final String title,
                             final String body, final long helpfulCount,
                             final long commentCount) {
            this.href = href;
            this.author = author;
            this.timeText = timeText;
            this.title = title;
            this.body = body;
            this.helpfulCount = helpfulCount;
            this.commentCount = commentCount;
        }

        public String getHref() {
            return href;
        }

        public String getAuthor() {
            return author;
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

        public long getHelpfulCount() {
            return helpfulCount;
        }

        public long getCommentCount() {
            return commentCount;
        }
    }

    public static final class PostDetailView {

        private final String communityName;
        private final String communityHandle;
        private final String author;
        private final String timeText;
        private final String title;
        private final String body;
        private final long helpfulCount;
        private final boolean helpfulByCurrentUser;
        private final long commentCount;
        private final List<CommentView> comments;

        private PostDetailView(final String communityName, final String communityHandle,
                               final String author, final String timeText,
                               final String title, final String body,
                               final long helpfulCount, final boolean helpfulByCurrentUser,
                               final long commentCount,
                               final List<CommentView> comments) {
            this.communityName = communityName;
            this.communityHandle = communityHandle;
            this.author = author;
            this.timeText = timeText;
            this.title = title;
            this.body = body;
            this.helpfulCount = helpfulCount;
            this.helpfulByCurrentUser = helpfulByCurrentUser;
            this.commentCount = commentCount;
            this.comments = comments;
        }

        public String getCommunityName() {
            return communityName;
        }

        public String getCommunityHandle() {
            return communityHandle;
        }

        public String getAuthor() {
            return author;
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

        public long getHelpfulCount() {
            return helpfulCount;
        }

        public boolean getHelpfulByCurrentUser() {
            return helpfulByCurrentUser;
        }

        public long getCommentCount() {
            return commentCount;
        }

        public List<CommentView> getComments() {
            return comments;
        }
    }

    public static final class CommentView {

        private final String author;
        private final String timeText;
        private final String body;
        private final long helpfulCount;
        private final boolean op;

        private CommentView(final String author, final String timeText,
                            final String body, final long helpfulCount,
                            final boolean op) {
            this.author = author;
            this.timeText = timeText;
            this.body = body;
            this.helpfulCount = helpfulCount;
            this.op = op;
        }

        public String getAuthor() {
            return author;
        }

        public String getTimeText() {
            return timeText;
        }

        public String getBody() {
            return body;
        }

        public long getHelpfulCount() {
            return helpfulCount;
        }

        public boolean getOp() {
            return op;
        }
    }
}
