package com.woowapractice.sync;

import com.woowapractice.problem.domain.ProblemDomainException;
import com.woowapractice.problem.domain.ProblemErrorCode;
import java.nio.file.Path;

public class InvalidProblemDefinitionException extends ProblemDomainException {

  public InvalidProblemDefinitionException(Path path) {
    super(
        ProblemErrorCode.INVALID_PROBLEM_DEFINITION,
        ProblemErrorCode.INVALID_PROBLEM_DEFINITION.getMessage() + " 경로: " + path);
  }

  public InvalidProblemDefinitionException(Path path, Throwable cause) {
    super(
        ProblemErrorCode.INVALID_PROBLEM_DEFINITION,
        ProblemErrorCode.INVALID_PROBLEM_DEFINITION.getMessage() + " 경로: " + path,
        cause);
  }
}
