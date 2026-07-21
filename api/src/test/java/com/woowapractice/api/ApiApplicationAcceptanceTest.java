package com.woowapractice.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
class ApiApplicationAcceptanceTest {

  @Autowired private ApplicationContext applicationContext;

  @Test
  @DisplayName("애플리케이션 컨텍스트를 실행한다")
  void applicationContext_starts_successfully() {
    // then
    assertThat(applicationContext).isNotNull();
  }
}
