package com.woowapractice.problem.domain;

public class ProblemVersionConflictException extends ProblemDomainException {

  public ProblemVersionConflictException() {
    super(ProblemErrorCode.PROBLEM_VERSION_CONFLICT);
  }
}
