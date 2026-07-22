package com.woowapractice.common.presentation;

public enum CommonErrorCode {
  API_NOT_FOUND("요청한 API를 찾을 수 없습니다."),
  METHOD_NOT_ALLOWED("지원하지 않는 HTTP 메서드입니다."),
  INTERNAL_SERVER_ERROR("서버 내부 오류가 발생했습니다.");

  private final String message;

  CommonErrorCode(String message) {
    this.message = message;
  }

  public String getMessage() {
    return message;
  }
}
