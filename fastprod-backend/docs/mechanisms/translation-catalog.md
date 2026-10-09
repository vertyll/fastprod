# Translation catalog

Where the text behind every message key comes from, and how an administrator's edits survive a deployment.

The catalog ships in `modules/translation/src/main/resources/i18n`. At startup the stored catalog is brought in
line with those files: new keys are added, changed defaults adopted and keys the code no longer uses dropped. An admin
override survives a new default until it is reset, and must parse as ICU MessageFormat using only the placeholders of
its default.
