CREATE INDEX idx_user_email_not_deleted ON "user" (email) WHERE is_deleted = false;
CREATE INDEX idx_user_nickname_not_deleted ON "user" (nickname) WHERE is_deleted = false;

COMMENT ON INDEX idx_user_email_not_deleted IS '로그인 인증 최적화 (Partial Index)';
COMMENT ON INDEX idx_user_nickname_not_deleted IS '닉네임 중복 체크 최적화';

CREATE INDEX idx_team_member_team_user_not_deleted ON team_member (team_id, user_id) WHERE is_deleted = false;
CREATE INDEX idx_team_member_user_not_deleted ON team_member (user_id) WHERE is_deleted = false;

COMMENT ON INDEX idx_team_member_team_user_not_deleted IS '권한 검증(@RequireTeamRole) AOP 최적화';
COMMENT ON INDEX idx_team_member_user_not_deleted IS '사용자 팀 조회 최적화';

CREATE INDEX idx_match_post_location_gist ON match_post USING GIST (location) WHERE is_deleted = false AND status = 'OPEN';

COMMENT ON INDEX idx_match_post_location_gist IS '지도 기반 매치 검색 (PostGIS GIST Index)';

-- 정렬: ORDER BY match_date ASC, id ASC (기본 정렬)
CREATE INDEX idx_match_post_status_date ON match_post (status, match_date ASC, id ASC) WHERE is_deleted = false;

-- 정렬: ORDER BY created_at DESC, id DESC
CREATE INDEX idx_match_post_team_created ON match_post (team_id, created_at DESC, id DESC) WHERE is_deleted = false;

COMMENT ON INDEX idx_match_post_status_date IS '매치 목록 필터링 최적화 (ASC 정렬)';
COMMENT ON INDEX idx_match_post_team_created IS '팀별 매치글 조회 최적화 (DESC 정렬)';

CREATE INDEX idx_team_created_id_not_deleted ON team (created_at DESC, id DESC) WHERE is_deleted = false;
CREATE INDEX idx_team_leader_not_deleted ON team (leader_user_id) WHERE is_deleted = false;

COMMENT ON INDEX idx_team_created_id_not_deleted IS '팀 목록 정렬 최적화 (무한스크롤)';
COMMENT ON INDEX idx_team_leader_not_deleted IS '팀장 중복 체크 최적화';

CREATE INDEX idx_team_app_team_status_created ON team_application (team_id, status, created_at DESC);

CREATE INDEX idx_team_app_user_status ON team_application (user_id, status);

COMMENT ON INDEX idx_team_app_team_status_created IS '팀 가입 신청 목록 조회 최적화';
COMMENT ON INDEX idx_team_app_user_status IS '사용자 신청 상태 조회';

CREATE INDEX idx_match_app_post_status_created ON match_application (post_id, status, created_at DESC);

CREATE UNIQUE INDEX idx_match_app_post_team_unique ON match_application (post_id, applicant_team_id);

COMMENT ON INDEX idx_match_app_post_status_created IS '매치 신청 목록 조회 최적화';
COMMENT ON INDEX idx_match_app_post_team_unique IS '중복 신청 방지 (UNIQUE 제약)';

CREATE INDEX idx_oauth_provider_userid_not_deleted ON oauth_account (provider, provider_user_id) WHERE is_deleted = false;
COMMENT ON INDEX idx_oauth_provider_userid_not_deleted IS 'OAuth 인증 최적화 (Kakao 로그인)';

ANALYZE "user";
ANALYZE team;
ANALYZE team_member;
ANALYZE team_application;
ANALYZE match_post;
ANALYZE match_application;
ANALYZE oauth_account;
