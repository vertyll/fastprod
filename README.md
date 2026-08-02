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
- Spring Mail.
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

- JWT-based authentication – the application uses JWT tokens for user authentication and includes token refresh 
mechanism (http only secure cookie).
- The application allows login on multiple devices simultaneously.

### Core back-end:

- Gradle multi-module build system.
- The application has an exception handling mechanism.
- The application has a logging mechanism.
- The application has an email sending mechanism.
- The application has a scheduled task handling mechanism (cron).
- The application has separate environments for dev and prod.
- The application has a dedicated configuration file.
- The application has RBAC (Role Based Access Control).
- The application has an audit mechanism (who and when modified data).
- The application has Flyway database migration mechanism.
- And many other features that can be found in the application code.

### Core front-end:

- Gradle build system.
- Vaadin.
- The application has separate environments for dev and prod.
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

> [!NOTE]
>
> During application development, SOLID principles, DRY, composition over inheritance, dependency injection, design 
> patterns, architectural patterns were applied, tests were written, and other good programming practices were adopted.

## Preview Screenshots

![Project View](https://raw.githubusercontent.com/vertyll/fastprod/refs/heads/main/screenshots/register.png)
![Project View](https://raw.githubusercontent.com/vertyll/fastprod/refs/heads/main/screenshots/profile.png)
![Project View](https://raw.githubusercontent.com/vertyll/fastprod/refs/heads/main/screenshots/employees-list.png)
![Project View](https://raw.githubusercontent.com/vertyll/fastprod/refs/heads/main/screenshots/employees-form.png)
