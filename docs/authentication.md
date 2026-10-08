# Authentication

- **Identity provider**: Keycloak (realm `fastprod`) owns every page that touches a credential: sign-up, sign-in, email
  verification, password reset, two-factor authentication and acceptance of the terms of use. The application never sees
  a password.
- **Pattern**: BFF. The Vaadin front-end signs users in with the authorization code flow and PKCE and keeps the tokens
  in its server-side session; the browser holds only the `FASTPROD_SESSION` cookie (`HttpOnly`, `SameSite=Lax`, `Secure`
  in production), and Vaadin protects every request against CSRF.
- **Session store**: the front-end's memory (Vaadin keeps the UI state on the server anyway).
- **JWT**: the front-end calls the back-end with the Keycloak access token. The back-end is a stateless OAuth2 resource
  server: it verifies the token's signature (Keycloak's JWKS), issuer, expiry and audience (`fastprod-api`) and takes
  the roles (`ADMIN`, `MANAGER`, `EMPLOYEE`, `USER`) from it.
- **State**: the back-end is stateless: every request is authorized by the JWT alone, so any instance can serve it. The
  front-end is not, by Vaadin's design: it keeps the whole UI state together with the session in its own memory. A
  restart sends the user through Keycloak again, which signs them straight back in while the Keycloak session lasts.
- **Token lifecycle**: access tokens live five minutes; every refresh returns a new refresh token and invalidates the
  old one, and concurrent requests of one session share a single refresh. Signing out revokes the refresh token at
  Keycloak.
- **Accounts**: the back-end creates the local account in PostgreSQL on the first request and mirrors names and roles
  from the token. Administrators create employees through the Keycloak Admin API with a temporary password.
