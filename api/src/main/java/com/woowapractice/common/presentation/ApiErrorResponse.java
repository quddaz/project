package com.woowapractice.common.presentation;

import java.util.List;

public record ApiErrorResponse(
    String code, String message, List<FieldError> fieldErrors, String traceId) {

  public record FieldError(String field, String reason) {}
}
