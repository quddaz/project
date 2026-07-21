package com.woowapractice.problem.application;

import com.woowapractice.problem.domain.Problem;
import com.woowapractice.problem.domain.ProblemStage;
import java.util.List;
import java.util.Optional;

public interface ProblemCatalog {

  List<Problem> findActiveByStage(ProblemStage stage);

  Optional<Problem> findActiveBySlug(String slug);

  Problem save(Problem problem);
}
