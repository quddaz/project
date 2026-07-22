package com.woowapractice.support;

import org.springframework.jdbc.core.JdbcTemplate;

public class DatabaseCleaner {

  private final JdbcTemplate jdbcTemplate;

  public DatabaseCleaner(JdbcTemplate jdbcTemplate) {
    this.jdbcTemplate = jdbcTemplate;
  }

  public void clean() {
    jdbcTemplate.update("DELETE FROM problem_versions");
    jdbcTemplate.update("DELETE FROM problems");
  }
}
