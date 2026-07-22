package com.woowapractice.problem.application;

import com.woowapractice.problem.domain.ProblemDomainException;
import com.woowapractice.problem.domain.ProblemErrorCode;

public class ProblemAlreadyExistsException extends ProblemDomainException {

  public ProblemAlreadyExistsException() {
    super(ProblemErrorCode.PROBLEM_ALREADY_EXISTS);
  }
}
