package com.woowapractice.grading;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class GradingResultService {

  private final SubmissionRepository submissionRepository;
  private final TestResultRepository testResultRepository;

  @Transactional
  public void record(Long submissionId, List<TestResultValue> values) {
    Submission submission =
        submissionRepository
            .findById(submissionId)
            .orElseThrow(() -> new SubmissionNotFoundException(submissionId));
    values.stream()
        .map(value -> TestResult.create(submission, value))
        .forEach(testResultRepository::save);
    SubmissionStatus status =
        values.stream().allMatch(value -> value.status() == TestResultStatus.PASSED)
            ? SubmissionStatus.PASSED
            : SubmissionStatus.FAILED;
    submission.complete(status, null);
  }

  @Transactional
  public void recordPlatformError(Long submissionId, String message) {
    Submission submission =
        submissionRepository
            .findById(submissionId)
            .orElseThrow(() -> new SubmissionNotFoundException(submissionId));
    submission.complete(SubmissionStatus.ERROR, message);
  }
}
