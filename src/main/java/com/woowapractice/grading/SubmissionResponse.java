package com.woowapractice.grading;

import java.time.Instant;
import java.util.List;

public final class SubmissionResponse {

  private SubmissionResponse() {}

  public record Detail(
      Long id,
      String repositoryUrl,
      String commitSha,
      SubmissionStatus status,
      Instant submittedAt,
      Instant completedAt,
      String errorMessage,
      List<Test> tests) {

    public static Detail from(Submission submission, List<TestResult> testResults) {
      return new Detail(
          submission.getId(),
          submission.getRepositoryUrl(),
          submission.getCommitSha(),
          submission.getStatus(),
          submission.getSubmittedAt(),
          submission.getCompletedAt(),
          submission.getErrorMessage(),
          testResults.stream().map(Test::from).toList());
    }
  }

  public record Test(
      String testIdentifier,
      String displayName,
      TestResultStatus status,
      Long durationMillis,
      String failureMessage) {

    static Test from(TestResult result) {
      return new Test(
          result.getTestIdentifier(),
          result.getDisplayName(),
          result.getStatus(),
          result.getDurationMillis(),
          result.getFailureMessage());
    }
  }
}
