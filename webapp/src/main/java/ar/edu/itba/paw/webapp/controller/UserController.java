package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.Car;
import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.Pagination;
import ar.edu.itba.paw.model.Review;
import ar.edu.itba.paw.model.ReviewStats;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.services.AdminRequestService;
import ar.edu.itba.paw.services.CarFavoriteService;
import ar.edu.itba.paw.services.CarService;
import ar.edu.itba.paw.services.ReviewLikeService;
import ar.edu.itba.paw.services.ReviewService;
import ar.edu.itba.paw.services.UserFollowService;
import ar.edu.itba.paw.services.UserService;
import ar.edu.itba.paw.services.exception.InvalidServiceInputException;
import ar.edu.itba.paw.services.exception.SelfFollowException;
import ar.edu.itba.paw.services.exception.UsernameAlreadyExistsException;
import ar.edu.itba.paw.webapp.auth.AuthenticatedUser;
import ar.edu.itba.paw.webapp.exception.ResourceNotFoundException;
import ar.edu.itba.paw.webapp.form.ProfileForm;
import ar.edu.itba.paw.webapp.form.ReviewForm;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
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
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
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
import org.springframework.web.servlet.view.RedirectView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class UserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        UserController.class
    );

    private static final String TAB_REVIEWS = "reviews";
    private static final String TAB_FAVORITES = "favorites";
    private static final String TAB_LIKED = "liked";

    private static final int CONNECTIONS_PAGE_SIZE = Pagination.CONNECTIONS_PAGE_SIZE;
    private static final String CONNECTIONS_KIND_FOLLOWERS = "followers";
    private static final String CONNECTIONS_KIND_FOLLOWING = "following";

    private final ReviewService reviewService;
    private final ReviewLikeService reviewLikeService;
    private final CarService carService;
    private final CarFavoriteService carFavoriteService;
    private final UserService userService;
    private final UserFollowService userFollowService;
    private final AdminRequestService adminRequestService;
    private final LocaleResolver localeResolver;

    @Autowired
    public UserController(
        final ReviewService reviewService,
        final ReviewLikeService reviewLikeService,
        final CarService carService,
        final CarFavoriteService carFavoriteService,
        final UserService userService,
        final UserFollowService userFollowService,
        final AdminRequestService adminRequestService,
        final LocaleResolver localeResolver
    ) {
        this.reviewService = reviewService;
        this.reviewLikeService = reviewLikeService;
        this.carService = carService;
        this.carFavoriteService = carFavoriteService;
        this.userService = userService;
        this.userFollowService = userFollowService;
        this.adminRequestService = adminRequestService;
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
        @RequestParam(value = "followingPage", defaultValue = "1") final int followingPage
    ) {
        if (currentUser == null) {
            return new ModelAndView("redirect:/login");
        }
        return profile(currentUser.getId(), currentUser, tab, page, submitted,
                connectionsModal, followersPage, followingPage);
    }

    ModelAndView ownProfile(final AuthenticatedUser currentUser) {
        return ownProfile(currentUser, null, 1, null, null, 1, 1);
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
        @RequestParam(value = "followingPage", defaultValue = "1") final int followingPage
    ) {
        return profile(userId, currentUser, tab, page, submitted,
                connectionsModal, followersPage, followingPage);
    }

    ModelAndView publicProfile(
        final long userId,
        final AuthenticatedUser currentUser
    ) {
        return publicProfile(userId, currentUser, null, 1, null, null, 1, 1);
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
        } catch (final DataIntegrityViolationException e) {
            return profileEditError(
                "profile.edit.error.username.exists",
                normalizedUsername,
                redirectAttributes
            );
        } catch (final UsernameAlreadyExistsException e) {
            return profileEditError(
                "profile.edit.error.username.exists",
                normalizedUsername,
                redirectAttributes
            );
        } catch (final InvalidServiceInputException e) {
            return profileEditError(
                "profile.edit.error.generic",
                normalizedUsername,
                redirectAttributes
            );
        } catch (final DataAccessException e) {
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
        } catch (final InvalidServiceInputException | DataAccessException e) {
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
        @RequestParam(value = "back", required = false) final String back,
        @RequestParam(value = "q", required = false) final String query,
        @RequestParam(value = "page", defaultValue = "1") final int page,
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

        if ("search".equals(back)) {
            return new ModelAndView(new RedirectView(buildSearchRedirect(query, page), true));
        }
        return new ModelAndView("redirect:/users/" + userId);
    }

    @RequestMapping(value = "/users/search", method = RequestMethod.GET)
    public ModelAndView searchUsers(@RequestParam(value = "q", required = false) final String query,
                                    @RequestParam(value = "page", defaultValue = "1") final int page,
                                    @AuthenticationPrincipal final AuthenticatedUser currentUser) {
        LOGGER.debug("rendering user search results page={} hasQuery={}", page, query != null && !query.isBlank());
        final Page<User> resultsPage = userService.searchUsers(query, page);
        final List<User> results = resultsPage.getItems();

        final Set<Long> followedIds;
        if (currentUser != null && !results.isEmpty()) {
            final List<Long> targetIds = results.stream()
                    .map(User::getId)
                    .filter(id -> id != currentUser.getId())
                    .collect(Collectors.toList());
            followedIds = targetIds.isEmpty()
                    ? Collections.emptySet()
                    : userFollowService.getFollowedIds(currentUser.getId(), targetIds);
        } else {
            followedIds = Collections.emptySet();
        }

        final ModelAndView mav = new ModelAndView("users-search.jsp");
        mav.addObject("query", query == null ? "" : query);
        mav.addObject("results", results);
        mav.addObject("currentPage", resultsPage.getPageNumber());
        mav.addObject("totalPages", resultsPage.getTotalPages());
        mav.addObject("totalItems", resultsPage.getTotalItems());
        mav.addObject("followedIds", followedIds);
        mav.addObject("currentUserId", currentUser == null ? null : currentUser.getId());
        return mav;
    }

    private String buildSearchRedirect(final String query, final int page) {
        final StringBuilder sb = new StringBuilder("/users/search");
        boolean hasParam = false;
        if (query != null && !query.isBlank()) {
            sb.append("?q=").append(URLEncoder.encode(query, StandardCharsets.UTF_8));
            hasParam = true;
        }
        if (page > 1) {
            sb.append(hasParam ? "&" : "?").append("page=").append(page);
        }
        return sb.toString();
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
        final int followingPage
    ) {
        final User profileUser = userService
            .getUserById(profileUserId)
            .orElseThrow(() ->
                new ResourceNotFoundException("User", profileUserId)
            );
        final Long currentUserId =
            currentUser == null ? null : currentUser.getId();
        final boolean ownProfile =
            currentUserId != null && currentUserId == profileUser.getId();
        final String activeTab = ownProfile
            ? normalizeProfileTab(tab)
            : TAB_REVIEWS;
        final String profileBasePath = ownProfile
            ? "/user"
            : "/users/" + profileUser.getId();

        final long reviewCount = reviewService.countReviewsByUser(
            profileUser.getId()
        );
        final long favoriteCarCount = ownProfile
            ? carFavoriteService.countFavoriteCars(profileUser.getId())
            : 0L;
        final long likedReviewCount = ownProfile
            ? reviewLikeService.countLikedReviewsByUser(profileUser.getId())
            : 0L;

        Page<Review> userReviewPage = Page.empty(1, 0);
        Page<Car> favoriteCarPage = Page.empty(1, 0);
        Page<Long> likedReviewIdPage = Page.empty(1, 0);

        List<ProfileReviewCard> reviews = List.of();
        List<Car> favoriteCars = List.of();
        List<ProfileReviewCard> likedReviewCards = List.of();
        Map<Long, ReviewStats> reviewStatsByCarId = Map.of();

        if (TAB_FAVORITES.equals(activeTab)) {
            favoriteCarPage = carFavoriteService.getFavoriteCars(
                profileUser.getId(),
                page
            );
            favoriteCars = favoriteCarPage.getItems();
            reviewStatsByCarId = reviewStatsByCarId(favoriteCars);
        } else if (TAB_LIKED.equals(activeTab)) {
            likedReviewIdPage = reviewLikeService.getLikedReviewIdsByUser(
                profileUser.getId(),
                page
            );
            likedReviewCards = buildLikedReviewCards(
                likedReviewIdPage.getItems(),
                currentUserId
            );
        } else {
            userReviewPage = reviewService.getReviewsByUser(
                profileUser.getId(),
                page
            );
            reviews = buildProfileReviewCards(
                userReviewPage.getItems(),
                reviewedCarsById(userReviewPage.getItems()),
                currentUserId
            );
        }

        final boolean followingProfile =
            currentUserId != null &&
            !ownProfile &&
            userFollowService.isFollowing(currentUserId, profileUser.getId());

        final ModelAndView mav = new ModelAndView("profile.jsp");
        mav.addObject("profile", toProfileData(profileUser, reviewCount));
        mav.addObject("profileBasePath", profileBasePath);
        mav.addObject("activeTab", activeTab);
        mav.addObject("profileReviewCount", reviewCount);
        mav.addObject("favoriteCarCount", favoriteCarCount);
        mav.addObject("likedReviewCount", likedReviewCount);
        mav.addObject("profileReviews", reviews);
        mav.addObject(
            "profileReviewsCurrentPage",
            userReviewPage.getPageNumber()
        );
        mav.addObject(
            "profileReviewsTotalPages",
            userReviewPage.getTotalPages()
        );
        mav.addObject("likedReviews", likedReviewCards);
        mav.addObject(
            "likedReviewsCurrentPage",
            likedReviewIdPage.getPageNumber()
        );
        mav.addObject(
            "likedReviewsTotalPages",
            likedReviewIdPage.getTotalPages()
        );
        mav.addObject("likedActivityCount", likedReviewCount);
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
            final Page<User> followingPageResult = userFollowService.getFollowing(profileUser.getId(), followingPage);
            followingUsers = toConnections(followingPageResult.getItems(), currentUserId);
            connectionsPagination = new ConnectionsPagination(
                    CONNECTIONS_KIND_FOLLOWING,
                    followingPageResult.getPageNumber(),
                    followingPageResult.getTotalPages()
            );
        } else if (CONNECTIONS_KIND_FOLLOWERS.equals(activeConnectionsKind)) {
            final Page<User> followersPageResult = userFollowService.getFollowers(profileUser.getId(), followersPage);
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

    private List<ProfileReviewCard> buildProfileReviewCards(
        final List<Review> reviews,
        final Map<Long, Car> carsById,
        final Long currentUserId
    ) {
        final List<Long> reviewIds = reviews
            .stream()
            .map(Review::getId)
            .toList();
        final Map<Long, Long> likeCounts =
            reviewLikeService.countReviewLikesByReviewIds(reviewIds);
        final Set<Long> likedByCurrentUser = likedReviewIds(
            reviewIds,
            currentUserId
        );
        return reviews
            .stream()
            .map(review ->
                toProfileReviewCard(
                    review,
                    carsById,
                    likeCounts,
                    likedByCurrentUser,
                    currentUserId
                )
            )
            .toList();
    }

    private List<ProfileReviewCard> buildLikedReviewCards(
        final List<Long> reviewIds,
        final Long currentUserId
    ) {
        final List<Review> likedReviews = orderedExistingReviews(reviewIds);
        final Map<Long, Car> carsById = reviewedCarsById(likedReviews);
        final List<Long> likedReviewIds = likedReviews
            .stream()
            .map(Review::getId)
            .toList();
        final Map<Long, Long> likeCounts =
            reviewLikeService.countReviewLikesByReviewIds(likedReviewIds);
        final Set<Long> likedByCurrentUser = likedReviewIds(
            likedReviewIds,
            currentUserId
        );
        return likedReviews
            .stream()
            .map(review ->
                toProfileReviewCard(
                    review,
                    carsById,
                    likeCounts,
                    likedByCurrentUser,
                    currentUserId
                )
            )
            .toList();
    }

    private List<Review> orderedExistingReviews(final List<Long> reviewIds) {
        final Map<Long, Review> reviewsById = reviewService
            .getReviewsByIds(reviewIds)
            .stream()
            .collect(
                Collectors.toMap(
                    Review::getId,
                    Function.identity(),
                    (left, right) -> left
                )
            );
        return reviewIds
            .stream()
            .map(reviewsById::get)
            .filter(Objects::nonNull)
            .toList();
    }

    private Set<Long> likedReviewIds(
        final List<Long> reviewIds,
        final Long currentUserId
    ) {
        return currentUserId == null
            ? Set.of()
            : reviewLikeService.getLikedReviewIds(reviewIds, currentUserId);
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

    private ProfileData toProfileData(final User user, final long reviewCount) {
        return new ProfileData(
            user.getId(),
            displayName(user),
            user.getEmail(),
            user.getPreferredLocale(),
            initials(user),
            reviewCount,
            userFollowService.countFollowing(user.getId()),
            userFollowService.countFollowers(user.getId())
        );
    }

    private ProfileReviewCard toProfileReviewCard(
        final Review review,
        final Map<Long, Car> carsById,
        final Map<Long, Long> likeCounts,
        final Set<Long> likedByCurrentUser,
        final Long currentUserId
    ) {
        return new ProfileReviewCard(
            review,
            carsById.get(review.getCarId()),
            likedByCurrentUser.contains(review.getId()),
            likeCounts.getOrDefault(review.getId(), 0L),
            isOwnedByCurrentUser(review, currentUserId)
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

    private boolean isOwnedByCurrentUser(
        final Review review,
        final Long currentUserId
    ) {
        return (
            currentUserId != null &&
            review.getUserId() != null &&
            review.getUserId().equals(currentUserId)
        );
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
            return TAB_REVIEWS;
        }
        final String normalizedTab = tab.trim().toLowerCase(Locale.ROOT);
        switch (normalizedTab) {
            case TAB_FAVORITES:
                return TAB_FAVORITES;
            case TAB_LIKED:
                return TAB_LIKED;
            case TAB_REVIEWS:
            default:
                return TAB_REVIEWS;
        }
    }

    public static final class ProfileData {

        private final long id;
        private final String name;
        private final String email;
        private final String preferredLocale;
        private final String initials;
        private final long reviewCount;
        private final long followingCount;
        private final long followerCount;

        private ProfileData(
            final long id,
            final String name,
            final String email,
            final String preferredLocale,
            final String initials,
            final long reviewCount,
            final long followingCount,
            final long followerCount
        ) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.preferredLocale = preferredLocale;
            this.initials = initials;
            this.reviewCount = reviewCount;
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

        public long getReviewCount() {
            return reviewCount;
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

        private ProfileReviewCard(
            final Review review,
            final Car car,
            final boolean liked,
            final long likeCount,
            final boolean ownedByCurrentUser
        ) {
            this.review = review;
            this.car = car;
            this.liked = liked;
            this.likeCount = likeCount;
            this.ownedByCurrentUser = ownedByCurrentUser;
        }

        public Review getReview() {
            return review;
        }

        public Car getCar() {
            return car;
        }

        public String getCarName() {
            if (car == null) {
                return "Auto no disponible";
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
