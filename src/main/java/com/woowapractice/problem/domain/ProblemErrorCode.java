package com.woowapractice.problem.domain;

public enum ProblemErrorCode {
  PROBLEM_VERSION_ALREADY_ASSOCIATED("이미 다른 문제에 속한 문제 버전입니다."),
  PROBLEM_VERSION_CONFLICT("같은 문제 버전에 다른 체크섬을 사용할 수 없습니다."),
  INVALID_PROBLEM_DEFINITION("문제 정의가 유효하지 않습니다."),
  INVALID_PROBLEM_SYNC_ARGUMENTS("문제 정의 루트 경로를 정확히 하나 입력해야 합니다."),
  INVALID_STAGE("유효하지 않은 차수입니다."),
  INVALID_PROBLEM_TEST_SOURCE("공식 ApplicationTest 소스가 올바르지 않습니다."),
  PROBLEM_ALREADY_EXISTS("이미 존재하는 문제 slug입니다."),
  PROBLEM_NOT_FOUND("문제를 찾을 수 없습니다.");

  private final String message;

  ProblemErrorCode(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }
}
