# Pre-Demo QA Checklist

Comprehensive, guided manual QA for the entire webapp. Run sections **in order**: each section assumes the previous one passed, so test data builds up naturally. The expected behavior is always a server-rendered flow — form POST, controller/service work, redirect, then JSP renders the updated page.

## Setup

Before starting:

- [ ] App starts with `mvn -pl webapp jetty:run` and is reachable at `http://localhost:8080/webapp`.
- [ ] DB env vars (`DB_URL`, `DB_USERNAME`, `DB_PASSWORD`) point to a clean-ish PostgreSQL.
- [ ] Have ready: 2 regular user accounts, 1 admin account, 1 moderator account (or create as you go).
- [ ] Open DevTools Console + Network tab; keep them open the whole session.
- [ ] Test in at least Chrome + Firefox. Try one full pass on mobile viewport.

Throughout the run, **after each successful POST**:

- [ ] URL changes to a GET (no form re-POST on refresh).
- [ ] Reloading the page does **not** resubmit (PRG pattern).
- [ ] No JS console errors.
- [ ] Network shows a document/form navigation, **not** fetch/XHR for mutating actions.

---

## 1. Anonymous Browsing (logged out)

Public surface — must work without auth.

- [ ] `GET /` landing page renders, featured cars visible.
- [ ] `GET /cars` renders catalog, pagination works.
- [ ] `GET /activity` renders with only the **Latest** tab (no Following/Favorites).
- [ ] `GET /cars/recommend` wizard renders.
- [ ] `GET /reviews/car/{carId}` for an existing car renders reviews + car details.
- [ ] `GET /profiles/{userId}` for an existing user renders public profile.
- [ ] `GET /login` and `GET /register` render.

Anonymous gating:

- [ ] Click favorite on a car → redirected to login (with `?redirect=` + `?intent=`).
- [ ] Click like on a review → redirected to login (or auth-required modal opens).
- [ ] Click Follow on a profile → redirected to login with intent preserved.
- [ ] Try to submit a reply form → blocked / redirected to login.
- [ ] Direct-navigate to `/profile` → redirected to `/login`.
- [ ] Direct-navigate to `/admin` → 403 or login redirect.
- [ ] Direct-navigate to `/cars/new` → redirected to login.

Error pages:

- [ ] `GET /cars/99999999` → 404 page.
- [ ] `GET /profiles/99999999` → 404 page.
- [ ] `GET /reviews/car/99999999` → 404 page.
- [ ] `GET /error/403`, `/error/404`, `/error/500` render directly.

---

## 2. Registration

- [ ] `GET /register` renders.
- [ ] Submit empty form → inline client validation, no POST sent.
- [ ] Submit invalid email (`foo@`, `foo`, `@bar.com`) → inline error.
- [ ] Submit username with disallowed chars (`test@user`, with spaces) → error.
- [ ] Submit password < 8 chars → error.
- [ ] Submit password > 72 chars → error.
- [ ] Password ≠ confirm password → error.
- [ ] Submit with an already-registered email → error message, **password field NOT repopulated**, email/username are repopulated.
- [ ] Submit with an already-registered username → error message.
- [ ] Submit a valid form → auto-login + redirect to `/`. User is logged in (nav shows profile).
- [ ] `register` form includes CSRF hidden input.
- [ ] If already logged in, `GET /register` redirects to `/`.

---

## 3. Login / Logout

- [ ] `GET /login` renders.
- [ ] Submit with wrong password → error message, email repopulated.
- [ ] Submit with non-existent email → error message.
- [ ] Submit empty form → inline client validation.
- [ ] Submit valid credentials → redirect to `/` (or to `?redirect=` target).
- [ ] Login from a deep link: anonymous-click favorite on `/cars` → login page has `?redirect=/cars&intent=...` → after login, lands back on `/cars`.
- [ ] **Open-redirect check**: manually visit `/login?redirect=http://evil.com` → after login, **must NOT** navigate to evil.com; falls back to safe default.
- [ ] **Open-redirect check**: `/login?redirect=//evil.com` → blocked.
- [ ] Check "remember me" → close browser tab → reopen app → still logged in.
- [ ] Logout button opens **confirmation modal**.
- [ ] Cancel in modal → no logout.
- [ ] Confirm in modal → POST `/logout`, redirect to `/login?logout`, session invalidated, JSESSIONID cleared.
- [ ] After logout, visiting `/profile` redirects to login.
- [ ] Logout form includes CSRF.

---

## 4. Car Catalog (`/cars`)

### 4.1 List + Pagination

- [ ] Default page (no params) renders.
- [ ] `?page=2` navigates correctly. Prev/Next buttons work.
- [ ] First page: Prev disabled / hidden. Last page: Next disabled / hidden.
- [ ] `?page=0`, `?page=-1`, `?page=999999` handled gracefully (redirected / clamped, not 500).
- [ ] Pagination links are **normal navigation**, not fragment replacement (check Network tab).

### 4.2 Toolbar Filters

- [ ] Type a query, press Enter → GET `/cars?q=...`, results filter, query stays in input.
- [ ] Select Brand → results filter, brand stays selected after reload.
- [ ] Select Body type → same.
- [ ] Change Sort (rating asc / desc / default) → results re-order, sort persists.
- [ ] Combine query + brand + body + sort → all params preserved in URL and across pagination.

### 4.3 Advanced Filters Panel

- [ ] Open advanced filters → panel expands.
- [ ] Set HP min/max, max-speed min, consumption max, airbags min.
- [ ] Set transmission (single) and fuel types (multi-select).
- [ ] Set price USD min/max.
- [ ] Set year min/max.
- [ ] Submit → results match, all selected values stay selected after reload.
- [ ] **Invalid range**: min year > max year → client-side validation blocks submit.
- [ ] **Invalid range**: min price > max price → blocks submit.
- [ ] **Clear** button → GET `/cars` (no filter params), all controls reset.

### 4.4 Search edge cases

- [ ] Query with quotes: `"Toyota"` → no SQL error, sensible results.
- [ ] Query with SQL injection attempt: `'; DROP TABLE cars; --` → safe, empty results, table still alive.
- [ ] Query with XSS: `<script>alert(1)</script>` → displayed escaped, no alert.
- [ ] Single-char query → falls back to LIKE path, no error.
- [ ] Empty query (just spaces) → handled, no error.
- [ ] No matches → empty-state message shown.

### 4.5 Favorites

- [ ] Click favorite on a car card (catalog) → normal navigation, heart fills.
- [ ] Reload page → favorite state persists.
- [ ] Click again to unfavorite → state updates.
- [ ] Click favorite on car review page → same behavior.
- [ ] Click favorite on landing/featured cars → same behavior.
- [ ] Profile → Favorites tab lists the favorited car.

---

## 5. Create Car Request (`/cars/new`)

- [ ] As logged-in user, `GET /cars/new` renders.
- [ ] Submit empty form → inline errors on all required fields, no POST.
- [ ] Required: brand, body type, model, year.
- [ ] Description > 1500 chars → error.
- [ ] Numeric fields (HP, airbags, consumption, max speed, price): non-numeric input rejected.
- [ ] Year: out-of-range (e.g., 1500, 3000) handled.

### 5.1 Image upload

- [ ] Drag-and-drop 1 image → preview appears.
- [ ] Upload 5 images → all 5 previews appear, carousel/thumbnails work.
- [ ] Try 6th image → blocked with error.
- [ ] Upload non-image file (`.pdf`, `.txt`) → rejected.
- [ ] Upload `.gif` / `.bmp` / `.svg` → rejected (only JPEG/PNG/WebP allowed).
- [ ] Upload > 10MB image → rejected.
- [ ] Remove an image from preview → it's gone, others remain.
- [ ] Carousel prev/next on preview works.

### 5.2 Inline Brand / Body Type request

- [ ] Click "Request new brand" → modal opens.
- [ ] Submit with empty name → validation error.
- [ ] Submit valid brand request → POST `/brand-requests`, redirect to `/cars?submitted=brand`, success toast.
- [ ] Same for body type.

### 5.3 Successful submit

- [ ] Submit valid form → POST `/cars`, redirect to `/cars?submitted=true`, toast appears.
- [ ] Refresh after redirect → no resubmit.
- [ ] CSRF token present on form.
- [ ] **Double-submit check**: click submit twice quickly → only one request fires.

---

## 6. Reviews

### 6.1 Reviews page (`/reviews/car/{carId}`)

- [ ] Car detail header renders (brand, model, year, average rating, image carousel).
- [ ] Image carousel prev/next + thumbnail clicks work.
- [ ] Year-variants dropdown (if present) navigates.
- [ ] Review cards show: author, rating stars, title, body, relative date, ownership badge, mileage, like count, tag chips.
- [ ] Sort by newest / rating asc / rating desc → URL has `?sort=...`, reviews re-order.
- [ ] Pagination preserves `?sort=`.
- [ ] Empty-state for a car with no reviews.

### 6.2 Create review (`/reviews/new?carId=...`)

- [ ] As anonymous → redirected to login.
- [ ] As logged-in user, form renders with car context.
- [ ] Star rating: click each star, half-star (click left edge) works.
- [ ] Required: rating, title, body, mileage.
- [ ] Title > 200 chars → error.
- [ ] Body > 2000 chars → error.
- [ ] Mileage < 0 or > 2,000,000 → error.
- [ ] Mileage non-numeric → error.
- [ ] Tags: click chips to toggle, multi-select.
- [ ] Recommend checkbox toggles.
- [ ] Ownership radios (None / Current / Former).
- [ ] Submit valid → redirect to `/reviews/car/{carId}?reviewCreated=1` (or similar), toast appears, new review visible.

### 6.3 Edit review

- [ ] As owner, action menu → Edit → form pre-fills with current values.
- [ ] As non-owner, non-admin → no edit option; if URL-hacked to edit page → 403/forbidden.
- [ ] As admin → can edit any review.
- [ ] Submit edit → redirect back, changes visible.
- [ ] `?redirect=/profiles/{id}` honored after edit.

### 6.4 Delete review

- [ ] As owner, action menu → Delete → **confirmation modal**.
- [ ] Cancel → modal closes, review untouched.
- [ ] Confirm → POST `/reviews/{id}/delete`, redirect to reviews page (or `?redirect` target), review gone.
- [ ] As non-owner, non-admin → no Delete option.
- [ ] From own profile reviews tab → same delete flow works.

### 6.5 Like review

- [ ] As logged-in user, click like → POST, redirect to `/reviews/car/{carId}#review-{id}`, page scrolls to that review, like count +1, heart filled.
- [ ] Reload → like state persists.
- [ ] Click again to unlike → count -1.
- [ ] As anonymous → auth-required modal or login redirect with `?intent=like-review-{id}`.

### 6.6 Replies

- [ ] As logged-in user, click Reply → reply form appears.
- [ ] Submit blank → client-side validation blocks, **no AJAX request**.
- [ ] Submit reply > 1000 chars → error.
- [ ] Submit valid reply → POST, redirect to `#review-{id}`, reply rendered by JSP.
- [ ] Reload → reply persists.
- [ ] Like a reply → POST `/reviews/replies/{id}/like`, redirect to anchor, count updates.

### 6.7 Admin hide review

- [ ] As admin, on someone else's review → Hide button visible in action menu.
- [ ] As non-admin → Hide button NOT visible. URL-hack `POST /reviews/{id}/hide` → 403.
- [ ] Hide modal opens.
- [ ] Empty reason → inline client validation.
- [ ] Reason < 10 chars → blocked.
- [ ] Reason > 600 chars → blocked.
- [ ] Valid reason → POST, redirect, review disappears from page.

---

## 7. Profile

### 7.1 Own profile (`/profile`)

- [ ] Renders avatar (initials), display name, review count, followers count, following count.
- [ ] Tabs: Reviews / Favorites / Liked.
- [ ] Switching tab updates URL (`?tab=...`), content swaps, pagination resets to page 1.
- [ ] Pagination inside each tab works independently.
- [ ] Reload with `?tab=favorites` → that tab is active.
- [ ] Edit profile modal: change display name.
  - [ ] Empty name → error.
  - [ ] Invalid pattern → error.
  - [ ] Save → POST, redirect, new name reflected.
- [ ] Language selector (Spanish / English): submit → page reloads in chosen language, persists across logout/login.
- [ ] Logout button → confirmation modal → POST `/logout`.
- [ ] "Request moderator role" button (if eligible): opens modal.

### 7.2 Public profile (`/profiles/{userId}`)

- [ ] Other user's profile renders without edit / logout / language controls.
- [ ] Reviews tab visible; Favorites / Liked tabs **not** visible.
- [ ] Follow button visible (when not the same user).
- [ ] Click Follow → POST, redirect back, button now "Following", follower count +1.
- [ ] Click Following → POST, redirect, button now "Follow", count -1.
- [ ] Reload → state persists.

### 7.3 Followers / Following modals

- [ ] Click followers count → modal opens with paginated list.
- [ ] Each row has Follow/Following button.
- [ ] Click Follow inside modal → POST/redirect to that user's profile (no instant DOM update expected).
- [ ] Click Following count → analogous modal.
- [ ] Pagination inside modal works.
- [ ] Modal closes via X, backdrop click, and Escape key.
- [ ] **Anonymous**: trying to follow from modal → login redirect.

---

## 8. Connections / Follow edge cases

- [ ] Cannot follow self (button not shown on own profile, or POST is rejected).
- [ ] Following count and followers count match what's listed in modals.
- [ ] Anonymous click on Follow → login with `?intent=follow-profile-{id}`, after login lands back and ideally follow is queued or repeats action; if not auto-followed, follow state is correct on arrival.

---

## 9. Activity Feed (`/activity`)

- [ ] As anonymous → only Latest tab.
- [ ] As logged-in → Latest / Following / Favorites tabs visible.
- [ ] Latest: shows all recent reviews paginated.
- [ ] Following (auth): shows reviews from users you follow; empty state if following nobody.
- [ ] Favorites (auth): shows reviews on cars you favorited; empty state if no favorites.
- [ ] Tab switching preserves correctness; pagination resets.
- [ ] Click an activity card → navigates to that car's reviews page.

---

## 10. Recommendation Wizard

- [ ] `GET /cars/recommend` renders.
- [ ] Step through questions; counter updates if multi-step.
- [ ] Validation: required-question check before advancing.
- [ ] Submit → `POST /cars/recommend/results` (or GET), redirect to results.
- [ ] Results page lists matching cars with reasons / scores.
- [ ] Click result → navigates to that car's reviews page.
- [ ] Zero-match scenario → graceful empty state, no 500.

---

## 11. Catalog Requests (user-initiated)

- [ ] As logged-in user, brand request form (from `/cars/new` modal or dedicated page) submits → toast, request appears in admin panel.
- [ ] Body type request → same.
- [ ] Moderator role request form (from profile):
  - [ ] Required fields: motivation, bio, justification — empty → errors.
  - [ ] Submit valid → toast, request created.
  - [ ] Try to submit a second moderator request while one is pending → error / blocked (`PendingAdminRequestExistsException`).

---

## 12. Admin Panel (`/admin`)

Test as admin. Then re-test all admin URLs as a regular user → must be 403.

### 12.1 Dashboard

- [ ] `/admin` renders with pending counts (cars, brands, body types, moderators).
- [ ] Tabs: Cars / Brands / Body Types / Moderators. Each lists pending items.
- [ ] `/admin?tab=brands`, `?tab=body-types`, `?tab=moderators` deep links work.

### 12.2 Car request review

- [ ] Open a pending car request card → review page renders with all fields + images.
- [ ] Edit any field, then Accept → POST, redirect to `/admin?carAccepted=1`, toast, request gone, car visible at `/cars`.
- [ ] Reject another request → POST, redirect with toast, request gone, no car created.
- [ ] Add images on accept (validates same as create).
- [ ] CSRF on both actions.

### 12.3 Existing car edit / delete (admin)

- [ ] Edit an approved car → form pre-fills → submit → redirect to `/reviews/car/{id}`, changes reflected.
- [ ] Delete car → confirmation modal → POST → redirect, car gone from catalog.
- [ ] Reviews tied to a deleted car handled gracefully (no 500 anywhere).

### 12.4 Brand requests

- [ ] Open brand request card → modal shows name, submitter, comments.
- [ ] Accept (optionally overriding name) → POST, redirect, toast, brand available in catalog dropdown.
- [ ] Reject another → POST, redirect, toast, brand NOT created.

### 12.5 Body-type requests

- [ ] Same accept/reject flow as brand requests.

### 12.6 Moderator/admin requests

- [ ] `/admin?tab=moderators` → open card → modal shows motivation, bio, justification, submitter.
- [ ] Accept → POST, redirect, toast; the user now has admin/moderator role (verify by logging in as them).
- [ ] Reject another → POST, redirect, toast; user role unchanged.
- [ ] CSRF on both actions.

### 12.7 Admin authorization

- [ ] As regular user, direct-navigate `/admin` → 403.
- [ ] As regular user, POST to `/admin/requests/{id}/accept` (via curl or DevTools) with CSRF → 403.
- [ ] As admin, attempt to act on your own car request (if applicable) → handled.

---

## 13. Images & Caching

- [ ] Car image renders via `<img>` on catalog, reviews, admin pages.
- [ ] First request: 200 with `ETag` and `Cache-Control: public, max-age=3600, must-revalidate`.
- [ ] Second request (Network → Disable cache OFF): 304 Not Modified.
- [ ] No image stored inline in HTML (no `data:image/...;base64`).
- [ ] As non-admin, `POST /cars/{id}/image` → 403.

---

## 14. Forms / CSRF / Double-submit (cross-cutting)

For every mutating form discovered above, spot-check:

- [ ] Hidden CSRF input present (`_csrf` parameter name and token).
- [ ] Removing the CSRF token via DevTools and submitting → 403.
- [ ] Double-clicking submit → only one request goes through (`data-submit-lock`).
- [ ] After successful POST, hitting browser **Refresh** does NOT resubmit.
- [ ] Back button after POST does not cause weird state.

---

## 15. Authorization Matrix (spot-check by URL)

For each row, try as: anonymous / regular-user / owner / admin. Expected = ✓ allowed, ✗ denied.

| Action                         | Anon | User | Owner | Admin |
|--------------------------------|------|------|-------|-------|
| `GET /cars`                    | ✓    | ✓    | ✓     | ✓     |
| `GET /reviews/car/{id}`        | ✓    | ✓    | ✓     | ✓     |
| `GET /profile`                 | ✗    | ✓    | ✓     | ✓     |
| `GET /cars/new`                | ✗    | ✓    | ✓     | ✓     |
| `POST /reviews/{id}/like`      | ✗    | ✓    | ✓     | ✓     |
| `POST /reviews/{id}/delete`    | ✗    | ✗    | ✓     | ✓     |
| `GET /reviews/{id}/edit`       | ✗    | ✗    | ✓     | ✓     |
| `POST /reviews/{id}/hide`      | ✗    | ✗    | ✗     | ✓     |
| `GET /admin`                   | ✗    | ✗    | ✗     | ✓     |
| `POST /cars/{id}/image`        | ✗    | ✗    | ✗     | ✓     |

---

## 16. i18n

- [ ] Set Spanish in profile → nav, buttons, labels, validation errors, toasts in Spanish.
- [ ] Set English → all UI in English.
- [ ] Dates / relative times localized.
- [ ] Error pages (404, 403, 500) localized.
- [ ] **No hardcoded copy**: skim each page for stray Spanish/English text that doesn't switch.

---

## 17. Accessibility & Keyboard

- [ ] Tab through every page — focus visible, logical order.
- [ ] Forms: every input has a label; errors announced via `aria-invalid` / `aria-describedby`.
- [ ] Modals: focus trapped inside, Escape closes, backdrop click closes.
- [ ] Action menus: arrow-key navigation, Enter to activate.
- [ ] All `<img>` tags have alt text (or `aria-hidden` if decorative).
- [ ] Color contrast on text passes WCAG AA in dark theme.
- [ ] Native number-input spinners hidden globally (check on year/price/HP).

---

## 18. Mobile / Responsive

- [ ] Toggle DevTools mobile viewport.
- [ ] Nav collapses appropriately.
- [ ] `/cars` filters collapse into a panel; toolbar usable.
- [ ] Modals: full-screen feel, scrollable, close affordances visible.
- [ ] Tap targets ≥ 44×44px (favorite, like, action menus).
- [ ] Image carousels swipeable.
- [ ] Form inputs use appropriate keyboards (email, numeric) on real mobile.

---

## 19. DevTools Final Sweep

While re-running favorite, like, reply, delete, hide, follow, admin accept/reject, filters, sort, pagination:

- [ ] **Console**: zero errors, zero missing-script 404s (`reactions.js`, `review-replies.js`, `review-delete.js`, etc.).
- [ ] **Network**: mutating actions show as document/form POST navigations — **no** `fetch` or `XHR`.
- [ ] **Network**: image responses have ETag + Cache-Control.
- [ ] **Refresh** after each action → state stays correct (DB is the source of truth).
- [ ] No 500s anywhere across the whole session.

---

## 20. End-to-End Smoke Path (run last, full demo rehearsal)

Pretend you are the demo presenter. Run this script start-to-finish without notes:

1. [ ] Register a brand-new user → land on home.
2. [ ] Browse `/cars`, apply a filter, paginate.
3. [ ] Click a car → reviews page renders.
4. [ ] Favorite the car.
5. [ ] Write a review → appears on the reviews page.
6. [ ] Reply to someone else's review.
7. [ ] Like a review and a reply.
8. [ ] Open `/cars/new` → submit a car request with images (and a brand request inline).
9. [ ] Visit another user's profile → follow them.
10. [ ] Open `/activity` → see Following / Favorites populated.
11. [ ] Edit your profile name + switch language.
12. [ ] Log out.
13. [ ] Log in as admin → approve the car request → reject one brand request → accept a moderator request.
14. [ ] Log in as the user whose moderator request was accepted → confirm new role.
15. [ ] Log out, refresh `/`, confirm everything looks clean.

If all 20 sections pass: ship it. If anything fails: fix and re-run that section + the smoke path.
