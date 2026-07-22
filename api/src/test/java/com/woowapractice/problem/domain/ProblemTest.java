package com.woowapractice.problem.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class ProblemTest {

  @Test
  @DisplayName("문제 버전 목록은 외부에서 수정할 수 없다")
  void getVersions_mutationAttempted_throwsUnsupportedOperationException() {
    // given
    Problem problem = ProblemFixture.problem("racing-car");
    problem.addVersion(ProblemFixture.version(1));

    // when
    List<ProblemVersion> versions = problem.getVersions();

    // then
    assertThatThrownBy(() -> versions.add(ProblemFixture.version(2)))
        .isInstanceOf(UnsupportedOperationException.class);
    assertThat(problem.getVersions()).hasSize(1);
  }

  @Test
  @DisplayName("이미 다른 문제에 속한 버전은 추가할 수 없다")
  void addVersion_versionAlreadyAssociatedWithAnotherProblem_throwsProblemDomainException() {
    // given
    Problem assignedProblem = ProblemFixture.problem("racing-car");
    Problem targetProblem = ProblemFixture.problem("lotto");
    ProblemVersion version = ProblemFixture.version(1);
    assignedProblem.addVersion(version);

    // when
    Runnable addVersion = () -> targetProblem.addVersion(version);

    // then
    assertThatThrownBy(addVersion::run)
        .isInstanceOfSatisfying(
            ProblemDomainException.class,
            exception ->
                assertThat(exception.getErrorCode())
                    .isEqualTo(ProblemErrorCode.PROBLEM_VERSION_ALREADY_ASSOCIATED));
    assertSoftly(
        softly -> {
          softly.assertThat(assignedProblem.getVersions()).containsExactly(version);
          softly.assertThat(targetProblem.getVersions()).isEmpty();
          softly.assertThat(version.getProblem()).isSameAs(assignedProblem);
        });
  }

  @Test
  @DisplayName("현재 문제 버전은 추가 순서와 관계없이 가장 높은 버전이다")
  void currentVersion_versionsAddedOutOfOrder_returnsGreatestVersion() {
    // given
    Problem problem = ProblemFixture.problem("racing-car");
    problem.addVersion(ProblemFixture.version(2));
    problem.addVersion(ProblemFixture.version(1));

    // when
    ProblemVersion currentVersion = problem.currentVersion();

    // then
    assertThat(currentVersion.getVersion()).isEqualTo(2);
  }

  private static class ProblemFixture {

    private static Problem problem(String slug) {
      return Problem.create(
          slug,
          "자동차 경주",
          ProblemStage.ROUND_2,
          1,
          "기능 요구사항",
          "https://github.com/example/java-racingcar");
    }

    private static ProblemVersion version(int version) {
      return ProblemVersion.create(version, 21, "racing-car/v" + version, "checksum");
    }
  }
}
