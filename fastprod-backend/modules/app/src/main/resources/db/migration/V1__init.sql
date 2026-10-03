CREATE TABLE role (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by TEXT NOT NULL,
    updated_by TEXT NOT NULL,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_role_name UNIQUE (name)
);

CREATE TABLE "user" (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by TEXT NOT NULL,
    updated_by TEXT NOT NULL,
    keycloak_id VARCHAR(36) NOT NULL,
    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    verified BOOLEAN NOT NULL DEFAULT FALSE,
    active BOOLEAN NOT NULL DEFAULT TRUE,
    CONSTRAINT uk_user_keycloak_id UNIQUE (keycloak_id),
    CONSTRAINT uk_user_email UNIQUE (email)
);

CREATE INDEX idx_user_active_verified ON "user" (active, verified);

CREATE TABLE user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    CONSTRAINT pk_user_role PRIMARY KEY (user_id, role_id),
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES "user" (id) ON DELETE CASCADE,
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES role (id) ON DELETE CASCADE
);

CREATE INDEX idx_user_role_role_id ON user_role (role_id);

INSERT INTO role (name, description, active, created_at, updated_at, created_by, updated_by)
VALUES ('ADMIN', 'Keycloak realm role ADMIN', TRUE, NOW(), NOW(), 'flyway', 'flyway'),
       ('MANAGER', 'Keycloak realm role MANAGER', TRUE, NOW(), NOW(), 'flyway', 'flyway'),
       ('EMPLOYEE', 'Keycloak realm role EMPLOYEE', TRUE, NOW(), NOW(), 'flyway', 'flyway'),
       ('USER', 'Keycloak realm role USER', TRUE, NOW(), NOW(), 'flyway', 'flyway');

CREATE TABLE translation (
    key VARCHAR(200) PRIMARY KEY,
    message_pl TEXT NOT NULL,
    message_en TEXT NOT NULL,
    default_pl TEXT NOT NULL,
    default_en TEXT NOT NULL,
    customized BOOLEAN NOT NULL DEFAULT FALSE,
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL
);
