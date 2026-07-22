ALTER TABLE problem_versions
    ADD COLUMN application_test_source LONGTEXT NULL;

UPDATE problem_versions
SET application_test_source = ''
WHERE application_test_source IS NULL;

ALTER TABLE problem_versions
    MODIFY COLUMN application_test_source LONGTEXT NOT NULL;
