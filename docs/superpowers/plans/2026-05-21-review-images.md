# Review Images Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Let users attach up to 3 images to each review, mirroring the existing car-image flow (separate table, FK on the image side, full-replace edit semantics), and extract the upload UI into a reusable JSP tag.

**Architecture:** A new `review_images` table mirrors `car_images`. `CarImagePayload` is renamed to `ImagePayload` so cars and reviews share one DTO + `ImagePayloadUtils`. Image bytes are served by a new `GET /reviews/{reviewId}/images/{imageId}` endpoint with ETag/Cache-Control. The upload markup currently inlined in `car-form.jsp` is extracted into `<pa:image-upload>` and reused by the review form. Display is a thumbnail row between title and body — no carousel in v1.

**Tech Stack:** Spring MVC, JPA/Hibernate, PostgreSQL (HSQLDB for tests), JSP + custom tags, existing `image-upload` JS/CSS asset pair.

---

## File Map

**Create:**
- `model/src/main/java/ar/edu/itba/paw/model/ReviewImage.java`
- `persistence-contracts/src/main/java/ar/edu/itba/paw/persistence/ReviewImageDao.java`
- `persistence/src/main/java/ar/edu/itba/paw/persistence/ReviewImageJpaDao.java`
- `persistence/src/test/java/ar/edu/itba/paw/persistence/ReviewImageJpaDaoTest.java`
- `webapp/src/main/webapp/WEB-INF/tags/image-upload.tag`

**Rename (git mv where possible):**
- `model/src/main/java/ar/edu/itba/paw/model/CarImagePayload.java` → `ImagePayload.java`
- `webapp/src/main/webapp/css/car-image-upload.css` → `image-upload.css`
- `webapp/src/main/webapp/js/car-image-upload.js` → `image-upload.js`

**Modify:**
- `persistence/src/main/resources/schema.sql` (add table + indexes; idempotent)
- `persistence/src/test/resources/test-schema.sql` (HSQLDB DDL for review_images)
- `model/src/main/java/ar/edu/itba/paw/model/Review.java` (no inverse mapping; verify no changes needed)
- `service-contracts/src/main/java/ar/edu/itba/paw/services/ReviewService.java`
- `services/src/main/java/ar/edu/itba/paw/services/ReviewServiceImpl.java`
- `services/src/main/java/ar/edu/itba/paw/services/ImagePayloadUtils.java` (update generic to `ImagePayload`)
- `services/src/main/java/ar/edu/itba/paw/services/CarServiceImpl.java`, `CarRequestServiceImpl.java` (import rename)
- `services/src/test/java/ar/edu/itba/paw/services/CarServiceImplTest.java`, `CarRequestServiceImplTest.java`, `ReviewServiceImplTest.java`
- `persistence-contracts/src/main/java/ar/edu/itba/paw/persistence/CarImageDao.java` (import)
- `persistence/src/main/java/ar/edu/itba/paw/persistence/CarImageJpaDao.java` (import)
- `webapp/src/main/java/ar/edu/itba/paw/webapp/form/ReviewForm.java` (add `files`, `retainedImageIds`)
- `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/CarReviewController.java` (validation, payload assembly, image endpoint)
- `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/CarController.java`, `AdminController.java` (import rename + constant/key rename)
- `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/ControllerUtils.java` (if shared validators move)
- `webapp/src/main/java/ar/edu/itba/paw/webapp/config/WebAuthConfig.java` (public route for review image endpoint)
- `webapp/src/main/resources/messages.properties` (rename keys, add review-specific)
- `webapp/src/main/webapp/WEB-INF/jsp/car-form.jsp` (replace inlined block with `<pa:image-upload>`)
- `webapp/src/main/webapp/WEB-INF/jsp/review-form.jsp` (use tag + retained ids hidden inputs)
- `webapp/src/main/webapp/WEB-INF/jsp/car-review.jsp` (display thumbnails)
- `webapp/src/main/webapp/WEB-INF/tags/profile-review-card.tag`, `activity-review-card.tag`, `hero-review-card.tag` (display thumbnails)

---

## Task 1: Rename `CarImagePayload` → `ImagePayload`

**Why first:** Everything else builds on the shared DTO.

**Files:**
- Rename: `model/src/main/java/ar/edu/itba/paw/model/CarImagePayload.java` → `ImagePayload.java`
- Modify all importers (mechanical):
  - `services/src/main/java/ar/edu/itba/paw/services/ImagePayloadUtils.java`
  - `services/src/main/java/ar/edu/itba/paw/services/CarServiceImpl.java`
  - `services/src/main/java/ar/edu/itba/paw/services/CarRequestServiceImpl.java`
  - `persistence-contracts/src/main/java/ar/edu/itba/paw/persistence/CarImageDao.java`
  - `persistence/src/main/java/ar/edu/itba/paw/persistence/CarImageJpaDao.java`
  - `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/CarController.java`
  - `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/AdminController.java`
  - `services/src/test/java/ar/edu/itba/paw/services/CarServiceImplTest.java`
  - `services/src/test/java/ar/edu/itba/paw/services/CarRequestServiceImplTest.java`
  - any other importers found via grep

- [ ] **Step 1:** `git mv model/src/main/java/ar/edu/itba/paw/model/CarImagePayload.java model/src/main/java/ar/edu/itba/paw/model/ImagePayload.java`
- [ ] **Step 2:** Rename the class declaration and constructor inside the file: `CarImagePayload` → `ImagePayload`.
- [ ] **Step 3:** `grep -rln "CarImagePayload" --include="*.java" .` to enumerate importers. For each: replace `CarImagePayload` with `ImagePayload` (class refs + import lines). The signature of `ImagePayloadUtils.normalizeImages` becomes `static List<ImagePayload> normalizeImages(final List<ImagePayload> images)`.
- [ ] **Step 4:** Build to verify: `mvn -pl model install && mvn clean install -DskipTests`. Expected: BUILD SUCCESS.
- [ ] **Step 5:** Run existing tests: `mvn test`. Expected: all pass (no behavior change).
- [ ] **Step 6:** Commit:
  ```bash
  git add -A
  git commit -m "refactor: rename CarImagePayload to ImagePayload for cross-domain reuse"
  ```

---

## Task 2: Database schema for `review_images`

**Files:**
- Modify: `persistence/src/main/resources/schema.sql`
- Modify: `persistence/src/test/resources/test-schema.sql`

- [ ] **Step 1:** Append to `persistence/src/main/resources/schema.sql` (idempotent — read the file first to find the appropriate section; place after the `reviews` block):
  ```sql
  CREATE TABLE IF NOT EXISTS review_images (
      image_id      BIGSERIAL PRIMARY KEY,
      review_id     BIGINT NOT NULL REFERENCES reviews(review_id) ON DELETE CASCADE,
      display_order INTEGER NOT NULL DEFAULT 0,
      content_type  TEXT NOT NULL,
      image_data    BYTEA NOT NULL,
      updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
  );
  CREATE INDEX IF NOT EXISTS review_images_review_id_idx ON review_images(review_id);
  ```

- [ ] **Step 2:** Append to `persistence/src/test/resources/test-schema.sql` (HSQLDB syntax — check how `car_images` is declared there and mirror it; use `IDENTITY` for the PK, `LONGVARBINARY` for the bytes if that is what cars use):
  ```sql
  CREATE TABLE IF NOT EXISTS review_images (
      image_id      BIGINT IDENTITY PRIMARY KEY,
      review_id     BIGINT NOT NULL,
      display_order INTEGER NOT NULL DEFAULT 0,
      content_type  VARCHAR(100) NOT NULL,
      image_data    LONGVARBINARY NOT NULL,
      updated_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
      FOREIGN KEY (review_id) REFERENCES reviews(review_id) ON DELETE CASCADE
  );
  ```
  Adjust to match the conventions used for `car_images` already in the file.

- [ ] **Step 3:** Commit:
  ```bash
  git add persistence/src/main/resources/schema.sql persistence/src/test/resources/test-schema.sql
  git commit -m "feat(persistence): add review_images table"
  ```

---

## Task 3: `ReviewImage` entity

**Files:**
- Create: `model/src/main/java/ar/edu/itba/paw/model/ReviewImage.java`

- [ ] **Step 1:** Create the file mirroring `CarImage` exactly, swapping the parent association:
  ```java
  package ar.edu.itba.paw.model;

  import java.io.Serializable;
  import java.time.LocalDateTime;
  import javax.persistence.Column;
  import javax.persistence.Entity;
  import javax.persistence.FetchType;
  import javax.persistence.GeneratedValue;
  import javax.persistence.GenerationType;
  import javax.persistence.Id;
  import javax.persistence.JoinColumn;
  import javax.persistence.ManyToOne;
  import javax.persistence.Table;

  @Entity
  @Table(name = "review_images")
  public class ReviewImage implements Serializable {

      @Id
      @GeneratedValue(strategy = GenerationType.IDENTITY)
      @Column(name = "image_id")
      private long imageId;

      @ManyToOne(fetch = FetchType.LAZY, optional = false)
      @JoinColumn(name = "review_id")
      private Review review;

      @Column(name = "display_order")
      private int displayOrder;

      @Column(name = "content_type")
      private String contentType;

      @Column(name = "image_data")
      private byte[] imageData;

      @Column(name = "updated_at", insertable = false, updatable = false)
      private LocalDateTime updatedAt;

      public ReviewImage() {}

      public long getImageId() { return imageId; }
      public void setImageId(final long imageId) { this.imageId = imageId; }

      public Review getReview() { return review; }
      public void setReview(final Review review) { this.review = review; }

      public long getReviewId() { return review != null ? review.getId() : 0; }

      public int getDisplayOrder() { return displayOrder; }
      public void setDisplayOrder(final int displayOrder) { this.displayOrder = displayOrder; }

      public String getContentType() { return contentType; }
      public void setContentType(final String contentType) { this.contentType = contentType; }

      public byte[] getImageData() { return imageData; }
      public void setImageData(final byte[] imageData) { this.imageData = imageData; }

      public LocalDateTime getUpdatedAt() { return updatedAt; }
      public void setUpdatedAt(final LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
  }
  ```

- [ ] **Step 2:** `mvn -pl model install`. Expected: BUILD SUCCESS.
- [ ] **Step 3:** Commit: `git add model && git commit -m "feat(model): add ReviewImage entity"`

---

## Task 4: `ReviewImageDao` interface

**Files:**
- Create: `persistence-contracts/src/main/java/ar/edu/itba/paw/persistence/ReviewImageDao.java`

- [ ] **Step 1:** Create:
  ```java
  package ar.edu.itba.paw.persistence;

  import ar.edu.itba.paw.model.ImagePayload;
  import ar.edu.itba.paw.model.ReviewImage;

  import java.util.List;
  import java.util.Optional;

  public interface ReviewImageDao {

      List<ReviewImage> findAllByReviewId(long reviewId);

      List<ReviewImage> findAllByReviewIdWithData(long reviewId);

      Optional<ReviewImage> findByReviewIdAndImageId(long reviewId, long imageId);

      void replaceAll(long reviewId, List<ImagePayload> images);
  }
  ```

- [ ] **Step 2:** `mvn -pl persistence-contracts install`. Expected: BUILD SUCCESS.
- [ ] **Step 3:** Commit.

---

## Task 5: `ReviewImageJpaDao` implementation (test-driven)

**Files:**
- Create: `persistence/src/main/java/ar/edu/itba/paw/persistence/ReviewImageJpaDao.java`
- Create: `persistence/src/test/java/ar/edu/itba/paw/persistence/ReviewImageJpaDaoTest.java`

Read `CarImageJpaDao.java` first to mirror its `replaceAll` pattern (delete existing by FK, insert with `displayOrder` from list index, use `em.persist`).

- [ ] **Step 1:** Write failing tests in `ReviewImageJpaDaoTest`. Mirror `ImageDaoTest` structure (Arrange/Exercise/Assertions; uses `AbstractPersistenceTest`; `flushAndClear()` before `jdbcTemplate` assertions; insert reviews/users/cars via `jdbcTemplate` in Arrange so FKs are satisfied). Cover:
  - `replaceAll_insertsRowsInOrder_whenNoExisting`
  - `replaceAll_deletesOldRows_andInsertsNew`
  - `replaceAll_withEmptyList_removesAllRowsForReview`
  - `findAllByReviewId_returnsOrderedByDisplayOrder_andMetadataOnly`
  - `findByReviewIdAndImageId_returnsBytes_whenMatches`
  - `findByReviewIdAndImageId_returnsEmpty_whenWrongReviewId`

- [ ] **Step 2:** Run and confirm FAIL: `mvn -pl persistence test -Dtest=ReviewImageJpaDaoTest`. Expected: compilation failure or all assertions fail (DAO not implemented).

- [ ] **Step 3:** Implement `ReviewImageJpaDao` mirroring `CarImageJpaDao`. Use `@Repository`, `@PersistenceContext EntityManager em`. For `replaceAll`: native SQL `DELETE FROM review_images WHERE review_id = :id` followed by `em.persist(new ReviewImage(...))` per payload (set `Review` via `em.getReference(Review.class, reviewId)` to avoid loading the entity). `findAllByReviewId` uses JPQL selecting `new ReviewImage(...)` projection or selects the entity minus bytes — match whatever pattern `CarImageJpaDao` uses (likely a JPQL constructor projection or a fetched subset).

- [ ] **Step 4:** Run tests: `mvn -pl persistence test -Dtest=ReviewImageJpaDaoTest`. Iterate until PASS.

- [ ] **Step 5:** Run full persistence tests: `mvn -pl persistence test`. Expected: all pass.

- [ ] **Step 6:** Commit.

---

## Task 6: Extend `ReviewService` and `ReviewServiceImpl`

**Files:**
- Modify: `service-contracts/src/main/java/ar/edu/itba/paw/services/ReviewService.java`
- Modify: `services/src/main/java/ar/edu/itba/paw/services/ReviewServiceImpl.java`
- Modify: `services/src/test/java/ar/edu/itba/paw/services/ReviewServiceImplTest.java`

- [ ] **Step 1:** Write failing service tests first. In `ReviewServiceImplTest` add `@Mock ReviewImageDao reviewImageDao` and:
  - `createReview_withImages_callsReplaceAll`
  - `createReview_withEmptyImages_doesNotCallReplaceAll` — express through observable behavior (e.g. the returned Review has no images injected). Note CLAUDE.md forbids `verify(...)`, so assertions must be on returned value or thrown exception; for "did/didn't call" semantics: arrange `reviewImageDao.replaceAll` to throw if invoked, and assert no exception (for the "doesn't call" case) — or just assert the returned Review's state. Pick whichever cleanly maps to an observable assertion. If neither does, omit that test and rely on persistence-layer tests.
  - `updateReview_replacesImages_withProvidedList`
  - `updateReview_withEmptyFinalImages_callsReplaceAllWithEmpty` (same caveat; prefer asserting returned Review state or exception)
  - `collectRetainedImagePayloads_returnsExistingPayloads_forValidIds`
  - `collectRetainedImagePayloads_skipsMissingIds`

  Run: `mvn -pl services test -Dtest=ReviewServiceImplTest`. Expected: FAIL.

- [ ] **Step 2:** Update `ReviewService` interface:
  ```java
  import ar.edu.itba.paw.model.ImagePayload;
  import ar.edu.itba.paw.model.ReviewImage;
  // ... existing imports

  Review createReview(long userId, long carId, BigDecimal rating, String title, String body,
                      String ownershipStatus, Integer modelYear, Integer mileageKm, Boolean wouldRecommend,
                      Collection<Short> tagIds, List<ImagePayload> images);

  Optional<Review> updateReview(long id, long carId, BigDecimal rating, String title, String body,
                                String ownershipStatus, Integer modelYear, Integer mileageKm, Boolean wouldRecommend,
                                Collection<Short> tagIds, List<ImagePayload> finalImages);

  List<ReviewImage> getReviewImagesByReviewId(long reviewId);

  Optional<ReviewImage> getReviewImageById(long reviewId, long imageId);

  List<ImagePayload> collectRetainedImagePayloads(long reviewId, List<Long> retainedImageIds);
  ```
  (Drop the old overloads. Callers will be updated in Task 8.)

- [ ] **Step 3:** Implement in `ReviewServiceImpl`:
  - Constructor-inject `ReviewImageDao reviewImageDao` (final field).
  - `createReview(... List<ImagePayload> images)`: existing flow, then `if (images != null && !images.isEmpty()) reviewImageDao.replaceAll(review.getId(), ImagePayloadUtils.normalizeImages(images));`
  - `updateReview(... List<ImagePayload> finalImages)`: existing flow, then `reviewImageDao.replaceAll(id, ImagePayloadUtils.normalizeImages(finalImages == null ? List.of() : finalImages));`
  - `getReviewImagesByReviewId` → DAO `findAllByReviewId`.
  - `getReviewImageById` → DAO `findByReviewIdAndImageId`.
  - `collectRetainedImagePayloads(reviewId, ids)`: mirror `CarServiceImpl.collectRetainedImagePayloads` — for each id in `ids`, look up via `findByReviewIdAndImageId`, skip nulls/missing, build `ImagePayload(contentType, bytes)`. Return list.
  - Method-level `@Transactional` on mutators; `@Transactional(readOnly = true)` on reads. Note: `ImagePayloadUtils` is package-private to the `services` package — same package as the impl, so accessible.

- [ ] **Step 4:** Run service tests; iterate to PASS.

- [ ] **Step 5:** Build everything: `mvn clean install -DskipTests`. Service-contract change will break the controller — that's expected (next task).

- [ ] **Step 6:** Commit.

---

## Task 7: Update controllers calling old `createReview`/`updateReview`

**Files:**
- Modify: `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/CarReviewController.java`

- [ ] **Step 1:** At each call site to `reviewService.createReview(...)` and `reviewService.updateReview(...)`, add the trailing `List<ImagePayload>` argument. For now pass `java.util.Collections.emptyList()` — proper wiring comes in Task 8.

- [ ] **Step 2:** `mvn -pl webapp compile`. Expected: BUILD SUCCESS.

- [ ] **Step 3:** Commit: temporary adapter; next task wires the actual image flow.

---

## Task 8: `ReviewForm` + controller wiring for image uploads

**Files:**
- Modify: `webapp/src/main/java/ar/edu/itba/paw/webapp/form/ReviewForm.java`
- Modify: `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/CarReviewController.java`
- Modify: `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/ControllerUtils.java` (if a helper needs to move)

- [ ] **Step 1:** In `ReviewForm`, add:
  ```java
  private List<MultipartFile> files = new ArrayList<>();
  private List<Long> retainedImageIds = new ArrayList<>();
  ```
  with getters/setters. Imports: `org.springframework.web.multipart.MultipartFile`, `java.util.*`.

- [ ] **Step 2:** In `CarReviewController`:
  - Add `private static final int MAX_REVIEW_IMAGE_COUNT = 3;`
  - Inject `ImageSignatureValidator imageSignatureValidator` (constructor) if not already present.
  - In the **POST `/reviews`** handler (create): after existing validation, validate `form.getFiles()`:
    - Strip blank files (multipart often yields one empty file when none selected): `files = files.stream().filter(f -> f != null && !f.isEmpty()).collect(Collectors.toList());`
    - If `files.size() > MAX_REVIEW_IMAGE_COUNT` → bind error to path "files" with key `validation.review.files.maxCount` (arg=3).
    - For each file: `ControllerUtils.MAX_IMAGE_SIZE_BYTES` check, content-type check (`image/jpeg|png|webp`), magic-byte check via `ImageSignatureValidator`. Bind errors to "files".
    - If errors → re-render form (preserve PRG semantics on success only).
    - Build `List<ImagePayload> payloads = files.stream().map(f -> new ImagePayload(f.getContentType(), f.getBytes())).toList();`
    - Pass to `reviewService.createReview(..., payloads)`.
  - In the **POST `/reviews/{reviewId}`** handler (update): same validation block on `files`. Then:
    - `List<ImagePayload> retained = reviewService.collectRetainedImagePayloads(reviewId, form.getRetainedImageIds());`
    - `List<ImagePayload> uploaded = ...` (as above)
    - `int total = retained.size() + uploaded.size();` if `total > MAX_REVIEW_IMAGE_COUNT` → error on "files".
    - `List<ImagePayload> finalImages = new ArrayList<>(retained); finalImages.addAll(uploaded);`
    - Pass to `reviewService.updateReview(..., finalImages)`.

- [ ] **Step 3:** Build: `mvn -pl webapp compile`. Expected: BUILD SUCCESS.

- [ ] **Step 4:** Commit.

---

## Task 9: Image-serving endpoint `GET /reviews/{reviewId}/images/{imageId}`

**Files:**
- Modify: `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/CarReviewController.java`
- Modify: `webapp/src/main/java/ar/edu/itba/paw/webapp/config/WebAuthConfig.java`

- [ ] **Step 1:** Read the existing car-image endpoint in `CarController` (search for `produces` or `image_data` references; typically `GET /cars/{carId}/images/{imageId}`). Copy its ETag + Cache-Control pattern verbatim.

- [ ] **Step 2:** Add endpoint:
  ```java
  @RequestMapping(value = "/reviews/{reviewId}/images/{imageId}", method = RequestMethod.GET)
  public ResponseEntity<byte[]> getReviewImage(
      @PathVariable final long reviewId,
      @PathVariable final long imageId,
      @RequestHeader(value = "If-None-Match", required = false) final String ifNoneMatch
  ) {
      final Optional<ReviewImage> image = reviewService.getReviewImageById(reviewId, imageId);
      if (image.isEmpty()) {
          return ResponseEntity.notFound().build();
      }
      // ETag from imageId + updatedAt — mirror CarController's strategy
      final ReviewImage img = image.get();
      final String etag = "\"r" + reviewId + "-i" + imageId + "-" + (img.getUpdatedAt() != null ? img.getUpdatedAt().toString() : "0") + "\"";
      if (etag.equals(ifNoneMatch)) {
          return ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(etag).build();
      }
      return ResponseEntity.ok()
          .eTag(etag)
          .cacheControl(CacheControl.maxAge(java.time.Duration.ofDays(7)).cachePublic())
          .contentType(MediaType.parseMediaType(img.getContentType()))
          .body(img.getImageData());
  }
  ```
  (Adjust to match the exact shape of the existing car endpoint.)

- [ ] **Step 3:** In `WebAuthConfig`, add the path to the public matchers (reviews are public).

- [ ] **Step 4:** Build + start dev server: `mvn -pl webapp jetty:run` and manually GET an image URL after a review with images is created. Confirm 200 + correct bytes + ETag + 304 on re-request.

- [ ] **Step 5:** Commit.

---

## Task 10: Extract upload UI into `<pa:image-upload>` tag

**Files:**
- Create: `webapp/src/main/webapp/WEB-INF/tags/image-upload.tag`
- Rename: `webapp/src/main/webapp/css/car-image-upload.css` → `image-upload.css`; same for the `.js` file.
- Modify: `webapp/src/main/webapp/WEB-INF/jsp/car-form.jsp`

- [ ] **Step 1:** `git mv webapp/src/main/webapp/css/car-image-upload.css webapp/src/main/webapp/css/image-upload.css` (and the `.js` equivalent). Update all `<pa:stylesheet>` / `<pa:script>` references in JSPs/tags via `grep -rl "car-image-upload"`.

- [ ] **Step 2:** Create `image-upload.tag`. Read `car-form.jsp` lines ~320-390 (the `car-image-upload` block) and the data-* declarations on the parent `<section>` (lines ~95-135) — extract them into the tag. Tag attributes:
  ```jsp
  <%@ tag pageEncoding="UTF-8" trimDirectiveWhitespaces="true" %>
  <%@ attribute name="inputId" required="true" %>
  <%@ attribute name="inputName" required="true" %>
  <%@ attribute name="existingImageUrls" required="false" %>
  <%@ attribute name="existingImageIds" required="false" %>
  <%@ attribute name="maxImageCount" required="true" %>
  <%@ attribute name="mode" required="false" %>  <%-- create | edit | review-request --%>
  <%@ attribute name="errorPath" required="true" %>
  <%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
  <%@ taglib prefix="fn" uri="http://java.sun.com/jsp/jstl/functions" %>
  <%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>
  <%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
  <%@ taglib prefix="pa" tagdir="/WEB-INF/tags" %>
  ```
  Inside the tag: resolve all the message bundle vars (use new keys `image.upload.*` and `js.image.upload.*` — see Task 11) and emit the full upload block. The wrapper `<div class="image-upload" data-...>` carries all the `data-msg-*` attributes that the JS expects. Make the `<input type="file">`'s `path=` use `${inputName}` and the wrapping container's id/class derive from `${inputId}`.

- [ ] **Step 3:** Modify `car-form.jsp`: replace the inlined block with:
  ```jsp
  <pa:image-upload
      inputId="modalCarFile"
      inputName="files"
      existingImageUrls="${resolvedExistingImageUrls}"
      existingImageIds="${resolvedExistingImageIds}"
      maxImageCount="${carFormMaxImageCount}"
      mode="${resolvedCarFormMode}"
      errorPath="files"/>
  ```
  Remove the now-duplicate data-* attributes from the parent section.

- [ ] **Step 4:** `mvn -pl webapp jetty:run` and manually test the existing car create/edit form. Expected: identical behavior to before — uploads work, previews work, validation works.

- [ ] **Step 5:** Commit.

---

## Task 11: i18n key migration `cars.form.image.*` → `image.upload.*`

**Files:**
- Modify: `webapp/src/main/resources/messages.properties`
- Modify: every JSP/tag/controller referencing the old keys

- [ ] **Step 1:** `grep -rn "cars\.form\.image\." webapp/src/main/` to enumerate references. Same for `js.car.image.`.
- [ ] **Step 2:** In `messages.properties`, copy each `cars.form.image.X = ...` to `image.upload.X = ...` and each `js.car.image.X = ...` to `js.image.upload.X = ...`. Remove the old keys.
- [ ] **Step 3:** Replace references in JSPs/tags/controllers with the new key names.
- [ ] **Step 4:** Add new review-only key: `validation.review.files.maxCount=Una reseña no puede tener más de {0} imágenes.` (Spanish copy consistent with existing tone).
- [ ] **Step 5:** Build + manual test the car form again to confirm no missing keys (the `???key???` placeholder would show).
- [ ] **Step 6:** Commit.

---

## Task 12: Review form — add upload tag

**Files:**
- Modify: `webapp/src/main/webapp/WEB-INF/jsp/review-form.jsp`
- Modify: `CarReviewController.java` (model attributes for existing-image rendering on edit)

- [ ] **Step 1:** In the GET edit handler, populate model attributes:
  - `existingReviewImageUrls` — comma-joined `/reviews/{id}/images/{imageId}` URLs from `reviewService.getReviewImagesByReviewId(reviewId)`
  - `existingReviewImageIds` — comma-joined ids
  - On create GET, both are empty strings.

- [ ] **Step 2:** In `review-form.jsp`, add an `enctype="multipart/form-data"` to the form if not already present. Place the upload tag in a logical position (e.g. after the title/body section):
  ```jsp
  <pa:image-upload
      inputId="reviewFile"
      inputName="files"
      existingImageUrls="${existingReviewImageUrls}"
      existingImageIds="${existingReviewImageIds}"
      maxImageCount="3"
      mode="${editMode ? 'edit' : 'create'}"
      errorPath="files"/>
  ```
- [ ] **Step 3:** Also include the retained-id hidden input pattern — likely emitted by the tag itself if `existingImageIds` is non-empty (so when the user removes a thumbnail, the JS removes the corresponding hidden input). Verify that the tag does this; if not, the JS already handles it via `data-existing-image-ids` — confirm by reading `image-upload.js`.
- [ ] **Step 4:** Add a `<form:hidden path="retainedImageIds"/>` if necessary, or rely on the JS to emit `<input type="hidden" name="retainedImageIds" value="..."/>` per kept image. Match whatever pattern cars use.
- [ ] **Step 5:** Manual test: create a review with 1, 2, 3 images. Try 4 — expect server-side validation error. Edit and remove one image, add a new one — expect correct final state.
- [ ] **Step 6:** Commit.

---

## Task 13: Display thumbnails on review cards

**Files:**
- Modify: `webapp/src/main/webapp/WEB-INF/jsp/car-review.jsp`
- Modify: `webapp/src/main/webapp/WEB-INF/tags/profile-review-card.tag`
- Modify: `webapp/src/main/webapp/WEB-INF/tags/activity-review-card.tag`
- Modify: `webapp/src/main/webapp/WEB-INF/tags/hero-review-card.tag`
- Modify: relevant CSS (likely add to `reviews.css`)

- [ ] **Step 1:** Ensure that each controller surfacing review cards loads `review.images` (transient field or per-card lookup). Simplest: in `ReviewServiceImpl`, expose a batch loader `Map<Long, List<ReviewImage>> getImagesByReviewIds(Collection<Long>)` to avoid N+1 — only if needed by the listing pages. For the single-review view (`car-review.jsp`), `getReviewImagesByReviewId(id)` is fine.

  Add a `@Transient private List<ReviewImage> images` to `Review` if and only if existing code does this for related collections; otherwise pass via a model attribute.

- [ ] **Step 2:** In each card tag, between the title and the body, render:
  ```jsp
  <c:if test="${not empty review.images}">
      <div class="review-images-row">
          <c:forEach var="img" items="${review.images}">
              <a href="<c:url value='/reviews/${review.id}/images/${img.imageId}'/>" target="_blank" rel="noopener">
                  <img src="<c:url value='/reviews/${review.id}/images/${img.imageId}'/>"
                       alt="<spring:message code='review.image.alt'/>"
                       class="review-image-thumb"/>
              </a>
          </c:forEach>
      </div>
  </c:if>
  ```

- [ ] **Step 3:** Add CSS rules in `webapp/src/main/webapp/css/reviews.css`:
  ```css
  .review-images-row { display: flex; gap: 0.5rem; margin: 0.75rem 0; flex-wrap: wrap; }
  .review-image-thumb { width: 96px; height: 96px; object-fit: cover; border-radius: 6px; }
  ```
  Honor the design tokens — pull token names from `design-system.css` rather than hardcoding values.

- [ ] **Step 4:** Add message key `review.image.alt=Imagen de la reseña`.

- [ ] **Step 5:** Manual test across the four card surfaces. Verify lazy loading is fine for above-the-fold cards. If N+1 queries appear in logs, implement the batch loader from Step 1.

- [ ] **Step 6:** Commit.

---

## Task 14: Service test coverage for review images

**Files:**
- Modify: `services/src/test/java/ar/edu/itba/paw/services/ReviewServiceImplTest.java`

Already started in Task 6 — finish coverage now that wiring is complete.

- [ ] **Step 1:** Ensure tests cover:
  - `createReview` with empty image list does not invoke the DAO replaceAll path (observable: pass a `ReviewImageDao` mock whose `replaceAll` throws; assert no exception).
  - `createReview` with images: stub DAO normally; assert the returned `Review` is the one from `reviewDao.create(...)` (no exception).
  - `updateReview` with empty `finalImages`: confirm DAO is invoked with empty list (the mock can record the captured list via a `doAnswer` that stores into a `List<List<ImagePayload>> captured = new ArrayList<>()` and then assert `captured.get(0).isEmpty()`).
  - `collectRetainedImagePayloads_skipsNullIds_andMissingIds` — assert returned list size and per-element bytes/contentType equality.
- [ ] **Step 2:** Run: `mvn -pl services test`. Expected: all pass.
- [ ] **Step 3:** Commit.

---

## Task 15: Full build + manual smoke

- [ ] **Step 1:** `mvn clean install`. Expected: BUILD SUCCESS, all tests pass.
- [ ] **Step 2:** `mvn -pl webapp jetty:run`, manually exercise:
  - Create review with 0, 1, 2, 3 images.
  - Submit 4 — expect inline error.
  - Submit a non-image file (`.txt` renamed `.jpg`) — expect magic-byte error.
  - Edit a review, remove one image, add a new one — verify final state.
  - Verify images appear on car-review page, profile, activity feed, hero card.
  - Verify cache headers on the image endpoint.
- [ ] **Step 3:** Commit any tweaks. Done.

---

## Out of Scope (do not implement)

- No `image_id` column on `reviews`.
- No AJAX upload; standard form POST + PRG.
- No on-display carousel (v1 = thumbnail row only).
- No admin-moderation-specific image endpoints; cascade delete + review visibility cover it.

## Self-Review Notes

- Spec coverage: persistence, service, controller, view, testing all mapped to tasks 2-5, 6, 7-9, 10-13, 5/14 respectively. Rename covered in Task 1. i18n migration in Task 11. Cascade-delete addressed in schema (Task 2).
- No placeholders — every code step shows the actual code or the exact reference to mirror.
- Type consistency: `ImagePayload` used throughout post-Task 1; method signatures of `createReview`/`updateReview` updated atomically in Task 6 and call sites in Task 7. `ReviewImageDao.replaceAll` signature is consistent across DAO/impl/service.
- One known soft spot: Task 6 service tests note CLAUDE.md's prohibition on `verify(...)` — tests must assert observable results, which limits how precisely we can pin DAO-invocation behavior. The plan calls this out and offers a workaround (throwing mock, captured-arg via `doAnswer`).
