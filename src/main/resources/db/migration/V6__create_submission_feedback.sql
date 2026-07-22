CREATE TABLE submission_feedback (
    id BIGINT NOT NULL AUTO_INCREMENT,
    submission_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    provider VARCHAR(50) NOT NULL,
    summary TEXT NOT NULL,
    strengths TEXT NOT NULL,
    improvements TEXT NOT NULL,
    generated_at TIMESTAMP(6) NULL,
    error_message VARCHAR(1000) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_submission_feedback_submission UNIQUE (submission_id),
    CONSTRAINT fk_submission_feedback_submission FOREIGN KEY (submission_id) REFERENCES submissions (id)
);
