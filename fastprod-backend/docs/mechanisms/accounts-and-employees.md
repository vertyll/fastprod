# Accounts and employees

How a person gets a local account, and how an administrator creates an employee.

Keycloak owns the person. The back-end keeps a copy in PostgreSQL, created the first time the front-end asks for the
current account (`GET /users/me`) and updated from the token on every such call: email, name, verification and realm
roles.

Employees are created by an administrator rather than by signing up. The back-end creates the Keycloak account through
the Admin API (client `fastprod-backend`, a service account) with a temporary password and the `VERIFY_EMAIL` action, so
the employee confirms the address and sets a password at the first sign-in. Deleting an employee disables the Keycloak
account and deactivates the local one; nothing is erased.
