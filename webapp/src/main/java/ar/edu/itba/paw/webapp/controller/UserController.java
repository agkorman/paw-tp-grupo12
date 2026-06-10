package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.CommunityPost;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Pagination;
import ar.edu.itba.paw.model.ProfileActivityItem;
import ar.edu.itba.paw.model.ProfileActivityItem.ItemType;
import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.model.ReviewStats;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.services.AdminRequestService;
import ar.edu.itba.paw.services.CarFavoriteService;
import ar.edu.itba.paw.services.CarService;
import ar.edu.itba.paw.services.CommunityService;
import ar.edu.itba.paw.services.ReviewLikeService;
import ar.edu.itba.paw.services.ReviewReplyService;
import ar.edu.itba.paw.services.ReviewService;
import ar.edu.itba.paw.services.UserActivityService;
import ar.edu.itba.paw.services.UserFollowService;
import ar.edu.itba.paw.services.UserService;
import ar.edu.itba.paw.services.exception.InvalidServiceInputException;
import ar.edu.itba.paw.services.exception.SelfFollowException;
import ar.edu.itba.paw.services.exception.ServiceOperationException;
import ar.edu.itba.paw.services.exception.UsernameAlreadyExistsException;
import ar.edu.itba.paw.webapp.auth.AuthenticatedUser;
import ar.edu.itba.paw.webapp.exception.ResourceNotFoundException;
import ar.edu.itba.paw.webapp.form.ProfileForm;
import ar.edu.itba.paw.webapp.form.ReviewForm;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ModelAndView;

import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        UserController.class
    );

    private static final String TAB_ACTIVITY = "activity";
    private static final String TAB_FAVORITES = "favorites";
    private static final String TAB_LIKES = "likes";

    private static final int CONNECTIONS_PAGE_SIZE = Pagination.CONNECTIONS_PAGE_SIZE;
    private static final String CONNECTIONS_KIND_FOLLOWERS = "followers";
    private static final String CONNECTIONS_KIND_FOLLOWING = "following";

    private final ReviewService reviewService;
    private final ReviewLikeService reviewLikeService;
    private final ReviewReplyService reviewReplyService;
    private final CarService carService;
    private final CarFavoriteService carFavoriteService;
    private final UserService userService;
    private final UserFollowService userFollowService;
    private final AdminRequestService adminRequestService;
    private final CommunityService communityService;
    private final UserActivityService userActivityService;
    private final LocaleResolver localeResolver;

    @Autowired
    public UserController(
        final ReviewService reviewService,
        final ReviewLikeService reviewLikeService,
        final ReviewReplyService reviewReplyService,
        final CarService carService,
        final CarFavoriteService carFavoriteService,
        final UserService userService,
        final UserFollowService userFollowService,
        final AdminRequestService adminRequestService,
        final CommunityService communityService,
        final UserActivityService userActivityService,
        final LocaleResolver localeResolver
    ) {
        this.reviewService = reviewService;
        this.reviewLikeService = reviewLikeService;
        this.reviewReplyService = reviewReplyService;
        this.carService = carService;
        this.carFavoriteService = carFavoriteService;
        this.userService = userService;
        this.userFollowService = userFollowService;
        this.adminRequestService = adminRequestService;
        this.communityService = communityService;
        this.userActivityService = userActivityService;
        this.localeResolver = localeResolver;
    }

    @InitBinder
    public void initBinder(final WebDataBinder binder) {
        binder.registerCustomEditor(
            String.class,
            new StringTrimmerEditor(true)
        );
    }

    @RequestMapping(value = "/user", method = RequestMethod.GET)
    public ModelAndView ownProfile(
        @AuthenticationPrincipal final AuthenticatedUser currentUser,
        @RequestParam(value = "tab", required = false) final String tab,
        @RequestParam(value = "page", defaultValue = "1") final int page,
        @RequestParam(
            value = "submitted",
            required = false
        ) final String submitted,
        @RequestParam(value = "modal", required = false) final String connectionsModal,
        @RequestParam(value = "followersPage", defaultValue = "1") final int followersPage,
        @RequestParam(value = "followingPage", defaultValue = "1") final int followingPage,
        final HttpServletRequest request
    ) {
        if (currentUser == null) {
            return new ModelAndView("redirect:/login");
        }
        return profile(currentUser.getId(), currentUser, tab, page, submitted,
                connectionsModal, followersPage, followingPage, request);
    }

    ModelAndView ownProfile(final AuthenticatedUser currentUser) {
        return ownProfile(currentUser, null, 1, null, null, 1, 1, null);
    }

    @RequestMapping(value = "/users/{userId}", method = RequestMethod.GET)
    public ModelAndView publicProfile(
        @PathVariable("userId") final long userId,
        @AuthenticationPrincipal final AuthenticatedUser currentUser,
        @RequestParam(value = "tab", required = false) final String tab,
        @RequestParam(value = "page", defaultValue = "1") final int page,
        @RequestParam(
            value = "submitted",
            required = false
        ) final String submitted,
        @RequestParam(value = "modal", required = false) final String connectionsModal,
        @RequestParam(value = "followersPage", defaultValue = "1") final int followersPage,
        @RequestParam(value = "followingPage", defaultValue = "1") final int followingPage,
        final HttpServletRequest request
    ) {
        return profile(userId, currentUser, tab, page, submitted,
                connectionsModal, followersPage, followingPage, request);
    }

    ModelAndView publicProfile(
        final long userId,
        final AuthenticatedUser currentUser
    ) {
        return publicProfile(userId, currentUser, null, 1, null, null, 1, 1, null);
    }

    @RequestMapping(value = "/user", method = RequestMethod.POST)
    public ModelAndView updateOwnProfile(
        @Valid @ModelAttribute("profileForm") final ProfileForm profileForm,
        final BindingResult errors,
        @AuthenticationPrincipal final AuthenticatedUser currentUser,
        final RedirectAttributes redirectAttributes
    ) {
        if (currentUser == null) {
            return new ModelAndView("redirect:/login");
        }

        final String normalizedUsername = ControllerUtils.normalize(
            profileForm.getDisplayName()
        );
        if (errors.hasErrors()) {
            return profileEditError(
                profileErrorCode(errors),
                normalizedUsername,
                redirectAttributes
            );
        }

        try {
            final User updatedUser = userService.updateUsername(
                currentUser.getId(),
                normalizedUsername
            );
            refreshAuthenticatedUser(currentUser, updatedUser);
            return new ModelAndView("redirect:/user");
        } catch (final UsernameAlreadyExistsException e) {
            return profileEditError(
                "profile.edit.error.username.exists",
                normalizedUsername,
                redirectAttributes
            );
        } catch (final InvalidServiceInputException | ServiceOperationException e) {
            return profileEditError(
                "profile.edit.error.generic",
                normalizedUsername,
                redirectAttributes
            );
        }
    }

    @RequestMapping(value = "/user/language", method = RequestMethod.POST)
    public ModelAndView updateOwnProfileLanguage(
        @RequestParam("lang") final String language,
        @AuthenticationPrincipal final AuthenticatedUser currentUser,
        final HttpServletRequest request,
        final HttpServletResponse response,
        final RedirectAttributes redirectAttributes
    ) {
        if (currentUser == null) {
            return new ModelAndView("redirect:/login");
        }

        try {
            final User updatedUser = userService.updatePreferredLocale(
                currentUser.getId(),
                language
            );
            refreshAuthenticatedUser(currentUser, updatedUser);
            localeResolver.setLocale(
                request,
                response,
                Locale.forLanguageTag(updatedUser.getPreferredLocale())
            );
            redirectAttributes.addFlashAttribute(
                "profileLanguageSuccessCode",
                "profile.language.toast.success"
            );
        } catch (final InvalidServiceInputException | ServiceOperationException e) {
            redirectAttributes.addFlashAttribute(
                "profileLanguageErrorCode",
                "profile.language.error"
            );
        }
        return new ModelAndView("redirect:/user");
    }

    @RequestMapping(
        value = "/users/{userId}/follow",
        method = RequestMethod.POST
    )
    public ModelAndView toggleFollow(
        @PathVariable("userId") final long userId,
        @AuthenticationPrincipal final AuthenticatedUser currentUser
    ) {
        if (currentUser == null) {
            return new ModelAndView("redirect:/login");
        }
        if (userService.getUserById(userId).isEmpty()) {
            throw new ResourceNotFoundException("User", userId);
        }

        try {
            userFollowService.toggleFollow(currentUser.getId(), userId);
        } catch (final SelfFollowException e) {
            LOGGER.warn("self-follow attempt blocked userId={}", userId);
        }

        return new ModelAndView("redirect:/users/" + userId);
    }

    @RequestMapping(value = "/users/search", method = RequestMethod.GET)
    public ModelAndView searchUsers(@RequestParam(value = "q", required = false) final String query) {
        LOGGER.debug("rendering user search panel page={}", query != null && !query.isBlank());
        final ModelAndView mav = new ModelAndView("users-search.jsp");
        if (query == null || query.isBlank()) {
            return mav;
        }
        final List<User> results = userService.searchUsers(query, 1).getItems();
        mav.addObject("userSearchQuery", query);
        mav.addObject("userSearchResults", results);
        return mav;
    }

    private String profileErrorCode(final BindingResult errors) {
        final FieldError fieldError = errors.getFieldError("displayName");
        if (fieldError == null) {
            return "profile.edit.error.generic";
        }
        if ("NotBlank".equals(fieldError.getCode())) {
            return "profile.edit.error.username.required";
        }
        if ("Size".equals(fieldError.getCode())) {
            return "profile.edit.error.username.max";
        }
        return "profile.edit.error.username.pattern";
    }

    private ModelAndView profileEditError(
        final String errorCode,
        final String username,
        final RedirectAttributes redirectAttributes
    ) {
        redirectAttributes.addFlashAttribute("profileEditErrorCode", errorCode);
        redirectAttributes.addFlashAttribute("profileEditUsername", username);
        redirectAttributes.addFlashAttribute("openEditProfileModal", true);
        return new ModelAndView("redirect:/user");
    }

    private void refreshAuthenticatedUser(
        final AuthenticatedUser currentUser,
        final User updatedUser
    ) {
        final AuthenticatedUser refreshed = new AuthenticatedUser(
            currentUser.getId(),
            updatedUser.getUsername(),
            currentUser.getEmail(),
            currentUser.getPassword(),
            updatedUser.getPreferredLocale(),
            currentUser.getAuthorities()
        );
        SecurityContextHolder.getContext().setAuthentication(
            new UsernamePasswordAuthenticationToken(
                refreshed,
                refreshed.getPassword(),
                refreshed.getAuthorities()
            )
        );
    }

    private ModelAndView profile(
        final long profileUserId,
        final AuthenticatedUser currentUser,
        final String tab,
        final int page,
        final String submitted,
        final String connectionsModal,
        final int followersPage,
        final int followingPage,
        final HttpServletRequest request
    ) {
        final int normalizedPage = Pagination.normalizePage(page);
        final int normalizedFollowersPage = Pagination.normalizePage(followersPage);
        final int normalizedFollowingPage = Pagination.normalizePage(followingPage);
        final User profileUser = userService
            .getUserById(profileUserId)
            .orElseThrow(() ->
                new ResourceNotFoundException("User", profileUserId)
            );
        final Long currentUserId =
            currentUser == null ? null : currentUser.getId();
        final boolean viewerAdmin =
            request != null && request.isUserInRole("ADMIN");
        final boolean ownProfile =
            currentUserId != null && currentUserId == profileUser.getId();
        final String activeTab = ownProfile
            ? normalizeProfileTab(tab)
            : TAB_ACTIVITY;

        Page<ProfileActivityItem> activityPage = Page.empty(1, 0);
        Page<Car> favoriteCarPage = Page.empty(1, 0);
        Page<ProfileActivityItem> likesPage = Page.empty(1, 0);

        List<ProfileActivityEntry> activityEntries = List.of();
        List<Car> favoriteCars = List.of();
        List<ProfileActivityEntry> likesEntries = List.of();
        Map<Long, ReviewStats> reviewStatsByCarId = Map.of();

        if (TAB_FAVORITES.equals(activeTab)) {
            favoriteCarPage = carFavoriteService.getFavoriteCars(
                profileUser.getId(),
                normalizedPage
            );
            favoriteCars = favoriteCarPage.getItems();
            reviewStatsByCarId = reviewStatsByCarId(favoriteCars);
        } else if (TAB_LIKES.equals(activeTab)) {
            likesPage = userActivityService.getLikedActivity(
                profileUser.getId(),
                normalizedPage
            );
            likesEntries = buildActivityEntries(
                likesPage.getItems(),
                currentUserId,
                viewerAdmin
            );
        } else {
            activityPage = userActivityService.getAuthoredActivity(
                profileUser.getId(),
                normalizedPage
            );
            activityEntries = buildActivityEntries(
                activityPage.getItems(),
                currentUserId,
                viewerAdmin
            );
        }

        final long activityCount = activityPage.getPageSize() > 0
            ? activityPage.getTotalItems()
            : userActivityService.countAuthoredActivity(profileUser.getId());
        final long favoriteCarCount = ownProfile
            ? (favoriteCarPage.getPageSize() > 0
                ? favoriteCarPage.getTotalItems()
                : carFavoriteService.countFavoriteCars(profileUser.getId()))
            : 0L;
        final long likedCount = ownProfile
            ? (likesPage.getPageSize() > 0
                ? likesPage.getTotalItems()
                : userActivityService.countLikedActivity(profileUser.getId()))
            : 0L;

        final boolean followingProfile =
            currentUserId != null &&
            !ownProfile &&
            userFollowService.isFollowing(currentUserId, profileUser.getId());

        final ModelAndView mav = new ModelAndView("profile.jsp");
        mav.addObject("profile", toProfileData(profileUser, activityCount));
        mav.addObject("profileUserId", profileUser.getId());
        mav.addObject("activeTab", activeTab);
        mav.addObject("activityCount", activityCount);
        mav.addObject("favoriteCarCount", favoriteCarCount);
        mav.addObject("likedCount", likedCount);
        mav.addObject("activityEntries", activityEntries);
        mav.addObject(
            "activityCurrentPage",
            activityPage.getPageNumber()
        );
        mav.addObject(
            "activityTotalPages",
            activityPage.getTotalPages()
        );
        mav.addObject("likesEntries", likesEntries);
        mav.addObject(
            "likesCurrentPage",
            likesPage.getPageNumber()
        );
        mav.addObject(
            "likesTotalPages",
            likesPage.getTotalPages()
        );
        mav.addObject("favoriteCars", favoriteCars);
        mav.addObject(
            "favoriteCarsCurrentPage",
            favoriteCarPage.getPageNumber()
        );
        mav.addObject(
            "favoriteCarsTotalPages",
            favoriteCarPage.getTotalPages()
        );
        mav.addObject("reviewStatsByCarId", reviewStatsByCarId);

        final String activeConnectionsKind = normalizeConnectionsKind(connectionsModal);

        List<ProfileConnection> followingUsers = List.of();
        List<ProfileConnection> followerUsers = List.of();
        ConnectionsPagination connectionsPagination = null;

        if (CONNECTIONS_KIND_FOLLOWING.equals(activeConnectionsKind)) {
            final Page<User> followingPageResult = userFollowService.getFollowing(profileUser.getId(), normalizedFollowingPage);
            followingUsers = toConnections(followingPageResult.getItems(), currentUserId);
            connectionsPagination = new ConnectionsPagination(
                    CONNECTIONS_KIND_FOLLOWING,
                    followingPageResult.getPageNumber(),
                    followingPageResult.getTotalPages()
            );
        } else if (CONNECTIONS_KIND_FOLLOWERS.equals(activeConnectionsKind)) {
            final Page<User> followersPageResult = userFollowService.getFollowers(profileUser.getId(), normalizedFollowersPage);
            followerUsers = toConnections(followersPageResult.getItems(), currentUserId);
            connectionsPagination = new ConnectionsPagination(
                    CONNECTIONS_KIND_FOLLOWERS,
                    followersPageResult.getPageNumber(),
                    followersPageResult.getTotalPages()
            );
        }

        mav.addObject("followingUsers", followingUsers);
        mav.addObject("followerUsers", followerUsers);
        mav.addObject("connectionsModal", activeConnectionsKind);
        mav.addObject("connectionsPagination", connectionsPagination);
        mav.addObject("ownProfile", ownProfile);
        mav.addObject("followingProfile", followingProfile);
        mav.addObject("reviewForm", new ReviewForm());
        mav.addObject(
            "canRequestModerator",
            ownProfile &&
                adminRequestService.isEligibleForModeratorRequest(
                    profileUser.getId()
                )
        );
        if (ownProfile) {
            addSubmittedToast(mav, submitted);
        }
        return mav;
    }

    private void addSubmittedToast(
        final ModelAndView mav,
        final String submitted
    ) {
        final String submittedToastMessageCode =
            ControllerUtils.submittedToastMessageCode(submitted);
        if (submittedToastMessageCode != null) {
            mav.addObject("showSubmittedToast", true);
            mav.addObject(
                "submittedToastMessageCode",
                submittedToastMessageCode
            );
        }
    }

    private List<ProfileActivityEntry> buildActivityEntries(
        final List<ProfileActivityItem> items,
        final Long currentUserId,
        final boolean viewerAdmin
    ) {
        final List<Long> reviewIds = items.stream()
            .filter(i -> i.getType() == ItemType.REVIEW)
            .map(ProfileActivityItem::getEntityId)
            .toList();
        final List<Long> postIds = items.stream()
            .filter(i -> i.getType() == ItemType.POST)
            .map(ProfileActivityItem::getEntityId)
            .toList();

        final Map<Long, Review> reviewsById = hydrateReviews(reviewIds);
        final Map<Long, Car> carsById = reviewedCarsById(new ArrayList<>(reviewsById.values()));
        final Map<Long, Long> reviewLikeCounts =
            reviewLikeService.countReviewLikesByReviewIds(reviewIds);
        final Map<Long, Long> reviewReplyCounts =
            reviewReplyService.countRepliesByReviewIds(reviewIds);
        final Set<Long> likedByCurrentUser = currentUserId == null
            ? Set.of()
            : reviewLikeService.getLikedReviewIds(reviewIds, currentUserId);

        final Map<Long, CommunityPost> postsById = hydratePosts(postIds);
        final Map<Long, Long> helpfulCounts =
            communityService.countHelpfulReactionsByPostIds(postIds);
        final Map<Long, Long> commentCounts =
            communityService.countCommentsByPostIds(postIds);
        final Set<Long> helpfulByCurrentUser = currentUserId == null
            ? Set.of()
            : communityService.findPostHelpfulReactionsByUser(postIds, currentUserId);
        final Set<Long> hideablePostIds = communityService.getHideablePostIds(
            postsById.values(), currentUserId, viewerAdmin);
        final Set<Long> editableReviewIds =
            reviewService.getEditableReviewIds(reviewsById.values(), currentUserId);
        final Set<Long> editablePostIds =
            communityService.getEditablePostIds(postsById.values(), currentUserId);

        final List<ProfileActivityEntry> entries = new ArrayList<>(items.size());
        for (final ProfileActivityItem item : items) {
            if (item.getType() == ItemType.REVIEW) {
                final Review review = reviewsById.get(item.getEntityId());
                if (review == null) {
                    continue;
                }
                final boolean reviewOwnedByCurrentUser =
                    editableReviewIds.contains(review.getId());
                entries.add(ProfileActivityEntry.review(
                    new ProfileReviewCard(
                        review,
                        carsById.get(review.getCarId()),
                        likedByCurrentUser.contains(review.getId()),
                        reviewLikeCounts.getOrDefault(review.getId(), 0L),
                        reviewOwnedByCurrentUser,
                        reviewReplyCounts.getOrDefault(review.getId(), 0L),
                        viewerAdmin && !reviewOwnedByCurrentUser
                    )
                ));
            } else {
                final CommunityPost post = postsById.get(item.getEntityId());
                if (post == null) {
                    continue;
                }
                final boolean postOwnedByCurrentUser =
                    editablePostIds.contains(post.getId());
                final boolean postHideable = hideablePostIds.contains(post.getId());
                entries.add(ProfileActivityEntry.post(
                    new ProfilePostCard(
                        post.getId(),
                        postAuthorName(post),
                        post.getCommunity().getSlug(),
                        post.getCommunity().getName(),
                        post.getSlug(),
                        post.getTitle(),
                        post.getBody(),
                        post.getCreatedAt(),
                        helpfulCounts.getOrDefault(post.getId(), 0L),
                        commentCounts.getOrDefault(post.getId(), 0L),
                        helpfulByCurrentUser.contains(post.getId()),
                        postOwnedByCurrentUser,
                        postHideable
                    )
                ));
            }
        }
        return entries;
    }

    private Map<Long, Review> hydrateReviews(final List<Long> reviewIds) {
        if (reviewIds.isEmpty()) {
            return Map.of();
        }
        return reviewService.getReviewsByIds(reviewIds)
            .stream()
            .collect(Collectors.toMap(
                Review::getId,
                Function.identity(),
                (left, right) -> left
            ));
    }

    private Map<Long, CommunityPost> hydratePosts(final List<Long> postIds) {
        if (postIds.isEmpty()) {
            return Map.of();
        }
        return communityService.getPostsByIds(postIds)
            .stream()
            .collect(Collectors.toMap(
                CommunityPost::getId,
                Function.identity(),
                (left, right) -> left
            ));
    }

    private Map<Long, Car> reviewedCarsById(final List<Review> reviews) {
        final Set<Long> reviewedCarIds = reviews
            .stream()
            .map(Review::getCarId)
            .collect(Collectors.toSet());
        return carService
            .getCarsByIds(reviewedCarIds)
            .stream()
            .collect(Collectors.toMap(Car::getId, Function.identity()));
    }

    private String postAuthorName(final CommunityPost post) {
        final User author = post.getAuthor();
        if (author == null) {
            return null;
        }
        return displayName(author);
    }

    private Map<Long, ReviewStats> reviewStatsByCarId(
        final List<Car> favoriteCars
    ) {
        if (favoriteCars.isEmpty()) {
            return Map.of();
        }
        return reviewService
            .getReviewStatsByCarIds(
                favoriteCars.stream().map(Car::getId).toList()
            )
            .stream()
            .collect(
                Collectors.toMap(ReviewStats::getCarId, Function.identity())
            );
    }

    private ProfileData toProfileData(final User user, final long activityCount) {
        return new ProfileData(
            user.getId(),
            displayName(user),
            user.getEmail(),
            user.getPreferredLocale(),
            initials(user),
            activityCount,
            userFollowService.countFollowing(user.getId()),
            userFollowService.countFollowers(user.getId())
        );
    }

    private List<ProfileConnection> toConnections(final List<User> users, final Long currentUserId) {
        if (users.isEmpty() || currentUserId == null) {
            return users.stream()
                    .map(user -> toConnection(user, currentUserId))
                    .toList();
        }
        final Set<Long> followedIds = userFollowService.getFollowedIds(
            currentUserId,
            users.stream().map(User::getId).toList()
        );
        return users.stream()
                .map(user -> toConnectionWithPreloadedStatus(user, currentUserId, followedIds))
                .toList();
    }

    private ProfileConnection toConnectionWithPreloadedStatus(final User user, final Long currentUserId, final Set<Long> followedIds) {
        final boolean currentUser = currentUserId != null && currentUserId == user.getId();
        final boolean following = !currentUser && followedIds.contains(user.getId());
        return new ProfileConnection(user.getId(), displayName(user), initials(user), following, !currentUser);
    }

    private ProfileConnection toConnection(final User user, final Long currentUserId) {
        final boolean currentUser = currentUserId != null && currentUserId == user.getId();
        final boolean following = currentUserId != null
                && !currentUser
                && userFollowService.isFollowing(currentUserId, user.getId());
        return new ProfileConnection(user.getId(), displayName(user), initials(user), following, !currentUser);
    }

    private String displayName(final User user) {
        if (
            user.getUsername() != null && !user.getUsername().trim().isEmpty()
        ) {
            return user.getUsername().trim();
        }
        return user.getEmail();
    }

    private String initials(final User user) {
        final String value = displayName(user);
        if (value == null || value.trim().isEmpty()) {
            return "?";
        }
        final String[] parts = value.trim().split("\\s+");
        if (parts.length > 1) {
            return (
                parts[0].substring(0, 1) +
                parts[1].substring(0, 1)
            ).toUpperCase(Locale.ROOT);
        }
        return value
            .substring(0, Math.min(2, value.length()))
            .toUpperCase(Locale.ROOT);
    }

    private String normalizeConnectionsKind(final String raw) {
        if (raw == null) {
            return null;
        }
        final String value = raw.trim().toLowerCase(java.util.Locale.ROOT);
        if (CONNECTIONS_KIND_FOLLOWERS.equals(value) || CONNECTIONS_KIND_FOLLOWING.equals(value)) {
            return value;
        }
        return null;
    }

    private String normalizeProfileTab(final String tab) {
        if (tab == null || tab.isBlank()) {
            return TAB_ACTIVITY;
        }
        final String normalizedTab = tab.trim().toLowerCase(Locale.ROOT);
        switch (normalizedTab) {
            case TAB_FAVORITES:
                return TAB_FAVORITES;
            case TAB_LIKES:
                return TAB_LIKES;
            default:
                return TAB_ACTIVITY;
        }
    }

    public static final class ProfileData {

        private final long id;
        private final String name;
        private final String email;
        private final String preferredLocale;
        private final String initials;
        private final long activityCount;
        private final long followingCount;
        private final long followerCount;

        private ProfileData(
            final long id,
            final String name,
            final String email,
            final String preferredLocale,
            final String initials,
            final long activityCount,
            final long followingCount,
            final long followerCount
        ) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.preferredLocale = preferredLocale;
            this.initials = initials;
            this.activityCount = activityCount;
            this.followingCount = followingCount;
            this.followerCount = followerCount;
        }

        public long getId() {
            return id;
        }

        public String getName() {
            return name;
        }

        public String getEmail() {
            return email;
        }

        public String getPreferredLocale() {
            return preferredLocale;
        }

        public String getInitials() {
            return initials;
        }

        public long getActivityCount() {
            return activityCount;
        }

        public long getFollowingCount() {
            return followingCount;
        }

        public long getFollowerCount() {
            return followerCount;
        }
    }

    public static final class ProfileReviewCard {

        private final Review review;
        private final Car car;
        private final boolean liked;
        private final long likeCount;
        private final boolean ownedByCurrentUser;
        private final long replyCount;
        private final boolean hideable;

        private ProfileReviewCard(
            final Review review,
            final Car car,
            final boolean liked,
            final long likeCount,
            final boolean ownedByCurrentUser,
            final long replyCount,
            final boolean hideable
        ) {
            this.review = review;
            this.car = car;
            this.liked = liked;
            this.likeCount = likeCount;
            this.ownedByCurrentUser = ownedByCurrentUser;
            this.replyCount = replyCount;
            this.hideable = hideable;
        }

        public Review getReview() {
            return review;
        }

        public Car getCar() {
            return car;
        }

        public boolean getHasCar() {
            return car != null;
        }

        public String getCarName() {
            if (car == null) {
                return "";
            }
            return car.getBrandName() + " " + car.getModel();
        }

        public boolean getHasCarImage() {
            return car != null && car.getHasImage();
        }

        public boolean getLiked() {
            return liked;
        }

        public long getLikeCount() {
            return likeCount;
        }

        public boolean getOwnedByCurrentUser() {
            return ownedByCurrentUser;
        }

        public long getReplyCount() {
            return replyCount;
        }

        public boolean getHideable() {
            return hideable;
        }
    }

    public static final class ProfileActivityEntry {

        private final String type;
        private final ProfileReviewCard reviewCard;
        private final ProfilePostCard postCard;

        private ProfileActivityEntry(
            final String type,
            final ProfileReviewCard reviewCard,
            final ProfilePostCard postCard
        ) {
            this.type = type;
            this.reviewCard = reviewCard;
            this.postCard = postCard;
        }

        static ProfileActivityEntry review(final ProfileReviewCard card) {
            return new ProfileActivityEntry("review", card, null);
        }

        static ProfileActivityEntry post(final ProfilePostCard card) {
            return new ProfileActivityEntry("post", null, card);
        }

        public String getType() {
            return type;
        }

        public boolean getIsReview() {
            return "review".equals(type);
        }

        public boolean getIsPost() {
            return "post".equals(type);
        }

        public ProfileReviewCard getReviewCard() {
            return reviewCard;
        }

        public ProfilePostCard getPostCard() {
            return postCard;
        }
    }

    public static final class ProfilePostCard {

        private final long postId;
        private final String authorName;
        private final String communitySlug;
        private final String communityName;
        private final String postSlug;
        private final String title;
        private final String body;
        private final LocalDateTime createdAt;
        private final long helpfulCount;
        private final long commentCount;
        private final boolean helpfulByCurrentUser;
        private final boolean ownedByCurrentUser;
        private final boolean hideable;

        private ProfilePostCard(
            final long postId,
            final String authorName,
            final String communitySlug,
            final String communityName,
            final String postSlug,
            final String title,
            final String body,
            final LocalDateTime createdAt,
            final long helpfulCount,
            final long commentCount,
            final boolean helpfulByCurrentUser,
            final boolean ownedByCurrentUser,
            final boolean hideable
        ) {
            this.postId = postId;
            this.authorName = authorName;
            this.communitySlug = communitySlug;
            this.communityName = communityName;
            this.postSlug = postSlug;
            this.title = title;
            this.body = body;
            this.createdAt = createdAt;
            this.helpfulCount = helpfulCount;
            this.commentCount = commentCount;
            this.helpfulByCurrentUser = helpfulByCurrentUser;
            this.ownedByCurrentUser = ownedByCurrentUser;
            this.hideable = hideable;
        }

        public long getPostId() {
            return postId;
        }

        public String getAuthorName() {
            return authorName;
        }

        public String getCommunitySlug() {
            return communitySlug;
        }

        public String getCommunityName() {
            return communityName;
        }

        public String getPostSlug() {
            return postSlug;
        }

        public String getTitle() {
            return title;
        }

        public String getBody() {
            return body;
        }

        public LocalDateTime getCreatedAt() {
            return createdAt;
        }

        public long getHelpfulCount() {
            return helpfulCount;
        }

        public long getCommentCount() {
            return commentCount;
        }

        public boolean getHelpfulByCurrentUser() {
            return helpfulByCurrentUser;
        }

        public boolean getOwnedByCurrentUser() {
            return ownedByCurrentUser;
        }

        public boolean getHideable() {
            return hideable;
        }
    }

    public static final class ProfileConnection {

        private final long id;
        private final String username;
        private final String initials;
        private final boolean following;
        private final boolean followable;

        private ProfileConnection(
            final long id,
            final String username,
            final String initials,
            final boolean following,
            final boolean followable
        ) {
            this.id = id;
            this.username = username;
            this.initials = initials;
            this.following = following;
            this.followable = followable;
        }

        public long getId() {
            return id;
        }

        public String getUsername() {
            return username;
        }

        public String getInitials() {
            return initials;
        }

        public boolean getFollowing() {
            return following;
        }

        public boolean getFollowable() {
            return followable;
        }
    }

    public static final class ConnectionsPagination {

        private final String kind;
        private final int currentPage;
        private final int totalPages;

        private ConnectionsPagination(final String kind, final int currentPage, final int totalPages) {
            this.kind = kind;
            this.currentPage = currentPage;
            this.totalPages = totalPages;
        }

        public String getKind() {
            return kind;
        }

        public int getCurrentPage() {
            return currentPage;
        }

        public int getTotalPages() {
            return totalPages;
        }

        public boolean getHasPrevious() {
            return currentPage > 1;
        }

        public boolean getHasNext() {
            return currentPage < totalPages;
        }
    }
}
