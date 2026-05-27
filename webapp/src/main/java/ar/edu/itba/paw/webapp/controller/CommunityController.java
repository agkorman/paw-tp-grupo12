package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.Community;
import ar.edu.itba.paw.model.CommunityDetailData;
import ar.edu.itba.paw.model.CommunityEditData;
import ar.edu.itba.paw.model.CommunityHubEntry;
import ar.edu.itba.paw.model.CommunityMembersData;
import ar.edu.itba.paw.model.CommunityMembershipEntry;
import ar.edu.itba.paw.model.CommunityPost;
import ar.edu.itba.paw.model.CommunityPostComment;
import ar.edu.itba.paw.model.CommunityPostDetailData;
import ar.edu.itba.paw.model.CommunityPostSummary;
import ar.edu.itba.paw.model.CommunitySearchCriteria;
import ar.edu.itba.paw.model.CommunityTopic;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.services.CommunityService;
import ar.edu.itba.paw.services.exception.CommunityMembershipRequiredException;
import ar.edu.itba.paw.services.exception.InvalidCommunityTopicSelectionException;
import ar.edu.itba.paw.webapp.auth.AuthenticatedUser;
import ar.edu.itba.paw.webapp.auth.LoginRedirectUtils;
import ar.edu.itba.paw.webapp.util.LogSanitizer;
import ar.edu.itba.paw.webapp.controller.support.RelativeTimeFormatter;
import ar.edu.itba.paw.webapp.exception.ResourceNotFoundException;
import ar.edu.itba.paw.webapp.form.CommunityPostCommentForm;
import ar.edu.itba.paw.webapp.form.CommunityForm;
import ar.edu.itba.paw.webapp.form.CommunityHideForm;
import ar.edu.itba.paw.webapp.form.CommunityPostForm;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.stream.Collectors;
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

    @ModelAttribute("communityHideForm")
    public CommunityHideForm communityHideForm() {
        return new CommunityHideForm();
    }

    @RequestMapping(value = "/communities", method = RequestMethod.GET)
    public ModelAndView communitiesHub(
        @ModelAttribute final CommunitySearchCriteria criteria,
        @AuthenticationPrincipal final AuthenticatedUser currentUser
    ) {
        final Page<CommunityHubEntry> entries = communityService.getCommunityHub(criteria, currentUserId(currentUser));
        final ModelAndView mav = new ModelAndView("communities.jsp");
        mav.addObject("communityCards", toCommunityCards(entries.getItems()));
        mav.addObject("communitiesCurrentPage", entries.getPageNumber());
        mav.addObject("communitiesTotalPages", entries.getTotalPages());
        mav.addObject("communitiesTotalItems", entries.getTotalItems());
        mav.addObject("criteria", criteria);
        mav.addObject("searchQuery", criteria.getQ());
        mav.addObject("selectedTopic", criteria.getTopic());
        mav.addObject("selectedMembership", criteria.getMembership());
        mav.addObject("sortBy", criteria.getSortBy());
        mav.addObject("communityTopics", toTopicViews(communityService.getAvailableTopics()));
        mav.addObject("authenticated", currentUser != null);
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
            errors.rejectValue("selectedTopicIds", resolveTopicErrorKey(e.getReason()));
            populateCreateCommunityPageModel(model);
            return "community-create.jsp";
        }
    }

    @RequestMapping(value = "/communities/{communitySlug}/edit", method = RequestMethod.GET)
    public ModelAndView editCommunity(
        @PathVariable final String communitySlug,
        @ModelAttribute("communityForm") final CommunityForm communityForm,
        @AuthenticationPrincipal final AuthenticatedUser currentUser
    ) {
        if (currentUser == null) {
            return new ModelAndView("redirect:/communities/" + communitySlug);
        }
        final CommunityEditData editData = communityService
                .getCommunityForEdit(communitySlug, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("community not found"));
        communityForm.setName(editData.getCommunity().getName());
        communityForm.setDescription(editData.getCommunity().getDescription());
        communityForm.setSelectedTopicIds(editData.getSelectedTopics().stream()
                .map(CommunityTopic::getId)
                .collect(Collectors.toCollection(LinkedHashSet::new)));
        final ModelAndView mav = new ModelAndView("community-edit.jsp");
        mav.addObject("communitySlug", communitySlug);
        mav.addObject("communityName", editData.getCommunity().getName());
        mav.addObject("viewerIsCreator", editData.isViewerCreator());
        populateCreateCommunityPageModel(mav);
        return mav;
    }

    @RequestMapping(value = "/communities/{communitySlug}/edit", method = RequestMethod.POST)
    public String editCommunity(
        @PathVariable final String communitySlug,
        @Valid @ModelAttribute("communityForm") final CommunityForm communityForm,
        final BindingResult errors,
        final Model model,
        @AuthenticationPrincipal final AuthenticatedUser currentUser
    ) {
        if (currentUser == null) {
            return "redirect:/communities/" + communitySlug;
        }

        if (errors.hasErrors()) {
            LOGGER.warn("edit community rejected: validation errors userId={} communitySlug={} errorCount={}",
                    currentUser.getId(),
                    LogSanitizer.forLog(communitySlug, LogSanitizer.MAX_LOG_URL_CODE_POINTS),
                    errors.getErrorCount());
            populateEditCommunityPageModel(model, communitySlug, communityForm.getName());
            return "community-edit.jsp";
        }

        try {
            communityService.editCommunity(
                    communitySlug,
                    currentUser.getId(),
                    communityForm.getName(),
                    communityForm.getDescription(),
                    communityForm.getSelectedTopicIds()
            ).orElseThrow(() -> new ResourceNotFoundException("community not found"));
            LOGGER.info("edited community slug={} userId={}",
                    LogSanitizer.forLog(communitySlug, LogSanitizer.MAX_LOG_URL_CODE_POINTS), currentUser.getId());
            return "redirect:/communities/" + communitySlug;
        } catch (final InvalidCommunityTopicSelectionException e) {
            LOGGER.warn("edit community rejected: invalid topic selection userId={} reason={}",
                    currentUser.getId(), e.getReason());
            errors.rejectValue("selectedTopicIds", resolveTopicErrorKey(e.getReason()));
            populateEditCommunityPageModel(model, communitySlug, communityForm.getName());
            return "community-edit.jsp";
        }
    }

    @RequestMapping(value = "/communities/{communitySlug}/delete", method = RequestMethod.POST)
    public String deleteCommunity(
        @PathVariable final String communitySlug,
        @AuthenticationPrincipal final AuthenticatedUser currentUser
    ) {
        if (currentUser == null) {
            return "redirect:/communities/" + communitySlug;
        }
        communityService.deleteCommunity(communitySlug, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("community not found"));
        return "redirect:/communities";
    }

    @RequestMapping(value = "/communities/{communitySlug}", method = RequestMethod.GET)
    public ModelAndView communityDetail(
        @PathVariable final String communitySlug,
        @RequestParam(value = "sort", required = false) final String sort,
        @RequestParam(value = "page", required = false, defaultValue = "1") final int page,
        @AuthenticationPrincipal final AuthenticatedUser currentUser
    ) {
        final CommunityDetailData communityDetail = communityService
            .getCommunityDetail(communitySlug, currentUserId(currentUser), sort, page)
            .orElseThrow(() -> new ResourceNotFoundException("community not found"));

        final ModelAndView mav = new ModelAndView("community-detail.jsp");
        mav.addObject("pageTitle", communityDetail.getCommunity().getName() + " | La Posta Autos");
        mav.addObject("communityDetail", communityDetail);
        mav.addObject("currentSort", communityDetail.getCurrentSort());
        mav.addObject("postCards", toPostCards(communityDetail.getPosts(), communityDetail.getCommunity().getSlug()));
        mav.addObject("postsCurrentPage", communityDetail.getPostsPage().getPageNumber());
        mav.addObject("postsTotalPages", communityDetail.getPostsPage().getTotalPages());
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
        final String defaultRedirect = communityPostDetailPath(communitySlug, postSlug);
        final String authRedirect = redirectToLoginIfAnonymous(request, currentUser, defaultRedirect);
        if (authRedirect != null) {
            return authRedirect;
        }

        communityService.togglePostHelpfulReaction(communitySlug, postSlug, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("community post not found"));
        return redirectTo(defaultRedirect);
    }

    @RequestMapping(
        value = "/communities/{communitySlug}/posts/{postSlug}/comments/{commentId}/helpful",
        method = RequestMethod.POST
    )
    public String toggleCommentHelpful(
        @PathVariable final String communitySlug,
        @PathVariable final String postSlug,
        @PathVariable final long commentId,
        final HttpServletRequest request,
        @AuthenticationPrincipal final AuthenticatedUser currentUser
    ) {
        final String defaultRedirect = communityPostDetailPath(communitySlug, postSlug);
        final String authRedirect = redirectToLoginIfAnonymous(request, currentUser, defaultRedirect);
        if (authRedirect != null) {
            return authRedirect;
        }

        communityService.toggleCommentHelpfulReaction(communitySlug, postSlug, commentId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("community post comment not found"));
        return redirectTo(defaultRedirect);
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
                    currentUser.getId(),
                    LogSanitizer.forLog(communitySlug, LogSanitizer.MAX_LOG_URL_CODE_POINTS),
                    LogSanitizer.forLog(postSlug, LogSanitizer.MAX_LOG_URL_CODE_POINTS));
            return communityPostPageWithCommentError(postDetail);
        }

        try {
            communityService.createCommunityPostComment(
                            communitySlug,
                            postSlug,
                            currentUser.getId(),
                            communityPostCommentForm.getBody()
                    )
                    .orElseThrow(() -> new ResourceNotFoundException("community post not found"));
        } catch (final CommunityMembershipRequiredException e) {
            LOGGER.warn("create community post comment rejected: not a member userId={} communitySlug={} postSlug={}",
                    currentUser.getId(),
                    LogSanitizer.forLog(communitySlug, LogSanitizer.MAX_LOG_URL_CODE_POINTS),
                    LogSanitizer.forLog(postSlug, LogSanitizer.MAX_LOG_URL_CODE_POINTS));
            return new ModelAndView("redirect:/communities/" + communitySlug + "/posts/" + postSlug);
        }
        LOGGER.info("user id={} commented on communitySlug={} postSlug={}",
                currentUser.getId(),
                LogSanitizer.forLog(communitySlug, LogSanitizer.MAX_LOG_URL_CODE_POINTS),
                LogSanitizer.forLog(postSlug, LogSanitizer.MAX_LOG_URL_CODE_POINTS));
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
        value = "/communities/{communitySlug}/posts/{postSlug}/edit",
        method = RequestMethod.GET
    )
    public ModelAndView editCommunityPost(
        @PathVariable final String communitySlug,
        @PathVariable final String postSlug,
        @ModelAttribute("communityPostForm") final CommunityPostForm communityPostForm,
        @AuthenticationPrincipal final AuthenticatedUser currentUser
    ) {
        if (currentUser == null) {
            return new ModelAndView("redirect:/communities/" + communitySlug + "/posts/" + postSlug);
        }
        final CommunityPost post = communityService
                .getCommunityPostForEdit(communitySlug, postSlug, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("community post not found"));
        final Community community = communityService
                .getCommunityBySlug(communitySlug)
                .orElseThrow(() -> new ResourceNotFoundException("community not found"));
        communityPostForm.setTitle(post.getTitle());
        communityPostForm.setBody(post.getBody());
        final ModelAndView mav = new ModelAndView("community-post-form.jsp");
        populateCommunityPostFormModel(mav, community);
        mav.addObject("editMode", true);
        mav.addObject("postSlug", postSlug);
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
                    currentUser.getId(),
                    LogSanitizer.forLog(communitySlug, LogSanitizer.MAX_LOG_URL_CODE_POINTS),
                    errors.getErrorCount());
            populateCommunityPostFormModel(model, community);
            return "community-post-form.jsp";
        }

        final CommunityPost createdPost = communityService
            .createCommunityPost(communitySlug, currentUser.getId(),
                    communityPostForm.getTitle(), communityPostForm.getBody())
            .orElseThrow(() -> new ResourceNotFoundException("community not found"));
        LOGGER.info("created community post slug={} communitySlug={} userId={}",
                LogSanitizer.forLog(createdPost.getSlug(), LogSanitizer.MAX_LOG_URL_CODE_POINTS),
                LogSanitizer.forLog(communitySlug, LogSanitizer.MAX_LOG_URL_CODE_POINTS),
                currentUser.getId());
        return "redirect:/communities/" + communitySlug + "/posts/" + createdPost.getSlug();
    }

    @RequestMapping(
        value = "/communities/{communitySlug}/posts/{postSlug}/edit",
        method = RequestMethod.POST
    )
    public String updateCommunityPost(
        @PathVariable final String communitySlug,
        @PathVariable final String postSlug,
        @Valid @ModelAttribute("communityPostForm") final CommunityPostForm communityPostForm,
        final BindingResult errors,
        final Model model,
        @AuthenticationPrincipal final AuthenticatedUser currentUser
    ) {
        if (currentUser == null) {
            return "redirect:/communities/" + communitySlug + "/posts/" + postSlug;
        }

        final CommunityPost post = communityService
                .getCommunityPostForEdit(communitySlug, postSlug, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("community post not found"));
        final Community community = communityService
                .getCommunityBySlug(communitySlug)
                .orElseThrow(() -> new ResourceNotFoundException("community not found"));
        if (errors.hasErrors()) {
            LOGGER.warn("edit community post rejected: validation errors userId={} communitySlug={} postSlug={} errorCount={}",
                    currentUser.getId(),
                    LogSanitizer.forLog(communitySlug, LogSanitizer.MAX_LOG_URL_CODE_POINTS),
                    LogSanitizer.forLog(postSlug, LogSanitizer.MAX_LOG_URL_CODE_POINTS),
                    errors.getErrorCount());
            populateCommunityPostFormModel(model, community);
            model.addAttribute("editMode", true);
            model.addAttribute("postSlug", postSlug);
            return "community-post-form.jsp";
        }

        communityService.updateCommunityPost(
                        communitySlug,
                        postSlug,
                        currentUser.getId(),
                        communityPostForm.getTitle(),
                        communityPostForm.getBody()
                )
                .orElseThrow(() -> new ResourceNotFoundException("community post not found"));
        LOGGER.info("updated community post slug={} communitySlug={} userId={}",
                LogSanitizer.forLog(postSlug, LogSanitizer.MAX_LOG_URL_CODE_POINTS),
                LogSanitizer.forLog(communitySlug, LogSanitizer.MAX_LOG_URL_CODE_POINTS),
                currentUser.getId());
        return "redirect:/communities/" + communitySlug + "/posts/" + postSlug;
    }

    @RequestMapping(value = "/communities/{communitySlug}/members", method = RequestMethod.GET)
    public ModelAndView communityMembers(
        @PathVariable final String communitySlug,
        @AuthenticationPrincipal final AuthenticatedUser currentUser
    ) {
        if (currentUser == null) {
            return new ModelAndView("redirect:/communities/" + communitySlug);
        }
        final CommunityMembersData membersData = communityService
                .getCommunityMembers(communitySlug, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("community not found"));
        final List<MemberRowView> rows = new ArrayList<>();
        for (final CommunityMembershipEntry entry : membersData.getMembers()) {
            rows.add(new MemberRowView(
                    entry.getUserId(),
                    entry.getUsername(),
                    entry.getRole(),
                    entry.isModerator(),
                    entry.isCreator(),
                    entry.getUserId() == currentUser.getId()
            ));
        }
        final ModelAndView mav = new ModelAndView("community-members.jsp");
        mav.addObject("pageTitle", membersData.getCommunity().getName() + " | La Posta Autos");
        mav.addObject("community", membersData.getCommunity());
        mav.addObject("memberRows", rows);
        mav.addObject("viewerIsModerator", membersData.isViewerModerator());
        mav.addObject("viewerIsCreator", membersData.isViewerCreator());
        return mav;
    }

    @RequestMapping(value = "/communities/{communitySlug}/members/{userId}/kick", method = RequestMethod.POST)
    public String kickCommunityMember(
        @PathVariable final String communitySlug,
        @PathVariable final long userId,
        @AuthenticationPrincipal final AuthenticatedUser currentUser
    ) {
        if (currentUser == null) {
            return "redirect:/communities/" + communitySlug;
        }
        if (userId == currentUser.getId()) {
            LOGGER.warn("kick community member rejected: self kick userId={} communitySlug={}",
                    currentUser.getId(),
                    LogSanitizer.forLog(communitySlug, LogSanitizer.MAX_LOG_URL_CODE_POINTS));
            return "redirect:/communities/" + communitySlug + "/members";
        }
        communityService.kickMember(communitySlug, userId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("community not found"));
        return "redirect:/communities/" + communitySlug + "/members";
    }

    @RequestMapping(value = "/communities/{communitySlug}/members/{userId}/kick", method = RequestMethod.GET)
    public String kickCommunityMemberGet(
        @PathVariable final String communitySlug
    ) {
        return "redirect:/communities/" + communitySlug + "/members";
    }

    @RequestMapping(value = "/communities/{communitySlug}/members/{userId}/transfer", method = RequestMethod.POST)
    public String transferCommunityOwnership(
        @PathVariable final String communitySlug,
        @PathVariable final long userId,
        @AuthenticationPrincipal final AuthenticatedUser currentUser
    ) {
        if (currentUser == null) {
            return "redirect:/communities/" + communitySlug;
        }
        communityService.transferOwnership(communitySlug, userId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("community not found"));
        return "redirect:/communities/" + communitySlug + "/members";
    }

    @RequestMapping(value = "/communities/{communitySlug}/members/{userId}/transfer", method = RequestMethod.GET)
    public String transferCommunityOwnershipGet(
        @PathVariable final String communitySlug
    ) {
        return "redirect:/communities/" + communitySlug + "/members";
    }

    @RequestMapping(value = "/communities/{communitySlug}/members/{userId}/promote", method = RequestMethod.POST)
    public String promoteCommunityMember(
        @PathVariable final String communitySlug,
        @PathVariable final long userId,
        @AuthenticationPrincipal final AuthenticatedUser currentUser
    ) {
        if (currentUser == null) {
            return "redirect:/communities/" + communitySlug;
        }
        communityService.promoteToModerator(communitySlug, userId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("community not found"));
        return "redirect:/communities/" + communitySlug + "/members";
    }

    @RequestMapping(value = "/communities/{communitySlug}/members/{userId}/promote", method = RequestMethod.GET)
    public String promoteCommunityMemberGet(
        @PathVariable final String communitySlug
    ) {
        return "redirect:/communities/" + communitySlug + "/members";
    }

    @RequestMapping(value = "/communities/{communitySlug}/posts/{postSlug}/hide", method = RequestMethod.POST)
    public String hidePost(
        @PathVariable final String communitySlug,
        @PathVariable final String postSlug,
        @Valid @ModelAttribute("communityHideForm") final CommunityHideForm communityHideForm,
        final BindingResult errors,
        @AuthenticationPrincipal final AuthenticatedUser currentUser
    ) {
        if (currentUser == null) {
            return "redirect:/communities/" + communitySlug + "/posts/" + postSlug;
        }
        if (errors.hasErrors()) {
            return "redirect:/communities/" + communitySlug + "/posts/" + postSlug;
        }
        communityService.hidePost(communitySlug, postSlug, currentUser.getId(), communityHideForm.getReason())
                .orElseThrow(() -> new ResourceNotFoundException("community post not found"));
        return "redirect:/communities/" + communitySlug;
    }

    @RequestMapping(
        value = "/communities/{communitySlug}/posts/{postSlug}/comments/{commentId}/hide",
        method = RequestMethod.POST
    )
    public String hideComment(
        @PathVariable final String communitySlug,
        @PathVariable final String postSlug,
        @PathVariable final long commentId,
        @Valid @ModelAttribute("communityHideForm") final CommunityHideForm communityHideForm,
        final BindingResult errors,
        @AuthenticationPrincipal final AuthenticatedUser currentUser
    ) {
        if (currentUser == null) {
            return "redirect:/communities/" + communitySlug + "/posts/" + postSlug;
        }
        if (errors.hasErrors()) {
            return "redirect:/communities/" + communitySlug + "/posts/" + postSlug;
        }
        communityService.hideComment(communitySlug, commentId, currentUser.getId(), communityHideForm.getReason())
                .orElseThrow(() -> new ResourceNotFoundException("community not found"));
        return "redirect:/communities/" + communitySlug + "/posts/" + postSlug;
    }

    @RequestMapping(value = "/communities/{communitySlug}/posts/{postSlug}/delete", method = RequestMethod.POST)
    public String deletePost(
        @PathVariable final String communitySlug,
        @PathVariable final String postSlug,
        @AuthenticationPrincipal final AuthenticatedUser currentUser
    ) {
        if (currentUser == null) {
            return "redirect:/communities/" + communitySlug + "/posts/" + postSlug;
        }
        communityService.deletePost(communitySlug, postSlug, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("community post not found"));
        return "redirect:/communities/" + communitySlug;
    }

    @RequestMapping(
        value = "/communities/{communitySlug}/posts/{postSlug}/comments/{commentId}/update",
        method = RequestMethod.POST
    )
    public String updateComment(
        @PathVariable final String communitySlug,
        @PathVariable final String postSlug,
        @PathVariable final long commentId,
        @Valid @ModelAttribute("communityPostCommentForm") final CommunityPostCommentForm communityPostCommentForm,
        final BindingResult errors,
        @AuthenticationPrincipal final AuthenticatedUser currentUser
    ) {
        if (currentUser == null) {
            return "redirect:/communities/" + communitySlug + "/posts/" + postSlug;
        }
        if (!errors.hasFieldErrors("body")) {
            communityService.updateCommunityPostComment(
                            communitySlug,
                            commentId,
                            currentUser.getId(),
                            communityPostCommentForm.getBody()
                    )
                    .orElseThrow(() -> new ResourceNotFoundException("community not found"));
            LOGGER.info("updated community comment id={} communitySlug={} postSlug={} userId={}",
                    commentId,
                    LogSanitizer.forLog(communitySlug, LogSanitizer.MAX_LOG_URL_CODE_POINTS),
                    LogSanitizer.forLog(postSlug, LogSanitizer.MAX_LOG_URL_CODE_POINTS),
                    currentUser.getId());
        }
        return "redirect:/communities/" + communitySlug + "/posts/" + postSlug + "#comment-" + commentId;
    }

    @RequestMapping(
        value = "/communities/{communitySlug}/posts/{postSlug}/comments/{commentId}/delete",
        method = RequestMethod.POST
    )
    public String deleteComment(
        @PathVariable final String communitySlug,
        @PathVariable final String postSlug,
        @PathVariable final long commentId,
        @AuthenticationPrincipal final AuthenticatedUser currentUser
    ) {
        if (currentUser == null) {
            return "redirect:/communities/" + communitySlug + "/posts/" + postSlug;
        }
        communityService.deleteComment(communitySlug, commentId, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("community not found"));
        return "redirect:/communities/" + communitySlug + "/posts/" + postSlug;
    }

    private Long currentUserId(final AuthenticatedUser currentUser) {
        return currentUser == null ? null : currentUser.getId();
    }

    private String resolveTopicErrorKey(final InvalidCommunityTopicSelectionException.Reason reason) {
        switch (reason) {
            case REQUIRED: return "validation.community.topics.required";
            case TOO_MANY: return "validation.community.topics.max";
            default:       return "validation.community.topics.invalid";
        }
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

    private void populateEditCommunityPageModel(final Model model, final String communitySlug,
                                                final String communityName) {
        model.addAttribute("communityTopics", toTopicViews(communityService.getAvailableTopics()));
        model.addAttribute("communitySlug", communitySlug);
        model.addAttribute("communityName", communityName);
    }

    private void populateCommunityPostFormModel(final ModelAndView mav, final Community community) {
        mav.addObject("community", community);
    }

    private void populateCommunityPostFormModel(final Model model, final Community community) {
        model.addAttribute("community", community);
    }

    private String redirectToLoginIfAnonymous(final HttpServletRequest request,
                                              final AuthenticatedUser currentUser,
                                              final String defaultRedirect) {
        if (currentUser != null) {
            return null;
        }
        final String safeRedirect = LoginRedirectUtils.safeRedirect(defaultRedirect, request.getContextPath())
                .orElse(defaultRedirect);
        return redirectTo("/login?redirect=" + safeRedirect);
    }

    private String communityPostDetailPath(final String communitySlug, final String postSlug) {
        return "/communities/" + communitySlug + "/posts/" + postSlug;
    }

    private String redirectTo(final String path) {
        return "redirect:" + path;
    }

    private List<PostCardView> toPostCards(final List<CommunityPostSummary> postSummaries,
                                           final String communitySlug) {
        final List<PostCardView> cards = new ArrayList<>();
        for (final CommunityPostSummary postSummary : postSummaries) {
            cards.add(new PostCardView(
                "/communities/" + communitySlug + "/posts/" + postSummary.getPost().getSlug(),
                "/users/" + postSummary.getPost().getAuthorUserId(),
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
        final boolean viewerModerator = postDetail.isViewerModerator();
        final List<CommentView> comments = new ArrayList<>();
        for (final CommunityPostComment comment : postDetail.getComments()) {
            comments.add(new CommentView(
                comment.getId(),
                "/users/" + comment.getUserId(),
                comment.getAuthorUsername(),
                relativeTimeFormatter.format(comment.getCreatedAt()),
                comment.getBody(),
                postDetail.getHelpfulCountForComment(comment.getId()),
                postDetail.isHelpfulByCurrentUserForComment(comment.getId()),
                comment.getUserId() == postDetail.getPost().getAuthorUserId(),
                postDetail.isCommentDeletableByViewer(comment),
                viewerModerator
            ));
        }

        return new PostDetailView(
            postDetail.getCommunity().getName(),
            "c/" + postDetail.getCommunity().getSlug(),
            postDetail.getCommunity().getSlug(),
            postDetail.getPost().getSlug(),
            "/users/" + postDetail.getPost().getAuthorUserId(),
            postDetail.getPost().getAuthorUsername(),
            relativeTimeFormatter.format(postDetail.getPost().getCreatedAt()),
            postDetail.getPost().getTitle(),
            postDetail.getPost().getBody(),
            postDetail.getHelpfulCount(),
            postDetail.getHelpfulByCurrentUser(),
            postDetail.getCommentCount(),
            comments,
            postDetail.getViewerRole(),
            viewerModerator,
            postDetail.isViewerMember(),
            postDetail.isPostDeletableByViewer()
        );
    }

    public static final class MemberRowView {

        private final long userId;
        private final String username;
        private final String role;
        private final boolean moderator;
        private final boolean creator;
        private final boolean currentUser;

        private MemberRowView(final long userId, final String username, final String role,
                              final boolean moderator, final boolean creator,
                              final boolean currentUser) {
            this.userId = userId;
            this.username = username;
            this.role = role;
            this.moderator = moderator;
            this.creator = creator;
            this.currentUser = currentUser;
        }

        public long getUserId() {
            return userId;
        }

        public String getUsername() {
            return username;
        }

        public String getRole() {
            return role;
        }

        public boolean getModerator() {
            return moderator;
        }

        public boolean getCreator() {
            return creator;
        }

        public boolean getCurrentUser() {
            return currentUser;
        }
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
        private final String authorProfileHref;
        private final String author;
        private final String timeText;
        private final String title;
        private final String body;
        private final long helpfulCount;
        private final long commentCount;

        private PostCardView(final String href, final String authorProfileHref,
                             final String author,
                             final String timeText, final String title,
                             final String body, final long helpfulCount,
                             final long commentCount) {
            this.href = href;
            this.authorProfileHref = authorProfileHref;
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

        public String getAuthorProfileHref() {
            return authorProfileHref;
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
        private final String communitySlug;
        private final String postSlug;
        private final String authorProfileHref;
        private final String author;
        private final String timeText;
        private final String title;
        private final String body;
        private final long helpfulCount;
        private final boolean helpfulByCurrentUser;
        private final long commentCount;
        private final List<CommentView> comments;
        private final String viewerRole;
        private final boolean viewerModerator;
        private final boolean viewerMember;
        private final boolean deletable;
        private final boolean editable;

        private PostDetailView(final String communityName, final String communityHandle,
                               final String communitySlug, final String postSlug,
                               final String authorProfileHref, final String author, final String timeText,
                               final String title, final String body,
                               final long helpfulCount, final boolean helpfulByCurrentUser,
                               final long commentCount,
                               final List<CommentView> comments,
                               final String viewerRole,
                               final boolean viewerModerator, final boolean viewerMember,
                               final boolean deletable) {
            this.communityName = communityName;
            this.communityHandle = communityHandle;
            this.communitySlug = communitySlug;
            this.postSlug = postSlug;
            this.authorProfileHref = authorProfileHref;
            this.author = author;
            this.timeText = timeText;
            this.title = title;
            this.body = body;
            this.helpfulCount = helpfulCount;
            this.helpfulByCurrentUser = helpfulByCurrentUser;
            this.commentCount = commentCount;
            this.comments = comments;
            this.viewerRole = viewerRole;
            this.viewerModerator = viewerModerator;
            this.viewerMember = viewerMember;
            this.deletable = deletable;
            this.editable = deletable;
        }

        public String getCommunitySlug() {
            return communitySlug;
        }

        public String getPostSlug() {
            return postSlug;
        }

        public String getViewerRole() {
            return viewerRole;
        }

        public boolean getViewerModerator() {
            return viewerModerator;
        }

        public boolean getViewerMember() {
            return viewerMember;
        }

        public boolean getDeletable() {
            return deletable;
        }

        public boolean getEditable() {
            return editable;
        }

        public String getCommunityName() {
            return communityName;
        }

        public String getCommunityHandle() {
            return communityHandle;
        }

        public String getAuthorProfileHref() {
            return authorProfileHref;
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

        private final long commentId;
        private final String authorProfileHref;
        private final String author;
        private final String timeText;
        private final String body;
        private final long helpfulCount;
        private final boolean helpfulByCurrentUser;
        private final boolean op;
        private final boolean deletable;
        private final boolean hideable;
        private final boolean editable;

        private CommentView(final long commentId,
                            final String authorProfileHref, final String author, final String timeText,
                            final String body, final long helpfulCount,
                            final boolean helpfulByCurrentUser,
                            final boolean op, final boolean deletable, final boolean hideable) {
            this.commentId = commentId;
            this.authorProfileHref = authorProfileHref;
            this.author = author;
            this.timeText = timeText;
            this.body = body;
            this.helpfulCount = helpfulCount;
            this.helpfulByCurrentUser = helpfulByCurrentUser;
            this.op = op;
            this.deletable = deletable;
            this.hideable = hideable;
            this.editable = deletable;
        }

        public long getCommentId() {
            return commentId;
        }

        public boolean getDeletable() {
            return deletable;
        }

        public boolean getHideable() {
            return hideable;
        }

        public boolean getEditable() {
            return editable;
        }

        public String getAuthorProfileHref() {
            return authorProfileHref;
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

        public boolean getHelpfulByCurrentUser() {
            return helpfulByCurrentUser;
        }

        public boolean getOp() {
            return op;
        }
    }
}
