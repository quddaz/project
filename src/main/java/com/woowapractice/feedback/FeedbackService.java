package com.woowapractice.feedback;

import com.woowapractice.grading.SubmissionNotFoundException;
import com.woowapractice.grading.SubmissionRepository;
import com.woowapractice.grading.TestResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class FeedbackService {

  private final SubmissionRepository submissionRepository;
  private final TestResultRepository testResultRepository;
  private final SubmissionFeedbackRepository feedbackRepository;
  private final FeedbackGenerator feedbackGenerator;

  @Transactional
  public SubmissionFeedback generate(Long submissionId) {
    var submission =
        submissionRepository
            .findById(submissionId)
            .orElseThrow(() -> new SubmissionNotFoundException(submissionId));
    var results =
        testResultRepository.findAllBySubmissionIdOrderByIdAsc(submissionId).stream()
            .map(
                result ->
                    new com.woowapractice.grading.TestResultValue(
                        result.getTestIdentifier(),
                        result.getDisplayName(),
                        result.getStatus(),
                        result.getDurationMillis(),
                        result.getFailureMessage()))
            .toList();
    SubmissionFeedback feedback =
        feedbackRepository
            .findBySubmissionId(submissionId)
            .orElseGet(
                () ->
                    feedbackRepository.save(
                        SubmissionFeedback.generated(
                            submission, feedbackGenerator.generate(results))));
    return feedback;
  }

  public SubmissionFeedback find(Long submissionId) {
    return feedbackRepository
        .findBySubmissionId(submissionId)
        .orElseThrow(() -> new FeedbackNotFoundException(submissionId));
  }
}
