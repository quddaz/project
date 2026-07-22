package com.woowapractice.problem;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

import com.woowapractice.api.ApiApplication;
import com.woowapractice.problem.application.ProblemCatalog;
import com.woowapractice.support.DatabaseCleaner;
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
class ProblemAdminApiAcceptanceTest {

  @LocalServerPort private int port;

  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private ProblemCatalog problemCatalog;

  private DatabaseCleaner databaseCleaner;

  @BeforeEach
  void setUp() {
    RestAssured.port = port;
    databaseCleaner = new DatabaseCleaner(jdbcTemplate);
    databaseCleaner.clean();
  }

  @AfterEach
  void tearDown() {
    databaseCleaner.clean();
    RestAssured.reset();
  }

  @Test
  @DisplayName("관리자는 공식 ApplicationTest 소스와 함께 문제를 등록한다")
  void create_validProblemRequest_persistsProblemAndTestSource() {
    String request =
        """
        {
          "slug": "racing-car",
          "title": "자동차 경주",
          "stage": "ROUND_2",
          "displayOrder": 1,
          "description": "자동차 경주 문제",
          "starterRepositoryUrl": "https://github.com/example/racing-car",
          "version": 1,
          "javaVersion": 21,
          "applicationTestSource": "import camp.nextstep.edu.missionutils.test.NsTest; class ApplicationTest extends NsTest { @Override public void runMain() {} }"
        }
        """;

    given()
        .contentType("application/json")
        .body(request)
        .when()
        .post("/api/admin/problems")
        .then()
        .statusCode(201)
        .body("slug", equalTo("racing-car"))
        .body("version", equalTo(1))
        .body("testChecksum", notNullValue());

    given()
        .queryParam("stage", "ROUND_2")
        .when()
        .get("/api/problems")
        .then()
        .statusCode(200)
        .body("problems[0].slug", equalTo("racing-car"));
  }

  @Test
  @DisplayName("관리자는 NsTest 계약이 없는 공식 테스트를 등록할 수 없다")
  void create_invalidTestSource_returnsBadRequest() {
    given()
        .contentType("application/json")
        .body(
            """
            {"slug":"bad","title":"잘못된 문제","stage":"ROUND_1","displayOrder":1,
             "description":"설명","starterRepositoryUrl":"https://github.com/example/bad",
             "version":1,"javaVersion":21,"applicationTestSource":"class ApplicationTest {}"}
            """)
        .when()
        .post("/api/admin/problems")
        .then()
        .statusCode(400)
        .body("code", equalTo("INVALID_PROBLEM_TEST_SOURCE"));
  }

  @Test
  @DisplayName("관리자는 필수 필드가 없는 문제 등록 요청을 보내면 검증 오류를 받는다")
  void create_missingRequiredFields_returnsBadRequest() {
    given()
        .contentType("application/json")
        .body("{}")
        .when()
        .post("/api/admin/problems")
        .then()
        .statusCode(400)
        .body("code", equalTo("INVALID_REQUEST"));
  }
}
