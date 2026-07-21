package com.woowapractice.problem;

import static org.assertj.core.api.Assertions.assertThat;

import com.woowapractice.api.ApiApplication;
import com.woowapractice.problem.application.ProblemCatalog;
import com.woowapractice.problem.domain.Problem;
import com.woowapractice.problem.domain.ProblemStage;
import com.woowapractice.problem.domain.ProblemVersion;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest(classes = ApiApplication.class)
@Transactional
class ProblemCatalogPersistenceAcceptanceTest {

  @Autowired private ProblemCatalog problemCatalog;

  @Test
  @DisplayName("활성 문제를 차수와 표시 순서로 조회한다")
  void findActiveByStage_activeProblemExists_returnsProblems() {
    // given
    Problem problem =
        Problem.create(
            "racing-car",
            "자동차 경주",
            ProblemStage.ROUND_2,
            1,
            "기능 요구사항",
            "https://github.com/example/java-racingcar");
    problem.addVersion(ProblemVersion.create(1, 21, "racing-car/v1", "checksum"));
    problemCatalog.save(problem);

    // when
    List<Problem> result = problemCatalog.findActiveByStage(ProblemStage.ROUND_2);

    // then
    assertThat(result).extracting(Problem::getSlug).containsExactly("racing-car");
  }
}
