package com.woowapractice.common.presentation;

import com.woowapractice.feedback.FeedbackNotFoundException;
import com.woowapractice.grading.GitHubClientException;
import com.woowapractice.grading.InvalidRepositoryException;
import com.woowapractice.grading.SubmissionNotFoundException;
import com.woowapractice.problem.application.InvalidProblemTestSourceException;
import com.woowapractice.problem.application.ProblemAlreadyExistsException;
import com.woowapractice.problem.application.ProblemNotFoundException;
import com.woowapractice.problem.domain.ProblemErrorCode;
import java.util.List;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@Slf4j
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

  @ExceptionHandler(InvalidRepositoryException.class)
  public ResponseEntity<ApiErrorResponse> handleInvalidRepository(
      InvalidRepositoryException exception) {
    return ResponseEntity.badRequest()
        .body(
            new ApiErrorResponse(
                "INVALID_REPOSITORY", exception.getMessage(), List.of(), getTraceId()));
  }

  @ExceptionHandler(GitHubClientException.class)
  public ResponseEntity<ApiErrorResponse> handleGitHubClientFailure(
      GitHubClientException exception) {
    return ResponseEntity.status(HttpStatus.BAD_GATEWAY)
        .body(
            new ApiErrorResponse(
                "GITHUB_UNAVAILABLE", exception.getMessage(), List.of(), getTraceId()));
  }

  @ExceptionHandler(SubmissionNotFoundException.class)
  public ResponseEntity<ApiErrorResponse> handleSubmissionNotFound(
      SubmissionNotFoundException exception) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(
            new ApiErrorResponse(
                "SUBMISSION_NOT_FOUND", exception.getMessage(), List.of(), getTraceId()));
  }

  @ExceptionHandler(FeedbackNotFoundException.class)
  public ResponseEntity<ApiErrorResponse> handleFeedbackNotFound(
      FeedbackNotFoundException exception) {
    return ResponseEntity.status(HttpStatus.NOT_FOUND)
        .body(
            new ApiErrorResponse(
                "FEEDBACK_NOT_FOUND", exception.getMessage(), List.of(), getTraceId()));
  }

  @ExceptionHandler(InvalidProblemTestSourceException.class)
  public ResponseEntity<ApiErrorResponse> handleInvalidProblemTestSource(
      InvalidProblemTestSourceException exception) {
    return ResponseEntity.badRequest()
        .body(
            new ApiErrorResponse(
                exception.getErrorCode().name(), exception.getMessage(), List.of(), getTraceId()));
  }

  @ExceptionHandler(ProblemAlreadyExistsException.class)
  public ResponseEntity<ApiErrorResponse> handleProblemAlreadyExists(
      ProblemAlreadyExistsException exception) {
    return ResponseEntity.status(HttpStatus.CONFLICT)
        .body(
            new ApiErrorResponse(
                exception.getErrorCode().name(), exception.getMessage(), List.of(), getTraceId()));
  }

  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(
      MethodArgumentNotValidException exception) {
    List<ApiErrorResponse.FieldError> fieldErrors =
        exception.getBindingResult().getFieldErrors().stream()
            .map(
                error ->
                    new ApiErrorResponse.FieldError(error.getField(), error.getDefaultMessage()))
            .toList();
    return ResponseEntity.badRequest()
        .body(
            new ApiErrorResponse("INVALID_REQUEST", "요청 값이 올바르지 않습니다.", fieldErrors, getTraceId()));
  }

  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ApiErrorResponse> handleMethodArgumentTypeMismatch(
      MethodArgumentTypeMismatchException exception) {
    ApiErrorResponse response = createInvalidStageResponse(exception.getName());
    return ResponseEntity.badRequest().body(response);
  }

  @ExceptionHandler(NoResourceFoundException.class)
  public ResponseEntity<ApiErrorResponse> handleNoResourceFound() {
    return createErrorResponse(HttpStatus.NOT_FOUND, CommonErrorCode.API_NOT_FOUND);
  }

  @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
  public ResponseEntity<ApiErrorResponse> handleHttpRequestMethodNotSupported(
      HttpRequestMethodNotSupportedException exception) {
    HttpHeaders headers = new HttpHeaders();
    headers.setAllow(exception.getSupportedHttpMethods());
    return ResponseEntity.status(HttpStatus.METHOD_NOT_ALLOWED)
        .headers(headers)
        .body(createErrorResponse(CommonErrorCode.METHOD_NOT_ALLOWED, getTraceId()));
  }

  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ApiErrorResponse> handleMissingServletRequestParameter(
      MissingServletRequestParameterException exception) {
    ApiErrorResponse response = createInvalidStageResponse(exception.getParameterName());
    return ResponseEntity.badRequest().body(response);
  }

  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiErrorResponse> handleUnexpected(Exception exception) {
    String traceId = getTraceId();
    log.error("Unexpected REST failure [traceId={}]", traceId, exception);
    ApiErrorResponse response = createErrorResponse(CommonErrorCode.INTERNAL_SERVER_ERROR, traceId);
    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
  }

  private ApiErrorResponse createInvalidStageResponse(String field) {
    String message = ProblemErrorCode.INVALID_STAGE.getMessage();
    return new ApiErrorResponse(
        ProblemErrorCode.INVALID_STAGE.name(),
        message,
        List.of(new ApiErrorResponse.FieldError(field, message)),
        getTraceId());
  }

  private ResponseEntity<ApiErrorResponse> createErrorResponse(
      HttpStatus status, CommonErrorCode errorCode) {
    return ResponseEntity.status(status).body(createErrorResponse(errorCode, getTraceId()));
  }

  private ApiErrorResponse createErrorResponse(CommonErrorCode errorCode, String traceId) {
    return new ApiErrorResponse(errorCode.name(), errorCode.getMessage(), List.of(), traceId);
  }

  private String getTraceId() {
    String traceId = MDC.get(TRACE_ID_KEY);
    if (traceId == null || traceId.isBlank()) {
      return UUID.randomUUID().toString();
    }
    return traceId;
  }
}
