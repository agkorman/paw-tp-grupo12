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

Vite proxies `/api` to `http://localhost:8080/webapp/api` by default. Set `PAW_DEV_API_ORIGIN` and `PAW_DEV_API_CONTEXT_PATH` to change the development backend origin or context path. The frontend base currently contains a landing screen, a catalog placeholder, shared layout, lazy routes, translations, and a navigation test.
