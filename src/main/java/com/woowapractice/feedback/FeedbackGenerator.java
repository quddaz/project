package com.woowapractice.feedback;

import com.woowapractice.grading.TestResultValue;
import java.util.List;

public interface FeedbackGenerator {

  FeedbackDraft generate(List<TestResultValue> results);
}
