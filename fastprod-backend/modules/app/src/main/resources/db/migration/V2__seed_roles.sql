-- Seed default roles based on RoleType enum
-- Database: PostgreSQL

-- ===============
-- role
INSERT INTO role (
    name,
    description,
    active,
    created_at,
    updated_at,
    created_by,
    updated_by
) VALUES(
    'ADMIN',
    'Default role: ADMIN',
    TRUE,
    NOW(),
    NOW(),
    'flyway',
    'flyway'
) ON CONFLICT (name) DO NOTHING;

INSERT INTO role (
    name,
    description,
    active,
    created_at,
    updated_at,
    created_by,
    updated_by
) VALUES(
    'USER',
    'Default role: USER',
    TRUE,
    NOW(),
    NOW(),
    'flyway',
    'flyway'
) ON CONFLICT (name) DO NOTHING;

INSERT INTO role (
    name,
    description,
    active,
    created_at,
    updated_at,
    created_by,
    updated_by
) VALUES(
    'MANAGER',
    'Default role: MANAGER',
    TRUE,
    NOW(),
    NOW(),
    'flyway',
    'flyway'
) ON CONFLICT (name) DO NOTHING;

INSERT INTO role (
    name,
    description,
    active,
    created_at,
    updated_at,
    created_by,
    updated_by
) VALUES(
    'EMPLOYEE',
    'Default role: EMPLOYEE',
    TRUE,
    NOW(),
    NOW(),
    'flyway',
    'flyway'
) ON CONFLICT (name) DO NOTHING;
