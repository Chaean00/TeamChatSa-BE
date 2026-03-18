SET search_path TO app, public;

CREATE EXTENSION IF NOT EXISTS pg_trgm;
       
CREATE INDEX idx_team_name_trgm_not_deleted
    ON team
    USING GIN (name gin_trgm_ops)
    WHERE deleted_at IS NULL;

ALTER TABLE team
     ALTER COLUMN name SET STATISTICS 1000;

ALTER TABLE team_member
   ALTER COLUMN team_id SET STATISTICS 1000;

ANALYZE team;
ANALYZE team_member;
