ALTER TABLE grading_jobs
    ADD COLUMN lease_expires_at TIMESTAMP(6) NULL;

CREATE INDEX idx_grading_jobs_lease
    ON grading_jobs (status, lease_expires_at, id);
