DELETE FROM user_role;
DELETE FROM "user";

DROP TABLE IF EXISTS refresh_token;
DROP TABLE IF EXISTS verification_token;

ALTER TABLE "user" DROP COLUMN password;
ALTER TABLE "user" ADD COLUMN keycloak_id VARCHAR(36) NOT NULL;
ALTER TABLE "user" ADD CONSTRAINT uk_user_keycloak_id UNIQUE (keycloak_id);
