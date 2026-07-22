package com.woowapractice.grading;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.stereotype.Component;

@Component
public class OfficialTestInjector {

  private static final String TEST_PATH = "src/test/java/ApplicationTest.java";

  public Path inject(Path workspace, String source) {
    try {
      Path root = workspace.toAbsolutePath().normalize();
      Path testPath = root.resolve(TEST_PATH).normalize();
      if (!testPath.startsWith(root) || Files.isSymbolicLink(root.resolve("src"))) {
        throw new IllegalArgumentException("공식 테스트 주입 경로가 안전하지 않습니다.");
      }
      Files.createDirectories(testPath.getParent());
      if (Files.isSymbolicLink(testPath)) {
        throw new IllegalArgumentException("공식 테스트 대상이 심볼릭 링크입니다.");
      }
      Files.writeString(
          testPath,
          source,
          StandardCharsets.UTF_8,
          java.nio.file.StandardOpenOption.CREATE,
          java.nio.file.StandardOpenOption.TRUNCATE_EXISTING);
      return testPath;
    } catch (IOException exception) {
      throw new IllegalStateException("공식 테스트를 주입하지 못했습니다.", exception);
    }
  }
}
