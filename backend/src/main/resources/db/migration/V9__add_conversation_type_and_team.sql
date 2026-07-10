-- Conversations grow a kind discriminator and an optional team link:
--   MATCH  - auto-created when a listing response is accepted (match_id set)
--   DIRECT - ad-hoc 1:1 between two users
--   TEAM   - group chat for a team roster (team_id set)
-- Existing rows predate the accept flow: rows with match_id are MATCH,
-- the rest are DIRECT.

ALTER TABLE conversations
    ADD COLUMN type VARCHAR(32) NOT NULL DEFAULT 'DIRECT',
    ADD COLUMN team_id BIGINT UNIQUE REFERENCES teams(id);

UPDATE conversations SET type = 'MATCH' WHERE match_id IS NOT NULL;

ALTER TABLE conversations
    ADD CONSTRAINT chk_conversations_type_links CHECK (
        (type = 'MATCH'  AND match_id IS NOT NULL AND team_id IS NULL) OR
        (type = 'TEAM'   AND team_id  IS NOT NULL AND match_id IS NULL) OR
        (type = 'DIRECT' AND match_id IS NULL     AND team_id IS NULL)
    );
