<p align="center">
    <img alt="" src="https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white">
    <img alt="" src="https://img.shields.io/badge/Spring_Boot-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white">
    <img alt="" src="https://img.shields.io/badge/Vaadin-00B4F0?style=for-the-badge&logo=vaadin&logoColor=white">
    <img alt="" src="https://img.shields.io/badge/PostgreSQL-316192?style=for-the-badge&logo=postgresql&logoColor=white">
    <img alt="" src="https://img.shields.io/badge/Keycloak-00b8e3?style=for-the-badge&logo=keycloak&logoColor=4D4D4D">
    <img alt="" src="https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white">
</p>

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

- **Identity provider**: Keycloak (realm `fastprod`); the application never sees a password.
- **Pattern**: BFF; the Vaadin front-end keeps the tokens, the browser holds only a session cookie.
- **JWT**: the back-end is a stateless resource server; roles come from the token.
- **Accounts**: mirrored into PostgreSQL; employees are created by an admin through Keycloak.
- **Details**: [Authentication](./docs/authentication.md).

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

## Documentation

- [Glossary](./GLOSSARY.md) – every term the docs use, and where it is explained.
- [Standards](./STANDARDS.md) – the RFCs and specifications the code implements or depends on.
- [Development Setup](./docs/development-setup.md) – the infrastructure and starting both applications.
- [Architecture](./docs/architecture.md) – the two applications, and the errors and translations they share.
- [Authentication](./docs/authentication.md) – sign-in, back-end calls, the session and refreshing.
- [Back-end](./fastprod-backend/README.md) – modules, accounts and employees, translations, running and production.
- [Front-end](./fastprod-frontend/README.md) – layout, routes, text, running and production.

## Preview Screenshots

![Project View](https://raw.githubusercontent.com/vertyll/fastprod/refs/heads/main/screenshots/register.png)
![Project View](https://raw.githubusercontent.com/vertyll/fastprod/refs/heads/main/screenshots/profile.png)
![Project View](https://raw.githubusercontent.com/vertyll/fastprod/refs/heads/main/screenshots/employees-list.png)
![Project View](https://raw.githubusercontent.com/vertyll/fastprod/refs/heads/main/screenshots/employees-form.png)
