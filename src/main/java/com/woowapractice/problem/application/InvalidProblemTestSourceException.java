package com.woowapractice.problem.application;

import com.woowapractice.problem.domain.ProblemDomainException;
import com.woowapractice.problem.domain.ProblemErrorCode;

public class InvalidProblemTestSourceException extends ProblemDomainException {

  public InvalidProblemTestSourceException() {
    super(ProblemErrorCode.INVALID_PROBLEM_TEST_SOURCE);
  }
}
