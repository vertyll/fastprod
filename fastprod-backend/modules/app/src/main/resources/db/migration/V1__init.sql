-- Flyway initial schema based on JPA entities
-- Database: PostgreSQL

-- ===============
-- Common base columns (from BaseEntity)
-- id BIGSERIAL PK
-- created_at TIMESTAMP NOT NULL
-- updated_at TIMESTAMP NOT NULL
-- created_by TEXT NOT NULL
-- updated_by TEXT NOT NULL

-- ===============
-- role
CREATE TABLE IF NOT EXISTS role (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by TEXT NOT NULL,
    updated_by TEXT NOT NULL,

    name VARCHAR(255) NOT NULL,
    description TEXT,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT uk_role_name UNIQUE (name)
);

CREATE INDEX IF NOT EXISTS idx_role_is_active ON role (is_active);
CREATE INDEX IF NOT EXISTS idx_role_created_at ON role (created_at);
CREATE INDEX IF NOT EXISTS idx_role_name_is_active ON role (name, is_active);

-- ===============
-- user
CREATE TABLE IF NOT EXISTS "user" (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by TEXT NOT NULL,
    updated_by TEXT NOT NULL,

    first_name VARCHAR(255) NOT NULL,
    last_name VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    password VARCHAR(255) NOT NULL,
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    is_active BOOLEAN NOT NULL DEFAULT TRUE,

    CONSTRAINT uk_user_email UNIQUE (email)
);

CREATE INDEX IF NOT EXISTS idx_user_email ON "user" (email);
CREATE INDEX IF NOT EXISTS idx_user_is_active ON "user" (is_active);
CREATE INDEX IF NOT EXISTS idx_user_is_verified ON "user" (is_verified);
CREATE INDEX IF NOT EXISTS idx_user_created_at ON "user" (created_at);
CREATE INDEX IF NOT EXISTS idx_user_is_active_is_verified ON "user" (is_active, is_verified);

-- ===============
-- user_role
CREATE TABLE IF NOT EXISTS user_role (
    user_id BIGINT NOT NULL,
    role_id BIGINT NOT NULL,
    CONSTRAINT fk_user_role_user FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE,
    CONSTRAINT fk_user_role_role FOREIGN KEY (role_id) REFERENCES role(id) ON DELETE CASCADE,
    CONSTRAINT pk_user_role PRIMARY KEY (user_id, role_id)
);

CREATE INDEX IF NOT EXISTS idx_user_role_user_id ON user_role (user_id);
CREATE INDEX IF NOT EXISTS idx_user_role_role_id ON user_role (role_id);

-- ===============
-- refresh_token
CREATE TABLE IF NOT EXISTS refresh_token (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by TEXT NOT NULL,
    updated_by TEXT NOT NULL,

    token VARCHAR(500) NOT NULL,
    user_id BIGINT NOT NULL,
    expiry_date TIMESTAMP NOT NULL,
    is_revoked BOOLEAN NOT NULL DEFAULT FALSE,
    device_info TEXT,
    ip_address VARCHAR(45),
    user_agent VARCHAR(500),
    last_used_at TIMESTAMP NULL,
    revoked_at TIMESTAMP NULL,

    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE,
    CONSTRAINT uk_refresh_token_token UNIQUE (token)
);

CREATE INDEX IF NOT EXISTS idx_refresh_token_user_id ON refresh_token (user_id);
CREATE INDEX IF NOT EXISTS idx_refresh_token_expiry_date ON refresh_token (expiry_date);
CREATE INDEX IF NOT EXISTS idx_refresh_token_is_revoked ON refresh_token (is_revoked);
CREATE INDEX IF NOT EXISTS idx_refresh_token_token ON refresh_token (token);

-- ===============
-- verification_token
CREATE TABLE IF NOT EXISTS verification_token (
    id BIGSERIAL PRIMARY KEY,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by TEXT NOT NULL,
    updated_by TEXT NOT NULL,

    token TEXT,
    user_id BIGINT,
    expiry_date TIMESTAMP,
    is_used BOOLEAN NOT NULL DEFAULT FALSE,
    token_type VARCHAR(255) NOT NULL,
    additional_data TEXT,

    CONSTRAINT fk_verification_token_user FOREIGN KEY (user_id) REFERENCES "user"(id) ON DELETE CASCADE
);

CREATE INDEX IF NOT EXISTS idx_verification_token_user_id ON verification_token (user_id);
CREATE INDEX IF NOT EXISTS idx_verification_token_expiry_date ON verification_token (expiry_date);
CREATE INDEX IF NOT EXISTS idx_verification_token_is_used ON verification_token (is_used);
CREATE INDEX IF NOT EXISTS idx_verification_token_token_type ON verification_token (token_type);
CREATE INDEX IF NOT EXISTS idx_verification_token_token ON verification_token (token);
