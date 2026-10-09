# Architecture

## Two applications

```mermaid
flowchart LR
    browser([Browser])
    front["fastprod-frontend<br/>Vaadin, session in memory"]
    back["fastprod-backend<br/>REST API, stateless"]
    kc[Keycloak]
    db[(PostgreSQL)]

    browser -- "FASTPROD_SESSION cookie" --> front
    front -- "Bearer access token" --> back
    front -- "sign-in, refresh" --> kc
    back -- "token keys, Admin API for employees" --> kc
    back --> db
```

| Application                                           | What it is                                                                        |
|-------------------------------------------------------|-----------------------------------------------------------------------------------|
| [`fastprod-frontend`](../fastprod-frontend/README.md) | a Vaadin Flow application: the UI, rendered on the server, and the user's session |
| [`fastprod-backend`](../fastprod-backend/README.md)   | a stateless REST API that owns the data                                           |

The browser talks only to the front-end. The front-end calls the back-end over HTTP with the signed-in user's access
token (`BaseHttpService`), and the back-end authorizes every call by that token alone. Both check access: a Vaadin
route refuses the navigation, and the back-end refuses the call, so hiding a button is never the only guard.

## Errors and translations

The back-end never sends a sentence a person reads. Every refusal is an RFC 9457 problem document
(`application/problem+json`):

| Field    | Holds                                                                   |
|----------|-------------------------------------------------------------------------|
| `status` | the HTTP status                                                         |
| `detail` | a key of the translation catalog, e.g. `errors.common.validationFailed` |
| `code`   | the same key                                                            |
| `args`   | the ICU arguments for that key                                          |
| `errors` | in a validation error, the message keys of each invalid field           |

The catalog is the back-end's: it ships in `fastprod-backend/modules/translation/src/main/resources/i18n`
(`pl.json`, `en.json`, ICU MessageFormat), an admin can override any message, and `GET /api/v1/translations/{language}`
serves it.

The front-end renders it. `BackendI18NProvider` is Vaadin's `I18NProvider`: it loads each language's catalog from the
back-end, keeps it for five minutes, and formats a key with its arguments through ICU4J's `MessageFormat` in the
reader's language. A failed call becomes an `ApiException` carrying the problem's key, arguments and field errors, and
`I18n.error` turns it into text; a key the catalog does not know shows `errors.common.unexpected` instead of the raw
key. The same catalog holds every label of the UI, so a new error or a new label is a new key in both files, never a
sentence in the code.
