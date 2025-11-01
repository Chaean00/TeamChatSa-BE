-- Enum Type 삭제 이후 VARCHAR로 변경
-- 제약조건을 통해 허용값 강제
-- 애플리케이션 레벨에서는 Enum Class로 통제
BEGIN; -- Transaction

ALTER TABLE "user"
    ALTER COLUMN role DROP DEFAULT,
    ALTER COLUMN role TYPE VARCHAR(16) USING role::text;

ALTER TABLE "user"
    ADD CONSTRAINT ck_user_role CHECK ( role IN ('LEADER', 'PLAYER') );


ALTER TABLE "team_member"
    ALTER COLUMN position TYPE VARCHAR(8) USING position::text;

ALTER TABLE "team_member"
    ADD CONSTRAINT ck_team_member_position
        CHECK (position IN (
                            'GK','CB','LCB','RCB','LB','RB','LWB','RWB',
                            'CDM','CM','CAM','LM','RM','LW','RW','CF','LF','RF',
                            'ST','LS','RS'
            ));


ALTER TABLE "match_post"
    ALTER COLUMN status DROP DEFAULT,
    ALTER COLUMN status TYPE VARCHAR(8) USING status::text;

ALTER TABLE "match_post"
    ALTER COLUMN status SET DEFAULT 'OPEN';

ALTER TABLE "match_post"
    ADD CONSTRAINT ck_match_post_status CHECK (status IN ('OPEN','CLOSED'));


ALTER TABLE "match_application"
    ALTER COLUMN status DROP DEFAULT,
    ALTER COLUMN status TYPE VARCHAR(10) USING status::text;

ALTER TABLE "match_application"
    ALTER COLUMN status SET DEFAULT 'PENDING';

ALTER TABLE "match_application"
    ADD CONSTRAINT ck_match_application_status
        CHECK (status IN ('PENDING','ACCEPTED','REJECTED','CANCELLED'));


ALTER TABLE "oauth_account"
    ALTER COLUMN provider DROP DEFAULT,
    ALTER COLUMN provider TYPE VARCHAR(16) USING provider::text;

ALTER TABLE "oauth_account"
    ADD CONSTRAINT ck_oauth_account_provider CHECK (provider IN ('KAKAO'));


-- 더 이상 사용하지 않는 ENUM TYPE 제거
DROP TYPE IF EXISTS role_type CASCADE;
DROP TYPE IF EXISTS position_type CASCADE;
DROP TYPE IF EXISTS match_post_status CASCADE;
DROP TYPE IF EXISTS match_application_status CASCADE;

COMMIT;