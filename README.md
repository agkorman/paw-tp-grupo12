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

export AUTH_REMEMBER_ME_KEY=replace-with-a-random-secret-at-least-32-chars
export AUTH_JWT_SECRET="$(openssl rand -base64 32)"
```

`AUTH_JWT_SECRET` is required for access-token signing. It must be Base64 encoding of at least
32 cryptographically random bytes. Generate it once per environment and retain it across restarts;
do not commit or log it. The application refuses to start if it is missing or invalid. JWT signing
has no properties-file fallback. Use a different secret for each environment.

For DB, mail, and legacy remember-me configuration, if your deploy target does not let you
configure Tomcat environment variables, you can instead
package `db.properties`, `mail.properties`, and `auth.properties` files inside the WAR at
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

# auth.properties
auth.rememberMeKey=replace-with-a-random-secret-at-least-32-chars
```

## Running locally

```bash
mvn clean install
mvn -pl webapp jetty:run
```

The app starts on `http://localhost:8080`. The database schema and seed data are created automatically on first startup.

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

The React + TypeScript app lives in `frontend/`. Maven runs `npm ci`, the frontend tests, the TypeScript check, and the Vite production build as part of `mvn clean package`. The generated files are packaged under `WEB-INF/spa/` in the WAR. Their public URL and the SPA route fallback will be configured alongside the Jersey and security servlet mappings.

For standalone frontend development:

```bash
cd frontend
npm ci
npm run dev
```

Vite proxies `/webapp/api` (and its descendants) to `http://localhost:8080` by default, preserving the path and public `Host`. Canonical `Link` and `Location` URLs from the backend can therefore be followed without rewriting them. Set `PAW_DEV_API_ORIGIN` to change the backend origin. Set `PAW_API_CONTEXT_PATH` before starting/building the frontend for a different WAR context (default `/webapp`; empty or `/` for the root context). `PAW_DEV_API_CONTEXT_PATH` optionally overrides it in development only.

Vite writes the resulting API path into `<meta name="paw-api-base-path" content="/webapp/api">`. The shared client resolves it against the page origin, independently of the current SPA route. When P1 configures the WAR's SPA entry page, it can replace this metadata with the actual servlet context path to support deploying the same build under another context. Do not publish a build with metadata that differs from the backend context.

The shared Axios transport lives in `frontend/src/shared/api/httpClient.ts`. App composition in `frontend/src/app/auth.ts` creates one client with injected authentication callbacks and supplies it through `HttpClientProvider`. Features obtain it with `useHttpClient()` and pass it to their resource API functions; `shared` does not import auth features.

- `request<T>` returns `{ data, status, headers, url }`; header names are lowercase. It retains `Link`, `Location`, cache and pagination headers. `204`, `HEAD` and `304` have no data; a `304` must be combined with a previously cached representation by the consumer.
- Each feature supplies its resource's `Accept` and body `Content-Type`. Requests without a body omit `Content-Type`; `FormData` lets the browser generate the multipart boundary. Use `responseType: 'blob'`, `'arraybuffer'` or `'text'` for non-JSON resources.
- The timeout is 30 seconds per HTTP attempt; use `timeoutMs` for larger uploads and `signal` for cancellation. URLs outside the configured API origin/path are rejected before obtaining credentials. Redirects are refused and browser cookies are omitted.
- Automatic requests obtain credentials through `getAuthorization`. Initial Basic authentication and explicit refresh logout use `authentication: 'manual'` with an `Authorization` header; anonymous registration uses `authentication: 'none'`.
- `onResponse` receives headers before body decoding or status rejection, so rotated credentials can be saved even on resource errors. `recoverExpiredToken` is called only for an automatic request with credentials receiving `401 TOKEN_EXPIRED`. It owns refresh coordination and may call the supplied `retry(authorization)` once, repeating the original request. There are no automatic retries for other HTTP failures, connection failures or timeouts.
- `ApiClientError` distinguishes `http`, `network`, `timeout`, `canceled`, `invalid-response` and `configuration`. HTTP errors retain status, code, field errors, body and diagnostic headers, excluding credential headers. They never retain Axios request/config objects. Screens translate codes and decide how to display errors or navigate.

## Frontend authentication state

`AuthProvider` exposes `useAuth()` with `status`, the owner `user`, localized notice codes, and `login`, `logout`, `retryProfile`, and `changeLocale` operations. Initial restoration runs once, including under React StrictMode. Public pages remain available throughout restoration. Consumers must render private content only when `status === 'authenticated'`; `restoring`, `authenticating`, and `unavailable` do not expose a user. Login and registration **screens** remain the next implementation step; legacy MVC authentication has not been retired yet.

- Startup reads a refresh from the selected browser storage, requests `GET /api` with that refresh, then follows the authenticated-user relation in `Link`. Resource URLs are confined to this deployment's API. Access tokens and user/profile data stay in memory; storage contains only the refresh, user/session URLs, remember-me choice and a format version. Keys are scoped to the API URL. Saving clears the other store's copy; conflicting or malformed saved records are discarded.
- Within one tab, simultaneous expired requests share one rotation. The leading original request spends the refresh; waiting requests retry with the resulting access. Each request still has a single retry limit. New credentials are saved before interpreting the resource's status/body. A lost rotation response clears the local session and requires a new login without replaying the uncertain operation; ordinary access-token connection failures do not sign the user out.
- Failed auth persistence prevents completing login and attempts to revoke the newly issued session. Failed persistence during rotation likewise clears local identity and attempts revocation. There is no silent memory-only fallback. If browser storage cannot be cleared, the UI explicitly reports that limitation.
- Logout immediately removes local credentials and the current profile, then deletes the canonical session using its refresh without rotation. If revocation cannot be confirmed, the UI reports it. Responses from a previous identity generation cannot repopulate credentials or return stale private data to consumers. If a late login/rotation returns usable new credentials after logout, the client attempts to revoke them.
- After login/restoration and each rotation, the owner resource supplies current effective roles and the profile language. A profile-fetch failure preserves the already rotated refresh and exposes an `unavailable` state with a retry action. Explicit language changes PATCH the owner resource before applying the choice. Before login, locale selection is local preference, then a supported browser language, then Spanish. Features adding private query caches must scope them to the authenticated identity and clear them when that identity ends; no private query cache has been introduced in this step.

**Deferred by agreement: coordination between tabs and the deployment HTTPS decision.** Rotation serialization and logout cleanup currently apply only to the current tab. Do not treat multi-tab use of the same session as complete: shared `localStorage` refreshes (or copied `sessionStorage` credentials) can still race and trigger server-side reuse revocation; logout propagation is not implemented. Resolve and test that coordination before completing authentication delivery. P1/P3 also need the final notification about retiring the legacy authentication consumers when their replacement is complete.
