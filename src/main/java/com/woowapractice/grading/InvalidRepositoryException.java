package com.woowapractice.grading;

public class InvalidRepositoryException extends RuntimeException {

  public InvalidRepositoryException() {
    super("공개 GitHub 저장소 주소만 제출할 수 있습니다.");
  }
}
