package com.woowapractice.grading;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class DockerSandboxRunner implements SandboxRunner {

  private static final int OUTPUT_LIMIT = 64_000;
  private final String image;
  private final Duration timeout;

  public DockerSandboxRunner(
      @Value("${grading.docker.image:woowapractice/grader:java21}") String image,
      @Value("${grading.docker.timeout-seconds:120}") long timeoutSeconds) {
    this.image = image;
    this.timeout = Duration.ofSeconds(timeoutSeconds);
  }

  @Override
  public SandboxExecutionResult run(Path workspace) {
    List<String> command =
        List.of(
            "docker",
            "run",
            "--rm",
            "--network",
            "none",
            "--cpus",
            "1",
            "--memory",
            "1g",
            "--pids-limit",
            "128",
            "-v",
            workspace.toAbsolutePath() + ":/workspace:rw",
            "-w",
            "/workspace",
            image,
            "./gradlew",
            "test",
            "--tests",
            "ApplicationTest");
    try {
      Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
      boolean completed = process.waitFor(timeout.toSeconds(), TimeUnit.SECONDS);
      if (!completed) {
        process.destroyForcibly();
        return new SandboxExecutionResult(124, readOutput(process), true);
      }
      return new SandboxExecutionResult(process.exitValue(), readOutput(process), false);
    } catch (IOException exception) {
      throw new IllegalStateException("Docker 샌드박스를 실행하지 못했습니다.", exception);
    } catch (InterruptedException exception) {
      Thread.currentThread().interrupt();
      throw new IllegalStateException("Docker 샌드박스를 실행하지 못했습니다.", exception);
    }
  }

  private String readOutput(Process process) throws IOException {
    String output = new String(process.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    return output.length() <= OUTPUT_LIMIT ? output : output.substring(0, OUTPUT_LIMIT);
  }
}
