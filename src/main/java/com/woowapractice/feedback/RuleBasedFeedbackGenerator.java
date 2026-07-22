package com.woowapractice.feedback;

import com.woowapractice.grading.TestResultStatus;
import com.woowapractice.grading.TestResultValue;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

@Component
public class RuleBasedFeedbackGenerator implements FeedbackGenerator {

  @Override
  public FeedbackDraft generate(List<TestResultValue> results) {
    List<TestResultValue> failures =
        results.stream().filter(result -> result.status() != TestResultStatus.PASSED).toList();
    if (failures.isEmpty()) {
      return new FeedbackDraft("모든 공식 테스트를 통과했습니다.", "입력·출력·예외 처리 요구사항을 현재 테스트 기준으로 만족했습니다.", "");
    }
    String improvements =
        failures.stream()
            .map(
                failure ->
                    failure.displayName()
                        + ": "
                        + (failure.failureMessage() == null
                            ? "실패 원인을 확인해 주세요."
                            : failure.failureMessage()))
            .collect(Collectors.joining("\n"));
    return new FeedbackDraft(
        "공식 테스트 " + failures.size() + "개가 실패했습니다.",
        "통과한 테스트부터 유지하고 실패한 시나리오를 하나씩 재현해 보세요.",
        improvements);
  }
}
