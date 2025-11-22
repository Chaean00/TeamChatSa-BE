ALTER TABLE match_post
    ADD COLUMN head_count INT NOT NULL DEFAULT 11,
    ADD CONSTRAINT head_count_check CHECK (head_count IN (4, 5, 6, 7, 8, 9, 10, 11));