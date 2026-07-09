-- Fix mock seed data: V3 used 'TEAM' but the application format values are
-- ONE_VS_ONE / TEAM_VS_TEAM.
UPDATE matches SET format = 'TEAM_VS_TEAM' WHERE format = 'TEAM';

-- These are always required at submission time; tighten the schema to match
-- the application-level guarantee (defense in depth against raw inserts).
ALTER TABLE match_results
    ALTER COLUMN submitted_by_user_id SET NOT NULL,
ALTER COLUMN home_score SET NOT NULL,
    ALTER COLUMN away_score SET NOT NULL,
    ALTER COLUMN winner SET NOT NULL;

-- Defense in depth: a match is either two users (ONE_VS_ONE) or two teams
-- (TEAM_VS_TEAM), never a mix of both and never missing a side.
ALTER TABLE matches
    ADD CONSTRAINT chk_matches_participants CHECK (
        (format = 'ONE_VS_ONE'
            AND home_user_id IS NOT NULL AND away_user_id IS NOT NULL
            AND home_team_id IS NULL AND away_team_id IS NULL)
            OR
        (format = 'TEAM_VS_TEAM'
            AND home_team_id IS NOT NULL AND away_team_id IS NOT NULL
            AND home_user_id IS NULL AND away_user_id IS NULL)
        );

CREATE INDEX idx_matches_status ON matches (status);

-- An open "looking for an opponent" post. Exactly one of posted_by_user_id /
-- posted_by_team_id is set, matching the listing's format. When a response
-- gets accepted, match_id is filled in and the listing becomes FILLED.
CREATE TABLE match_listings (
                                id BIGSERIAL PRIMARY KEY,
                                sport_id BIGINT NOT NULL REFERENCES sports(id),
                                format VARCHAR(30) NOT NULL,
                                posted_by_user_id BIGINT REFERENCES users(id),
                                posted_by_team_id BIGINT REFERENCES teams(id),
                                location VARCHAR(255),
                                proposed_datetime TIMESTAMPTZ,
                                status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
                                match_id BIGINT REFERENCES matches(id),
                                created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                                updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                                CONSTRAINT chk_match_listings_poster CHECK (
                                    (format = 'ONE_VS_ONE' AND posted_by_user_id IS NOT NULL AND posted_by_team_id IS NULL)
                                        OR
                                    (format = 'TEAM_VS_TEAM' AND posted_by_team_id IS NOT NULL AND posted_by_user_id IS NULL)
                                    )
);

CREATE INDEX idx_match_listings_status ON match_listings (status);

-- A response from an interested user/team to an open listing. Exactly one of
-- responder_user_id / responder_team_id is set, matching the listing's format.
CREATE TABLE match_listing_responses (
                                         id BIGSERIAL PRIMARY KEY,
                                         listing_id BIGINT NOT NULL REFERENCES match_listings(id),
                                         responder_user_id BIGINT REFERENCES users(id),
                                         responder_team_id BIGINT REFERENCES teams(id),
                                         status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
                                         created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                                         updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
                                         CONSTRAINT chk_match_listing_responses_responder CHECK (
                                             (responder_user_id IS NOT NULL AND responder_team_id IS NULL)
                                                 OR
                                             (responder_team_id IS NOT NULL AND responder_user_id IS NULL)
                                             )
);

CREATE INDEX idx_match_listing_responses_listing_status ON match_listing_responses (listing_id, status);