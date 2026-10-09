# Glossary

Every term the documentation uses without defining it on the spot, and where it is explained. The specifications behind
them are in [STANDARDS.md](STANDARDS.md).

| Term                       | Meaning                                                                                        | Explained in                                                                          |
|----------------------------|------------------------------------------------------------------------------------------------|---------------------------------------------------------------------------------------|
| Access token               | Short-lived JWT (five minutes) that authorizes one request.                                    | [Authentication: Calls to the back-end](docs/authentication.md#calls-to-the-back-end) |
| Audience                   | The `aud` claim naming whom a token is for; a token for anyone else is refused.                | [Authentication: Calls to the back-end](docs/authentication.md#calls-to-the-back-end) |
| Authorization code flow    | Sign-in by redirecting to Keycloak and exchanging the code it returns, on the server.          | [Authentication: Signing in](docs/authentication.md#signing-in)                       |
| BFF (backend for frontend) | The front-end signs the user in and keeps the tokens; the browser holds only a session cookie. | [Authentication](docs/authentication.md)                                              |
| Catalog                    | Every label and error key, in Polish and English, owned by the back-end.                       | [Error responses](docs/mechanisms/error-responses.md)                                 |
| Default                    | A message as shipped in the back-end's `i18n` files; refreshed at every start.                 | [Translation catalog](fastprod-backend/docs/mechanisms/translation-catalog.md)        |
| Employee                   | An account an administrator creates through the Keycloak Admin API rather than by signing up.  | [Accounts and employees](fastprod-backend/docs/mechanisms/accounts-and-employees.md)  |
| Keycloak realm             | The Keycloak tenant holding this application's users, roles and clients.                       | [Authentication](docs/authentication.md)                                              |
| Message key                | A key of the catalog the back-end sends instead of a sentence; the front-end renders it.       | [Error responses](docs/mechanisms/error-responses.md)                                 |
| Mirrored account           | The back-end's copy of a Keycloak user, updated on every `GET /users/me`.                      | [Accounts and employees](fastprod-backend/docs/mechanisms/accounts-and-employees.md)  |
| Override                   | An administrator's text for a key; it survives new defaults until it is reset.                 | [Translation catalog](fastprod-backend/docs/mechanisms/translation-catalog.md)        |
| PKCE                       | A one-time secret binding the returned code to the browser that started the sign-in.           | [Authentication: Signing in](docs/authentication.md#signing-in)                       |
| Problem document           | The JSON body of every refusal, carrying a message key and its arguments.                      | [Error responses](docs/mechanisms/error-responses.md)                                 |
| Refresh token              | Long-lived token traded for a new access token; Keycloak rotates it on every use.              | [Token refresh](fastprod-frontend/docs/mechanisms/token-refresh.md)                   |
| Refresh token rotation     | Every refresh invalidates the refresh token it used; replaying a spent one ends the session.   | [Token refresh](fastprod-frontend/docs/mechanisms/token-refresh.md)                   |
| Resource server            | The back-end: it accepts a call only with a valid access token for its audience.               | [Authentication: Calls to the back-end](docs/authentication.md#calls-to-the-back-end) |
| Role                       | `ADMIN`, `MANAGER`, `EMPLOYEE` or `USER`, a Keycloak realm role read from the access token.    | [Authentication: Signing in](docs/authentication.md#signing-in)                       |
| Route access               | `@AnonymousAllowed`, `@PermitAll` or `@RolesAllowed` on a view, checked before it is built.    | [fastprod — front-end: Access](fastprod-frontend/README.md#access)                    |
| Service account            | The `fastprod-backend` client acting as itself to manage employees in Keycloak.                | [Accounts and employees](fastprod-backend/docs/mechanisms/accounts-and-employees.md)  |
| Session                    | `FASTPROD_SESSION`, kept in the front-end's memory for ten hours, holding the tokens.          | [Token refresh](fastprod-frontend/docs/mechanisms/token-refresh.md)                   |
| Sign-out                   | Ends the local session and the Keycloak session.                                               | [Authentication: Signing out](docs/authentication.md#signing-out)                     |
| Single-flight refresh      | One refresh per refresh token, shared by every request of the session that needs it at once.   | [Token refresh](fastprod-frontend/docs/mechanisms/token-refresh.md)                   |
