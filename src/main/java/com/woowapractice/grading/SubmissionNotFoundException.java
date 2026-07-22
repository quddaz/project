package com.woowapractice.grading;

public class SubmissionNotFoundException extends RuntimeException {

  public SubmissionNotFoundException(Long id) {
    super("제출 결과를 찾을 수 없습니다: " + id);
  }
}
