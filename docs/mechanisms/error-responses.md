# Error responses

What the back-end answers when it refuses a call, and how the front-end turns that into text.

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
