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

> [!NOTE]
>
> The realm lives in the `keycloak-data` volume afterwards, so a change to the export file only takes effect after
> `docker compose -f docker-compose.local.yml down -v`.

## Run the applications

Start the back-end, then the front-end; each README says how, and lists its checks and production settings:
[back-end](../fastprod-backend/README.md), [front-end](../fastprod-frontend/README.md). The `.run` directory holds
IntelliJ run configurations for both.

| Application | Address                                        |
|-------------|------------------------------------------------|
| Front-end   | `http://localhost:8001`                        |
| Back-end    | `http://localhost:8080/api/v1/swagger-ui.html` |
