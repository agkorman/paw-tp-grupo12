# JavaScript Context

This document describes every file under `webapp/src/main/webapp/js`, what behavior it owns, and where that behavior is wired into the JSP/tag markup.

## Loading Model

Scripts are included from JSPs through `WEB-INF/tags/script.tag`, which emits a `<script src="...">` tag and optionally adds `defer`.

Most files are self-contained IIFEs. They do not use a bundler. Shared browser globals are intentionally small:

- `window.PawModal` from `shared/modal-utils.js`.
- `window.PawToast` from `shared/toast.js`.
- `window.PawActionMenus` from `shared/action-menu.js`.

The JavaScript is mostly progressive enhancement. Forms and links still have normal server-rendered fallbacks, while scripts add validation, modal handling, filter/pagination fragments, and accessibility behavior.

## Folder Layout

The files under `webapp/src/main/webapp/js` are grouped by responsibility:

- `shared/`: cross-page utilities and shared enhancements.
- `auth/`: login/register validation and login-required modal behavior.
- `cars/`: car forms, car catalog filters, car image carousel, and car admin actions.
- `catalog/`: user-facing brand/body-type request modals and validation.
- `reviews/`: review form, review tags, replies, moderation, deletion, and anchor highlighting.
- `profile/`: profile page behavior and profile-side moderator application modal.
- `admin/`: admin dashboard review modals.
- `activity/`: activity page tabs and preview panel behavior.
- `recommendations/`: recommendation wizard behavior.

## Direct JSP Includes

| Page | Scripts |
| --- | --- |
| `WEB-INF/jsp/landing.jsp` | `shared/modal-utils.js`, `auth/auth-required-modal.js` |
| `WEB-INF/jsp/cars.jsp` | `cars/cars-filters-panel.js`, `shared/modal-utils.js`, `auth/auth-required-modal.js`, `shared/form-submit-lock.js`, `shared/toast.js` |
| `WEB-INF/jsp/car-review.jsp` | `shared/action-menu.js`, `cars/car-image-carousel.js`, `reviews/review-anchor-highlight.js`, `reviews/review-tag-chips.js`, `shared/modal-utils.js`, `auth/auth-required-modal.js`, `shared/toast.js`, plus admin-only `cars/car-admin.js`, `reviews/review-moderation.js`, `shared/confirmation-modal.js`, and always `shared/form-submit-lock.js` |
| `WEB-INF/jsp/car-form.jsp` | `cars/car-form.js`, admin/non-admin catalog request helpers `shared/modal-utils.js`, `catalog/catalog-request-modal.js`, `catalog/catalog-request-validation.js`, plus `shared/form-submit-lock.js`, `shared/toast.js` |
| `WEB-INF/jsp/review-form.jsp` | `reviews/review-form.js`, `reviews/review-tag-chips.js`, `shared/form-submit-lock.js` |
| `WEB-INF/jsp/profile.jsp` | `shared/action-menu.js`, `shared/modal-utils.js`, `auth/auth-required-modal.js`, `shared/confirmation-modal.js`, `shared/form-submit-lock.js`, `profile/profile.js`, optional `profile/moderator-application-modal.js`, `shared/toast.js` |
| `WEB-INF/jsp/activity.jsp` | `activity/activity.js` |
| `WEB-INF/jsp/admin.jsp` | `shared/modal-utils.js`, `admin/admin-catalog-request-modal.js`, `admin/moderator-application-review-modal.js`, `shared/form-submit-lock.js`, `shared/toast.js` |
| `WEB-INF/jsp/login.jsp` | `auth/auth-form.js`, `shared/form-submit-lock.js`, `shared/toast.js` |
| `WEB-INF/jsp/register.jsp` | `auth/auth-form.js`, `shared/form-submit-lock.js`, `shared/toast.js` |
| `WEB-INF/jsp/recommend.jsp` | `recommendations/recommend-wizard.js` |
| `WEB-INF/jsp/recommend-results.jsp` | none |

## Shared Utilities

### `shared/modal-utils.js`

Defines `window.PawModal`, a tiny modal utility used by several feature scripts.

Responsibilities:

- Provides `PawModal.closest(node, predicate)` for delegated DOM traversal.
- Provides `PawModal.createController({ modal, bodyClass, modalSelector })`.
- Controller `open(trigger, focusTarget)` removes `hidden`, adds a body lock class, stores the trigger, and can focus an initial field.
- Controller `close()` hides the modal, removes the body class when no matching modal remains open, and restores focus to the trigger.
- Provides `bindCloseAttr(modal, closeFn)` for elements with `data-close-modal="<modalId>"`.
- Provides `bindEscKey(modal, closeFn)` for Escape close behavior.

Used by:

- `catalog/catalog-request-modal.js`
- `admin/admin-catalog-request-modal.js`
- `auth/auth-required-modal.js`
- Any JSP that includes those features: `landing.jsp`, `cars.jsp`, `car-review.jsp`, `car-form.jsp`, `profile.jsp`, `admin.jsp`

Markup contracts:

- Generic close buttons from `auth-required-modal.tag` use `data-close-modal`.
- Catalog/admin modals pass concrete modal elements into `PawModal.createController`.

### `shared/toast.js`

Defines `window.PawToast`.

Responsibilities:

- Reads the global toast from `WEB-INF/tags/toast.tag` using `#globalToast`.
- Shows success/error toasts through `PawToast.show(message, type, options)`.
- Hides the toast through `PawToast.hide()`.
- Applies accessibility roles: success uses `status`/`polite`, error uses `alert`/`assertive`.
- Supports an initial server-rendered toast through `data-toast-initial-message`, `data-toast-initial-type`, and `data-toast-initial-timeout`.
- Close button is `[data-toast-close]`.

Used by:

- `login.jsp`, `register.jsp`, `cars.jsp`, `car-review.jsp`, `car-form.jsp`, `profile.jsp`, `admin.jsp`
- Server-rendered pages can seed an initial toast through `toast.tag`.

### `shared/form-submit-lock.js`

Prevents accidental double submission for normal server-submitted forms.

Responsibilities:

- Opt-in only: `form[data-submit-lock="true"]`.
- On submit, disables the first submit button and sets `aria-busy="true"`.
- If another submit handler already called `preventDefault()`, it does not lock the button.
- If the button has `data-loading-label`, swaps the button label while submitting.

Used by:

- `car-form.jsp`, `review-form.jsp`, `login.jsp`, `register.jsp`, `cars.jsp`, `car-review.jsp`, `profile.jsp`, `admin.jsp`
- Markup examples include `data-submit-lock="true"` in `car-form.jsp`, `review-form.jsp`, `login.jsp`, and `register.jsp`.

## Authentication And Forms

### `auth/auth-form.js`

Client-side validation for login/register forms.

Responsibilities:

- Installs on `form[data-auth-form]`.
- Disables native browser validation with `form.noValidate = true`.
- Validates required fields, email format, username format, min/max lengths, and register password confirmation.
- Uses localized messages from `data-msg-*` attributes on the form.
- Renders inline `.client-form-error` elements with `role="alert"`.
- Maintains `aria-invalid` and `aria-describedby`.
- Focuses the first invalid field on failed submit.

Used by:

- `WEB-INF/jsp/login.jsp`
- `WEB-INF/jsp/register.jsp`

Markup contracts:

- `login.jsp` has `#loginForm` with `data-auth-form="login"`.
- `register.jsp` has `#registerForm` with `data-auth-form="register"`.
- Register-specific field IDs include `registerUsername`, `registerPassword`, and `registerConfirmPassword`.

### `auth/auth-required-modal.js`

Intercepts unauthenticated actions and opens the login-required modal instead of allowing the click/submit to proceed.

Responsibilities:

- Requires `#authRequiredModal` from `auth-required-modal.tag`.
- Captures clicks and submits in the capture phase for elements marked `data-auth-required="true"`.
- Writes the attempted action label into `[data-auth-required-action]`.
- Builds login links with `redirect` and optional safe `intent` query parameters.
- Supports return/resume highlighting after login via `?intent=...` and `[data-auth-resume-intent]`.
- Adds/removes `modal-open`.
- Uses `PawModal.bindCloseAttr` and `PawModal.bindEscKey`.

Used by:

- `landing.jsp`, `cars.jsp`, `car-review.jsp`, `profile.jsp`
- Tags: `auth-required-modal.tag`, `car-favorite-button.tag`, `review-like-button.tag`, `cars-content.tag`, `reviews-feed.tag`, `profile-connections-modal.tag`

Important activating markup:

- `data-auth-required="true"`
- `data-auth-required-action`
- `data-auth-required-intent`
- `data-auth-return-url`
- `data-auth-resume-intent`

## Cars, Catalog, And Filters

### `cars/car-form.js`

Client-side validation and multi-image preview management for the create/edit car form.

Responsibilities:

- Activates only when `#createCarFormPage` and `#createCarForm` exist.
- Disables native validation.
- Validates required fields, email fields, numeric min/max fields, radio groups, and image upload constraints.
- Image validation mirrors server constraints:
  - Allowed MIME types: `image/jpeg`, `image/png`, `image/webp`.
  - Max image size: 10 MB.
  - Max total images from `data-max-image-count`, defaulting to 5.
- Supports existing images through `data-existing-image-urls` and `data-existing-image-ids`.
- Maintains hidden `retainedImageIds` inputs when an existing image is removed.
- Uses `DataTransfer` when available to accumulate multiple file selections and remove selected files.
- Shows preview carousel, thumbnails, add-more button, remove button, and file status.
- Renders inline validation errors with `aria-invalid` and `aria-describedby`.

Used by:

- `WEB-INF/jsp/car-form.jsp`

Markup contracts:

- Form/page IDs: `createCarFormPage`, `createCarForm`.
- Inputs: `modalCarBrand`, `modalCarBodyType`, `modalCarModel`, `modalCarDescription`, `modalCarHorsepower`, `modalCarAirbagCount`, `modalCarFuelConsumption`, `modalCarMaxSpeed`, `modalCarFile`.
- Preview IDs: `modalCarImagePreview`, `modalCarImagePreviewImg`, `modalCarImagePrev`, `modalCarImageNext`, `modalCarImageRemove`, `modalCarImageCounter`, `modalCarImageThumbnails`.
- Existing-image holder: `modalCarRetainedImageInputs`.

### `cars/car-image-carousel.js`

Image carousel for car detail/listing image galleries.

Responsibilities:

- Activates on `[data-car-image-carousel]`.
- Switches slides with previous/next buttons.
- Switches slides with thumbnail buttons.
- Supports keyboard navigation with ArrowLeft and ArrowRight.
- Updates active thumbnail class and `data-carousel-count`.

Used by:

- `WEB-INF/jsp/car-review.jsp`
- Markup comes from `WEB-INF/tags/car-image-carousel.tag`.

Markup contracts:

- `[data-carousel-slide]`
- `[data-carousel-prev]`
- `[data-carousel-next]`
- `[data-carousel-thumb]`
- `[data-carousel-count]`

### `cars/cars-filters-panel.js`

Advanced slide-out filters panel for the car catalog.

Responsibilities:

- Activates when `#carsFiltersPanel` and `#car-filter-form` exist.
- Opens/closes the panel from `#filtersToggleBtn`, `[data-open-filters-panel]`, `[data-close-filters-panel]`, overlay, and Escape.
- Syncs toolbar search/brand/body filters into hidden panel fields before opening.
- Handles chip and segmented controls through `[data-filter-target]`.
- Supports multi-select filters like fuel type.
- Manages single range sliders for consumption and max speed.
- Hides/disables consumption filter when electric-only fuel filter is selected.
- Implements dual range sliders for price, year, and horsepower.
- Handles price scale with custom interpolation and currency formatting.
- Allows typed range inputs and validates them before submitting.
- Injects panel params into the toolbar form as hidden inputs.
- Validates allowed values and ranges before normal GET submit.
- Apply submits the toolbar form normally so Spring MVC returns the JSP.
- Clear button resets advanced filters, removes injected fields, and submits the toolbar form normally.
- Tracks active filter state on the toolbar toggle.

Used by:

- `WEB-INF/jsp/cars.jsp`
- Markup comes mainly from `WEB-INF/tags/cars-filters-panel.tag` and `WEB-INF/tags/cars-toolbar.tag`.

Important markup contracts:

- `#carsFiltersPanel`, `#filtersToggleBtn`, `#filtersApplyBtn`, `#filtersClearBtn`, `#filtersVehicleCount`.
- `#car-filter-form`, `#cars-toolbar-search`, `#filter-brand`, `#filter-body`.
- Hidden panel fields: `panelHiddenQ`, `panelHiddenBrand`, `panelHiddenBodyType`.
- Advanced fields: `panelFuelType`, `panelPriceMin`, `panelPriceMax`, `panelYearMin`, `panelYearMax`, `panelHpMin`, `panelHpMax`, `panelTransmission`, `panelAirbagMin`, `panelConsumptionSlider`, `panelMaxSpeedSlider`.

### `catalog/catalog-request-modal.js`

Opens user-facing catalog request modals for missing brand/body type requests.

Responsibilities:

- Uses `PawModal.createController`.
- Opens `#requestBrandModal` for `data-open-catalog-request="brand"`.
- Opens `#requestBodyTypeModal` for `data-open-catalog-request="body-type"`.
- Focuses the first text input/textarea.
- Closes via `[data-close-catalog-request-modal]` and Escape.

Used by:

- `WEB-INF/jsp/car-form.jsp`
- Tags: `catalog-request-brand-modal.tag`, `catalog-request-body-type-modal.tag`, `catalog-request-modal.tag`

### `catalog/catalog-request-validation.js`

Client-side validation for catalog request modal forms.

Responsibilities:

- Installs on `[data-catalog-request-form]`.
- Disables native validation.
- Validates required fields and maxlength.
- Uses localized `data-msg-required-generic` and `data-msg-length-max` messages.
- Creates inline errors and updates accessibility attributes.

Used by:

- `WEB-INF/jsp/car-form.jsp`
- Markup from `catalog-request-modal.tag`.

### `cars/car-admin.js`

Admin-only delete-car modal on the car detail page.

Responsibilities:

- Uses `#deleteCarModal` and `#deleteCarForm`.
- Opens when clicking `[data-open-delete-car-modal]`.
- Copies delete action URL from `data-car-delete-action`.
- Copies car title into `[data-delete-car-title]`.
- Closes action menus through `window.PawActionMenus.close()`.
- Closes through `[data-close-delete-car-modal]` and Escape.
- Restores focus to the trigger.

Used by:

- Admin section of `WEB-INF/jsp/car-review.jsp`
- Markup from `WEB-INF/tags/car-delete-modal.tag`.

### `reviews/review-form.js`

Client-side behavior for the create review form.

Responsibilities:

- Activates on `#createReviewFormPage` and `#createReviewForm`.
- Manages a custom 0.5-step star rating UI.
- Writes selected rating into hidden `#modalRating`.
- Updates SVG gradient stops in `.star-slot`.
- Supports keyboard rating changes with arrow keys.
- Validates required fields.
- Validates mileage as an integer between 0 and 2,000,000.
- Uses localized messages from `data-msg-*` attributes on the form.
- Renders inline errors and rating errors with accessibility attributes.
- Disables native browser validation.

Used by:

- `WEB-INF/jsp/review-form.jsp`

Markup contracts:

- `#createReviewForm`, `#createReviewFormPage`.
- `#modalRating`, `#modalMileageKm`.
- `.star-rating`, `.star-rating-value`, `.star-slot`, `.star-hit`.

### `reviews/review-tag-chips.js`

Validation and UI state for review tag chips.

Responsibilities:

- Activates on `[data-review-tag-chips]`.
- Tracks checkboxes named `tagIds`.
- Toggles `.is-selected` on selected chip labels.
- Enforces a max selected count from `data-max-selected`, defaulting to 6.
- Prevents selecting positive and negative tags for the same `data-dimension`.
- Disables opposite-dimension unchecked chips while a conflicting chip is selected.
- Shows group-level errors from `data-msg-max-selected` and `data-msg-opposites`.

Used by:

- `review-form.jsp`
- `car-review.jsp`
- Markup from `WEB-INF/tags/review-tag-chips.tag`.

### `reviews/review-moderation.js`

Admin/moderator “hide review” flow.

Responsibilities:

- Uses `[data-hide-review-modal]`, `#hideReviewForm`, and `#hideReviewReason`.
- Opens from `[data-open-hide-review-modal]`.
- Copies action URL from `data-review-hide-action` and review ID from `data-review-id`.
- Validates moderation reason:
  - Required.
  - Minimum length 10.
  - Maximum length 600.
- Allows the valid form to submit normally to the Spring controller.
- The server handles the hide action and redirects to the JSP-rendered review page.
- Closes through `[data-close-hide-review-modal]` and Escape.

Used by:

- Admin-only section of `WEB-INF/jsp/car-review.jsp`
- Markup from `WEB-INF/tags/review-hide-modal.tag` and review action buttons in `reviews-feed.tag`.

### `reviews/review-anchor-highlight.js`

Highlights a review when the page hash points to it.

Responsibilities:

- If `window.location.hash` starts with `#review-`, finds that element.
- Only applies when target class contains `review-item`.
- Adds `is-anchor-highlighted` for 1 second.
- Re-runs on `hashchange`.

Used by:

- `WEB-INF/jsp/car-review.jsp`

## Profile And Activity

### `profile/profile.js`

Large profile-page behavior script.

Responsibilities:

- Profile modals:
  - Opens edit profile modal from `[data-open-edit-profile-modal]`.
  - Opens followers/following modal from `[data-open-connections-modal]`.
  - Closes `.profile-modal` through `[data-close-profile-modal]` and Escape.
  - Auto-opens `.profile-modal[data-open-on-load="true"]`, used for server-side validation failure reopen behavior.
- Connections modal:
  - Switches between followers/following lists with `[data-connections-list]`.
  - Updates `[data-connections-title]`.
  - Filters rows via `[data-connections-search]` and row `data-search-text`.
  - Shows `[data-connections-empty]` when search has no matches.
- Follow/unfollow forms submit normally to the Spring controller; the JSP renders the updated state after redirect.
- Profile tabs:
  - Activates tabs marked `[data-profile-tab-target]` inside `[data-profile-tabs]`.
  - Persists selected tab in `localStorage` key `paw.profile.activeTab.<pathname>`.
  - Supports hash initial tab selection.
  - Supports keyboard navigation: ArrowLeft, ArrowRight, Home, End, Enter, Space.
- Linked cards:
  - Makes `[data-profile-card-link]` cards clickable/keyboard activatable while ignoring nested interactive controls.
- Collapsible sections:
  - Initializes `[data-collapsible-section]`.
  - Toggles `[data-collapsible-extra]` from `[data-collapsible-toggle]`.
- Reviews shortcut:
  - `[data-scroll-to-reviews]` activates the reviews tab and scrolls to it.

Used by:

- `WEB-INF/jsp/profile.jsp`
- Tags: `edit-profile-modal.tag`, `profile-connections-modal.tag`, `profile-review-card.tag`, `collapsible-toggle.tag`

### `activity/activity.js`

Tabs and preview panel behavior for the activity page.

Responsibilities:

- Activates on `[data-activity-tabs]`.
- Persists selected activity tab in `localStorage` key `paw.activity.activeTab.<pathname>`.
- Supports hash initial tab selection.
- Implements accessible tab state with `aria-selected`, `tabindex`, and keyboard navigation.
- Opens review preview panels from cards with `[data-activity-review-card]`.
- Uses `data-activity-preview-target` to find `[data-activity-preview-panel]`.
- Closes preview through `[data-close-activity-preview]`.
- Adds selected state to the active card.
- On wide viewports, locks body scrolling and sets `--activity-panel-height` so the preview column fits the viewport.
- Recalculates layout on resize.

Used by:

- `WEB-INF/jsp/activity.jsp`
- Tags: `activity-tab-panel.tag`, `activity-review-card.tag`, `activity-review-preview-panel.tag`

## Menus, Modals, And Admin Flows

### `shared/action-menu.js`

Reusable dropdown/action menu behavior.

Responsibilities:

- Activates on `[data-action-menu]`.
- Toggles menu panels from `[data-action-menu-toggle]`.
- Shows/hides `[data-action-menu-panel]`.
- Keeps `aria-expanded` synchronized.
- Closes other menus when one opens.
- Closes menus on outside click and Escape.
- Exposes `window.PawActionMenus.close()` for other scripts.

Used by:

- `car-review.jsp`
- `profile.jsp`
- Markup from `WEB-INF/tags/action-menu.tag`.
- `profile/profile.js` and `cars/car-admin.js` can close open menus before opening modals.

### `shared/confirmation-modal.js`

Generic confirmation modal for forms.

Responsibilities:

- Intercepts submit on forms with `data-confirm-modal="<modalId>"`.
- Opens the matching confirmation modal and stores the pending form.
- The confirm button `[data-confirmation-submit]` re-submits the form after marking it `data-confirmed="true"`.
- Close buttons use `[data-close-confirmation-modal]`.
- Closes on Escape.
- Uses the same `profile-modal-open` body class as profile modals.

Used by:

- `car-review.jsp`
- `profile.jsp`
- Markup from `WEB-INF/tags/confirmation-modal.tag`.
- Forms using it include profile logout, review delete forms, and any other form with `data-confirm-modal`.

### `admin/admin-catalog-request-modal.js`

Admin modal for reviewing brand/body-type catalog requests.

Responsibilities:

- Uses `#adminCatalogRequestModal`.
- Opens from `[data-open-admin-catalog-request]`.
- Supports request types `brand` and `body-type`.
- Copies request name, submitter, and comments from trigger data attributes into modal fields.
- Sets accept/reject form actions under `data-admin-base-url`.
- Accept/reject forms submit normally to the Spring controller; the admin JSP renders the updated request list after redirect.
- Uses `PawModal.createController`.

Used by:

- `WEB-INF/jsp/admin.jsp`
- Tags: `admin-catalog-request-review-modal.tag`, `admin-catalog-request-card.tag`

### `profile/moderator-application-modal.js`

Profile-side modal for applying to become a moderator/admin.

Responsibilities:

- Uses `#requestAdminModal` and `#requestAdminForm`.
- Opens from `[data-open-request-admin-modal]`.
- Closes from `[data-close-request-admin-modal]` and Escape.
- Disables native validation.
- Validates required textarea/input/select fields.
- Adds inline errors and accessibility attributes.
- Restores focus to the trigger after close.

Used by:

- Optional include in `WEB-INF/jsp/profile.jsp`.
- Markup from `WEB-INF/tags/moderator-application-modal.tag`.

### `admin/moderator-application-review-modal.js`

Admin-side modal for accepting/rejecting moderator/admin applications.

Responsibilities:

- Uses `#adminRequestReviewModal`.
- Opens from `[data-open-admin-request-review]`.
- Copies submitter, motivation, bio, and justification from trigger data attributes.
- Sets accept/reject form actions under `data-admin-base-url + /admin-requests/<id>`.
- Accept/reject forms submit normally to the Spring controller; the admin JSP renders the updated request list after redirect.
- Closes through `[data-close-admin-request-review-modal]` and Escape.

Used by:

- `WEB-INF/jsp/admin.jsp`
- Markup from `WEB-INF/tags/moderator-application-review-modal.tag`.

## Recommendation Flow

### `recommendations/recommend-wizard.js`

Step-by-step UI for the recommendation questionnaire.

Responsibilities:

- Activates on `#recommend-wizard`.
- Converts the full form into a JS wizard by adding `wizard-form--js`.
- Hides fallback actions.
- Finds `.wizard-step` sections and tracks the active index.
- Supports step types through `data-step-type`: `intro`, `question`, and `filters`.
- Updates progress bar transform and progress label.
- Uses localized question index template from `data-recommend-question-index-template`.
- Handles next/previous buttons with `data-wizard-action`.
- Auto-advances 280 ms after selecting a radio option on question steps.
- Enter key advances on most steps, except the final filters step and submit button.
- Focuses the first checked radio, first radio, or first focusable control in the active step.

Used by:

- `WEB-INF/jsp/recommend.jsp`

## File-by-File Quick Map

| JS file | Main feature | Included/activated by |
| --- | --- | --- |
| `shared/action-menu.js` | Reusable dropdown action menus | `car-review.jsp`, `profile.jsp`, `action-menu.tag` |
| `activity/activity.js` | Activity tabs and review preview column | `activity.jsp`, activity tags |
| `admin/admin-catalog-request-modal.js` | Admin brand/body-type request review modal | `admin.jsp`, admin catalog request tags |
| `auth/auth-form.js` | Login/register validation | `login.jsp`, `register.jsp` |
| `auth/auth-required-modal.js` | Login-required modal and resume intent | `landing.jsp`, `cars.jsp`, `car-review.jsp`, `profile.jsp`, auth-required/favorite/like tags |
| `cars/car-admin.js` | Admin delete-car modal | Admin block in `car-review.jsp`, `car-delete-modal.tag` |
| `cars/car-form.js` | Create/edit car validation and image previews | `car-form.jsp` |
| `cars/car-image-carousel.js` | Car image gallery carousel | `car-review.jsp`, `car-image-carousel.tag` |
| `cars/cars-filters-panel.js` | Advanced car catalog filter drawer | `cars.jsp`, `cars-filters-panel.tag`, `cars-toolbar.tag` |
| `catalog/catalog-request-modal.js` | User brand/body-type request modals | `car-form.jsp`, catalog request modal tags |
| `catalog/catalog-request-validation.js` | Catalog request modal validation | `car-form.jsp`, `catalog-request-modal.tag` |
| `shared/confirmation-modal.js` | Generic confirm-before-submit modal | `car-review.jsp`, `profile.jsp`, `confirmation-modal.tag` |
| `shared/form-submit-lock.js` | Double-submit prevention | Forms with `data-submit-lock="true"` across auth/car/review/admin/profile pages |
| `shared/modal-utils.js` | Shared modal helper global | Pages using auth/catalog/admin modal scripts |
| `profile/moderator-application-modal.js` | Profile-side moderator request modal | Optional in `profile.jsp`, `moderator-application-modal.tag` |
| `admin/moderator-application-review-modal.js` | Admin-side moderator request review modal | `admin.jsp`, `moderator-application-review-modal.tag` |
| `profile/profile.js` | Profile modals, tabs, follows, cards, collapsibles | `profile.jsp`, profile tags |
| `recommendations/recommend-wizard.js` | Recommendation questionnaire wizard | `recommend.jsp` |
| `reviews/review-anchor-highlight.js` | Highlight `#review-*` anchors | `car-review.jsp` |
| `reviews/review-form.js` | Create review validation and star rating | `review-form.jsp` |
| `reviews/review-moderation.js` | Moderator/admin hide-review modal and client-side reason validation | Admin block in `car-review.jsp`, `review-hide-modal.tag` |
| `reviews/review-tag-chips.js` | Review tag chip constraints | `review-form.jsp`, `car-review.jsp`, `review-tag-chips.tag` |
| `shared/toast.js` | Global toast API | Pages rendering `toast.tag` |

## Backend Response Contracts Used By JavaScript

- Admin accept/reject forms submit normally after their modal script sets the action URL.

## Cross-File Dependencies To Keep In Mind

- `auth/auth-required-modal.js`, `catalog/catalog-request-modal.js`, and `admin/admin-catalog-request-modal.js` expect `shared/modal-utils.js` to be loaded first.
- `cars/car-admin.js` and `profile/profile.js` can call `window.PawActionMenus.close()` when `shared/action-menu.js` is present.
- `shared/confirmation-modal.js` must run before a confirmed form is finally submitted, but it deliberately respects later real submissions through its `data-confirmed` guard.
- `shared/form-submit-lock.js` is safe beside validation handlers because it skips locking when another submit handler has already prevented default.
