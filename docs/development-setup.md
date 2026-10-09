# Development Setup

## Prerequisites

- Docker or Podman, with Compose
- JDK 25

## Start the infrastructure

```bash
docker compose -f docker-compose.local.yml up -d
```

Every `docker compose` command here works verbatim as `podman compose`.

| Service    | Address                                     | Purpose                                         |
|------------|---------------------------------------------|-------------------------------------------------|
| PostgreSQL | `localhost:5432` (`postgres` / `postgres`)  | the `fastprod` database                         |
| Keycloak   | `http://localhost:9000` (`admin` / `admin`) | realm `fastprod`, imported on start             |
| MailDev    | `http://localhost:1080`                     | catches Keycloak's verification and reset mails |

Keycloak imports `keycloak/realm-export.json` on its first start, with two accounts:

| Account                | Password   | Roles           |
|------------------------|------------|-----------------|
| `admin@fastprod.local` | `fastprod` | `USER`, `ADMIN` |
| `user@fastprod.local`  | `fastprod` | `USER`          |

The realm lives in the `keycloak-data` volume afterwards, so a change to the export file only takes effect after
`docker compose -f docker-compose.local.yml down -v`.

## Run the back-end and the front-end

Two applications, each with its own Gradle build. The `local` profile is the default for both, and their
`application-local.yml` already points at the containers above, so there is nothing to configure.

```bash
cd fastprod-backend && ./gradlew :modules:app:bootRun
cd fastprod-frontend && ./gradlew bootRun
```

| Address                                        | What                                   |
|------------------------------------------------|----------------------------------------|
| `http://localhost:8001`                        | the application (Vaadin)               |
| `http://localhost:8080/api/v1/swagger-ui.html` | the back-end's Swagger UI (local only) |

The front-end's first start prepares the Vaadin front-end bundle, which takes a while; later starts reuse it. The `.run`
directory holds IntelliJ run configurations for both applications.

## Checks and production

Each application lists its checks and production settings in its own README:
[back-end](../fastprod-backend/README.md), [front-end](../fastprod-frontend/README.md). CI runs the checks per
application, then the Sonar analysis and the image build.
