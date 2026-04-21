# Branch Review Checklist

Branch: `joaco/carrousel`

## Commits Checked

Compared to `origin/develop`, this branch adds:

- `5d993e0` - `feat: gallery images, review threads, and carousel UI`
- `18e928a` - `feat: auth-required UX, login return URL, profile liked replies`

There are also local review fixes that should be included in the final branch state:

- `webapp/src/main/webapp/WEB-INF/tags/car-card.tag`: context-path-safe admin modal gallery URLs.
- `webapp/src/main/webapp/WEB-INF/tags/review-like-button.tag`: anonymous like fallback no longer submits to protected POST endpoints.
- `webapp/src/main/webapp/css/components.css`: link fallback keeps like-button styling.

Compared to `origin/main`, this branch also includes inherited `develop` work: admin panel, Spring Security, profile, follows, favorites, edit/delete reviews, backend validations, and custom errors. Test those as regression smoke tests.

## Before Manual Testing

Run the automated suite:

```bash
mvn test
```

Use a fresh or migrated database. This branch changes schema around `car_images`, `car_request_images`, `review_replies`, `review_likes`, and `review_reply_likes`. Because `schema.sql` uses `CREATE TABLE IF NOT EXISTS`, an old local DB may not pick up all structural changes cleanly. For safest testing, recreate the local DB or apply the schema changes manually.

Start the app:

```bash
mvn -pl webapp jetty:run
```

Check both paths if needed:

- `http://localhost:8080/`
- `http://localhost:8080/webapp/`

Use at least these browser states:

- Anonymous user.
- Normal user A.
- Normal user B.
- Admin user, with `users.role = 'admin'` if you need to promote one locally.

## 1. Multi-Image Car Creation And Gallery

Files to keep in mind:

- `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/CarController.java`
- `webapp/src/main/webapp/WEB-INF/tags/car-image-carousel.tag`
- `webapp/src/main/webapp/js/car-image-carousel.js`

Test:

1. Log in as a normal user.
2. Go to `/cars`.
3. Open `Agregar auto`.
4. Upload 1 valid image, submit, and confirm the request succeeds.
5. Repeat with 2 to 5 valid images.
6. Try 6 images. Expected: validation error, no request created.
7. Try unsupported file type. Expected: validation error.
8. Try an image over 10 MB. Expected: validation error.
9. Try no image. Expected: image-required validation error.
10. As admin, go to `/admin`, open the pending request, and confirm all uploaded images preview in the modal.
11. Accept the request.
12. Open the created car review page. Expected: carousel appears with all images.
13. Use next/previous buttons and thumbnails. Expected: image and counter update correctly.
14. With JavaScript disabled, reload the review page. Expected: first image still renders, page remains usable.

Also test direct image endpoints:

- `/cars/{carId}/image`
- `/cars/{carId}/images/{imageId}`
- `/admin/requests/{requestId}/image`
- `/admin/requests/{requestId}/images/{imageId}`

Expected: valid images load; invalid IDs return not found.

## 2. Admin Pending Request Carousel Preview

This includes the local review fix in `webapp/src/main/webapp/WEB-INF/tags/car-card.tag`.

Test especially under context path `/webapp`:

1. Run/open the app under `/webapp`.
2. As admin, open `/webapp/admin`.
3. Click a pending request with multiple images.
4. Confirm every modal preview image URL starts with `/webapp/admin/...`, not raw `/admin/...`.
5. Confirm images do not 404 in the browser network tab.
6. Accept and reject request flows still redirect back to admin.

## 3. Review Threads And Replies

Files to keep in mind:

- `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/CarReviewController.java`
- `webapp/src/main/webapp/WEB-INF/tags/reviews-feed.tag`
- `webapp/src/main/webapp/js/reactions.js`

Test:

1. As user A, create a review on a car.
2. As user B, open the same car review page.
3. Reply to user A's review.
4. Confirm the reply appears under the correct review.
5. Submit a blank reply. Expected: validation error, no reply created.
6. Refresh the page. Expected: reply persists.
7. Check `/reviews/feed?carId={id}` if the UI uses progressive loading. Expected: fragment includes replies and like state.
8. Disable JavaScript and submit a reply. Expected: normal form POST works and redirects back to the review anchor.

## 4. Review Likes And Reply Likes

Files to keep in mind:

- `services/src/main/java/ar/edu/itba/paw/services/ReviewLikeServiceImpl.java`
- `persistence/src/main/java/ar/edu/itba/paw/persistence/ReviewLikeJdbcDao.java`
- `webapp/src/main/webapp/WEB-INF/tags/review-like-button.tag`

Test:

1. As user A, like a review.
2. Confirm count increments and button becomes active.
3. Click again. Expected: count decrements and active state clears.
4. Refresh. Expected: persisted state is correct.
5. Repeat for a reply like.
6. Log in as user B and confirm user B has independent like state.
7. Like the same review/reply as user B. Expected: count reflects both users.
8. Disable JavaScript and like/unlike via form submit. Expected: redirect returns to the correct review/reply anchor.
9. As anonymous user with JavaScript enabled, click like. Expected: auth-required modal opens.
10. As anonymous user with JavaScript disabled, click like. Expected: navigates to login, does not POST to `/reviews/.../like`, and does not end in 405.

## 5. Auth-Required Modal And Login Return

Files to keep in mind:

- `webapp/src/main/webapp/js/auth-required-modal.js`
- `webapp/src/main/java/ar/edu/itba/paw/webapp/auth/LoginRedirectUtils.java`
- `webapp/src/main/java/ar/edu/itba/paw/webapp/config/WebAuthConfig.java`

Test anonymous flows:

1. Click `Agregar auto` from `/cars`.
2. Expected: auth modal says login is required.
3. Click login, authenticate.
4. Expected: return to `/cars` and create-car modal resumes/open-highlights.
5. Repeat for `publicar una reseña`.
6. Repeat for replying to a review.
7. Repeat for liking a review/reply.
8. Repeat for following a user.
9. Visit `/login?redirect=/cars&intent=create-car` while already logged in. Expected: redirects back to `/cars?intent=create-car`.
10. Visit `/login?redirect=https://evil.example`. Expected: external redirect is ignored.
11. Try bad intent values like `../../x` or very long strings. Expected: ignored.

## 6. Profile Liked Activity

Files to keep in mind:

- `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/ProfileController.java`
- `webapp/src/main/webapp/WEB-INF/jsp/profile.jsp`
- `webapp/src/main/webapp/WEB-INF/tags/profile-liked-reply-card.tag`

Test:

1. As user A, like one review and one reply.
2. Open `/profile`.
3. Expected: `Reviews likeadas` section shows both liked reviews and liked replies.
4. Click a liked review card. Expected: navigates to its car review page.
5. Click a liked reply card. Expected: navigates to the parent review anchor.
6. Unlike the review/reply from profile.
7. Refresh profile. Expected: removed liked items disappear or counts update.
8. Open user A's public profile as user B. Expected: liked activity renders without exposing edit controls for user A's reviews.

## 7. Profile Connections And Follow Auth

Files to keep in mind:

- `webapp/src/main/webapp/WEB-INF/tags/profile-connections-modal.tag`
- `webapp/src/main/webapp/js/profile.js`

Test:

1. As user A, open user B's profile.
2. Follow user B.
3. Expected: follower/following counts update after redirect.
4. Open followers/following modal. Expected: correct users listed.
5. Unfollow. Expected: counts update.
6. Try following yourself by direct POST if possible. Expected: redirects safely, no self-follow created.
7. As anonymous user, click follow. Expected: auth modal/login flow, not a broken POST.

## 8. Regression Smoke Tests From Inherited Develop Work

Because this branch includes develop commits when compared to `main`, also smoke test:

1. Admin route authorization: anonymous and normal users cannot access `/admin`; admin can.
2. Edit/delete review ownership: user can edit/delete own review, cannot edit/delete another user's review.
3. Car favorites: authenticated user can favorite/unfavorite cars and see favorites on profile.
4. Profile edit modal and profile image preview still work.
5. Catalog filtering/search still works with `/cars` and `/cars/content`.
6. Custom error pages render for bad/unknown routes.
7. Spring Security login/logout/register still work.
8. CSRF-protected forms reject invalid/missing CSRF when submitted directly.

## Final Checks

Run:

```bash
mvn test
mvn -pl webapp test
```

Then do one full browser pass with JavaScript enabled and one with JavaScript disabled for:

- `/cars`
- `/reviews?carId={id}`
- `/profile`
- `/profiles/{userId}`
- `/admin`

Highest-risk areas:

- Old databases missing new schema.
- Anonymous no-JavaScript auth fallbacks.
- Context-path deployments under `/webapp`.
- Multi-image admin previews.
