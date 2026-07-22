package com.woowapractice.feedback;

import static org.assertj.core.api.Assertions.assertThat;

import com.woowapractice.grading.TestResultStatus;
import com.woowapractice.grading.TestResultValue;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RuleBasedFeedbackGeneratorTest {

  private final RuleBasedFeedbackGenerator generator = new RuleBasedFeedbackGenerator();

  @Test
  @DisplayName("모든 테스트가 성공하면 통과 피드백을 생성한다")
  void generate_allPassed_returnsPositiveFeedback() {
    FeedbackDraft draft =
        generator.generate(
            List.of(new TestResultValue("one", "기능_테스트", TestResultStatus.PASSED, 10L, null)));

    assertThat(draft.summary()).contains("통과");
    assertThat(draft.improvements()).isEmpty();
  }

  @Test
  @DisplayName("실패한 테스트와 메시지를 개선점에 포함한다")
  void generate_failedTest_includesFailureFeedback() {
    FeedbackDraft draft =
        generator.generate(
            List.of(new TestResultValue("one", "예외_테스트", TestResultStatus.FAILED, 10L, "예외 메시지")));

    assertThat(draft.summary()).contains("실패");
    assertThat(draft.improvements()).contains("예외_테스트", "예외 메시지");
  }
}
