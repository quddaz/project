package com.woowapractice.problem.domain;

public enum ProblemErrorCode {
  PROBLEM_VERSION_ALREADY_ASSOCIATED("이미 다른 문제에 속한 문제 버전입니다."),
  PROBLEM_NOT_FOUND("문제를 찾을 수 없습니다.");

  private final String message;

  ProblemErrorCode(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }
}
