# Business Logic in Controllers

Audit of business logic found in the `webapp` controller layer that violates the architecture rule:
> Controllers must only receive input, validate, call a service, and choose a view. Business and application logic belongs exclusively in the service layer.

---

## ActivityController

| Location | Description |
|---|---|
| `normalizeActivityTab()` L102–111 | Enforces the rule "unauthenticated users cannot access `following` or `favorites` tabs". Domain rule that belongs in the service or a dedicated validator. |

---

## AdminController

| Location | Description |
|---|---|
| L182–183 | Computes `totalPendingItems` by summing counts from four separate services. Aggregation belongs in the service. |
| L248–252, L325–329 | Calls `existsDuplicateCar(...)` and manually rejects. The service should throw an exception on duplicate; the controller should not make this decision. |
| `submitterLabel()` L725–734, L736–751 | Resolves submitter identity with multiple fallbacks: email on request → user lookup by ID → `"Usuario sin identificar"`. Domain data resolution logic. |
| `resolveSubmitterEmail()` L789–800 | Queries `UserService` to resolve the submitter's email. Domain data retrieval logic. |
| `validateUploadedImages()` L806–822 | Applies the business rule `MAX_IMAGE_COUNT = 5` and validates each file. Belongs in the image service. |
| `retainedImageIds()` / `hasUnknownRetainedImageIds()` L846–879 | Validates that submitted image IDs exist among available ones. Business security rule: retained IDs must belong to the entity. |
| `requestImagePayloads()` / `carImagePayloads()` L881–915 | Converts retained IDs to payloads including legacy image handling (`imageId == 0`). Domain logic about the image model. |
| `rejectInvalidSpecFields()` L926–937 | Validates `fuelType` and `transmission` against `CarSearchCriteria.ALLOWED_FUEL_TYPES` / `ALLOWED_TRANSMISSIONS`. Allowed value lists are domain rules. |

---

## AuthController

| Location | Description |
|---|---|
| L102–103 | Normalizes `username` and `email` before passing them to the service. The service should receive raw input and normalize internally. |
| `registrationErrorCode()` L152–182 | Maps Bean Validation constraint codes (`NotBlank`, `Size`, etc.) to domain error codes. Brittle domain knowledge about what each validation failure means. |

---

## CarController

| Location | Description |
|---|---|
| `landingPage()` L100–115 | Selects the hero car (first featured car) and hero review (top-rated for that car). Product/domain selection rule that belongs in the service. |
| L193–196 | Calls `existsDuplicateCar(...)` and rejects manually. Same issue as AdminController. |
| `ignoreConsumptionFilterForElectricOnly()` L500–504 | Mutates `criteria` by nulling out the fuel consumption filter when all selected fuel types are electric. Domain rule: "electric cars have no fuel consumption." Belongs in the service. |
| `rejectInvalidSpecFields()` L515–526 | Validates `fuelType` and `transmission` against domain-defined allowed value lists. Same issue as AdminController. |

---

## CarReviewController

| Location | Description |
|---|---|
| `buildYearVariants()` L287–301 | Fetches cars by brand and body type, then filters by model name (case-insensitive, `Locale.ROOT`) and sorts by year descending. Domain query with filtering/ordering logic belongs in the service. |
| L320–327 (inside `resolveReviewPageData`) | Determines `latestReviewLiked` by chaining `reviewLikeService.getLikedReviewIds(...)` over the latest review. Domain orchestration logic. |
| `validateReviewHideReason()` L603–614 | Applies length limits to the hide reason (min 10, max 600 chars). Business rules that belong in the service or Bean Validation. |
| `resolveReviewRecipientEmail()` L616–628 | Resolves the reviewer's email from the `Review` object first, then falls back to querying `UserService`. Domain data resolution logic. |
| `validateReplyInput()` L652–660 | Validates reply body length (max 1000 chars). Business rule. |
| `normalizeOwnershipStatus()` L662–664 | Normalizes a domain field. Should be done in the service. |

---

## CatalogRequestController

| Location | Description |
|---|---|
| L100–103 | Checks `adminRequestService.hasPendingRequest(currentUser.getId())` and silently redirects. The invariant "only one pending request per user" should be enforced by the service (via exception), not silently short-circuited by the controller. |

---

## ProfileController

| Location | Description |
|---|---|
| L161–167 | Prevents self-follow (`currentUser.getId() == userId`). Domain invariant that `UserFollowService.followUser()` should enforce by throwing an exception. |
| L172–178 | Toggle follow: controller reads `wasFollowing` and decides which service method to call. The service should expose a single `toggleFollow(followerId, targetId)` method. |
| L229 | Forces `activeTab = TAB_REVIEWS` for profiles that are not the viewer's own. Business rule: "visitors only see the reviews tab." |
| L233–234 | Only counts `favoriteCarCount` and `likedReviewCount` for own profiles. Access-based data loading decision belongs in the service. |
| `canRequestModerator()` L303–312 | Full eligibility rule: must be own profile, role must be `user`, and no pending request must exist. Pure business logic in the controller. |
| `orderedExistingReviews()` L349–356 | Filters null reviews and preserves order by `reviewId`. Domain data filtering/ordering logic. |
| `toConnection()` L401–407 | Calls `userFollowService.isFollowing(...)` inside a loop over each follower/following user. Causes N+1 queries and mixes domain relationship logic into the controller. |

---

## ControllerUtils

| Location | Description |
|---|---|
| `validateUploadedImage()` L55–75 | Validates file size (10 MB limit), content type (JPEG/PNG/WEBP), and magic bytes. The rules defining valid images are business rules that belong in the image service. |
| `submittedToastMessageCode()` L81–101 | Maps `submitted` parameter values to message keys. Not strictly business logic, but encodes domain knowledge about which actions correspond to which toast notification. |

---

## Priority Summary

These are the violations where a live domain invariant is being enforced (or silently ignored) in the controller instead of the service:

1. **`ProfileController.canRequestModerator()`** — complex eligibility rule entirely in the controller.
2. **Self-follow prevention** (`ProfileController` L161–167) — domain invariant the service should reject.
3. **`CarController.ignoreConsumptionFilterForElectricOnly()`** — domain rule about car type.
4. **`CatalogRequestController` pending request check** (L100–103) — uniqueness invariant the service should enforce.
5. **`existsDuplicateCar` checks** (AdminController + CarController) — service should throw, not controller validate.
6. **`ProfileController` N+1 `isFollowing` in loop** — both a correctness issue and business logic leak.
