CREATE TABLE submissions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    problem_version_id BIGINT NOT NULL,
    repository_url VARCHAR(500) NOT NULL,
    commit_sha VARCHAR(40) NOT NULL,
    status VARCHAR(20) NOT NULL,
    submitted_at TIMESTAMP(6) NOT NULL,
    completed_at TIMESTAMP(6) NULL,
    error_message VARCHAR(1000) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_submissions_problem_version FOREIGN KEY (problem_version_id) REFERENCES problem_versions (id),
    INDEX idx_submissions_problem_submitted (problem_version_id, submitted_at DESC, id DESC)
);

CREATE TABLE grading_jobs (
    id BIGINT NOT NULL AUTO_INCREMENT,
    submission_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL,
    available_at TIMESTAMP(6) NOT NULL,
    claimed_at TIMESTAMP(6) NULL,
    worker_id VARCHAR(100) NULL,
    attempt INT NOT NULL,
    last_error VARCHAR(1000) NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_grading_jobs_submission UNIQUE (submission_id),
    CONSTRAINT fk_grading_jobs_submission FOREIGN KEY (submission_id) REFERENCES submissions (id),
    INDEX idx_grading_jobs_claim (status, available_at, id)
);

CREATE TABLE test_results (
    id BIGINT NOT NULL AUTO_INCREMENT,
    submission_id BIGINT NOT NULL,
    test_identifier VARCHAR(300) NOT NULL,
    display_name VARCHAR(300) NOT NULL,
    status VARCHAR(20) NOT NULL,
    duration_ms BIGINT NULL,
    failure_message VARCHAR(2000) NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_test_results_submission FOREIGN KEY (submission_id) REFERENCES submissions (id),
    CONSTRAINT uk_test_results_submission_identifier UNIQUE (submission_id, test_identifier)
);
