CREATE TABLE match_requests (
    id BIGSERIAL PRIMARY KEY,
    sport_id BIGINT NOT NULL REFERENCES sports(id),
    format VARCHAR(30) NOT NULL,
    requester_user_id BIGINT REFERENCES users(id),
    requester_team_id BIGINT REFERENCES teams(id),
    opponent_user_id BIGINT REFERENCES users(id),
    opponent_team_id BIGINT REFERENCES teams(id),
    location VARCHAR(255),
    proposed_datetime TIMESTAMPTZ,
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    match_id BIGINT UNIQUE REFERENCES matches(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_match_requests_participants CHECK (
        (format = 'ONE_VS_ONE'
            AND requester_user_id IS NOT NULL AND opponent_user_id IS NOT NULL
            AND requester_team_id IS NULL AND opponent_team_id IS NULL
            AND requester_user_id <> opponent_user_id)
        OR
        (format = 'TEAM_VS_TEAM'
            AND requester_team_id IS NOT NULL AND opponent_team_id IS NOT NULL
            AND requester_user_id IS NULL AND opponent_user_id IS NULL
            AND requester_team_id <> opponent_team_id)
    )
);

CREATE INDEX idx_match_requests_status ON match_requests (status);
CREATE INDEX idx_match_requests_requester_user ON match_requests (requester_user_id);
CREATE INDEX idx_match_requests_opponent_user ON match_requests (opponent_user_id);
CREATE INDEX idx_match_requests_requester_team ON match_requests (requester_team_id);
CREATE INDEX idx_match_requests_opponent_team ON match_requests (opponent_team_id);
