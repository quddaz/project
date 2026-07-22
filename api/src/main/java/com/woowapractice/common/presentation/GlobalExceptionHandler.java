package com.woowapractice.common.presentation;

import com.woowapractice.problem.application.ProblemNotFoundException;
import java.util.List;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final String TRACE_ID_KEY = "traceId";
  private static final String INVALID_STAGE = "INVALID_STAGE";
  private static final String INVALID_STAGE_MESSAGE = "유효하지 않은 차수입니다.";

  @ExceptionHandler(ProblemNotFoundException.class)
  public ResponseEntity<ApiErrorResponse> handleProblemNotFound(
      ProblemNotFoundException exception) {
    ApiErrorResponse response =
        new ApiErrorResponse(
            exception.getErrorCode().name(), exception.getMessage(), List.of(), getTraceId());
    return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ApiErrorResponse> handleMethodArgumentTypeMismatch(
      MethodArgumentTypeMismatchException exception) {
    ApiErrorResponse response =
        new ApiErrorResponse(
            INVALID_STAGE,
            INVALID_STAGE_MESSAGE,
            List.of(new ApiErrorResponse.FieldError(exception.getName(), INVALID_STAGE_MESSAGE)),
            getTraceId());
    return ResponseEntity.badRequest().body(response);
  }

  private String getTraceId() {
    String traceId = MDC.get(TRACE_ID_KEY);
    if (traceId == null || traceId.isBlank()) {
      return UUID.randomUUID().toString();
    }
    return traceId;
  }
}
