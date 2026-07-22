package com.woowapractice.sync;

import com.woowapractice.problem.domain.ProblemDomainException;
import com.woowapractice.problem.domain.ProblemErrorCode;

public class InvalidProblemSyncArgumentsException extends ProblemDomainException {

  public InvalidProblemSyncArgumentsException() {
    super(ProblemErrorCode.INVALID_PROBLEM_SYNC_ARGUMENTS);
  }
}
