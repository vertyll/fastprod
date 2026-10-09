# fastprod — back-end

A stateless Spring Boot REST API over PostgreSQL. It authorizes every call by the Keycloak access token the front-end
sends ([Authentication](../docs/authentication.md)) and answers errors as translation keys
([Errors and translations](../docs/architecture.md#errors-and-translations)).

## Layout

A Gradle multi-module build; `app` assembles the others into one Spring Boot application.

| Module        | Holds                                                      |
|---------------|------------------------------------------------------------|
| `app`         | the application class, configuration and Flyway migrations |
| `security`    | the resource server, CORS and auditing                     |
| `user`        | the local account and the Keycloak Admin API client        |
| `employee`    | employee management                                        |
| `role`        | the roles the application knows                            |
| `translation` | the message catalog, its defaults and the admin overrides  |
| `file`        | file storage on the local disk                             |
| `common`      | the base entity, error handling and shared DTOs            |

## API

The Swagger UI at `http://localhost:8080/api/v1/swagger-ui.html` (local profile only) lists every endpoint. Access is
decided by `@PreAuthorize` on each controller method, on top of `SecurityConfig`, which admits the public paths and
requires a token for the rest.

## Database

PostgreSQL, migrated by Flyway (`modules/app/src/main/resources/db/migration`); Hibernate only validates the schema.

## Mechanisms

- [Accounts and employees](docs/mechanisms/accounts-and-employees.md) – How a person gets a local account, and how an
  administrator creates an employee.
- [Translation catalog](docs/mechanisms/translation-catalog.md) – Where the text behind every message key comes from,
  and how an administrator's edits survive a deployment.

## Running it

Start the infrastructure first ([Development Setup](../docs/development-setup.md)), then:

```bash
./gradlew :modules:app:bootRun
```

The `local` profile is the default and points at the local containers; it listens on `http://localhost:8080/api/v1`.

## Checks

```bash
./gradlew spotlessApply   # format
./gradlew check -x test   # Spotless and static analysis
./gradlew test
```

## Production

The image runs with the `prod` profile, which takes its settings from the environment:

| Variable                                                                | Purpose                                             |
|-------------------------------------------------------------------------|-----------------------------------------------------|
| `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`                                  | PostgreSQL                                          |
| `KEYCLOAK_REALM_URL`                                                    | the realm whose tokens are accepted                 |
| `KEYCLOAK_ADMIN_CLIENT_SECRET`                                          | the `fastprod-backend` client that manages accounts |
| `FRONTEND_URL`                                                          | the only origin CORS admits                         |
| `FILE_UPLOAD_PATH`                                                      | where uploaded files are written                    |
| `MAIL_HOST`, `MAIL_PORT`, `MAIL_USERNAME`, `MAIL_PASSWORD`, `MAIL_FROM` | SMTP                                                |
