package com.woowapractice.submission;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

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
class SubmissionApiAcceptanceTest {

  @LocalServerPort private int port;

  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private ProblemCatalog problemCatalog;

  private DatabaseCleaner databaseCleaner;

  @BeforeEach
  void setUp() {
    RestAssured.port = port;
    databaseCleaner = new DatabaseCleaner(jdbcTemplate);
    databaseCleaner.clean();
    new ProblemFixture(problemCatalog).save("racing-car", "자동차 경주", "ROUND_2", 1, 1, 21);
  }

  @AfterEach
  void tearDown() {
    databaseCleaner.clean();
    RestAssured.reset();
  }

  @Test
  @DisplayName("GitHub 커밋을 제출하면 대기 중인 채점 작업을 만든다")
  void create_validSubmission_createsQueuedSubmission() {
    given()
        .contentType("application/json")
        .body(
            """
            {"repositoryUrl":"https://github.com/example/racing-car","commitSha":"0123456789012345678901234567890123456789"}
            """)
        .when()
        .post("/api/problems/racing-car/submissions")
        .then()
        .statusCode(202)
        .body("id", notNullValue())
        .body("status", equalTo("QUEUED"))
        .body("commitSha", equalTo("0123456789012345678901234567890123456789"));
  }

  @Test
  @DisplayName("GitHub가 아닌 저장소 주소는 제출할 수 없다")
  void create_nonGithubRepository_returnsBadRequest() {
    given()
        .contentType("application/json")
        .body(
            """
            {"repositoryUrl":"https://example.com/repository","commitSha":"0123456789012345678901234567890123456789"}
            """)
        .when()
        .post("/api/problems/racing-car/submissions")
        .then()
        .statusCode(400)
        .body("code", equalTo("INVALID_REPOSITORY"));
  }

  @Test
  @DisplayName("제출 결과 상세를 조회한다")
  void findById_submissionExists_returnsSubmission() {
    Number id =
        given()
            .contentType("application/json")
            .body(
                """
                {"repositoryUrl":"https://github.com/example/racing-car","commitSha":"0123456789012345678901234567890123456789"}
                """)
            .when()
            .post("/api/problems/racing-car/submissions")
            .then()
            .statusCode(202)
            .extract()
            .path("id");

    given()
        .when()
        .get("/api/submissions/" + id.longValue())
        .then()
        .statusCode(200)
        .body("id", equalTo(id.intValue()))
        .body("status", equalTo("QUEUED"));
  }
}
