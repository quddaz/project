package com.woowapractice.problem.application;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProblemTestSourceValidatorTest {

  private final ProblemTestSourceValidator validator = new ProblemTestSourceValidator();

  @Test
  @DisplayName("ApplicationTest의 NsTest 계약을 만족하는 소스를 허용한다")
  void validate_validSource_doesNotThrow() {
    assertThatCode(() -> validator.validate(validSource())).doesNotThrowAnyException();
  }

  @Test
  @DisplayName("ApplicationTest 계약이 없으면 등록을 거부한다")
  void validate_missingContract_throwsInvalidProblemTestSourceException() {
    assertThatThrownBy(() -> validator.validate("class ApplicationTest {}"))
        .isInstanceOf(InvalidProblemTestSourceException.class);
  }

  private String validSource() {
    return """
        import camp.nextstep.edu.missionutils.test.NsTest;
        class ApplicationTest extends NsTest {
          @Override public void runMain() {}
        }
        """;
  }
}
