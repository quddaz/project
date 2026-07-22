package com.woowapractice.common.presentation;

import com.woowapractice.problem.application.ProblemNotFoundException;
import com.woowapractice.problem.domain.ProblemErrorCode;
import java.util.List;
import java.util.UUID;
import org.slf4j.MDC;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
public class GlobalExceptionHandler {

  private static final String TRACE_ID_KEY = "traceId";

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
    ApiErrorResponse response = createInvalidStageResponse(exception.getName());
    return ResponseEntity.badRequest().body(response);
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ApiErrorResponse> handleMissingServletRequestParameter(
      MissingServletRequestParameterException exception) {
    ApiErrorResponse response = createInvalidStageResponse(exception.getParameterName());
    return ResponseEntity.badRequest().body(response);
  }

  private ApiErrorResponse createInvalidStageResponse(String field) {
    String message = ProblemErrorCode.INVALID_STAGE.getMessage();
    return new ApiErrorResponse(
        ProblemErrorCode.INVALID_STAGE.name(),
        message,
        List.of(new ApiErrorResponse.FieldError(field, message)),
        getTraceId());
  }

  private String getTraceId() {
    String traceId = MDC.get(TRACE_ID_KEY);
    if (traceId == null || traceId.isBlank()) {
      return UUID.randomUUID().toString();
    }
    return traceId;
  }
}
