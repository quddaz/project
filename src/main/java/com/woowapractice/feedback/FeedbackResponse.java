package com.woowapractice.feedback;

import java.time.Instant;

public record FeedbackResponse(
    FeedbackStatus status,
    String provider,
    String summary,
    String strengths,
    String improvements,
    Instant generatedAt) {

  public static FeedbackResponse from(SubmissionFeedback feedback) {
    return new FeedbackResponse(
        feedback.getStatus(),
        feedback.getProvider(),
        feedback.getSummary(),
        feedback.getStrengths(),
        feedback.getImprovements(),
        feedback.getGeneratedAt());
  }
}
