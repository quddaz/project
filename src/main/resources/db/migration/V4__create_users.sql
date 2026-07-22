CREATE TABLE users (
    id BIGINT NOT NULL AUTO_INCREMENT,
    github_id VARCHAR(100) NOT NULL,
    github_login VARCHAR(100) NOT NULL,
    display_name VARCHAR(200) NULL,
    role VARCHAR(20) NOT NULL,
    created_at TIMESTAMP(6) NOT NULL,
    updated_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_users_github_id UNIQUE (github_id)
);

ALTER TABLE submissions
    ADD COLUMN user_id BIGINT NULL;

ALTER TABLE submissions
    ADD CONSTRAINT fk_submissions_user FOREIGN KEY (user_id) REFERENCES users (id);

CREATE INDEX idx_submissions_user_submitted
    ON submissions (user_id, submitted_at DESC, id DESC);
