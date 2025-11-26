-- team 테이블의 name UNIQUE 제약 제거
ALTER TABLE team DROP CONSTRAINT IF EXISTS team_name_key;

-- Partial Unique Index 생성: is_deleted = false인 레코드에만 unique 적용
CREATE UNIQUE INDEX team_name_unique_active ON team (name) WHERE is_deleted = false;

-- team_member 테이블의 UNIQUE 제약 제거
ALTER TABLE team_member DROP CONSTRAINT IF EXISTS unique_team_user;
ALTER TABLE team_member DROP CONSTRAINT IF EXISTS unique_team_team_user;

-- Partial Unique Index 생성: is_deleted = false인 레코드에만 unique 적용
-- 한 사용자는 하나의 활성 팀에만 속할 수 있음
CREATE UNIQUE INDEX team_member_user_unique_active ON team_member (user_id) WHERE is_deleted = false;

-- 한 팀 내에서 같은 사용자는 중복될 수 없음 (활성 멤버만)
CREATE UNIQUE INDEX team_member_team_user_unique_active ON team_member (team_id, user_id) WHERE is_deleted = false;