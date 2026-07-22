package com.woowapractice.sync;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.SoftAssertions.assertSoftly;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

import com.woowapractice.problem.application.ProblemSyncCommand;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileSystemException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class ProblemDefinitionLoaderTest {

  private static final String VALID_DEFINITION =
      """
      slug: racing-car
      title: 자동차 경주
      stage: ROUND_2
      displayOrder: 1
      starterRepositoryUrl: https://github.com/example/java-racingcar
      version: 1
      javaVersion: 21
      testBundleRef: racing-car/v1
      """;

  @TempDir Path tempDirectory;

  private final ProblemDefinitionLoader loader = new ProblemDefinitionLoader();

  @Test
  @DisplayName("YAML과 마크다운을 하나의 동기화 명령으로 읽는다")
  void load_validYamlAndMarkdown_returnsSynchronizationCommand() throws IOException {
    // given
    ProblemDefinitionFixture.write(tempDirectory, "racing-car");

    // when
    List<ProblemSyncCommand> commands = loader.load(tempDirectory);

    // then
    assertThat(commands)
        .singleElement()
        .satisfies(
            command ->
                assertSoftly(
                    softly -> {
                      softly.assertThat(command.slug()).isEqualTo("racing-car");
                      softly.assertThat(command.title()).isEqualTo("자동차 경주");
                      softly.assertThat(command.stage().name()).isEqualTo("ROUND_2");
                      softly.assertThat(command.descriptionMarkdown()).isEqualTo("# 자동차 경주");
                      softly.assertThat(command.configChecksum()).matches("[0-9a-f]{64}");
                    }));
  }

  @Test
  @DisplayName("문제 디렉터리와 테스트 파일 경로를 정렬해 결정적인 결과를 만든다")
  void load_unsortedDirectoriesAndTestFiles_returnsDeterministicImmutableCommands()
      throws IOException {
    // given
    ProblemDefinitionFixture.write(
        tempDirectory,
        "lotto",
        VALID_DEFINITION.replace("racing-car", "lotto").replace("자동차 경주", "로또"));
    Path racingCarDirectory = ProblemDefinitionFixture.write(tempDirectory, "racing-car");
    ProblemDefinitionFixture.writeTest(racingCarDirectory, "z-last.txt", "last");
    ProblemDefinitionFixture.writeTest(racingCarDirectory, "a-first.txt", "first");

    // when
    List<ProblemSyncCommand> first = loader.load(tempDirectory);
    Files.delete(racingCarDirectory.resolve("tests/z-last.txt"));
    Files.delete(racingCarDirectory.resolve("tests/a-first.txt"));
    ProblemDefinitionFixture.writeTest(racingCarDirectory, "a-first.txt", "first");
    ProblemDefinitionFixture.writeTest(racingCarDirectory, "z-last.txt", "last");
    List<ProblemSyncCommand> second = loader.load(tempDirectory);

    // then
    assertSoftly(
        softly -> {
          softly
              .assertThat(first)
              .extracting(ProblemSyncCommand::slug)
              .containsExactly("lotto", "racing-car");
          softly.assertThat(second).isEqualTo(first);
          softly
              .assertThatThrownBy(() -> first.add(first.getFirst()))
              .isInstanceOf(UnsupportedOperationException.class);
        });
  }

  @Test
  @DisplayName("테스트 파일의 상대 경로나 내용이 바뀌면 체크섬이 바뀐다")
  void load_testTreeChanges_returnsDifferentChecksum() throws IOException {
    // given
    Path problemDirectory = ProblemDefinitionFixture.write(tempDirectory, "racing-car");
    ProblemDefinitionFixture.writeTest(problemDirectory, "public/input.txt", "before");
    String originalChecksum = loader.load(tempDirectory).getFirst().configChecksum();

    // when
    Files.move(
        problemDirectory.resolve("tests/public/input.txt"),
        problemDirectory.resolve("tests/public/renamed.txt"));
    String pathChangedChecksum = loader.load(tempDirectory).getFirst().configChecksum();
    Files.writeString(
        problemDirectory.resolve("tests/public/renamed.txt"), "after", StandardCharsets.UTF_8);
    String contentChangedChecksum = loader.load(tempDirectory).getFirst().configChecksum();

    // then
    assertSoftly(
        softly -> {
          softly.assertThat(pathChangedChecksum).isNotEqualTo(originalChecksum);
          softly.assertThat(contentChangedChecksum).isNotEqualTo(pathChangedChecksum);
        });
  }

  @Test
  @DisplayName("테스트 경로와 내용의 경계를 구분해 체크섬을 만든다")
  void load_ambiguousTestPathAndPayload_returnsDifferentChecksum() throws IOException {
    // given
    Path firstRoot = Files.createDirectories(tempDirectory.resolve("first"));
    Path firstProblem = ProblemDefinitionFixture.write(firstRoot, "racing-car");
    ProblemDefinitionFixture.writeTest(firstProblem, "a", "b");
    Path secondRoot = Files.createDirectories(tempDirectory.resolve("second"));
    Path secondProblem = ProblemDefinitionFixture.write(secondRoot, "racing-car");
    ProblemDefinitionFixture.writeTest(secondProblem, "ab", "");

    // when
    String firstChecksum = loader.load(firstRoot).getFirst().configChecksum();
    String secondChecksum = loader.load(secondRoot).getFirst().configChecksum();

    // then
    assertThat(firstChecksum).isNotEqualTo(secondChecksum);
  }

  @Test
  @DisplayName("심볼릭 링크인 문제 디렉터리는 경로가 포함된 예외를 던진다")
  void load_symbolicLinkProblemDirectory_throwsInvalidProblemDefinitionException()
      throws IOException {
    // given
    Path definitionsRoot = Files.createDirectories(tempDirectory.resolve("definitions"));
    Path externalProblem = ProblemDefinitionFixture.write(tempDirectory, "external-problem");
    Path symbolicLink = definitionsRoot.resolve("racing-car");
    createSymbolicLinkOrSkip(symbolicLink, externalProblem);

    // when
    Runnable loadDefinitions = () -> loader.load(definitionsRoot);

    // then
    assertThatThrownBy(loadDefinitions::run)
        .isInstanceOf(InvalidProblemDefinitionException.class)
        .hasMessageContaining(symbolicLink.toString());
  }

  @Test
  @DisplayName("심볼릭 링크인 테스트 파일은 경로가 포함된 예외를 던진다")
  void load_symbolicLinkTestFile_throwsInvalidProblemDefinitionException() throws IOException {
    // given
    Path definitionsRoot = Files.createDirectories(tempDirectory.resolve("definitions"));
    Path problemDirectory = ProblemDefinitionFixture.write(definitionsRoot, "racing-car");
    Path externalTest = Files.writeString(tempDirectory.resolve("external-test.txt"), "external");
    Path symbolicLink = problemDirectory.resolve("tests/input.txt");
    createSymbolicLinkOrSkip(symbolicLink, externalTest);

    // when
    Runnable loadDefinitions = () -> loader.load(definitionsRoot);

    // then
    assertThatThrownBy(loadDefinitions::run)
        .isInstanceOf(InvalidProblemDefinitionException.class)
        .hasMessageContaining(symbolicLink.toString());
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("invalidDefinitions")
  @DisplayName("잘못된 문제 정의는 해당 경로가 포함된 예외를 던진다")
  void load_invalidDefinition_throwsInvalidProblemDefinitionException(
      String scenario, Consumer<Path> invalidDefinition, String offendingRelativePath)
      throws IOException {
    // given
    Path problemDirectory = ProblemDefinitionFixture.write(tempDirectory, "racing-car");
    invalidDefinition.accept(problemDirectory);

    // when
    Runnable loadDefinitions = () -> loader.load(tempDirectory);

    // then
    assertThatThrownBy(loadDefinitions::run)
        .isInstanceOf(InvalidProblemDefinitionException.class)
        .hasMessageContaining(problemDirectory.resolve(offendingRelativePath).toString());
  }

  @Test
  @DisplayName("중복된 슬러그는 두 번째 정의 경로가 포함된 예외를 던진다")
  void load_duplicateSlug_throwsInvalidProblemDefinitionException() throws IOException {
    // given
    ProblemDefinitionFixture.write(tempDirectory, "first");
    Path duplicateDirectory = ProblemDefinitionFixture.write(tempDirectory, "second");

    // when
    Runnable loadDefinitions = () -> loader.load(tempDirectory);

    // then
    assertThatThrownBy(loadDefinitions::run)
        .isInstanceOf(InvalidProblemDefinitionException.class)
        .hasMessageContaining(duplicateDirectory.resolve("problem.yaml").toString());
  }

  private static Stream<Arguments> invalidDefinitions() {
    return Stream.of(
        Arguments.of(
            "README 누락",
            definition(directory -> delete(directory.resolve("README.md"))),
            "README.md"),
        Arguments.of(
            "잘못된 차수",
            definition(directory -> replaceYaml(directory, "stage: ROUND_2", "stage: UNKNOWN")),
            "problem.yaml"),
        Arguments.of(
            "0 이하 버전",
            definition(directory -> replaceYaml(directory, "version: 1", "version: 0")),
            "problem.yaml"),
        Arguments.of(
            "0 이하 표시 순서",
            definition(directory -> replaceYaml(directory, "displayOrder: 1", "displayOrder: 0")),
            "problem.yaml"),
        Arguments.of(
            "0 이하 자바 버전",
            definition(directory -> replaceYaml(directory, "javaVersion: 21", "javaVersion: 0")),
            "problem.yaml"),
        Arguments.of(
            "빈 필수 값",
            definition(directory -> replaceYaml(directory, "title: 자동차 경주", "title: ' '")),
            "problem.yaml"),
        Arguments.of(
            "잘못된 README UTF-8",
            definition(directory -> writeMalformedUtf8(directory.resolve("README.md"))),
            "README.md"));
  }

  private static Consumer<Path> definition(ThrowingPathConsumer consumer) {
    return directory -> {
      try {
        consumer.accept(directory);
      } catch (IOException exception) {
        throw new AssertionError(exception);
      }
    };
  }

  private static void replaceYaml(Path directory, String target, String replacement)
      throws IOException {
    Path yaml = directory.resolve("problem.yaml");
    Files.writeString(
        yaml,
        Files.readString(yaml, StandardCharsets.UTF_8).replace(target, replacement),
        StandardCharsets.UTF_8);
  }

  private static void delete(Path path) throws IOException {
    Files.delete(path);
  }

  private static void writeMalformedUtf8(Path path) throws IOException {
    Files.write(path, new byte[] {(byte) 0xC3, 0x28});
  }

  private static void createSymbolicLinkOrSkip(Path link, Path target) throws IOException {
    try {
      Files.createSymbolicLink(link, target);
    } catch (FileSystemException | UnsupportedOperationException | SecurityException exception) {
      assumeTrue(false, "심볼릭 링크를 생성할 수 없음: " + exception.getMessage());
    }
  }

  @FunctionalInterface
  private interface ThrowingPathConsumer {

    void accept(Path path) throws IOException;
  }

  private static class ProblemDefinitionFixture {

    private static Path write(Path root, String directoryName) throws IOException {
      return write(root, directoryName, VALID_DEFINITION);
    }

    private static Path write(Path root, String directoryName, String yaml) throws IOException {
      Path problemDirectory = Files.createDirectories(root.resolve(directoryName));
      Files.writeString(problemDirectory.resolve("problem.yaml"), yaml, StandardCharsets.UTF_8);
      Files.writeString(problemDirectory.resolve("README.md"), "# 자동차 경주", StandardCharsets.UTF_8);
      Files.createDirectories(problemDirectory.resolve("tests"));
      return problemDirectory;
    }

    private static void writeTest(Path problemDirectory, String relativePath, String content)
        throws IOException {
      Path testFile = problemDirectory.resolve("tests").resolve(relativePath);
      Files.createDirectories(testFile.getParent());
      Files.writeString(testFile, content, StandardCharsets.UTF_8);
    }
  }
}
