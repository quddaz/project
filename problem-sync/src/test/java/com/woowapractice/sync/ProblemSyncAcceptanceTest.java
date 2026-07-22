package com.woowapractice.sync;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.woowapractice.problem.application.ProblemCatalog;
import com.woowapractice.problem.application.ProblemSyncCommand;
import com.woowapractice.problem.application.ProblemSyncService;
import com.woowapractice.problem.domain.Problem;
import com.woowapractice.problem.domain.ProblemErrorCode;
import com.woowapractice.problem.domain.ProblemStage;
import com.woowapractice.problem.domain.ProblemVersionConflictException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import javax.sql.DataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(
    classes = ProblemSyncAcceptanceTest.TestApplication.class,
    properties = {
      "spring.datasource.url=jdbc:h2:mem:problem-sync;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1",
      "spring.datasource.username=sa",
      "spring.datasource.password=",
      "spring.jpa.hibernate.ddl-auto=validate",
      "spring.jpa.open-in-view=false"
    })
class ProblemSyncAcceptanceTest {

  @Autowired private ProblemSyncService syncService;
  @Autowired private ProblemCatalog problemCatalog;
  @Autowired private DataSource dataSource;

  @BeforeEach
  void cleanDatabase() throws SQLException {
    try (Connection connection = dataSource.getConnection();
        Statement statement = connection.createStatement()) {
      statement.executeUpdate("DELETE FROM problem_versions");
      statement.executeUpdate("DELETE FROM problems");
    }
  }

  @Test
  @DisplayName("검증된 문제 목록을 한 트랜잭션으로 동기화한다")
  void synchronize_validCommands_savesProblemAndVersion() {
    // given
    ProblemSyncCommand command = ProblemSyncFixture.racingCarCommand();

    // when
    syncService.synchronize(List.of(command));

    // then
    Problem saved = problemCatalog.findActiveBySlug("racing-car").orElseThrow();
    assertSoftly(
        softly -> {
          softly.assertThat(saved.getTitle()).isEqualTo("자동차 경주");
          softly.assertThat(saved.getStage()).isEqualTo(ProblemStage.ROUND_2);
          softly.assertThat(saved.getDisplayOrder()).isEqualTo(1);
          softly.assertThat(saved.currentVersion().getVersion()).isEqualTo(1);
          softly.assertThat(saved.currentVersion().getConfigChecksum()).isEqualTo("checksum-v1");
        });
  }

  @Test
  @DisplayName("같은 버전과 체크섬을 다시 동기화해도 버전은 중복되지 않는다")
  void synchronize_sameVersionAndChecksum_reusesPublishedVersion() {
    // given
    ProblemSyncCommand command = ProblemSyncFixture.racingCarCommand();
    syncService.synchronize(List.of(command));

    // when
    syncService.synchronize(List.of(command));

    // then
    Problem saved = problemCatalog.findBySlug("racing-car").orElseThrow();
    assertThat(saved.getVersions()).singleElement();
  }

  @Test
  @DisplayName("버전 체크섬 충돌 시 같은 동기화의 앞선 변경도 롤백한다")
  void synchronize_versionChecksumConflicts_rollsBackAllCommands() {
    // given
    syncService.synchronize(List.of(ProblemSyncFixture.racingCarCommand()));
    ProblemSyncCommand newProblem = ProblemSyncFixture.lottoCommand();
    ProblemSyncCommand conflictingProblem =
        ProblemSyncFixture.racingCarCommand("변경된 자동차 경주", "different-checksum");

    // when
    Runnable synchronize = () -> syncService.synchronize(List.of(newProblem, conflictingProblem));

    // then
    assertThatThrownBy(synchronize::run)
        .isInstanceOfSatisfying(
            ProblemVersionConflictException.class,
            exception ->
                assertThat(exception.getErrorCode())
                    .isEqualTo(ProblemErrorCode.PROBLEM_VERSION_CONFLICT));
    Problem saved = problemCatalog.findBySlug("racing-car").orElseThrow();
    assertSoftly(
        softly -> {
          softly.assertThat(problemCatalog.findBySlug("lotto")).isEmpty();
          softly.assertThat(saved.getTitle()).isEqualTo("자동차 경주");
          softly.assertThat(saved.getVersions()).singleElement();
          softly.assertThat(saved.currentVersion().getConfigChecksum()).isEqualTo("checksum-v1");
        });
  }

  @SpringBootConfiguration
  @AutoConfigurationPackage(basePackages = "com.woowapractice")
  @EnableAutoConfiguration
  @ComponentScan(
      basePackages = {
        "com.woowapractice.problem.application",
        "com.woowapractice.problem.infrastructure"
      })
  static class TestApplication {}

  private static class ProblemSyncFixture {

    private static ProblemSyncCommand racingCarCommand() {
      return racingCarCommand("자동차 경주", "checksum-v1");
    }

    private static ProblemSyncCommand racingCarCommand(String title, String checksum) {
      return new ProblemSyncCommand(
          "racing-car",
          title,
          ProblemStage.ROUND_2,
          1,
          "# 자동차 경주",
          "https://github.com/example/java-racingcar",
          1,
          21,
          "racing-car/v1",
          checksum);
    }

    private static ProblemSyncCommand lottoCommand() {
      return new ProblemSyncCommand(
          "lotto",
          "로또",
          ProblemStage.ROUND_2,
          2,
          "# 로또",
          "https://github.com/example/java-lotto",
          1,
          21,
          "lotto/v1",
          "lotto-checksum-v1");
    }
  }
}
