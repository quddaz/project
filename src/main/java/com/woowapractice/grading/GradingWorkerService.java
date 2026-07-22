package com.woowapractice.grading;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class GradingWorkerService {

  private final OfficialTestInjector officialTestInjector;
  private final SandboxRunner sandboxRunner;
  private final JunitXmlResultParser resultParser;
  private final GradingResultService gradingResultService;

  public void execute(Submission submission) {
    Path workspace = null;
    try {
      workspace = Files.createTempDirectory("woowapractice-grading-");
      runCommand(
          "git",
          "clone",
          "--depth",
          "1",
          "--no-checkout",
          submission.getRepositoryUrl(),
          workspace.toString());
      runCommand("git", "-C", workspace.toString(), "checkout", submission.getCommitSha());
      officialTestInjector.inject(
          workspace, submission.getProblemVersion().getApplicationTestSource());
      SandboxExecutionResult execution = sandboxRunner.run(workspace);
      if (execution.timedOut()) {
        gradingResultService.recordPlatformError(submission.getId(), "채점 시간이 초과되었습니다.");
        return;
      }
      Path report = findJUnitReport(workspace);
      if (report == null) {
        gradingResultService.recordPlatformError(submission.getId(), "JUnit 테스트 결과 파일을 찾을 수 없습니다.");
        return;
      }
      gradingResultService.record(
          submission.getId(), resultParser.parse(Files.newInputStream(report)));
    } catch (Exception exception) {
      gradingResultService.recordPlatformError(submission.getId(), "채점 실행 중 오류가 발생했습니다.");
    } finally {
      deleteWorkspace(workspace);
    }
  }

  private void runCommand(String... command) throws IOException, InterruptedException {
    Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
    if (process.waitFor() != 0) {
      throw new IllegalStateException("저장소 작업을 완료하지 못했습니다.");
    }
  }

  private Path findJUnitReport(Path workspace) throws IOException {
    try (var paths = Files.walk(workspace.resolve("build/test-results/test"))) {
      return paths
          .filter(Files::isRegularFile)
          .filter(path -> path.getFileName().toString().startsWith("TEST-"))
          .findFirst()
          .orElse(null);
    } catch (IOException exception) {
      return null;
    }
  }

  private void deleteWorkspace(Path workspace) {
    if (workspace == null) {
      return;
    }
    try (var paths = Files.walk(workspace)) {
      paths.sorted(Comparator.reverseOrder()).forEach(this::deleteIfExists);
    } catch (IOException ignored) {
      // 작업 디렉터리 정리 실패는 채점 결과를 덮어쓰지 않는다.
    }
  }

  private void deleteIfExists(Path path) {
    try {
      Files.deleteIfExists(path);
    } catch (IOException ignored) {
      // best effort cleanup
    }
  }
}
