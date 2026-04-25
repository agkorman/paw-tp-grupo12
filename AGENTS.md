# Repository Guidelines

## Purpose

This repository is an ITBA PAW course project: a server-rendered car listing and review web application. It is a multi-module Maven project using Spring MVC, Spring JDBC, JSP/JSTL, PostgreSQL, and Jetty for local execution. The UI is not a separate SPA; views, reusable tags, CSS, and JavaScript live inside `webapp`.

Use this file as the working guide for contributors and coding agents. Keep changes aligned with the existing layered architecture instead of introducing parallel patterns.

## Project Structure & Module Organization

The root `pom.xml` is the Maven reactor and defines these active modules:

- `model`: domain objects such as `Car`, `Brand`, `BodyType`, `CarImage`, `CarRequest`, `Review`, `ReviewStats`, and `User`.
- `persistence-contracts`: DAO interfaces. Controllers and services should not depend on JDBC implementation classes.
- `service-contracts`: service interfaces exposed to the web layer.
- `persistence`: Spring JDBC DAO implementations and database resources under `persistence/src/main/resources`.
- `services`: business logic implementations that coordinate DAOs and service contracts.
- `webapp`: Spring MVC controllers, configuration, JSPs, JSP tag files, CSS, JS, and WAR packaging.

Expected dependency direction:

```text
model -> contracts -> implementations -> webapp
```

Do not make lower-level modules depend on `webapp`. Keep domain classes free of Spring MVC, servlet, JSP, and persistence framework concerns.

Controllers must depend on service interfaces from `service-contracts`, not on DAOs from `persistence-contracts` or JDBC implementations from `persistence`. Business rules, authorization decisions, transaction boundaries, validation beyond request binding, and orchestration across repositories belong in services. Calling DAOs directly from controllers couples HTTP request handling to storage details, bypasses the service layer, duplicates business logic, makes controller tests heavier, and makes later persistence changes harder.

## Runtime Architecture

`webapp/src/main/java/ar/edu/itba/paw/webapp/config/WebConfig.java` is the main runtime wiring. It enables Spring MVC, scans controllers/services/persistence packages, serves static resources, configures JSP view resolution, initializes the datasource, configures mail, and runs SQL initialization.

Controllers currently live in:

- `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/CarController.java`
- `webapp/src/main/java/ar/edu/itba/paw/webapp/controller/CarReviewController.java`

The main user flows are:

- `/`: landing page.
- `/cars`: catalog, filters, search, and car creation.
- `/cars/content`: catalog fragment for progressive enhancement.
- `/car-image` and `/cars/{carId}/image`: image retrieval/upload endpoints.
- `/reviews`: car review page and review creation.
- `/reviews/feed`: review feed fragment.

Keep server-rendered fallback behavior working when adding JavaScript enhancements.

## Web Assets, Views, and Tags

JSP pages and fragments live under `webapp/src/main/webapp/WEB-INF/jsp`. Reusable JSP tags live under `webapp/src/main/webapp/WEB-INF/tags`. Static assets live under:

- `webapp/src/main/webapp/css`
- `webapp/src/main/webapp/js`

Prefer reusable JSP tags for repeated UI pieces such as car cards, navigation, review panels, toolbars, and modals. Keep large CSS in stylesheet files and large behavior in JS files, not inline in JSPs.

UI changes should respect `DESIGN.md`, especially the dark showroom-style visual direction, typography, spacing, and existing component language.

## Build, Test, and Development Commands

Common commands:

```bash
mvn clean install
mvn test
mvn -pl services test
mvn -pl webapp jetty:run
bash run-project.sh
```

Command meanings:

- `mvn clean install`: builds every module and installs artifacts into the local Maven repository.
- `mvn test`: runs tests for all modules through Maven Surefire.
- `mvn -pl services test`: runs only service module tests for faster feedback.
- `mvn -pl webapp jetty:run`: starts the web application locally on port `8080`.
- `bash run-project.sh`: resolves JDK 21, loads a root `.env` when present, builds, and starts Jetty.

The README documents the app at `http://localhost:8080/webapp`, but local Jetty behavior may mount the app at root in some setups. When verifying manually, check both `http://localhost:8080/` and `http://localhost:8080/webapp/` if needed.

## Environment and Configuration

The app expects Java 21, Maven 3.x, and PostgreSQL 16. Database configuration is read from environment variables first:

```bash
export DB_URL=jdbc:postgresql://localhost/pawdb
export DB_USERNAME=pawuser
export DB_PASSWORD=yourpassword
```

If environment variables are not available in the deploy target, `webapp/src/main/resources/db.properties` may be packaged with equivalent values. Do not commit real credentials. Use local, untracked configuration for secrets.

Mail configuration follows the same principle through environment variables or `mail.properties`. Car creation notification email is asynchronous; user-facing car creation must not block on SMTP success.

## Database and Data Model Notes

Primary schema resources live in `persistence/src/main/resources`:

- `schema.sql`: creates tables and seed data.
- `schema-pg-trgm.sql`: PostgreSQL trigram/search-related schema support, when used.
- `populate.sql`: supplemental seed data, when used.

`schema.sql` uses `CREATE TABLE IF NOT EXISTS`, so changing a table definition there does not migrate an already-created local database. For local testing after schema changes, recreate the database or add an explicit migration step.

Important model behavior:

- Reviews support guest submissions; `reviews.user_id` may be nullable and `reviewer_email` stores guest identity.
- Review stats are derived from `reviews`, not stored in a separate table.
- Car image bytes live in `car_images`; catalog views should avoid loading blobs except through image endpoints.
- Car submissions from users create only a pending `car_requests` row. The public `cars` row and its `car_images` bytes are created only when an admin approves the request.

## Maven Dependency Management

All dependency and plugin versions must be centralized in the root `pom.xml`.

Rules:

- Define version constants under the root `<properties>` section, for example `<spring.version>5.3.33</spring.version>`.
- Reference versions from root-level `<dependencyManagement>` or `<pluginManagement>` using `${property.name}`.
- Module `pom.xml` files must declare dependencies without `<version>`.
- Internal module dependencies should also be managed centrally in the root, using `${project.version}` there, so child modules do not repeat versions.
- Do not hard-code third-party versions inside module POMs.
- Avoid duplicating plugin version declarations in child modules unless a module has a clearly different plugin configuration need.

Preferred root pattern:

```xml
<properties>
  <spring.version>5.3.33</spring.version>
  <postgresql.version>42.7.3</postgresql.version>
</properties>

<dependencyManagement>
  <dependencies>
    <dependency>
      <groupId>org.postgresql</groupId>
      <artifactId>postgresql</artifactId>
      <version>${postgresql.version}</version>
    </dependency>
  </dependencies>
</dependencyManagement>
```

Preferred module pattern:

```xml
<dependency>
  <groupId>org.postgresql</groupId>
  <artifactId>postgresql</artifactId>
</dependency>
```

## Coding Style & Naming Conventions

Use package names under `ar.edu.itba.paw`. Follow the existing 4-space Java indentation style. Keep names descriptive and layer-specific:

- Controllers: `*Controller`
- Services: service interfaces in `service-contracts`, implementations as `*ServiceImpl`
- DAOs: DAO interfaces in `persistence-contracts`, JDBC implementations as `*JdbcDao`
- Tests: `*Test`
- JSP tags: lowercase, hyphenated names such as `car-card.tag`
- CSS/JS assets: lowercase, hyphenated names when adding new files

Prefer Spring-managed dependencies over manual object construction. Keep validation rules close to the service/controller boundary that owns them, and keep SQL-specific mapping inside persistence classes.

## Testing Guidelines

Tests belong in each module's `src/test/java` tree and should mirror production packages. Current committed coverage is minimal, with service tests using JUnit Jupiter and Maven Surefire.

When changing behavior:

- Add service tests for business rules, validation, sorting, filtering, and moderation decisions.
- Add persistence tests or focused DAO verification when changing SQL queries or mappings.
- Add web-layer checks when changing controller routing, request parameters, redirects, or multipart handling.
- Run `mvn test` before opening a PR.
- Use `mvn -pl <module> test` during development for faster feedback.

Do not rely only on browser checks for service or DAO behavior.

## Helper Scripts

Scripts under `scripts/` support local data and image workflows:

- `download-seeded-car-images.sh`
- `upload-car-images.sh`
- `populate-reviews.sh`
- `upload-reviews.sh`

Read the script before running it, confirm expected environment variables, and avoid committing generated local files. Some scripts assume a running local app and PostgreSQL database.

## Commit & Pull Request Guidelines

Recent commit messages are short and direct, for example `remove rating from landing review` and `expand image upload limit`. Keep that style:

- Use an imperative or descriptive summary.
- Keep each commit focused on one change.
- Mention schema, dependency, or configuration changes explicitly.
- Include tests run in the PR description.
- Add screenshots or short screen recordings for visible JSP/CSS/UI changes.
- Link the issue, task, or class requirement when applicable.

## Agent-Specific Instructions

Before editing, inspect the relevant module and follow its existing patterns. Do not refactor unrelated code while implementing a requested change. Do not delete `AGENTS.md`; it contains additional project context and known quirks.

Be careful with the current git state. There may be local untracked SQL or generated files. Do not revert or remove files you did not create unless explicitly asked.

When modifying Maven files, enforce the centralized version policy above. When adding web features, preserve non-JavaScript fallback paths and keep route behavior compatible with existing JSP forms and fragments.

## Image Uploads and Static Resource Policy

The previous project-specific agent guide had stricter image rules that are still important. Keep these rules in force when adding or changing image-related behavior.

- User-uploaded images must arrive as `multipart/form-data` and be received in Spring MVC with `MultipartFile`, either through individual `@RequestParam` values or a form-backing `@ModelAttribute` that includes the file.
- Do not transport, persist, or render project images as Base64 unless there is an explicit, exceptional approval.
- Keep Servlet 3 multipart parsing compatible. If upload initialization or `WebConfig` changes, preserve the configured multipart resolver behavior.
- Validate uploaded images on the server even when client-side validation exists. Required checks are: file exists, file is not empty, maximum size, allowed `content-type`, and the referenced entity exists before storing bytes.
- Store user-uploaded image bytes in dedicated image tables and expose them through backend endpoints. For cars, keep the established `cars` plus `car_images` pattern, where `car_images` owns binary data and `content_type`.
- Image endpoints that return database-backed images should set `Content-Type`, `Content-Length`, and appropriate cache headers when possible.
- `<img>` elements for uploaded images should point to internal project URLs, not inline blobs or Base64 strings.
- Static UI images such as logos, icons, and decorative illustrations belong in webapp static asset folders, not in the database.
- Reference static assets from JSPs and tag files with `c:url`.
- If a new static folder such as `/images/**` is added, expose it from `WebConfig.addResourceHandlers(...)`.
- Do not mix static assets and user uploads. Static images live in the repo and are served as resources; uploaded images are validated, persisted, and served through controller endpoints.
- Some legacy views or seed data may still use external `imageUrl` values. Treat that as legacy compatibility, not the direction for new code.
