# Agent Context

Rules and non-obvious constraints for this codebase. These are things you cannot derive just by reading the code.

---

Use this file as the working guide for contributors and coding agents. Keep changes aligned with the existing layered architecture instead of introducing parallel patterns.

## Purpose

This repository is an ITBA PAW course project: a server-rendered car listing and review web application. It is a multi-module Maven project using Spring MVC, Spring JDBC, JSP/JSTL, PostgreSQL, and Jetty for local execution.

## Commands

```bash
mvn clean install        # build all modules
mvn -pl webapp jetty:run # start dev server at http://localhost:8080/webapp
mvn test                 # run all tests
mvn -pl services test    # run only service tests (faster)
```

Required env vars: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD` (PostgreSQL 16). Mail: `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`.

---

## Architecture & Layers

- Dependency direction is strict: `webapp → service-contracts → persistence-contracts → model`. Never skip layers. Controllers must not call DAO interfaces directly.
- Business and application logic belongs exclusively in the service layer. Controllers receive input, validate, call a service, and choose a view. Logic in controllers or JSPs is a design violation.
- The service layer is the business facade. Each service method should express a use case, not expose raw DAO operations.
- All web and service components must be stateless and thread-safe. Never store shared mutable state in Spring-managed beans. Shared state lives in the database.
- Use Spring-managed beans for all application components. Never instantiate services, DAOs, or repositories with `new` inside business flow code.
- Use stereotypes correctly: `@Controller`, `@Service`, `@Repository`. Do not mix them.
- Controllers depend on service interfaces from `service-contracts`. Services depend on DAO interfaces from `persistence-contracts`. Neither should import implementation classes from `persistence` or reference JDBC-specific types.

## Module Naming

Modules are `model`, `persistence-contracts`, `service-contracts`, `persistence`, `services`, `webapp`. The contracts modules are not named `interfaces`.

## Transactions

- In production code, `@Transactional` goes on service methods only — never on DAO methods or controllers.
- Use `@Transactional(readOnly = true)` for read-only service methods.

## Controllers

- Use `@RequestMapping(value="...", method=RequestMethod.GET/POST)` — not `@GetMapping`/`@PostMapping`.
- Standard page-rendering methods return `ModelAndView` or `String` (logical view name). Image and AJAX endpoints return `ResponseEntity`.
- Access the authenticated user via `@AuthenticationPrincipal final AuthenticatedUser currentUser`.
- All controllers register `StringTrimmerEditor(true)` via `@InitBinder` to trim and nullify blank string inputs before they reach the service.
- Validate input at the controller boundary, then delegate to the service. Do not put validation logic inside services or JSPs.
- Use `GlobalExceptionHandler` for exception handling that is shared across controllers. Prefer reusable web exceptions and centralized mappings over private controller exception classes or repeated controller-local `@ExceptionHandler` methods.
- Shared model attributes used by multiple car/admin/review views belong in `SharedModelAttributesAdvice`. `brands`, `bodyTypes`, and the default `carForm` are already populated there; do not re-add them with repeated `model.addAttribute(...)` / `mav.addObject(...)` calls in individual controllers.
- Keep `@ControllerAdvice` focused and narrow: use it only for cross-controller concerns that are genuinely reused. Page-specific model data, modal-open flags, validation errors, selected filters, review/profile data, and form attributes that need request-specific setup stay in the owning controller.

## Services & DAOs

- Services use constructor injection (`@Autowired` on constructor, `final` fields).
- Return `Optional<T>` for single results; return empty collections (never `null`) for list results.
- DAOs define SQL fragments and `RowMapper` instances as `private static final` constants.
- Use `SimpleJdbcInsert` for INSERT; use `NamedParameterJdbcTemplate` for queries with many parameters.

## Views

- JSPs live under `WEB-INF/jsp/` and are resolved by the configured `InternalResourceViewResolver`. Controllers return logical names, not full paths.
- Views must not contain business logic. Use JSTL and EL only — no raw Java scriptlets.
- Always escape user-visible output: `<c:out value="${...}"/>` for text, `fn:escapeXml()` for values inside HTML attributes. Never print raw `${...}` where user-controlled data can appear (XSS prevention).
- Use `<c:url>` for all application URLs. Never hardcode context paths.
- Custom tags use the `pa:` namespace prefix and live in `WEB-INF/tags/`.
- Reuse the existing confirmation modal component whenever a confirmation UI is needed. Do not create a second modal pattern or inline ad hoc confirmation dialogs.
- Every JSP starts with `<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>`.

## Internationalization (i18n)

- All user-visible UI text belongs in the message bundles: `webapp/src/main/resources/messages.properties` and `webapp/src/main/resources/messages_en.properties`. Do not add hardcoded Spanish/English copy directly in JSPs, tag files, controllers, form validation annotations, or JavaScript when it is meant for users.
- Use Spring messages in JSP/tag files: declare `<%@ taglib prefix="spring" uri="http://www.springframework.org/tags" %>` and render with `<spring:message code="..."/>`. For HTML attributes, resolve into a variable first with `var` and escape it when needed.
- Bean Validation messages use bundle keys in braces, e.g. `@NotBlank(message = "{validation.review.body.required}")`. Spring MVC validation is wired to the app `MessageSource`; keep it that way when touching `WebConfig`.
- Key naming standard is dot-separated and domain-first: `domain.section.element.state`. Examples: `cars.form.description`, `review.feed.empty`, `admin.catalogRequest.title`, `auth.login.error`.
- Validation keys use `validation.entity.field.rule`, e.g. `validation.car.model.max`, `validation.review.rating.required`. Shared validation keys may use `validation.option.required` or `validation.email.invalid`.
- Shared UI labels/actions go under `common.*`; JavaScript-facing messages go under `js.*`; type mismatch keys keep Spring's required names such as `typeMismatch.reviewForm.rating`.
- Do not translate or externalize technical strings: route paths, view names, request parameter names, enum-like persisted values (`pending`, `approved`, `rejected`), CSS classes, data attributes, DOM ids, JS event names, SQL, and log/debug-only strings.
- When adding new UI copy, add the same key to both `messages.properties` and `messages_en.properties` in the matching section in the same change. Keep keys unique and descriptive; never reuse a key for unrelated text just because the current Spanish or English value matches.

## CSS & UI

- CSS rules go in dedicated files — never inline styles on elements.
- One CSS file per feature/page; shared design tokens live in `design-system.css` under `:root`.
- Use Flexbox and Grid for layout. Do not use HTML layout tables or inline styles for positioning.
- UI changes must follow the design language in `DESIGN.md` (dark showroom theme, color tokens, typography, component patterns). Prioritize clarity and usability over decoration.
- Hide native browser spinner arrows on `input[type="number"]` globally. Numeric fields should use normal text-field styling plus explicit controls only when the UI intentionally provides them.

## Icons

- All SVG icons are centralized in `WEB-INF/tags/icon.tag`. Never write inline SVGs in JSP or tag files — always use `<pa:icon name="..." size="..."/>`.
- `star-icon.tag` is the only intentional exception: it has gradient fill-percent logic that cannot be expressed as a simple SVG.
- Do not use external icon fonts (e.g. Material Symbols, Font Awesome). All icons are self-contained SVGs in `icon.tag`.
- When adding a new icon, add it to `icon.tag` first, then reference it by name everywhere it is needed.

## Security

- All routes are protected by Spring Security (`WebAuthConfig`). When adding a new endpoint, explicitly decide whether it is public or requires authentication/role, and add the corresponding `requestMatchers` rule.
- Password encoding uses `BCryptPasswordEncoder`. Do not use plaintext encoders.
- Any user-supplied redirect URL must be validated through `LoginRedirectUtils.safeRedirect()` before use. Never trust raw redirect parameters — they are an open-redirect vector.
- To gate UI sections by role in JSPs, use the Spring Security tag: `<sec:authorize access="hasRole('ADMIN')">`. Don't rely on a model attribute for role checks in views.
- CSRF tokens must be included in every mutating form. The hidden input pattern is: `<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">`. Spring's `<form:form>` includes it automatically; plain HTML `<form>` elements require it explicitly.

## Logging

- Logging API is **SLF4J**, backed by **Logback**. Spring framework logs route through `spring-jcl` to SLF4J automatically. Do not use `java.util.logging`, `System.out`, `System.err`, Log4j, or commons-logging directly.
- Every class that logs declares a single static final logger as its first field:
  ```java
  private static final Logger LOGGER = LoggerFactory.getLogger(CurrentClass.class);
  ```
  Imports are `org.slf4j.Logger` and `org.slf4j.LoggerFactory`.
- Always use parameterized logging (`LOGGER.info("created user id={}", id)`) — never string concatenation. When logging an exception, pass it as the LAST argument and do NOT include `e.getMessage()` in the format string: `LOGGER.error("failed to load car id={}", carId, e);`.
- Log level conventions:
  - `DEBUG`: routine reads/lookups, verbose flow used only for troubleshooting.
  - `INFO`: business state changes — creates, updates, deletes, approvals, rejections, login success, registration, follow/unfollow, like/unlike, favorite toggle, role grants.
  - `WARN`: recoverable problems — validation failures the system handled, missing optional resource, async retry, swallowed non-critical exceptions.
  - `ERROR`: unexpected exceptions, persistence/IO failures that prevent an operation from succeeding. `GlobalExceptionHandler` logs unhandled 5xx as `ERROR` and 4xx as `WARN`.
- Never log secrets: passwords, raw tokens, CSRF values, full image bytes, or full email bodies. Logging emails, usernames, and IDs is fine.
- Logback configs live under `webapp/src/main/resources/logback.xml` (production: root `WARN`, `RollingFileAppender` to `logs/paw-2026a-12.%d{yyyy-MM-dd}.log`, `maxHistory=5`) and `webapp/src/test/resources/logback-test.xml` (test/local: root `INFO`, `ConsoleAppender` to stdout). Both define a `logFormat` property used by the encoder pattern.

## Email

- All email sending is done from the service layer via `EmailService`. Controllers and JSPs must never send email directly.
- Every `EmailService` method is annotated `@Async("mailTaskExecutor")`. Email must never block the HTTP response.

## Images

- Store uploaded image bytes in dedicated image tables (`car_images`, `car_request_images`). Never store images as Base64, never in the main entity table.
- Validate uploads on the server regardless of client-side validation: file not null, not empty, within size limit, allowed content-type, magic-byte check via `ImageSignatureValidator`, and the referenced entity exists.
- Expose stored images through backend controller endpoints, not inline in HTML. `<img>` tags point to internal URLs.
- Static UI assets (logos, icons) belong in `webapp/css` or `webapp/js` resource folders, not in the database.

## Authentication Identity

- The login credential is the user's **email**, not their username. Spring Security's `UserDetailsService` receives an email in the `loadUserByUsername` parameter.
- `AuthenticatedUser.getUsername()` returns the email (as required by the `UserDetails` contract). Use `AuthenticatedUser.getDisplayName()` to get the human-readable username for display.
- Do not confuse email and username when rendering user identity in views or when querying users.

## AJAX / Dual-Response Endpoints

- Some endpoints serve both browser and AJAX clients from the same URL. They detect AJAX via the `X-Requested-With: XMLHttpRequest` request header and return a plain-text or JSON body instead of a redirect.
- When adding a new endpoint that must support both, check the header explicitly: `"XMLHttpRequest".equals(request.getHeader("X-Requested-With"))`. Return `ResponseEntity` for AJAX, redirect for browser.

## Post-Redirect-Get (PRG)

- After every successful mutating POST (create, update, delete, like, favorite, follow), redirect to a GET. Never render a page directly from a POST handler. Use `"redirect:/path"` return values.

## String Normalization

- Always use `Locale.ROOT` when calling `.toLowerCase()` or `.toUpperCase()` on application strings (e.g. filter values, enum-like strings). Never use the default locale — it produces incorrect results for some system locales (Turkish, Greek).

## `schema.sql` — Idempotency Rules

`schema.sql` runs on every application startup against a live database that already has data. Every statement must be safe to run on an already-initialized database.

- Tables: always `CREATE TABLE IF NOT EXISTS`.
- Columns: always `ALTER TABLE ... ADD COLUMN IF NOT EXISTS`. New columns must be nullable or have a default so that existing rows are not broken.
- Constraints: always `DROP CONSTRAINT IF EXISTS` before `ADD CONSTRAINT`, so re-runs do not fail on already-existing constraints.
- Indexes: always `CREATE INDEX IF NOT EXISTS`.
- Seed inserts: always use `ON CONFLICT (...) DO NOTHING` or `ON CONFLICT (...) DO UPDATE`.
- Data updates (e.g., translating descriptions, populating new columns): use `UPDATE ... WHERE <condition>`, not an `INSERT`. The `INSERT ... ON CONFLICT DO NOTHING` will silently skip existing rows, so description changes must be `UPDATE` statements.
- Never use `DROP TABLE`, `TRUNCATE`, or destructive column changes (`DROP COLUMN`, changing a nullable column to `NOT NULL` without a default and a backfill) unless explicitly authorized. The goal is always backwards-compatible, non-destructive evolution.

## Image Caching

- When adding a new image endpoint, follow the ETag + `Cache-Control` pattern used in the existing image endpoints in `CarController`.

## Domain Constraints to Know

- Review ratings are `BigDecimal` on a **0.0–5.0 scale** stored as `NUMERIC(3,1)`. Do not assume a 0–10 scale or integer type.
- Car request statuses are plain strings: `"pending"`, `"approved"`, `"rejected"`. There is no Java enum — these values are enforced by a database CHECK constraint.

## Search

- Full-text search uses a PostgreSQL `tsvector` column on `cars` maintained by a trigger. Short queries (under 2 chars) fall back to LIKE. When modifying search, both paths must stay consistent.

## Filters & Pagination

- Search/filter parameters are bound through `CarSearchCriteria` as a `@ModelAttribute`, not as individual `@RequestParam` fields. New filter parameters belong in `CarSearchCriteria`, not as extra controller arguments.
- The app currently has no pagination. Do not add pagination infrastructure until explicitly asked.

## Views & Tags

- Prefer JSP tag files (`WEB-INF/tags/`) for any UI piece that is reused or complex enough to deserve isolation. When building new views, modularize into tags rather than writing monolithic JSPs.
- Tag attributes are declared at the top of the tag file with `<%@ attribute name="..." required="..." %>` before any markup.

## Model Objects

- Domain objects are plain POJOs: hand-written getters/setters, constructor delegation via `this()`, implement `Serializable`. Lombok is prohibited — do not introduce it.

## Testing (Persistence Layer)

- Persistence tests live under `persistence/src/test/java/ar/edu/itba/paw/persistence/`.
- Stack: JUnit 5 + Spring Test + HSQLDB in memory. The `persistence` module declares these as test dependencies; versions stay pinned in the root `pom.xml` `<dependencyManagement>`.
- Persistence tests use real Spring-managed DAOs with `@Autowired`, a real test `DataSource`, and `TestConfiguration`. Do not mock DAOs or use Mockito in this layer.
- `TestConfiguration` uses HSQLDB with `jdbc:hsqldb:mem:paw;sql.syntax_pgs=true`, defines a `DataSourceTransactionManager`, and component-scans only `ar.edu.itba.paw.persistence`.
- The HSQLDB schema is `persistence/src/test/resources/test-schema.sql`. Keep it minimal and faithful to the DAO behavior under test: table/column names, relevant FKs, uniqueness, checks, nullability, and relationships. Do not try to port the full PostgreSQL production schema.
- Do not add global seed data. Each test prepares its own state explicitly in `// Arrange`; tests must not depend on execution order or data created by another test.
- The shared persistence test base uses Spring Test with `@Transactional` and `@Rollback` so every test method runs in its own transaction and rolls back automatically. Keep rollback explicit in the base class.
- Test layout follows Arrange / Exercise / Assertions, separated by comment markers. `// Arrange` must never be empty; even null, blank, or empty-collection cases should assign the input to a local variable first. The Exercise section must be a single-line invocation of the DAO method under test.
- Mutating DAO tests must assert both the returned value and the real persisted side effect in the database. Validate DB state with the `JdbcTemplate` available in `AbstractPersistenceTest`, using explicit SQL queries for inserted, updated, deleted, created relation, removed relation, and untouched-row checks. Do not rely only on the returned object, affected row count, Optional/list presence, DAO read-back methods, or `JdbcTestUtils`.
- Run with `mvn -pl persistence test`.

## Testing (Service Layer)

- Service-layer unit tests live under `services/src/test/java/ar/edu/itba/paw/services/`.
- Stack: JUnit 5 (`junit-bom` 5.11.0) + Mockito 5.23.0 (`mockito-core` and `mockito-junit-jupiter`). Versions are pinned in the root `pom.xml` `<dependencyManagement>`; the `services` module declares the deps with `<scope>test</scope>` and no version.
- Each test class uses `@ExtendWith(MockitoExtension.class)`, `@Mock` for DAOs/collaborators, and `@InjectMocks` for the service under test. Do not use `@BeforeEach`, `@RunWith(MockitoJUnitRunner.class)`, manual mock init, or `MockitoAnnotations.openMocks(this)`.
- Interaction verification is forbidden in this layer: do not use `Mockito.verify`, `verify(...)`, `Mockito.spy`, `spy(...)`, `times(...)`, `never(...)`, `atLeast(...)`, `atMost(...)`, or any other interaction check. Tests assert observable results, not implementation details.
- Every test must have at least one meaningful assertion on the returned value or thrown exception. Tests without assertions, or that only check non-null, are not allowed.
- Test layout follows Arrange / Exercise / Assertions, separated by comment markers. `// Arrange` must never be empty; even null, blank, or empty-collection cases should assign the input to a local variable first. The Exercise section must be a single-line invocation of the method under test.
- Scope: only services with non-trivial logic are covered (validation, normalization, state transitions, scoring, exception swallowing, and meaningful branching around missing data or collaborator failures). Pure delegation methods are not tested artificially.
- When adding or changing service implementations, review the corresponding service test coverage and add focused tests for new non-trivial behavior in the same change.
- DB, network, filesystem, sleeps, and shared mutable state are forbidden in unit tests. Run with `mvn -pl services test`.

## Maven

- All dependency versions are declared in the root `pom.xml` under `<dependencyManagement>`. Child modules declare dependencies without `<version>`.
- Do not hard-code third-party versions in module POMs.
- Spring Boot dependencies are **FORBIDDEN**. This is a plain Spring MVC project (Spring Framework, not Spring Boot). Never add `spring-boot-*`, `spring-boot-starter-*`, or any Boot autoconfiguration artifacts. Configuration is explicit Java/XML config wired through `WebConfig`/`WebAuthConfig`.
