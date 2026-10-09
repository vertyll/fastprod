# fastprod — front-end

A Vaadin Flow application: the UI is rendered on the server, which also holds the user's session and tokens
([Authentication](../docs/authentication.md)). It reads and writes data only through the back-end's API.

## Layout

| Package                            | Holds                                                        |
|------------------------------------|--------------------------------------------------------------|
| `modules/<feature>/views`          | the routed screens                                           |
| `modules/<feature>/service`, `dto` | the calls to the back-end and what they exchange             |
| `shared/service`                   | `BaseHttpService`: HTTP, the access token, problem documents |
| `shared/security`                  | sign-in, roles and the access token for back-end calls       |
| `shared/i18n`                      | the catalog from the back-end and ICU formatting             |
| `shared/components`, `base/ui`     | shared components and the main layout                        |

## Access

A view states its access with `@AnonymousAllowed`, `@PermitAll` or `@RolesAllowed`; Vaadin refuses the navigation
before the view is built.

## Text

Every label and every error is a key of the back-end's catalog, rendered with `I18n.t(key, args)` and, for a failed
call, `I18n.error(exception)` ([Errors and translations](../docs/architecture.md#errors-and-translations)). The
language is picked in the application and kept in the session.

## Mechanisms

- [Token refresh](docs/mechanisms/token-refresh.md) – How the session keeps a valid access token without signing the
  user out when requests race.

## Running it

Start the infrastructure and the back-end first ([Development Setup](../docs/development-setup.md)), then:

```bash
./gradlew bootRun
```

The application is at `http://localhost:8001`. The first start prepares the Vaadin front-end bundle, which takes a
while; later starts reuse it.

## Checks

```bash
./gradlew spotlessApply   # format
./gradlew check -x test   # Spotless and static analysis
./gradlew test
```

## Production

The image runs with the `prod` profile, which takes its settings from the environment:

| Variable                 | Purpose                                                     |
|--------------------------|-------------------------------------------------------------|
| `API_BACKEND_URL`        | the back-end, including `/api/v1`                           |
| `KEYCLOAK_REALM_URL`     | the realm                                                   |
| `KEYCLOAK_CLIENT_SECRET` | the `fastprod-frontend` client's secret                     |
| `APP_URL`                | the front-end's public address, used for Keycloak redirects |
