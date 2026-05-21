package ar.edu.itba.paw.webapp.controller;

import ar.edu.itba.paw.model.Page;
import ar.edu.itba.paw.model.User;
import ar.edu.itba.paw.services.UserFollowService;
import ar.edu.itba.paw.services.UserService;
import ar.edu.itba.paw.services.exception.SelfFollowException;
import ar.edu.itba.paw.webapp.auth.AuthenticatedUser;
import ar.edu.itba.paw.webapp.exception.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Controller
public class UsersController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UsersController.class);

    private final UserService userService;
    private final UserFollowService userFollowService;

    @Autowired
    public UsersController(final UserService userService, final UserFollowService userFollowService) {
        this.userService = userService;
        this.userFollowService = userFollowService;
    }

    @InitBinder
    public void initBinder(final WebDataBinder binder) {
        binder.registerCustomEditor(String.class, new StringTrimmerEditor(true));
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

    @RequestMapping(value = "/users/{userId}/follow", method = RequestMethod.POST)
    public ModelAndView toggleFollow(@PathVariable("userId") final long userId,
                                     @RequestParam(value = "q", required = false) final String query,
                                     @RequestParam(value = "page", defaultValue = "1") final int page,
                                     @AuthenticationPrincipal final AuthenticatedUser currentUser) {
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
        return new ModelAndView(new RedirectView(buildSearchRedirect(query, page), true));
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
}
