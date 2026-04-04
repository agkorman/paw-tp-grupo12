# AGENT.MD

## Purpose

This repository is an ITBA PAW course project: a server-rendered car listing and review web application built as a multi-module Maven project.

The UI is not a separate SPA. Views live in JSPs and JSP tag files under `webapp`, with styling in plain CSS.

## Stack

- Java 21 at the repository level.
- Maven multi-module build.
- Spring 5.3.33.
- Spring MVC in `webapp`.
- Spring JDBC in `persistence`.
- JSP/JSTL for rendering.
- Jetty Maven plugin for local execution.
- PostgreSQL 16 expected by the docs.

## Modules

Active Maven modules from the root `pom.xml`:

- `model`
- `persistence-contracts`
- `service-contracts`
- `persistence`
- `services`
- `webapp`

Dependency flow:

`model` -> (`persistence-contracts`, `service-contracts`) -> (`persistence`, `services`) -> `webapp`

Notes:

- `model` contains domain classes.
- `persistence-contracts` contains DAO contracts.
- `service-contracts` contains service interfaces.
- `persistence` implements DAO contracts with Spring JDBC and includes `schema.sql`.
- `services` implements service interfaces and coordinates DAO access.
- `webapp` contains Spring MVC configuration, controllers, JSPs, tags, and CSS.

## Build And Run

The README documents the standard local flow:

```bash
mvn clean install
mvn -pl webapp jetty:run
```

The application reads these environment variables:

```bash
DB_URL=jdbc:postgresql://localhost/pawdb
DB_USERNAME=pawuser
DB_PASSWORD=yourpassword
```

There is a checked-in `webapp/.env` with local defaults:

```bash
export DB_URL='jdbc:postgresql://localhost/pawdb'
export DB_USERNAME="pawuser"
export DB_PASSWORD="1234"
```

Important runtime detail:

- `run-project.sh` loads a root `.env` if present.
- `run-project.sh` does not auto-load `webapp/.env`.
- `run-project.sh` also tries to resolve and enforce JDK 21 before building.
- The helper image scripts do load both root `.env` and `webapp/.env`.
- In local `jetty:run`, the app has behaved as if it is mounted at the root context, so local URLs should be tested as `http://localhost:8080/...` rather than assuming `/webapp/...`.

## Runtime Wiring

`webapp/src/main/java/ar/edu/itba/paw/webapp/config/WebConfig.java`:

- enables Spring MVC
- scans `ar.edu.itba.paw.webapp.controller`, `ar.edu.itba.paw.services`, and `ar.edu.itba.paw.persistence`
- serves `/css/**`, `/js/**`, and `/favicon.ico`
- configures an `InternalResourceViewResolver` with prefix `/WEB-INF/jsp/`
- creates the datasource from environment variables first, then falls back to classpath `db.properties`
- runs `schema.sql` at startup through `DataSourceInitializer`

Because the resolver only sets the prefix, controllers currently return view names such as `cars.jsp` and `car-review.jsp`.

## Web Layer

Controllers currently present:

- `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/CarController.java`
- `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/CarReviewController.java`

Request behavior:

- `/` redirects to `/cars`
- `/cars` supports optional `brand` and `bodyType` filters
- `/cars/content` returns the filterable cars catalog fragment only
- `/car-image` on `GET` serves a stored car image by `carId`
- `/car-image` on `POST` accepts `multipart/form-data` upload with fields `carId` and `file`
- `/reviews` on `GET` requires `carId`; otherwise it redirects to `/cars`
- `/reviews` on `GET` also supports optional `sort=rating_asc|rating_desc`
- `/reviews/feed` returns the sortable reviews feed fragment only
- `/reviews` on `POST` creates a review and redirects back to `/reviews?carId=...`

Filtering and sorting are progressively enhanced:

- full-page `GET` behavior remains the fallback
- `webapp/src/main/webapp/js/enhanced-filters.js` intercepts eligible GET forms
- the browser fetches fragment HTML and replaces only the target section
- URL query parameters are kept in sync with `history.replaceState`

Views currently present:

- `WEB-INF/jsp/cars.jsp`
- `WEB-INF/jsp/cars-content.jsp`
- `WEB-INF/jsp/car-review.jsp`
- `WEB-INF/jsp/reviews-feed-fragment.jsp`
- `WEB-INF/jsp/legacy-index.jsp`

Reusable JSP tags currently present:

- `button.tag`
- `car-card.tag`
- `cars-content.tag`
- `create-review-modal.tag`
- `filter-chip.tag`
- `footer.tag`
- `latest-review.tag`
- `legacy-button.tag`
- `legacy-car-card.tag`
- `legacy-toast.tag`
- `nav.tag`
- `review-car-info.tag`
- `review-selected-car.tag`
- `reviews-feed.tag`
- `toast.tag`

Stylesheets currently present:

- `cars.css`
- `components.css`
- `design-system.css`
- `layout.css`
- `legacy-components.css`
- `reviews.css`

Client-side scripts currently present:

- `enhanced-filters.js`

## Domain Model

Main model classes under `model/src/main/java/ar/edu/itba/paw/model`:

- `BodyType`
- `Brand`
- `Car`
- `CarImage`
- `Review`
- `ReviewStats`
- `User`

`persistence/src/main/resources/schema.sql` creates:

- `users`
- `brands`
- `body_types`
- `cars`
- `car_images`
- `reviews`

It also seeds brands, body types, and cars.

Review identity currently supports guest submission:

- `reviews.user_id` is nullable
- `reviews.reviewer_email` stores the submitted email for guest reviews
- database constraint requires at least one reviewer identity source
- guest reviews are public in the UI but rendered as `anon`

Car image handling currently works like this:

- uploaded image bytes are stored in `car_images`, not inline on `cars`
- `Car` carries a derived `hasImage` flag for rendering decisions
- catalog pages avoid loading blob data and only fetch the actual image bytes through the image endpoint
- catalog cards lazy-load images; the selected review image is loaded eagerly
- current implementation serves the uploaded file as-is, without resized variants

## Services And Persistence

DAO contracts in `persistence-contracts`:

- `BodyTypeDao`
- `BrandDao`
- `CarDao`
- `CarImageDao`
- `ReviewDao`
- `UserDao`

Service contracts in `service-contracts`:

- `CarService`
- `ReviewService`
- `UserService`

DAO implementations in `persistence`:

- `BodyTypeJdbcDao`
- `BrandJdbcDao`
- `CarJdbcDao`
- `CarImageJdbcDao`
- `ReviewJdbcDao`
- `UserJdbcDao`

Service implementations in `services`:

- `CarServiceImpl`
- `ReviewServiceImpl`
- `UserServiceImpl`

Review stats support currently works like this:

- `ReviewStats` is a derived read model, not a separate table.
- `ReviewDao` and `ReviewService` expose aggregate lookups for one car or a batch of car ids.
- `ReviewJdbcDao` computes `review_count` and `average_rating` from `reviews`.
- `CarController` loads review stats for the car listing and exposes them as `reviewStatsByCarId`.
- `WEB-INF/tags/car-card.tag` renders rating summary data with a fallback when there are no reviews.
- `schema.sql` creates `idx_reviews_car_id` for review lookups and aggregate queries.

Review feed behavior currently works like this:

- `ReviewDao` and `ReviewService` support chronological review listing, rating-ascending listing, rating-descending listing, and latest-review lookup
- the main reviews feed can be sorted independently of the side-panel "latest review"
- the latest-review panel is resolved separately by newest `created_at`, not by the current feed order

Image upload and retrieval behavior currently works like this:

- `CarController` exposes `GET /car-image?carId=...` and `POST /car-image`
- uploads accept `multipart/form-data`
- allowed MIME types are `image/jpeg`, `image/png`, and `image/webp`
- upload size limit is `5 MB`
- image responses include `Content-Type`, `Content-Length`, `ETag`, `Last-Modified`, and cache headers
- local testing has been more reliable through `/car-image` than through the path-variable image routes

Helper scripts currently present:

- `scripts/download-seeded-car-images.sh`
  - parses the seeded image URLs from `schema.sql`
  - resolves the current `car_id` values from Postgres
  - downloads those source images into a local folder and writes `manifest.tsv`
- `scripts/upload-car-images.sh`
  - reads a manifest file
  - uploads all images to `POST /car-image`
  - defaults to `http://localhost:8080`

## Design Guidance

`DESIGN.md` defines the visual direction as "The Kinetic Gallery":

- dark showroom-inspired palette
- tonal layering instead of divider-heavy layouts
- Space Grotesk for display typography
- Inter for body copy
- asymmetry and editorial spacing over rigid card grids

When changing UI, preserve the existing server-rendered structure and keep styling aligned with that document.

## Testing

- Test coverage is minimal.
- The only committed test file currently present is `services/src/test/java/ar/edu/itba/paw/services/AppTest.java`.

## Known Quirks

- The root project targets Java 21, but `services/pom.xml` currently sets `maven.compiler.release` to `17`.
- `webapp/pom.xml` still carries an old JUnit 4 test dependency, while `services` uses JUnit 5 dependencies.
- The old `interfaces/` directory may still exist locally as leftover generated/build output, but it is no longer part of the Maven reactor.
- `AGENT.MD` is intended to be kept as a maintained project-context file, not deleted.
