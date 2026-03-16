-- 1. pgvector 확장 활성화
CREATE EXTENSION IF NOT EXISTS vector;

-- 2. Team 테이블 컬럼 수정 및 추가
ALTER TABLE team ADD COLUMN IF NOT EXISTS win_rate DOUBLE PRECISION DEFAULT 0.0 NOT NULL;
ALTER TABLE team ADD COLUMN IF NOT EXISTS style_vector vector(512);

-- 3. Team Review 테이블 생성
CREATE TABLE IF NOT EXISTS team_review (
    id BIGSERIAL PRIMARY KEY,
    team_id BIGINT NOT NULL,
    reviewer_user_id BIGINT NOT NULL,
    match_id BIGINT NOT NULL,
    rating INTEGER NOT NULL CHECK (rating >= 1 AND rating <= 5),
    content TEXT NOT NULL,
    created_at TIMESTAMP WITHOUT TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITHOUT TIME ZONE,
    deleted_at TIMESTAMP WITHOUT TIME ZONE
);
