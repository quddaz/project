package com.woowapractice.api;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Import(CommonApiErrorAcceptanceTest.UnexpectedErrorController.class)
@ActiveProfiles("test")
@SpringBootTest(
    classes = ApiApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class CommonApiErrorAcceptanceTest {

  private static final String INTERNAL_DETAIL = "database password must not be exposed";

  @LocalServerPort private int port;

  @BeforeEach
  void setUp() {
    RestAssured.port = port;
  }

  @AfterEach
  void tearDown() {
    RestAssured.reset();
  }

  @Test
  @DisplayName("존재하지 않는 API는 공통 오류 응답을 반환한다")
  void getApi_routeDoesNotExist_returnsApiNotFoundError() {
    // when
    var response = given().when().get("/api/does-not-exist").then().statusCode(404);

    // then
    response
        .body("code", equalTo("API_NOT_FOUND"))
        .body("message", equalTo("요청한 API를 찾을 수 없습니다."))
        .body("fieldErrors", empty())
        .body("traceId", not(emptyString()));
  }

  @Test
  @DisplayName("지원하지 않는 HTTP 메서드는 공통 오류 응답을 반환한다")
  void createProblem_methodIsNotSupported_returnsMethodNotAllowedError() {
    // when
    var response = given().when().post("/api/problems").then().statusCode(405);

    // then
    response
        .body("code", equalTo("METHOD_NOT_ALLOWED"))
        .body("message", equalTo("지원하지 않는 HTTP 메서드입니다."))
        .body("fieldErrors", empty())
        .body("traceId", not(emptyString()));
  }

  @Test
  @DisplayName("예기치 않은 오류는 내부 정보 없이 공통 오류 응답을 반환한다")
  void getUnexpectedError_unexpectedExceptionOccurs_returnsInternalServerError() {
    // when
    var response = given().when().get("/api/test/unexpected-error").then().statusCode(500);

    // then
    response
        .body("code", equalTo("INTERNAL_SERVER_ERROR"))
        .body("message", equalTo("서버 내부 오류가 발생했습니다."))
        .body("fieldErrors", empty())
        .body("traceId", not(emptyString()))
        .body(not(containsString(INTERNAL_DETAIL)));
  }

  @RestController
  @RequestMapping("/api/test")
  static class UnexpectedErrorController {

    @GetMapping("/unexpected-error")
    void throwUnexpectedError() {
      throw new IllegalStateException(INTERNAL_DETAIL);
    }
  }
}
