package com.woowapractice.problem.application;

import com.woowapractice.problem.domain.Problem;
import com.woowapractice.problem.domain.ProblemStage;

public record ProblemSummary(String slug, String title, ProblemStage stage, int displayOrder) {

  public static ProblemSummary from(Problem problem) {
    return new ProblemSummary(
        problem.getSlug(), problem.getTitle(), problem.getStage(), problem.getDisplayOrder());
  }
}
