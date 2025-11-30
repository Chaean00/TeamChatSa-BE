DROP INDEX IF EXISTS idx_match_post_status_date;

CREATE INDEX idx_match_post_status_date
    ON match_post (match_date ASC, id DESC )
    WHERE is_deleted = false
          AND status = 'OPEN';

COMMENT ON INDEX idx_match_post_status_date IS '매치 목록 조회 최적화 (OPEN 전용, 날짜/ID 정렬)';

ANALYZE match_post;