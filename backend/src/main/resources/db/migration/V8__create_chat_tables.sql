CREATE TABLE conversations (
    id BIGSERIAL PRIMARY KEY,
    match_id BIGINT UNIQUE REFERENCES matches(id),
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE conversation_participants (
    id BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT NOT NULL REFERENCES conversations(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    joined_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_conversation_participants_conversation_user UNIQUE (conversation_id, user_id)
);

CREATE TABLE chat_messages (
    id BIGSERIAL PRIMARY KEY,
    conversation_id BIGINT NOT NULL REFERENCES conversations(id),
    sender_id BIGINT NOT NULL REFERENCES users(id),
    body TEXT NOT NULL,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    read_at TIMESTAMPTZ
);

CREATE INDEX idx_conversation_participants_user
ON conversation_participants(user_id);

CREATE INDEX idx_chat_messages_conversation_created
ON chat_messages(conversation_id, created_at);
