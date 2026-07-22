package com.woowapractice.sync;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;

import com.woowapractice.problem.application.ProblemCatalog;
import com.woowapractice.problem.application.ProblemSyncService;
import com.woowapractice.problem.domain.Problem;
import com.woowapractice.problem.domain.ProblemStage;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.springframework.boot.DefaultApplicationArguments;
import org.springframework.boot.WebApplicationType;

class ProblemSyncApplicationTest {

  @TempDir Path tempDirectory;

  @Test
  @DisplayName("문제 동기화 애플리케이션은 웹 서버를 시작하지 않는다")
  void createSpringApplication_always_returnsNonWebApplication() {
    // when
    var application = ProblemSyncApplication.createSpringApplication();

    // then
    assertThat(application.getWebApplicationType()).isEqualTo(WebApplicationType.NONE);
  }

  @Test
  @DisplayName("SpringApplication 실행 중 동기화 실패를 호출자에게 전파한다")
  void run_invalidArguments_propagatesFailureFromSpringApplication() {
    // given
    var application = ProblemSyncApplication.createSpringApplication();

    // when
    Runnable run =
        () ->
            application.run(
                "--spring.datasource.url=jdbc:h2:mem:problem-sync-launcher;MODE=MySQL;DATABASE_TO_LOWER=TRUE",
                "--spring.datasource.username=sa",
                "--spring.datasource.password=",
                "--spring.jpa.hibernate.ddl-auto=validate");

    // then
    assertThatThrownBy(run::run).isInstanceOf(InvalidProblemSyncArgumentsException.class);
  }

  @Test
  @DisplayName("정의 루트 인자 하나로 문제를 불러와 동기화한다")
  void run_singleDefinitionsRoot_synchronizesLoadedCommands() throws IOException {
    // given
    ProblemSyncFixture.writeDefinition(tempDirectory);
    InMemoryProblemCatalog problemCatalog = new InMemoryProblemCatalog();
    ProblemSyncApplication application =
        new ProblemSyncApplication(
            new ProblemDefinitionLoader(), new ProblemSyncService(problemCatalog));
    DefaultApplicationArguments arguments =
        new DefaultApplicationArguments("--debug", tempDirectory.toString());

    // when
    application.run(arguments);

    // then
    Problem synchronizedProblem = problemCatalog.findBySlug("racing-car").orElseThrow();
    assertSoftly(
        softly -> {
          softly.assertThat(synchronizedProblem.getTitle()).isEqualTo("자동차 경주");
          softly.assertThat(synchronizedProblem.currentVersion().getVersion()).isEqualTo(1);
        });
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("invalidArguments")
  @DisplayName("정의 루트 인자가 정확히 하나가 아니면 실행을 거부한다")
  void run_invalidDefinitionsRootCount_throwsInvalidProblemSyncArgumentsException(
      String scenario, String[] arguments) {
    // given
    ProblemSyncApplication application =
        new ProblemSyncApplication(
            new ProblemDefinitionLoader(), new ProblemSyncService(new InMemoryProblemCatalog()));

    // when
    Runnable run = () -> application.run(new DefaultApplicationArguments(arguments));

    // then
    assertThatThrownBy(run::run).isInstanceOf(InvalidProblemSyncArgumentsException.class);
  }

  private static Stream<Arguments> invalidArguments() {
    return Stream.of(
        Arguments.of("인자 없음", new String[] {}),
        Arguments.of("루트 인자 두 개", new String[] {"first", "second"}),
        Arguments.of("옵션 인자만 있음", new String[] {"--debug"}));
  }

  private static class InMemoryProblemCatalog implements ProblemCatalog {

    private final Map<String, Problem> problems = new HashMap<>();

    @Override
    public List<Problem> findActiveByStage(ProblemStage stage) {
      return problems.values().stream()
          .filter(Problem::isActive)
          .filter(problem -> problem.getStage() == stage)
          .sorted(Comparator.comparingInt(Problem::getDisplayOrder))
          .toList();
    }

    @Override
    public Optional<Problem> findActiveBySlug(String slug) {
      return findBySlug(slug).filter(Problem::isActive);
    }

    @Override
    public Optional<Problem> findBySlug(String slug) {
      return Optional.ofNullable(problems.get(slug));
    }

    @Override
    public Problem save(Problem problem) {
      problems.put(problem.getSlug(), problem);
      return problem;
    }
  }

  private static class ProblemSyncFixture {

    private static void writeDefinition(Path root) throws IOException {
      Path problemDirectory = Files.createDirectories(root.resolve("racing-car"));
      Files.writeString(
          problemDirectory.resolve("problem.yaml"),
          """
          slug: racing-car
          title: 자동차 경주
          stage: ROUND_2
          displayOrder: 1
          starterRepositoryUrl: https://github.com/example/java-racingcar
          version: 1
          javaVersion: 21
          testBundleRef: racing-car/v1
          """,
          StandardCharsets.UTF_8);
      Files.writeString(problemDirectory.resolve("README.md"), "# 자동차 경주", StandardCharsets.UTF_8);
    }
  }
}
