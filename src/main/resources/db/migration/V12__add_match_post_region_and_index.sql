SET search_path TO app, public;

ALTER TABLE match_post
    ADD COLUMN IF NOT EXISTS region VARCHAR(10) DEFAULT '기타';

UPDATE match_post
SET region = CASE
    WHEN address LIKE '서울특별시%' OR address LIKE '서울%' THEN '서울'
    WHEN address LIKE '경기도%' OR address LIKE '경기%' THEN '경기'
    WHEN address LIKE '인천광역시%' OR address LIKE '인천%' THEN '인천'
    WHEN address LIKE '부산광역시%' OR address LIKE '부산%' THEN '부산'
    WHEN address LIKE '대구광역시%' OR address LIKE '대구%' THEN '대구'
    WHEN address LIKE '대전광역시%' OR address LIKE '대전%' THEN '대전'
    WHEN address LIKE '광주광역시%' OR address LIKE '광주%' THEN '광주'
    WHEN address LIKE '울산광역시%' OR address LIKE '울산%' THEN '울산'
    WHEN address LIKE '세종특별자치시%' OR address LIKE '세종%' THEN '세종'
    WHEN address LIKE '강원특별자치도%' OR address LIKE '강원도%' OR address LIKE '강원%' THEN '강원'
    WHEN address LIKE '충청북도%' OR address LIKE '충북%' THEN '충북'
    WHEN address LIKE '충청남도%' OR address LIKE '충남%' THEN '충남'
    WHEN address LIKE '전북특별자치도%' OR address LIKE '전라북도%' OR address LIKE '전북%' THEN '전북'
    WHEN address LIKE '전라남도%' OR address LIKE '전남%' THEN '전남'
    WHEN address LIKE '경상북도%' OR address LIKE '경북%' THEN '경북'
    WHEN address LIKE '경상남도%' OR address LIKE '경남%' THEN '경남'
    WHEN address LIKE '제주특별자치도%' OR address LIKE '제주%' THEN '제주'
    ELSE COALESCE(region, '기타')
END
WHERE region IS NULL OR region = '기타';

ALTER TABLE match_post
    ALTER COLUMN region SET NOT NULL;

DROP INDEX IF EXISTS idx_match_post_open_region_headcount_date;

CREATE INDEX idx_match_post_open_region_headcount_date
    ON match_post (region, head_count, match_date ASC, id DESC)
    WHERE deleted_at IS NULL
        AND status = 'OPEN';

COMMENT ON INDEX idx_match_post_open_region_headcount_date IS '매치 목록 조회 최적화 (region, headCount, 날짜/ID 정렬)';

ANALYZE match_post;
