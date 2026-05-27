# User Controller Consolidation — Design

**Date:** 2026-05-27
**Status:** Approved (pending spec review)

## Goal

Consolidate the two user-facing web controllers (`ProfileController`, `UsersController`)
into a single `UserController` with a consistent URL scheme, and remove the duplicate
follow endpoint. This is a web-layer-only refactor: no service, persistence, or model
changes.

## Motivation

- Two nouns address the same entity today: `/profiles/{id}` and `/users/{id}`/`/users/search`.
- The follow action is duplicated across two endpoints (`POST /profiles/{id}/follow` and
  `POST /users/{id}/follow`) that call the same `UserFollowService.toggleFollow`, differing
  only in redirect target. Changing follow behavior means editing two controllers.
- A single `UserController` with one noun is more consistent and removes the duplication.

## Decisions

- **Hard cut-over.** Old routes are deleted, not kept as redirects. This is an internal
  project; stale bookmarks are acceptable.
- **Single follow endpoint** with a whitelisted `back` parameter (`search` | profile-default).
  The redirect URL is built server-side — no user-supplied redirect URL, so no
  `LoginRedirectUtils.safeRedirect()` is needed and there is no open-redirect surface.
- **Inner view-model classes stay inline** in `UserController` (smaller diff). The class
  remains large (~900+ lines); this is accepted for now.

## URL Scheme

| Old | New | Handler | Auth |
|-----|-----|---------|------|
| `GET /profile` | `GET /user` | `ownProfile()` | authenticated |
| `GET /profiles/{id}` | `GET /users/{id}` | `publicProfile()` | public |
| `GET /users/search` | `GET /users/search` (unchanged) | `searchUsers()` | public |
| `POST /profile` | `POST /user` | `updateOwnProfile()` | authenticated |
| `POST /profile/language` | `POST /user/language` | `updateOwnProfileLanguage()` | authenticated |
| `POST /profiles/{id}/follow` **and** `POST /users/{id}/follow` | `POST /users/{id}/follow` | `toggleFollow()` | authenticated |

**Routing note:** `GET /users/search` (literal segment) and `GET /users/{userId}` (where
`{userId}` is a `long`) coexist. Spring MVC's `RequestMappingHandlerMapping` ranks a literal
path segment above a path-variable segment, so `search` resolves to `searchUsers()` and never
falls through to `publicProfile()` (which would fail `long` conversion).

## Controller Structure

- Rename `ProfileController` → `UserController`.
- Fold `UsersController.searchUsers()` and its follow logic into `UserController`.
- **Delete** `UsersController.java`.
- Constructor injects the union of both controllers' collaborators. Both already share
  `UserService` and `UserFollowService`; the profile-side services
  (`reviewService`, `carFavoriteService`, `reviewLikeService`, etc.) are carried over unchanged.
- Inner view-model classes (`ProfileData`, review/connection card classes) stay inline.
- **One `toggleFollow`** method handling `POST /users/{id}/follow`:
  - Reads `back` request param. `back=search` → rebuild `/users/search?q=…&page=…`
    (reusing the existing `buildSearchRedirect(query, page)` logic); any other/absent value →
    redirect to `/users/{id}`.
  - Keeps the specific `catch (SelfFollowException)` (logged as WARN), not a generic catch.
  - Redirects to `/login` when unauthenticated (existing behavior).
- `profileBasePath` is computed as `/user` (own profile) or `/users/{id}` (other profile).
  All internal redirect targets that currently point at `/profile` or `/profiles/{id}`
  are updated to the new paths.

## Security (`WebAuthConfig`)

- **Public GET block:** replace `antMatcher(GET, "/profiles/*")` with
  `antMatcher(GET, "/users/*")`. This keeps `/users/search` and `/users/{id}` public.
  `/user` is a distinct path and intentionally stays *out* of the public block.
- **Authenticated block:** `GET /profile` → `GET /user`; `POST /profile` → `POST /user`;
  `POST /profile/language` → `POST /user/language`; remove `POST /profiles/*/follow`; keep the
  single `POST /users/*/follow`.
- `GET /users/*` (public) and `POST /users/*/follow` (authenticated) do not collide — they are
  matched on different HTTP methods.

## Views / JSP & Tags

Links already routed through `${profileBasePath}` move automatically once the controller sets
the new base. Hardcoded `/profile` / `/profiles` references are updated in:

- `nav.tag`
- `edit-profile-modal.tag`
- `review-author-link.tag`
- `reviews-feed.tag`
- `profile.jsp`
- `profile-connections-modal.tag`
- `review-form.jsp`
- `car-review.jsp`
- `users-search.jsp`

Follow forms:

- **Search results** (`users-search.jsp`): post to `/users/${u.id}/follow` with a hidden
  `back=search` field, plus the existing `q` and `page` hidden fields.
- **Profile hero** (`profile.jsp`) and **connections modal** (`profile-connections-modal.tag`):
  post to `/users/${id}/follow` with the default `back` (→ profile).
- Model attributes `profileFollowUrl` and `connectionFollowUrl` are updated to the new path.

## Testing / Verification

This project's automated tests cover only the persistence and service layers (per CLAUDE.md);
there is no web-layer test harness, and this refactor changes no service/DAO/model code.
Verification is therefore manual:

1. `mvn clean install` (build all modules — confirms no compile breakage).
2. `mvn -pl webapp jetty:run`.
3. Exercise the flows:
   - View own profile at `/user` (anonymous request to `/user` redirects to `/login`).
   - View another user at `/users/{id}` (works while logged out).
   - `/users/{id}` for your own id renders your own profile (ownProfile branch).
   - Search users, follow/unfollow from results → returns to results with `q`/`page` preserved.
   - Follow/unfollow from a profile hero → returns to that profile.
   - Open the followers/following modal, follow/unfollow from it.
   - Edit profile (`POST /user`) and change language (`POST /user/language`).
   - Confirm self-follow attempts are silently ignored (no error page).

## Out of Scope

- No service, persistence, or model changes.
- No extraction of inner view-model classes into separate files (explicitly deferred).
- No backward-compatible redirect routes for the old URLs.
- No new automated web-layer tests (no harness exists; not introduced here).
