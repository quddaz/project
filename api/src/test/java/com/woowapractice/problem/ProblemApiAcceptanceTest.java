package com.woowapractice.problem;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyString;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.startsWith;

import com.woowapractice.api.ApiApplication;
import com.woowapractice.problem.application.ProblemCatalog;
import com.woowapractice.support.DatabaseCleaner;
import com.woowapractice.support.ProblemFixture;
import io.restassured.RestAssured;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest(
    classes = ApiApplication.class,
    webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ProblemApiAcceptanceTest {

  @LocalServerPort private int port;

  @Autowired private JdbcTemplate jdbcTemplate;

  @Autowired private ProblemCatalog problemCatalog;

  private DatabaseCleaner databaseCleaner;
  private ProblemFixture problemFixture;

  @BeforeEach
  void setUp() {
    RestAssured.port = port;
    databaseCleaner = new DatabaseCleaner(jdbcTemplate);
    problemFixture = new ProblemFixture(problemCatalog);
    databaseCleaner.clean();
  }

  @AfterEach
  void tearDown() {
    databaseCleaner.clean();
  }

  @Test
  @DisplayName("차수별 활성 문제 목록을 조회한다")
  void findAll_activeProblemsExist_returnsProblemsForStage() {
    // given
    problemFixture.save("racing-car", "자동차 경주", "ROUND_2", 1, 2, 21);

    // when
    var response =
        given().queryParam("stage", "ROUND_2").when().get("/api/problems").then().statusCode(200);

    // then
    response
        .body("problems.slug", contains("racing-car"))
        .body("problems[0].stage", equalTo("ROUND_2"));
  }

  @Test
  @DisplayName("문제 상세를 조회한다")
  void findBySlug_activeProblemExists_returnsProblemDetail() {
    // given
    problemFixture.save("racing-car", "자동차 경주", "ROUND_2", 1, 2, 21);

    // when
    var response = given().when().get("/api/problems/racing-car").then().statusCode(200);

    // then
    response
        .body("slug", equalTo("racing-car"))
        .body("title", equalTo("자동차 경주"))
        .body("version", equalTo(2))
        .body("javaVersion", equalTo(21));
  }

  @Test
  @DisplayName("존재하지 않는 문제는 안정적인 오류 코드를 반환한다")
  void findBySlug_problemDoesNotExist_returnsProblemNotFoundError() {
    // when
    var response = given().when().get("/api/problems/not-found").then().statusCode(404);

    // then
    response.body("code", equalTo("PROBLEM_NOT_FOUND")).body("traceId", not(emptyString()));
  }

  @Test
  @DisplayName("잘못된 차수는 안정적인 오류 코드를 반환한다")
  void findAll_stageIsInvalid_returnsInvalidStageError() {
    // when
    var response =
        given().queryParam("stage", "INVALID").when().get("/api/problems").then().statusCode(400);

    // then
    response
        .body("code", equalTo("INVALID_STAGE"))
        .body("fieldErrors[0].field", equalTo("stage"))
        .body("traceId", not(emptyString()));
  }

  @Test
  @DisplayName("공개 API와 오류 응답을 OpenAPI로 제공한다")
  void getOpenApi_publicApiExists_returnsDocument() {
    // when
    var response = given().when().get("/v3/api-docs").then().statusCode(200);

    // then
    response
        .body("openapi", startsWith("3.1"))
        .body("info.title", equalTo("Woowa Practice API"))
        .body("paths.'/api/problems'", notNullValue())
        .body("components.schemas.ApiErrorResponse", notNullValue());
  }

  @Test
  @DisplayName("Swagger UI를 제공한다")
  void getSwaggerUi_swaggerUiIsEnabled_returnsPage() {
    // when
    var response = given().when().get("/swagger-ui/index.html").then().statusCode(200);

    // then
    response.body(not(emptyString()));
  }
}
