package com.woowapractice.problem.application;

import com.woowapractice.problem.domain.ProblemErrorCode;
import lombok.Getter;

@Getter
public class ProblemNotFoundException extends RuntimeException {

  private final ProblemErrorCode errorCode;

  public ProblemNotFoundException(String slug) {
    super(ProblemErrorCode.PROBLEM_NOT_FOUND.getMessage());
    this.errorCode = ProblemErrorCode.PROBLEM_NOT_FOUND;
  }
}
