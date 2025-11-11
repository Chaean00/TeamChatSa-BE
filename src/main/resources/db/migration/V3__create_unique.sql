ALTER TABLE "team"
    ADD CONSTRAINT unique_team_leader UNIQUE (leader_user_id) DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "team_member"
    ADD CONSTRAINT unique_team_user UNIQUE (user_id) DEFERRABLE INITIALLY IMMEDIATE,
    ADD CONSTRAINT unique_team_team_user UNIQUE (team_id, user_id) DEFERRABLE INITIALLY IMMEDIATE;
