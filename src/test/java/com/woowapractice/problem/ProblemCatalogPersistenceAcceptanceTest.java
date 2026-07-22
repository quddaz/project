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
  void findActiveByStage_activeProblemsExist_returnsProblemsInDisplayOrder() {
    // given
    problemCatalog.save(ProblemFixture.problem("racing-car", ProblemStage.ROUND_2, 2));
    problemCatalog.save(ProblemFixture.problem("lotto", ProblemStage.ROUND_2, 1));
    problemCatalog.save(ProblemFixture.problem("baseball", ProblemStage.ROUND_1, 1));

    // when
    List<Problem> result = problemCatalog.findActiveByStage(ProblemStage.ROUND_2);

    // then
    assertThat(result).extracting(Problem::getSlug).containsExactly("lotto", "racing-car");
  }

  private static class ProblemFixture {

    private static Problem problem(String slug, ProblemStage stage, int displayOrder) {
      Problem problem =
          Problem.create(
              slug, "문제 제목", stage, displayOrder, "기능 요구사항", "https://github.com/example/" + slug);
      problem.addVersion(ProblemVersion.create(1, 21, slug + "/v1", "checksum"));
      return problem;
    }
  }
}
