ALTER TABLE "team"
    ADD CONSTRAINT unique_team_leader UNIQUE (leader_user_id) DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "team_member"
    ADD CONSTRAINT unique_team_user UNIQUE (user_id) DEFERRABLE INITIALLY IMMEDIATE,
    ADD CONSTRAINT unique_team_team_user UNIQUE (team_id, user_id) DEFERRABLE INITIALLY IMMEDIATE;

ALTER TABLE "match_application"
    ADD CONSTRAINT unique_match_post_team UNIQUE (post_id, applicant_team_id) DEFERRABLE INITIALLY IMMEDIATE;