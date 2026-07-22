package com.woowapractice.grading;

public class GitHubClientException extends RuntimeException {

  public GitHubClientException(String message, Throwable cause) {
    super(message, cause);
  }

  public GitHubClientException(String message) {
    super(message);
  }
}
