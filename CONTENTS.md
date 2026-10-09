# Contents

Every document in this repository, the module it belongs to, and what it covers. Terms are defined in
[GLOSSARY.md](GLOSSARY.md), and the specifications behind them are in [STANDARDS.md](STANDARDS.md).

## Start here

| Document                  | Module | Kind              | Covers                                                         |
|---------------------------|--------|-------------------|----------------------------------------------------------------|
| [fastprod](README.md)     | —      | repository README | What the repository is, its stack and where to start.          |
| [Glossary](GLOSSARY.md)   | —      | reference         | Every term the docs use, and where it is explained.            |
| [Standards](STANDARDS.md) | —      | reference         | The RFCs and specifications the code implements or depends on. |

## Overview

| Document                                       | Module | Kind     | Covers                                             |
|------------------------------------------------|--------|----------|----------------------------------------------------|
| [Development Setup](docs/development-setup.md) | —      | overview | The infrastructure and starting both applications. |
| [Architecture](docs/architecture.md)           | —      | overview | The two applications and how they talk.            |
| [Authentication](docs/authentication.md)       | —      | overview | Sign-in, calls to the back-end and sign-out.       |

## Mechanisms

| Document                                              | Module | Kind      | Covers                                                                                        |
|-------------------------------------------------------|--------|-----------|-----------------------------------------------------------------------------------------------|
| [Error responses](docs/mechanisms/error-responses.md) | —      | mechanism | What the back-end answers when it refuses a call, and how the front-end turns that into text. |

## Applications

| Document                                                                             | Module    | Kind          | Covers                                                                                                     |
|--------------------------------------------------------------------------------------|-----------|---------------|------------------------------------------------------------------------------------------------------------|
| [Back-end](fastprod-backend/README.md)                                               | back-end  | module README | Layout, API, database, running and production.                                                             |
| [Accounts and employees](fastprod-backend/docs/mechanisms/accounts-and-employees.md) | back-end  | mechanism     | How a person gets a local account, and how an administrator creates an employee.                           |
| [Translation catalog](fastprod-backend/docs/mechanisms/translation-catalog.md)       | back-end  | mechanism     | Where the text behind every message key comes from, and how an administrator's edits survive a deployment. |
| [Front-end](fastprod-frontend/README.md)                                             | front-end | module README | Layout, access, text, running and production.                                                              |
| [Token refresh](fastprod-frontend/docs/mechanisms/token-refresh.md)                  | front-end | mechanism     | How the session keeps a valid access token without signing the user out when requests race.                |
