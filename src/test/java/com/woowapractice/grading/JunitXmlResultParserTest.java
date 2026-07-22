package com.woowapractice.grading;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class JunitXmlResultParserTest {

  private final JunitXmlResultParser parser = new JunitXmlResultParser();

  @Test
  @DisplayName("JUnit XML의 각 테스트 메서드 결과를 분리한다")
  void parse_threeTests_returnsThreeResults() {
    String xml =
        """
        <testsuite name="ApplicationTest" tests="3" failures="1" time="0.123">
          <testcase classname="ApplicationTest" name="기능_테스트" time="0.010"/>
          <testcase classname="ApplicationTest" name="예외_테스트" time="0.020"><failure message="기대값이 다릅니다">details</failure></testcase>
          <testcase classname="ApplicationTest" name="경계값_테스트" time="0.030"><skipped/></testcase>
        </testsuite>
        """;

    var results = parser.parse(new ByteArrayInputStream(xml.getBytes(StandardCharsets.UTF_8)));

    assertThat(results).hasSize(3);
    assertThat(results.get(0).status()).isEqualTo(TestResultStatus.PASSED);
    assertThat(results.get(1).status()).isEqualTo(TestResultStatus.FAILED);
    assertThat(results.get(1).failureMessage()).contains("기대값이 다릅니다");
    assertThat(results.get(2).status()).isEqualTo(TestResultStatus.SKIPPED);
  }
}
