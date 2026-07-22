package com.woowapractice.grading;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Files;
import java.nio.file.Path;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class OfficialTestInjectorTest {

  private final OfficialTestInjector injector = new OfficialTestInjector();

  @Test
  @DisplayName("공식 테스트를 고정된 ApplicationTest 경로에 주입한다")
  void inject_source_writesApplicationTest(@TempDir Path workspace) throws Exception {
    Path target = injector.inject(workspace, "class ApplicationTest {}");

    assertThat(target).isEqualTo(workspace.resolve("src/test/java/ApplicationTest.java"));
    assertThat(Files.readString(target)).isEqualTo("class ApplicationTest {}");
  }
}
