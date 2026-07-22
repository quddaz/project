package com.woowapractice.support;

import com.woowapractice.problem.application.ProblemCatalog;
import com.woowapractice.problem.domain.Problem;
import com.woowapractice.problem.domain.ProblemStage;
import com.woowapractice.problem.domain.ProblemVersion;

public class ProblemFixture {

  private final ProblemCatalog problemCatalog;

  public ProblemFixture(ProblemCatalog problemCatalog) {
    this.problemCatalog = problemCatalog;
  }

  public void save(
      String slug, String title, String stage, int displayOrder, int version, int javaVersion) {
    Problem problem =
        Problem.create(
            slug,
            title,
            ProblemStage.valueOf(stage),
            displayOrder,
            "기능 요구사항",
            "https://github.com/example/" + slug);
    problem.addVersion(
        ProblemVersion.create(version, javaVersion, slug + "/v" + version, "checksum-" + version));
    problemCatalog.save(problem);
  }
}
