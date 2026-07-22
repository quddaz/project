package com.woowapractice.problem.domain;

import lombok.Getter;

@Getter
public class ProblemDomainException extends RuntimeException {

  private final ProblemErrorCode errorCode;

  public ProblemDomainException(ProblemErrorCode errorCode) {
    super(errorCode.getMessage());
    this.errorCode = errorCode;
  }

  protected ProblemDomainException(ProblemErrorCode errorCode, String message) {
    super(message);
    this.errorCode = errorCode;
  }

  protected ProblemDomainException(ProblemErrorCode errorCode, String message, Throwable cause) {
    super(message, cause);
    this.errorCode = errorCode;
  }
}
