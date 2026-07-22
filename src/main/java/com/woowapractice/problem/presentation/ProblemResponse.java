package com.woowapractice.problem.presentation;

import com.woowapractice.problem.application.ProblemDetail;
import com.woowapractice.problem.application.ProblemSummary;
import com.woowapractice.problem.domain.ProblemStage;
import java.util.List;

public final class ProblemResponse {

  private ProblemResponse() {}

  public record ListResponse(List<Summary> problems) {

    public static ListResponse from(List<ProblemSummary> problems) {
      return new ListResponse(problems.stream().map(Summary::from).toList());
    }
  }

  public record Summary(String slug, String title, ProblemStage stage, int displayOrder) {

    public static Summary from(ProblemSummary problem) {
      return new Summary(problem.slug(), problem.title(), problem.stage(), problem.displayOrder());
    }
  }

  public record Detail(
      String slug,
      String title,
      ProblemStage stage,
      int displayOrder,
      String descriptionMarkdown,
      String starterRepositoryUrl,
      int version,
      int javaVersion,
      String testChecksum) {

    public static Detail from(ProblemDetail problem) {
      return new Detail(
          problem.slug(),
          problem.title(),
          problem.stage(),
          problem.displayOrder(),
          problem.descriptionMarkdown(),
          problem.starterRepositoryUrl(),
          problem.version(),
          problem.javaVersion(),
          problem.testChecksum());
    }
  }
}
