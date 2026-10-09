# Standards

The specifications this repository implements or depends on, what for, and where the documentation covers it.

| Standard | Title | Used for | Explained in |
|---|---|---|---|
| [RFC 6749](https://www.rfc-editor.org/rfc/rfc6749) | OAuth 2.0 | the authorization code flow and refresh tokens | [Authentication: Signing in](docs/authentication.md#signing-in) |
| [RFC 7636](https://www.rfc-editor.org/rfc/rfc7636) | PKCE | binding the code to the browser | [Authentication: Signing in](docs/authentication.md#signing-in) |
| [OIDC Core](https://openid.net/specs/openid-connect-core-1_0.html) | OpenID Connect Core 1.0 | the ID token and the `openid` scope | [Authentication: Signing in](docs/authentication.md#signing-in) |
| [RFC 7519](https://www.rfc-editor.org/rfc/rfc7519) | JSON Web Token (JWT) | the access token and its claims | [Authentication: Calls to the back-end](docs/authentication.md#calls-to-the-back-end) |
| [RFC 7517](https://www.rfc-editor.org/rfc/rfc7517) | JSON Web Key (JWK) | Keycloak's published signing keys | [Authentication: Calls to the back-end](docs/authentication.md#calls-to-the-back-end) |
| [RFC 6750](https://www.rfc-editor.org/rfc/rfc6750) | OAuth 2.0 Bearer Token Usage | `Authorization: Bearer` on API calls | [Authentication: Calls to the back-end](docs/authentication.md#calls-to-the-back-end) |
| [OIDC RP-Initiated Logout](https://openid.net/specs/openid-connect-rpinitiated-1_0.html) | OpenID Connect RP-Initiated Logout 1.0 | sending the browser to Keycloak's end-session endpoint | [Authentication: Signing out](docs/authentication.md#signing-out) |
| [RFC 9457](https://www.rfc-editor.org/rfc/rfc9457) | Problem Details for HTTP APIs | the error body of every refusal | [Architecture: Errors and translations](docs/architecture.md#errors-and-translations) |
| [RFC 6265bis](https://datatracker.ietf.org/doc/draft-ietf-httpbis-rfc6265bis/) | Cookies: HTTP State Management Mechanism (draft) | the `SameSite=Lax` session cookie | [Authentication: Signing in](docs/authentication.md#signing-in) |
| [ICU MessageFormat](https://unicode-org.github.io/icu/userguide/format_parse/messages/) | ICU MessageFormat | formatting every label and error in the front-end | [Architecture: Errors and translations](docs/architecture.md#errors-and-translations) |
| [OpenAPI](https://spec.openapis.org/oas/latest.html) | OpenAPI Specification | the back-end's Swagger UI | [fastprod — back-end: API](fastprod-backend/README.md#api) |
| [Fetch Standard](https://fetch.spec.whatwg.org/) | Fetch Standard (WHATWG) | CORS, which admits only the front-end | [fastprod — back-end: Production](fastprod-backend/README.md#production) |
