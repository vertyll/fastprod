## Project Assumptions

Production management application.

## Link: https://fastprod.vertyll.dev

## Technology Stack

### Back-end:

- Spring Boot.
- Java.
- Gradle Kotlin DSL.
- JDBC.
- JPA.
- Hibernate.
- PostgreSQL.
- JUnit.
- Mockito.
- Lombok.
- Spring Security.
- Spring Data.
- Spring Web.
- Keycloak.
- Flyway.
- OpenAPI (Swagger).

### Front-end:

- Vaadin (Java-only – Flow).
- Spring Boot.
- Java.
- Gradle Kotlin DSL.
- JUnit.
- Mockito.
- Lombok.
- Spring Web.

### Authentication:

- Keycloak (realm `fastprod`) handles sign-up, sign-in, email verification, password reset, two-factor authentication
and acceptance of the terms of use.
- The front-end signs users in with the authorization code flow and PKCE, keeps the tokens in its server-side session
and calls the back-end with the Keycloak access token, refreshing it when needed.
- The back-end is an OAuth2 resource server: it verifies Keycloak tokens and takes the roles (`ADMIN`, `MANAGER`,
`EMPLOYEE`, `USER`) from them. It creates the local account on the first request and mirrors names and roles.
- Administrators create employees through the Keycloak Admin API with a temporary password; at the first sign-in the
employee confirms their email address and sets their own password.
- Locally, `docker-compose.local.yml` runs Keycloak on `:9000` (admin/admin) with the realm from
`keycloak/realm-export.json` and two accounts with the password `fastprod`: `admin@fastprod.local` (`ADMIN`) and
`user@fastprod.local`.

### Core back-end:

- Gradle multi-module build system.
- The application has an exception handling mechanism.
- The application has a logging mechanism.
- The application has separate environments for local and prod.
- The application has a dedicated configuration file.
- The application has RBAC (Role Based Access Control).
- The application has an audit mechanism (who and when modified data).
- The application has Flyway database migration mechanism.
- And many other features that can be found in the application code.

### Core front-end:

- Gradle build system.
- Vaadin.
- The application has separate environments for local and prod.
- The application has a dedicated configuration file.
- And many other features that can be found in the application code.

### Other:

- Docker for development environment.
- PMD for static code analysis.
- SpotBugs for static code analysis.
- JSpecify for null-safety annotations.
- NullAway for null-safety checks.
- Error Prone for static code analysis.
- Spotless for code formatting.

## Preview Screenshots

![Project View](https://raw.githubusercontent.com/vertyll/fastprod/refs/heads/main/screenshots/register.png)
![Project View](https://raw.githubusercontent.com/vertyll/fastprod/refs/heads/main/screenshots/profile.png)
![Project View](https://raw.githubusercontent.com/vertyll/fastprod/refs/heads/main/screenshots/employees-list.png)
![Project View](https://raw.githubusercontent.com/vertyll/fastprod/refs/heads/main/screenshots/employees-form.png)
