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
```

If your deploy target does not let you configure Tomcat environment variables, you can instead
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

## Frontend base

The React + TypeScript app lives in `frontend/`. Maven runs `npm ci`, the frontend tests, the TypeScript check, and the Vite production build as part of `mvn clean package`. The generated files are packaged under `WEB-INF/spa/` in the WAR. Their public URL and the SPA route fallback will be configured alongside the Jersey and security servlet mappings.

For standalone frontend development:

```bash
cd frontend
npm ci
npm run dev
```

Vite proxies `/api` to `http://localhost:8080/webapp/api` by default. Set `PAW_DEV_API_ORIGIN` and `PAW_DEV_API_CONTEXT_PATH` to change the development backend origin or context path. The frontend base currently contains a landing screen, a catalog placeholder, shared layout, lazy routes, translations, and a navigation test.
