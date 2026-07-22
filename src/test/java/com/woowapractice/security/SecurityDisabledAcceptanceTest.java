package com.woowapractice.security;

import static io.restassured.RestAssured.given;
import static org.assertj.core.api.Assertions.assertThat;

import com.woowapractice.api.ApiApplication;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.ApplicationContext;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(
    classes = ApiApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class SecurityDisabledAcceptanceTest {

  @LocalServerPort private int port;
  @Autowired private ApplicationContext applicationContext;

  @BeforeEach
  void setUp() {
    RestAssured.port = port;
  }

  @AfterEach
  void tearDown() {
    RestAssured.reset();
  }

  @Test
  @DisplayName("보안 비활성화 환경은 인증 없이 보호된 API 요청을 허용한다")
  void securityDisabled_allowsUnauthenticatedPostToProtectedApi() {
    // then
    assertThat(applicationContext.getBean("securityDisabledFilterChain", SecurityFilterChain.class))
        .isNotNull();

    Response response =
        given().contentType("application/json").body("{}").when().post("/api/admin/problems");

    assertThat(response.statusCode()).isNotIn(401, 403);
  }
}
