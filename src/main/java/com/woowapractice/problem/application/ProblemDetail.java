package com.woowapractice.problem.application;

import com.woowapractice.problem.domain.Problem;
import com.woowapractice.problem.domain.ProblemStage;
import com.woowapractice.problem.domain.ProblemVersion;

public record ProblemDetail(
    String slug,
    String title,
    ProblemStage stage,
    int displayOrder,
    String descriptionMarkdown,
    String starterRepositoryUrl,
    int version,
    int javaVersion,
    String testChecksum) {

  public static ProblemDetail from(Problem problem) {
    ProblemVersion currentVersion = problem.currentVersion();
    return new ProblemDetail(
        problem.getSlug(),
        problem.getTitle(),
        problem.getStage(),
        problem.getDisplayOrder(),
        problem.getDescriptionMarkdown(),
        problem.getStarterRepositoryUrl(),
        currentVersion.getVersion(),
        currentVersion.getJavaVersion(),
        currentVersion.getConfigChecksum());
  }
}
