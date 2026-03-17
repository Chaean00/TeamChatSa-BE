SET search_path TO app, public;

-- team 테이블 제약 조건 추가
ALTER TABLE team
    ADD CONSTRAINT unique_team_leader UNIQUE (leader_user_id) DEFERRABLE INITIALLY IMMEDIATE;

-- team_member 테이블 제약 조건 추가
ALTER TABLE team_member
    ADD CONSTRAINT unique_team_user UNIQUE (user_id) DEFERRABLE INITIALLY IMMEDIATE,
    ADD CONSTRAINT unique_team_team_user UNIQUE (team_id, user_id) DEFERRABLE INITIALLY IMMEDIATE;

-- match_application 테이블 제약 조건 추가
ALTER TABLE match_application
    ADD CONSTRAINT unique_match_post_team UNIQUE (post_id, applicant_team_id) DEFERRABLE INITIALLY IMMEDIATE;

-- users 테이블 제약 조건 추가
ALTER TABLE users
    ADD CONSTRAINT unique_user_email UNIQUE (email) DEFERRABLE INITIALLY IMMEDIATE,
    ADD CONSTRAINT unique_user_nickname UNIQUE (nickname) DEFERRABLE INITIALLY IMMEDIATE;
