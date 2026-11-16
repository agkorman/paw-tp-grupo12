# paw-tp-grupo12

Car listing web application — ITBA PAW course project.

## Requirements

- Java 21
- Maven 3.x
- PostgreSQL 16
- Node.js 20.19+ and npm 10+ (for the React build)

## Database setup

Create a database and a user:

```sql
CREATE USER pawuser WITH PASSWORD 'yourpassword';
CREATE DATABASE pawdb OWNER pawuser;
```

## Environment variables

The app reads DB, mail, and auth configuration from environment variables. Set them before running:

```bash
export DB_URL=jdbc:postgresql://localhost/pawdb
export DB_USERNAME=pawuser
export DB_PASSWORD=yourpassword

export MAIL_HOST=smtp.example.com
export MAIL_PORT=587
export MAIL_USERNAME=your-smtp-user
export MAIL_PASSWORD=your-smtp-password

export AUTH_JWT_SECRET="$(openssl rand -base64 32)"
```

`AUTH_JWT_SECRET` is required for access-token signing. It must be Base64 encoding of at least
32 cryptographically random bytes. Generate it once per environment and retain it across restarts;
do not commit or log it. The application refuses to start if it is missing or invalid. JWT signing
has no properties-file fallback. Use a different secret for each environment.

For DB and mail configuration, if your deploy target does not let you
configure Tomcat environment variables, you can instead
package `db.properties` and `mail.properties` files inside the WAR at
`webapp/src/main/resources/`:

```properties
# db.properties
db.url=jdbc:postgresql://localhost/pawdb
db.username=pawuser
db.password=yourpassword

# mail.properties
mail.host=smtp.example.com
mail.port=587
mail.username=your-smtp-user
mail.password=your-smtp-password
```

## Running locally

```bash
mvn clean install
mvn -pl webapp jetty:run-war
```

Open `http://127.0.0.1:8080/webapp/app/login` for the React login. Both Jetty goals use `/webapp`, matching the Vite proxy; override the local context with `-Dpaw.contextPath=/another-context` if needed. Using `127.0.0.1` also avoids unrelated cookies accumulated by other apps on `localhost`. Startup applies the idempotent schema and required lookup data.

## Authentication verification

`mvn clean install` runs the Java suites, frontend tests, TypeScript check and production build. The session concurrency test requires PostgreSQL; without the following variables, it is skipped and the remaining transaction tests use HSQLDB:

```bash
export PAW_AUTH_TEST_JDBC_URL=jdbc:postgresql://127.0.0.1:5432/paw_auth_test
export PAW_AUTH_TEST_USERNAME=your-test-user
export PAW_AUTH_TEST_PASSWORD=your-test-password
mvn clean install
```

Create a dedicated test database first. The transaction suite creates and drops a random schema inside it; the database user needs permission to create schemas. Do not point test runs at a production database.

## Identity API

Paths below are relative to the deployment context (locally `/webapp`). Negotiate responses with
`Accept: application/vnd.paw.<resource>.v1+json`, using `index`, `user`, or `session` as the resource.
Errors use `application/vnd.paw.error.v1+json` and private/token responses are `no-store`.

| Method and resource | Behavior |
| --- | --- |
| `GET /api` | Public index with links. Basic email/password authentication here emits `X-Access-Token`, `X-Refresh-Token`, and canonical user/session `Link` headers. |
| `POST /api/users` | Creates an account from `displayName`, `email`, and `password`; responds `201` with `Location` and its public representation. |
| `GET /api/users/{id}` | Public `id`, `displayName`, and links. Only the owner also receives email, preferred locale, and the request's effective roles. |
| `PATCH /api/users/{id}` | Owner-only partial update of `displayName` and/or `preferredLocale` (`es` or `en`); responds with the updated user. Omitted fields stay unchanged; null, empty patches, and other fields are rejected. Both changes commit atomically. |
| `GET /api/users/{id}/sessions/{sessionId}` | Owner-only active session metadata. Missing, foreign, expired, or revoked sessions respond `404`. |
| `DELETE /api/users/{id}/sessions/{sessionId}` | Revokes only the credential's own session and responds `204`. Accepts an access token or its current refresh; the latter is verified without rotation or replacement tokens. |

User creation and patch bodies require `Content-Type: application/vnd.paw.user.v1+json`.
Conflicting email/display name responds `409` with a stable error code and field errors when known.
Passwords require at least 8 characters and at most 72 UTF-8 bytes (BCrypt's input limit).
Registration preserves the existing claim of pre-registration reviews and car requests. After the
`201`, clients authenticate through the index using the form's credentials without persisting the password.

Normal requests use `Authorization: Bearer <access>`. Only `401 TOKEN_EXPIRED` permits one retry of
the original request with the current refresh as Bearer. Save replacement headers even if the resource
then returns an error. Logout does not invalidate already issued access tokens before their expiry.

## Frontend base

The React + TypeScript app lives in `frontend/`. Maven runs `npm ci`, the frontend tests, the TypeScript check, and the Vite production build as part of `mvn clean package`. The generated files are packaged under `WEB-INF/spa/` in the WAR and served under `/app` within the servlet context, including direct visits to nested React routes. `/app/assets/**` serves the generated assets; missing assets return 404, never the SPA HTML. `/api/**` remains Jersey-owned and other MVC routes keep their current handlers. Use `jetty:run-war` after building to exercise the packaged SPA; source-only `jetty:run` does not include the generated WAR resources.

For standalone frontend development:

```bash
cd frontend
npm ci
npm run dev
```

Vite proxies `/webapp/api` (and its descendants) to `http://localhost:8080` by default, preserving the path and public `Host`. Canonical `Link` and `Location` URLs from the backend can therefore be followed without rewriting them. Set `PAW_DEV_API_ORIGIN` to change the backend origin. Set `PAW_API_CONTEXT_PATH` before starting/building the frontend for a different WAR context (default `/webapp`; empty or `/` for the root context). `PAW_DEV_API_CONTEXT_PATH` optionally overrides it in development only.

Vite development serves the SPA at `/webapp/app/` by default and injects the API and SPA base paths. Production HTML contains deployment placeholders: `SpaController` replaces them with the actual servlet context for the API metadata, router basename and HTML base URL. The same WAR can therefore run at `/webapp`, another context, or the root without rebuilding. Do not serve `frontend/dist/index.html` directly without replacing these placeholders.

The shared Axios transport lives in `frontend/src/shared/api/httpClient.ts`. App composition in `frontend/src/app/auth.ts` creates one client with injected authentication callbacks and supplies it through `HttpClientProvider`. Features obtain it with `useHttpClient()` and pass it to their resource API functions; `shared` does not import auth features.

- `request<T>` returns `{ data, status, headers, url }`; header names are lowercase. It retains `Link`, `Location`, cache and pagination headers. `204`, `HEAD` and `304` have no data; a `304` must be combined with a previously cached representation by the consumer.
- Each feature supplies its resource's `Accept` and body `Content-Type`. Requests without a body omit `Content-Type`; `FormData` lets the browser generate the multipart boundary. Use `responseType: 'blob'`, `'arraybuffer'` or `'text'` for non-JSON resources.
- The timeout is 30 seconds per HTTP attempt; use `timeoutMs` for larger uploads and `signal` for cancellation. URLs outside the configured API origin/path are rejected before obtaining credentials. Redirects are refused and browser cookies are omitted.
- Automatic requests obtain credentials through `getAuthorization`. Initial Basic authentication and explicit refresh logout use `authentication: 'manual'` with an `Authorization` header; anonymous registration uses `authentication: 'none'`.
- `onResponse` receives headers before body decoding or status rejection, so rotated credentials can be saved even on resource errors. `recoverExpiredToken` is called only for an automatic request with credentials receiving `401 TOKEN_EXPIRED`. It owns refresh coordination and may call the supplied `retry(authorization)` once, repeating the original request. There are no automatic retries for other HTTP failures, connection failures or timeouts.
- `ApiClientError` distinguishes `http`, `network`, `timeout`, `canceled`, `invalid-response` and `configuration`. HTTP errors retain status, code, field errors, body and diagnostic headers, excluding credential headers. They never retain Axios request/config objects. Screens translate codes and decide how to display errors or navigate.

## Frontend authentication state

`AuthProvider` exposes `useAuth()` with `status`, the owner `user`, localized notice codes, and `login`, `logout`, `retryProfile`, and `changeLocale` operations. Initial restoration runs once, including under React StrictMode. Public pages remain available throughout restoration. Consumers must render private content only when `status === 'authenticated'`; `restoring`, `authenticating`, and `unavailable` do not expose a user. Login and registration screens are available at `/app/login` and `/app/register`. The header provides login/logout navigation. The old GET `/login` and `/register` URLs redirect to React; the old authentication form POSTs no longer authenticate or create accounts.

- Startup reads a refresh from the selected browser storage, requests `GET /api` with that refresh, then follows the authenticated-user relation in `Link`. Resource URLs are confined to this deployment's API. Access tokens and user/profile data stay in memory; storage contains only the refresh, user/session URLs, remember-me choice and a format version. Keys are scoped to the API URL. Saving clears the other store's copy; conflicting or malformed saved records are discarded.
- Within one tab, simultaneous expired requests share one rotation. The leading original request spends the refresh; waiting requests retry with the resulting access. Each request still has a single retry limit. New credentials are saved before interpreting the resource's status/body. A lost rotation response clears the local session and requires a new login without replaying the uncertain operation; ordinary access-token connection failures do not sign the user out.
- Failed auth persistence prevents completing login and attempts to revoke the newly issued session. Failed persistence during rotation likewise clears local identity and attempts revocation. There is no silent memory-only fallback. If browser storage cannot be cleared, the UI explicitly reports that limitation.
- Logout immediately removes local credentials and the current profile, then deletes the canonical session using its refresh without rotation. If revocation cannot be confirmed, the UI reports it. Responses from a previous identity generation cannot repopulate credentials or return stale private data to consumers. If a late login/rotation returns usable new credentials after logout, the client attempts to revoke them.
- After login/restoration and each rotation, the owner resource supplies current effective roles and the profile language. A profile-fetch failure preserves the already rotated refresh and exposes an `unavailable` state with a retry action. Explicit language changes PATCH the owner resource before applying the choice. Before login, locale selection is local preference, then a supported browser language, then Spanish. Features adding private query caches must scope them to the authenticated identity and clear them when that identity ends; no private query cache has been introduced in this step.

**Deferred by agreement: coordination between tabs and the deployment HTTPS decision.** Rotation serialization and logout cleanup currently apply only to the current tab. Do not treat multi-tab use of the same session as complete: shared `localStorage` refreshes (or copied `sessionStorage` credentials) can still race and trigger server-side reuse revocation; logout propagation is not implemented. Resolve and test that coordination before completing authentication delivery. P1/P3 also need the final notification about retiring the legacy authentication consumers when their replacement is complete.

## Authentication screens and migration cutover

Forms use React Hook Form + Zod. Fields validate on first blur and on submit, then update as they are corrected; server field errors use localized codes and focus the first invalid field. Registration preserves password confirmation in the form only, validates the 72 UTF-8 byte BCrypt limit, and never trims the password. Duplicate submissions are blocked. Passwords are cleared after submission and never placed in URLs, browser history or storage.

Registration discovers the users collection from the API index and sends an anonymous `POST` with the user media type. A confirmed `201` is followed by Basic authentication. If that login fails, the login screen reports that the account was created, pre-fills the email through one-time navigation state and leaves the password empty. An uncertain POST result is not automatically retried. After login/registration, only an internal SPA destination is accepted; absent/unsafe/auth-loop destinations fall back to the React home. No original domain mutation is automatically executed. Legacy MVC return destinations outside `/app` fall back to the React home until those consumers migrate.

MVC form login, cookie remember-me, logout handlers, session identity loading, the in-memory session registry and their orphaned views/scripts/styles/translations have been removed. `AUTH_REMEMBER_ME_KEY` and `auth.properties` are no longer used. Existing database users, passwords and domain data remain unchanged. Existing HTTP session cookies no longer authenticate even legacy MVC requests. Public MVC reads remain available, but protected MVC operations must be migrated to React/API by their owners; there is intentionally no cookie/JWT bridge. The legacy locale resolver remains while JSP pages consume it; it does not establish an authenticated identity. Admin role approval updates the database; existing access tokens retain their roles until expiry and renewal picks up the new role, as agreed.

P1/P3 handoff reminder for the final delivery: use the shared HTTP client and auth provider, register React routes under `/app`, migrate protected MVC consumers to their API resources, and review the agreed remaining cross-tab coordination work. No message has been sent yet in this implementation step.
