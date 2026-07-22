package com.woowapractice.feedback;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

import com.woowapractice.api.ApiApplication;
import com.woowapractice.grading.GradingResultService;
import com.woowapractice.grading.SubmissionRepository;
import com.woowapractice.grading.TestResultStatus;
import com.woowapractice.grading.TestResultValue;
import com.woowapractice.problem.application.ProblemCatalog;
import com.woowapractice.support.DatabaseCleaner;
import com.woowapractice.support.ProblemFixture;
import io.restassured.RestAssured;
import java.util.List;
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
class FeedbackApiAcceptanceTest {

  @LocalServerPort private int port;
  @Autowired private JdbcTemplate jdbcTemplate;
  @Autowired private ProblemCatalog problemCatalog;
  @Autowired private SubmissionRepository submissionRepository;
  @Autowired private GradingResultService gradingResultService;
  @Autowired private FeedbackService feedbackService;
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
  @DisplayName("제출 결과를 바탕으로 피드백을 생성하고 조회한다")
  void getFeedback_submissionHasResults_returnsFeedback() {
    long submissionId =
        ((Number)
                given()
                    .contentType("application/json")
                    .body(
                        "{\"repositoryUrl\":\"https://github.com/example/racing-car\",\"commitSha\":\""
                            + "0".repeat(40)
                            + "\"}")
                    .post("/api/problems/racing-car/submissions")
                    .then()
                    .statusCode(202)
                    .extract()
                    .path("id"))
            .longValue();
    gradingResultService.record(
        submissionId,
        List.of(
            new TestResultValue("one", "기능_테스트", TestResultStatus.PASSED, 10L, null),
            new TestResultValue("two", "예외_테스트", TestResultStatus.FAILED, 20L, "출력이 다릅니다")));

    feedbackService.generate(submissionId);

    given()
        .get("/api/submissions/" + submissionId + "/feedback")
        .then()
        .statusCode(200)
        .body("status", equalTo("GENERATED"))
        .body("summary", containsString("실패"))
        .body("improvements", containsString("예외_테스트"));
  }
}
