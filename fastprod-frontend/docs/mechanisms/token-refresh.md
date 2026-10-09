# Token refresh

How the session keeps a valid access token without signing the user out when requests race.

The session lives in the front-end's memory, next to the UI state Vaadin keeps there anyway, and lasts ten hours.

> [!NOTE]
>
> A restart of the front-end loses every session. The user is then sent through Keycloak again, which signs them
> straight back in while the Keycloak session lasts.

Access tokens live five minutes and are refreshed when a back-end call needs one that is about to expire.

> [!IMPORTANT]
>
> Keycloak rotates refresh tokens and refuses a spent one, so two calls of one session refreshing at once would sign
> the user out.

`SingleFlightRefreshTokenProvider` therefore lets one call refresh and hands its result to the calls of the same session
that arrive meanwhile. When Keycloak refuses the refresh, the call goes out without a token and the back-end answers
`401`.
