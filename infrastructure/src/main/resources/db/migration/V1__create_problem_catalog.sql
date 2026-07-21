CREATE TABLE problems (
    id BIGINT NOT NULL AUTO_INCREMENT,
    slug VARCHAR(100) NOT NULL,
    title VARCHAR(200) NOT NULL,
    stage VARCHAR(20) NOT NULL,
    display_order INT NOT NULL,
    description_md TEXT NOT NULL,
    starter_repo_url VARCHAR(500) NOT NULL,
    active BOOLEAN NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_problems_slug UNIQUE (slug),
    INDEX idx_problems_active_stage_order (active, stage, display_order, id)
);

CREATE TABLE problem_versions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    problem_id BIGINT NOT NULL,
    version INT NOT NULL,
    java_version INT NOT NULL,
    test_bundle_ref VARCHAR(300) NOT NULL,
    config_checksum VARCHAR(64) NOT NULL,
    published_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_problem_versions_problem FOREIGN KEY (problem_id) REFERENCES problems (id),
    CONSTRAINT uk_problem_versions_problem_version UNIQUE (problem_id, version)
);
