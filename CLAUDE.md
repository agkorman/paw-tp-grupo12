# Agent Context

Rules and non-obvious constraints for this codebase. These are things you cannot derive just by reading the code.

---

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

- `@Transactional` goes on service methods only — never on DAO methods or controllers.
- Use `@Transactional(readOnly = true)` for read-only service methods.

## Controllers

- Use `@RequestMapping(value="...", method=RequestMethod.GET/POST)` — not `@GetMapping`/`@PostMapping`.
- Standard page-rendering methods return `ModelAndView` or `String` (logical view name). Image and AJAX endpoints return `ResponseEntity`.
- Access the authenticated user via `@AuthenticationPrincipal final AuthenticatedUser currentUser`.
- All controllers register `StringTrimmerEditor(true)` via `@InitBinder` to trim and nullify blank string inputs before they reach the service.
- Validate input at the controller boundary, then delegate to the service. Do not put validation logic inside services or JSPs.

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
- Every JSP starts with `<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>`.

## CSS & UI

- CSS rules go in dedicated files — never inline styles on elements.
- One CSS file per feature/page; shared design tokens live in `design-system.css` under `:root`.
- Use Flexbox and Grid for layout. Do not use HTML layout tables or inline styles for positioning.
- UI changes must follow the design language in `DESIGN.md` (dark showroom theme, color tokens, typography, component patterns). Prioritize clarity and usability over decoration.

## Security

- All routes are protected by Spring Security (`WebAuthConfig`). When adding a new endpoint, explicitly decide whether it is public or requires authentication/role, and add the corresponding `requestMatchers` rule.
- Password encoding uses `BCryptPasswordEncoder`. Do not use plaintext encoders.
- Any user-supplied redirect URL must be validated through `LoginRedirectUtils.safeRedirect()` before use. Never trust raw redirect parameters — they are an open-redirect vector.
- To gate UI sections by role in JSPs, use the Spring Security tag: `<sec:authorize access="hasRole('ADMIN')">`. Don't rely on a model attribute for role checks in views.
- CSRF tokens must be included in every mutating form. The hidden input pattern is: `<input type="hidden" name="${_csrf.parameterName}" value="${_csrf.token}">`. Spring's `<form:form>` includes it automatically; plain HTML `<form>` elements require it explicitly.

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

## Progressive Enhancement

- Every feature that uses JavaScript must also work without it. Forms must submit via standard POST, and the server must handle the redirect correctly. Do not make a user flow depend exclusively on JS.

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

## Maven

- All dependency versions are declared in the root `pom.xml` under `<dependencyManagement>`. Child modules declare dependencies without `<version>`.
- Do not hard-code third-party versions in module POMs.
