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
| `shared/keycloak`                  | Keycloak's pages: account actions, the shared language       |
| `shared/components`, `base/ui`     | shared components and the main layout                        |

## Package dependencies

`ArchitectureTest` checks them on every build: `shared` knows neither `base` nor the modules, `base` knows no
module (the layout shows the signed-in person through `SignedInPerson`, which `modules/user` implements),
`shared.security` knows neither `shared.keycloak` nor the UI, only `shared.keycloak` opens Keycloak's account pages,
and the `shared` packages have no cycle between them.

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

## Content Security Policy

Every response carries a `Content-Security-Policy` (`shared/security/ContentSecurityPolicy`). In production mode the
page Vaadin starts from gets a new nonce on each request, 128 random bits, on the header and on each of its `script`
tags; those scripts load the rest of Vaadin's client. Every other response gets the same policy with `script-src
'self'` and no nonce.

```text
default-src 'self'; object-src 'none'; base-uri 'self'; form-action 'self'; frame-ancestors 'none';
img-src 'self' data:; font-src 'self' data:; style-src 'self' 'unsafe-inline';
script-src 'self' 'nonce-<nonce>' 'strict-dynamic' 'unsafe-eval'
```

Two keywords remain, as Vaadin Flow cannot work without them
([Vaadin: frequently reported issues](https://vaadin.com/docs/v25/flow/security/advanced-topics/frequent-issues#content-security-policy-csp-set-to-unsafe-values)):

- `'unsafe-eval'`: Flow's client turns the JavaScript the server sends, `executeJs` included, into functions with
  `new Function()`. Vaadin's [strict CSP](https://vaadin.com/docs/v25/flow/security/advanced-topics/strict-csp) setup
  removes it by mapping every such function in a script of its own, which breaks with each new `executeJs` or add-on,
  so it is not used here.
- `'unsafe-inline'` in `style-src`: Vaadin's components and Lumo add `<style>` elements and `style` attributes at run
  time, without a nonce.

`data:` is allowed for images and fonts, which Vaadin's styles use for component icons and Lumo's icon font. Signing in
and out navigate to Keycloak rather than submit a form, so `form-action` needs no other origin.

In development mode the page allows inline scripts instead of a nonce, which Vaadin supports in production mode only.

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
