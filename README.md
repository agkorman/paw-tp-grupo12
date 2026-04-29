# paw-tp-grupo12

Car listing web application — ITBA PAW course project.

## Requirements

- Java 21
- Maven 3.x
- PostgreSQL 16

## Database setup

Create a database and a user:

```sql
CREATE USER pawuser WITH PASSWORD 'yourpassword';
CREATE DATABASE pawdb OWNER pawuser;
```

## Environment variables

The app reads DB credentials from environment variables. Set them before running:

```bash
export DB_URL=jdbc:postgresql://localhost/pawdb
export DB_USERNAME=pawuser
export DB_PASSWORD=yourpassword
```

If your deploy target does not let you configure Tomcat environment variables, you can instead
package a `db.properties` file inside the WAR at `webapp/src/main/resources/db.properties`:

```properties
db.url=jdbc:postgresql://localhost/pawdb
db.username=pawuser
db.password=yourpassword
```

## Running locally

```bash
mvn clean install
mvn -pl webapp jetty:run
```

The app starts on `http://localhost:8080/webapp`. The database schema and seed data are created automatically on first startup.

## Useful endpoints

| URL | Description |
|-----|-------------|
| `http://localhost:8080/webapp/` | Home |
| `http://localhost:8080/webapp/cars` | Car listing |
