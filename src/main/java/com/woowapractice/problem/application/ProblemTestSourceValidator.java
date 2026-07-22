package com.woowapractice.problem.application;

import org.springframework.stereotype.Component;

@Component
public class ProblemTestSourceValidator {

  private static final int MAX_SOURCE_LENGTH = 200_000;

  public void validate(String source) {
    if (source == null
        || source.isBlank()
        || source.length() > MAX_SOURCE_LENGTH
        || !source.contains("class ApplicationTest")
        || !source.contains("extends NsTest")
        || !source.contains("runMain")) {
      throw new InvalidProblemTestSourceException();
    }
  }
}
