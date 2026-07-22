package com.woowapractice.support;

import org.springframework.jdbc.core.JdbcTemplate;

public class DatabaseCleaner {

  private final JdbcTemplate jdbcTemplate;

  public DatabaseCleaner(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public void clean() {
    jdbcTemplate.update("DELETE FROM test_results");
    jdbcTemplate.update("DELETE FROM grading_jobs");
    jdbcTemplate.update("DELETE FROM submissions");
    jdbcTemplate.update("DELETE FROM problem_versions");
    jdbcTemplate.update("DELETE FROM problems");
  }
}
