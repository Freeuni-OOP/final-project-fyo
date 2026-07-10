CREATE TABLE friend_requests (
    id BIGSERIAL PRIMARY KEY,
    requester_id BIGINT NOT NULL REFERENCES users(id),
    addressee_id BIGINT NOT NULL REFERENCES users(id),
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_friend_requests_pair UNIQUE (requester_id, addressee_id),
    CONSTRAINT ck_friend_requests_not_self CHECK (requester_id <> addressee_id)
);

CREATE INDEX idx_friend_requests_requester_status ON friend_requests (requester_id, status);
CREATE INDEX idx_friend_requests_addressee_status ON friend_requests (addressee_id, status);
