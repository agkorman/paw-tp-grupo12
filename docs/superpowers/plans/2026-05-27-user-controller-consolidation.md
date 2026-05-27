# UserController Consolidation Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Merge `ProfileController` and `UsersController` into a single `UserController` with a consistent `/user` + `/users/{id}` URL scheme and one follow endpoint.

**Architecture:** Web-layer-only refactor. No service, persistence, or model changes. The existing `ProfileController` is renamed to `UserController` and absorbs `UsersController`'s search + follow logic; the duplicate follow endpoint collapses into one that chooses its redirect via a whitelisted `back` parameter built server-side.

**Tech Stack:** Spring MVC (not Spring Boot), JSP/JSTL views, Spring Security (`WebAuthConfig`). Build with Maven.

**Note on testing:** This project's automated tests cover only the persistence and service layers (per `CLAUDE.md`); there is no web-layer test harness, and this refactor touches no service/DAO/model code. So there is no unit test to write first — each task is verified by a successful Maven build (which catches compile/wiring errors), and the whole change is verified manually in Task 4 by running Jetty. Commit after each task.

**Spec:** `docs/superpowers/specs/2026-05-27-user-controller-consolidation-design.md`

---

### Task 1: Consolidate the Java controllers

**Files:**
- Rename: `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/ProfileController.java` → `UserController.java`
- Delete: `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/UsersController.java`

- [ ] **Step 1: Rename the file with git**

Run:
```bash
cd webapp/src/main/java/ar/edu/itba/paw/webapp/controller
git mv ProfileController.java UserController.java
cd -
```

- [ ] **Step 2: Rename the class and logger**

In `UserController.java`, change the class declaration and logger.

Find:
```java
public class ProfileController {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        ProfileController.class
    );
```
Replace:
```java
public class UserController {

    private static final Logger LOGGER = LoggerFactory.getLogger(
        UserController.class
    );
```

Then rename the constructor. Find:
```java
    public ProfileController(
```
Replace:
```java
    public UserController(
```

- [ ] **Step 3: Add the imports needed by the merged search + follow code**

In `UserController.java`, add these imports (some may already be present — skip any duplicates; the build in Step 11 will flag a missing one):
```java
import ar.edu.itba.paw.services.exception.SelfFollowException;
import org.springframework.web.servlet.view.RedirectView;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;
```

- [ ] **Step 4: Repoint the profile GET/POST mappings to the new paths**

Make these four `@RequestMapping` edits in `UserController.java`:

Find: `@RequestMapping(value = "/profile", method = RequestMethod.GET)`
Replace: `@RequestMapping(value = "/user", method = RequestMethod.GET)`

Find: `@RequestMapping(value = "/profiles/{userId}", method = RequestMethod.GET)`
Replace: `@RequestMapping(value = "/users/{userId}", method = RequestMethod.GET)`

Find: `@RequestMapping(value = "/profile", method = RequestMethod.POST)`
Replace: `@RequestMapping(value = "/user", method = RequestMethod.POST)`

Find: `@RequestMapping(value = "/profile/language", method = RequestMethod.POST)`
Replace: `@RequestMapping(value = "/user/language", method = RequestMethod.POST)`

- [ ] **Step 5: Update the internal redirect targets**

In `UserController.java`, replace every `redirect:/profile` (own-profile redirects in `updateOwnProfile`, `updateOwnProfileLanguage`, `profileEditError`, and the follow error branch) with `redirect:/user`. There are 4 occurrences of the exact string `new ModelAndView("redirect:/profile")` — use replace-all on:

Find: `new ModelAndView("redirect:/profile")`
Replace: `new ModelAndView("redirect:/user")`

(Leave `new ModelAndView("redirect:/login")` untouched.)

- [ ] **Step 6: Update `profileBasePath`**

Find:
```java
        final String profileBasePath = ownProfile
            ? "/profile"
            : "/profiles/" + profileUser.getId();
```
Replace:
```java
        final String profileBasePath = ownProfile
            ? "/user"
            : "/users/" + profileUser.getId();
```

- [ ] **Step 7: Replace the profile follow handler with the unified one**

Find the existing profile follow handler:
```java
    @RequestMapping(
        value = "/profiles/{userId}/follow",
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
            return new ModelAndView("redirect:/profiles/" + userId);
        } catch (final Exception e) {
            return new ModelAndView("redirect:/user");
        }
    }
```
Replace with the unified handler (note: a `back=search` value rebuilds the search URL; anything else redirects to the followed user's profile):
```java
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
```

> The `redirect:/user` value in Step 5's replace-all does NOT touch this method, because this method is fully replaced in this step.

- [ ] **Step 8: Add the `searchUsers` handler and `buildSearchRedirect` helper**

Paste these two members into `UserController.java`, immediately after the `toggleFollow` method from Step 7 (they are copied verbatim from the deleted `UsersController`):
```java
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
```

- [ ] **Step 9: Delete `UsersController`**

Run:
```bash
git rm webapp/src/main/java/ar/edu/itba/paw/webapp/controller/UsersController.java
```

- [ ] **Step 10: Sanity-check there are no lingering references to the old names/paths in Java**

Run:
```bash
grep -rn "ProfileController\|UsersController" webapp/src/main/java
grep -rn '"/profile\|/profiles/' webapp/src/main/java
```
Expected: no output from either command (the only acceptable remaining hits would be inside comments — there should be none).

- [ ] **Step 11: Build to verify it compiles and wires**

Run: `mvn clean install`
Expected: `BUILD SUCCESS`. If a `cannot find symbol` error appears, it is almost certainly a missing import from Step 3 — add it and rebuild.

- [ ] **Step 12: Commit**

Run:
```bash
git add -A
git commit -m "Merge ProfileController and UsersController into UserController"
```

---

### Task 2: Update Spring Security route rules

**Files:**
- Modify: `webapp/src/main/java/ar/edu/itba/paw/webapp/config/WebAuthConfig.java`

- [ ] **Step 1: Make the public-profile GET matcher use the new noun**

Find:
```java
                                antMatcher(HttpMethod.GET, "/profiles/*"),
                                antMatcher(HttpMethod.GET, "/users/search"),
```
Replace:
```java
                                antMatcher(HttpMethod.GET, "/users/*"),
```
(`/users/*` already covers `/users/search` and `/users/{id}`, so the separate `/users/search` line is removed. `/user` is a different path and stays out of this public block.)

- [ ] **Step 2: Update the authenticated GET matcher for the own-profile page**

Find:
```java
                                antMatcher(HttpMethod.GET, "/profile"),
```
Replace:
```java
                                antMatcher(HttpMethod.GET, "/user"),
```

- [ ] **Step 3: Update the authenticated POST matchers (profile + follow)**

Find:
```java
                                antMatcher(HttpMethod.POST, "/profile"),
                                antMatcher(HttpMethod.POST, "/profile/language"),
```
Replace:
```java
                                antMatcher(HttpMethod.POST, "/user"),
                                antMatcher(HttpMethod.POST, "/user/language"),
```

Then collapse the two follow matchers into one. Find:
```java
                                antMatcher(HttpMethod.POST, "/profiles/*/follow"),
                                antMatcher(HttpMethod.POST, "/users/*/follow"),
```
Replace:
```java
                                antMatcher(HttpMethod.POST, "/users/*/follow"),
```

- [ ] **Step 4: Confirm no stale matchers remain**

Run:
```bash
grep -n "/profile" webapp/src/main/java/ar/edu/itba/paw/webapp/config/WebAuthConfig.java
```
Expected: no output.

- [ ] **Step 5: Build**

Run: `mvn -pl webapp -am compile`
Expected: `BUILD SUCCESS`.

- [ ] **Step 6: Commit**

Run:
```bash
git add webapp/src/main/java/ar/edu/itba/paw/webapp/config/WebAuthConfig.java
git commit -m "Update security route rules for UserController paths"
```

---

### Task 3: Update JSP and tag references

**Files:**
- Modify: `webapp/src/main/webapp/WEB-INF/tags/nav.tag`
- Modify: `webapp/src/main/webapp/WEB-INF/tags/edit-profile-modal.tag`
- Modify: `webapp/src/main/webapp/WEB-INF/tags/review-author-link.tag`
- Modify: `webapp/src/main/webapp/WEB-INF/tags/reviews-feed.tag`
- Modify: `webapp/src/main/webapp/WEB-INF/tags/profile-connections-modal.tag`
- Modify: `webapp/src/main/webapp/WEB-INF/jsp/profile.jsp`
- Modify: `webapp/src/main/webapp/WEB-INF/jsp/review-form.jsp`
- Modify: `webapp/src/main/webapp/WEB-INF/jsp/users-search.jsp`

- [ ] **Step 1: `nav.tag` — own-profile links**

Both occurrences. Find (replace-all):
```jsp
href="<c:url value='/profile'/>"
```
Replace:
```jsp
href="<c:url value='/user'/>"
```

- [ ] **Step 2: `edit-profile-modal.tag` — profile update form action**

Find:
```jsp
<c:url var="profileUpdateUrl" value="/profile"/>
```
Replace:
```jsp
<c:url var="profileUpdateUrl" value="/user"/>
```

- [ ] **Step 3: `review-author-link.tag` — author profile link**

Find:
```jsp
        <c:url var="authorProfileUrl" value="/profiles/${review.userId}"/>
```
Replace:
```jsp
        <c:url var="authorProfileUrl" value="/users/${review.userId}"/>
```

- [ ] **Step 4: `reviews-feed.tag` — reply author profile link**

Find:
```jsp
                                        <c:url var="replyAuthorProfileUrl" value="/profiles/${reply.userId}"/>
```
Replace:
```jsp
                                        <c:url var="replyAuthorProfileUrl" value="/users/${reply.userId}"/>
```

- [ ] **Step 5: `review-form.jsp` — own-profile link**

Find:
```jsp
    <c:url var="profileUrl" value="/profile"/>
```
Replace:
```jsp
    <c:url var="profileUrl" value="/user"/>
```

- [ ] **Step 6: `profile.jsp` — language form, profile link, follow URL, and login redirect**

Find: `<c:url var="profileLanguageUrl" value="/profile/language"/>`
Replace: `<c:url var="profileLanguageUrl" value="/user/language"/>`

Find: `<c:url var="profileFollowUrl" value="/profiles/${profile.id}/follow"/>`
Replace: `<c:url var="profileFollowUrl" value="/users/${profile.id}/follow"/>`

Find:
```jsp
                                <c:param name="redirect" value="/profiles/${profile.id}"/>
```
Replace:
```jsp
                                <c:param name="redirect" value="/users/${profile.id}"/>
```

(The profile follow form posts to `/users/{id}/follow` with no `back` param, so it redirects back to the profile by default — no other change needed in this form.)

- [ ] **Step 7: `profile-connections-modal.tag` — connection link, follow URL, login redirect**

Find:
```jsp
                        <c:url var="connectionProfileUrl" value="/profiles/${user.id}"/>
```
Replace:
```jsp
                        <c:url var="connectionProfileUrl" value="/users/${user.id}"/>
```

Find:
```jsp
                                <c:url var="connectionFollowUrl" value="/profiles/${user.id}/follow"/>
```
Replace:
```jsp
                                <c:url var="connectionFollowUrl" value="/users/${user.id}/follow"/>
```

Find:
```jsp
                                            <c:param name="redirect" value="/profiles/${user.id}"/>
```
Replace:
```jsp
                                            <c:param name="redirect" value="/users/${user.id}"/>
```

- [ ] **Step 8: `users-search.jsp` — result profile link and `back=search` on the follow form**

Find:
```jsp
                                <c:url var="profileUrl" value="/profiles/${u.id}"/>
```
Replace:
```jsp
                                <c:url var="profileUrl" value="/users/${u.id}"/>
```

Then add the `back=search` hidden field to the follow form so the unified endpoint returns to the results. Find:
```jsp
                                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                                <c:if test="${not empty query}">
                                                    <input type="hidden" name="q" value="<c:out value='${query}'/>">
                                                </c:if>
                                                <input type="hidden" name="page" value="${currentPage}">
```
Replace:
```jsp
                                                <input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">
                                                <input type="hidden" name="back" value="search">
                                                <c:if test="${not empty query}">
                                                    <input type="hidden" name="q" value="<c:out value='${query}'/>">
                                                </c:if>
                                                <input type="hidden" name="page" value="${currentPage}">
```

> The unauthenticated-user login link just below this follow form (`<c:url var="followLoginUrl" value="/login">`) already sets `<c:param name="redirect" value="/users/search"/>`, which is correct — leave it as-is. No other edit is needed in this file.

- [ ] **Step 9: Confirm no stale view references remain**

Run:
```bash
grep -rIn "/profiles/\|value=['\"]/profile['\"]\|value=['\"]/profile/" webapp/src/main/webapp/WEB-INF
```
Expected: no output. (CSS class names like `profile-follow-form`, message vars, and `${profileBasePath}` usages are fine and will not match this pattern.)

- [ ] **Step 10: Build the WAR**

Run: `mvn -pl webapp -am package -DskipTests`
Expected: `BUILD SUCCESS`.

- [ ] **Step 11: Commit**

Run:
```bash
git add webapp/src/main/webapp/WEB-INF
git commit -m "Point views at consolidated /user and /users paths"
```

---

### Task 4: Manual verification

**Files:** none (runtime verification).

- [ ] **Step 1: Start the dev server**

Run: `mvn -pl webapp jetty:run`
Expected: server starts at `http://localhost:8080/webapp` with no startup errors (a duplicate `@RequestMapping` would fail startup here).

- [ ] **Step 2: Walk through every flow**

Confirm each of these, logged in unless noted:
- [ ] `GET /webapp/user` renders your own profile; logged out, it redirects to `/login`.
- [ ] `GET /webapp/users/{otherId}` renders another user's profile while logged out.
- [ ] `GET /webapp/users/{yourId}` renders your own profile (ownProfile branch, no follow button).
- [ ] `GET /webapp/users/search?q=...` renders results; the literal `search` path is NOT treated as a user id.
- [ ] Follow/unfollow from a search result returns to the results page with `q` and `page` preserved.
- [ ] Follow/unfollow from a profile hero returns to that user's profile (`/users/{id}`).
- [ ] Open the followers/following modal from a profile and follow/unfollow from inside it.
- [ ] Edit your username (`POST /user`) — success and the duplicate-name error path both redirect to `/user`.
- [ ] Change language (`POST /user/language`) — toast shows, redirects to `/user`.
- [ ] A self-follow attempt (if reachable) produces no error page — it is silently ignored.
- [ ] The nav avatar/profile link and "view author" links on reviews navigate to the new paths.

- [ ] **Step 3: Stop the server**

Press `Ctrl-C` in the Jetty terminal.

---

## Notes for the implementer

- Keep `redirect:/login` strings as-is everywhere; only the `/profile` and `/profiles` application paths change.
- The package-private overloads `ownProfile(AuthenticatedUser)` and `publicProfile(long, AuthenticatedUser)` have no `@RequestMapping` and are not referenced outside the controller — leave them as plain helper methods.
- Do not touch any service, DAO, model, or `messages.properties` file. If a step seems to require it, stop — the plan or your edit is wrong.
