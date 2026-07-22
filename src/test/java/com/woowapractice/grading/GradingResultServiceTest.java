package com.woowapractice.grading;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class GradingResultServiceTest {

  @Mock private SubmissionRepository submissionRepository;
  @Mock private TestResultRepository testResultRepository;

  @Test
  @DisplayName("세 테스트 결과를 저장하고 모두 성공하면 제출을 통과 처리한다")
  void record_allTestsPassed_marksSubmissionPassed() {
    Submission submission =
        Submission.create(null, "https://github.com/example/app", "a".repeat(40));
    when(submissionRepository.findById(1L)).thenReturn(Optional.of(submission));
    GradingResultService service =
        new GradingResultService(submissionRepository, testResultRepository);

    service.record(
        1L,
        List.of(
            new TestResultValue("ApplicationTest#one", "one", TestResultStatus.PASSED, 10L, null),
            new TestResultValue("ApplicationTest#two", "two", TestResultStatus.PASSED, 20L, null),
            new TestResultValue(
                "ApplicationTest#three", "three", TestResultStatus.PASSED, 30L, null)));

    assertThat(submission.getStatus()).isEqualTo(SubmissionStatus.PASSED);
    verify(testResultRepository, org.mockito.Mockito.times(3)).save(any(TestResult.class));
  }
}
