-- 1. Match Result 테이블 생성
CREATE TABLE IF NOT EXISTS match_result (
    id BIGSERIAL PRIMARY KEY,
    match_post_id BIGINT NOT NULL,
    home_team_id BIGINT NOT NULL,
    away_team_id BIGINT NOT NULL,
    winner_team_id BIGINT,
    home_score INTEGER,
    away_score INTEGER,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    deleted_at TIMESTAMP WITHOUT TIME ZONE
);
