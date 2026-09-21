-- Apply once to an existing database; schema.sql is only for disposable development data.
BEGIN;
ALTER TABLE user_account ADD COLUMN account_activated BOOLEAN NOT NULL DEFAULT FALSE;
ALTER TABLE user_account ADD COLUMN session_version BIGINT NOT NULL DEFAULT 0;
-- Legacy inactive accounts have no reliable activation history: require a new invitation.
UPDATE user_account SET account_activated = TRUE WHERE account_status = 'Active';
COMMIT;
