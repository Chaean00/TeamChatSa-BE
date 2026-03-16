-- user 테이블: is_deleted 제거 및 deleted_at 추가
ALTER TABLE "user" ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP WITHOUT TIME ZONE;
ALTER TABLE "user" DROP COLUMN IF EXISTS is_deleted;

-- team 테이블: is_deleted 제거 및 deleted_at 추가
ALTER TABLE "team" ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP WITHOUT TIME ZONE;
ALTER TABLE "team" DROP COLUMN IF EXISTS is_deleted;

-- team_member 테이블: is_deleted 제거 및 deleted_at 추가
ALTER TABLE "team_member" ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP WITHOUT TIME ZONE;
ALTER TABLE "team_member" DROP COLUMN IF EXISTS is_deleted;

-- team_application 테이블: deleted_at 추가
ALTER TABLE "team_application" ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP WITHOUT TIME ZONE;

-- match_post 테이블: is_deleted 제거 및 deleted_at 추가
ALTER TABLE "match_post" ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP WITHOUT TIME ZONE;
ALTER TABLE "match_post" DROP COLUMN IF EXISTS is_deleted;

-- match_application 테이블: deleted_at 추가
ALTER TABLE "match_application" ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP WITHOUT TIME ZONE;

-- oauth_account 테이블: is_deleted 제거 및 deleted_at 추가
ALTER TABLE "oauth_account" ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP WITHOUT TIME ZONE;
ALTER TABLE "oauth_account" DROP COLUMN IF EXISTS is_deleted;

-- notification 테이블: deleted_at 추가
ALTER TABLE notification ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP WITHOUT TIME ZONE;
