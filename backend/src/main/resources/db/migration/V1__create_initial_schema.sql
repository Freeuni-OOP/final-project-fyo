CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    firebase_uid VARCHAR(255) UNIQUE NOT NULL,
    name VARCHAR(255) NOT NULL,
    surname VARCHAR(255) NOT NULL,
    username VARCHAR(255) UNIQUE NOT NULL,
    email VARCHAR(255) UNIQUE NOT NULL,
    age SMALLINT,
    sex VARCHAR(30),
    region VARCHAR(255),
    image_url TEXT,
    is_admin BOOLEAN NOT NULL DEFAULT FALSE,
    is_onboarding BOOLEAN NOT NULL DEFAULT FALSE,
    is_archived BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    archived_at TIMESTAMPTZ
);

CREATE TABLE sports (
    id BIGSERIAL PRIMARY KEY,
    sport_name VARCHAR(255) UNIQUE NOT NULL
);

CREATE TABLE user_sports (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id),
    sport_id BIGINT NOT NULL REFERENCES sports(id),
    skill_level VARCHAR(30) NOT NULL DEFAULT 'BEGINNER',
    CONSTRAINT uk_user_sports_user_sport UNIQUE (user_id, sport_id)
);

CREATE TABLE teams (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    sport_id BIGINT NOT NULL REFERENCES sports(id),
    region VARCHAR(255),
    description TEXT,
    logo_url TEXT,
    captain_id BIGINT NOT NULL REFERENCES users(id),
    max_players SMALLINT NOT NULL DEFAULT 11,
    open_spots SMALLINT NOT NULL DEFAULT 11,
    is_recruiting BOOLEAN NOT NULL DEFAULT TRUE,
    is_archived BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE team_members (
    id BIGSERIAL PRIMARY KEY,
    team_id BIGINT NOT NULL REFERENCES teams(id),
    user_id BIGINT NOT NULL REFERENCES users(id),
    role VARCHAR(30) NOT NULL DEFAULT 'MEMBER',
    joined_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_team_members_team_user UNIQUE (team_id, user_id)
);

CREATE TABLE matches (
    id BIGSERIAL PRIMARY KEY,
    sport_id BIGINT NOT NULL REFERENCES sports(id),
    format VARCHAR(30) NOT NULL,
    home_user_id BIGINT REFERENCES users(id),
    away_user_id BIGINT REFERENCES users(id),
    home_team_id BIGINT REFERENCES teams(id),
    away_team_id BIGINT REFERENCES teams(id),
    location VARCHAR(255),
    proposed_datetime TIMESTAMPTZ,
    status VARCHAR(30) NOT NULL DEFAULT 'UPCOMING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT now()
);

CREATE TABLE match_results (
    id BIGSERIAL PRIMARY KEY,
    match_id BIGINT UNIQUE NOT NULL REFERENCES matches(id),
    home_score SMALLINT,
    away_score SMALLINT,
    winner VARCHAR(30),
    submitted_by_user_id BIGINT REFERENCES users(id),
    confirmation_status VARCHAR(30) NOT NULL DEFAULT 'PENDING',
    submitted_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    confirmed_at TIMESTAMPTZ
);

CREATE TABLE user_reviews (
    id BIGSERIAL PRIMARY KEY,
    match_id BIGINT NOT NULL REFERENCES matches(id),
    review_user_id BIGINT NOT NULL REFERENCES users(id),
    reviewer_team_id BIGINT REFERENCES teams(id),
    reviewed_user_id BIGINT REFERENCES users(id),
    reviewed_team_id BIGINT REFERENCES teams(id),
    score SMALLINT NOT NULL,
    comment TEXT,
    created_at TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT chk_user_reviews_score CHECK (score BETWEEN 1 AND 5)
);
