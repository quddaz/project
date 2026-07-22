package com.woowapractice.feedback;

public class FeedbackNotFoundException extends RuntimeException {

  public FeedbackNotFoundException(Long submissionId) {
    super("피드백을 찾을 수 없습니다: " + submissionId);
  }
}
