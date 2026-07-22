package com.woowapractice.problem.infrastructure;

import com.woowapractice.problem.application.ProblemCatalog;
import com.woowapractice.problem.domain.Problem;
import com.woowapractice.problem.domain.ProblemStage;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class ProblemCatalogAdapter implements ProblemCatalog {

  private final JpaProblemRepository problemRepository;

  @Override
  public List<Problem> findActiveByStage(ProblemStage stage) {
    return problemRepository.findAllByActiveTrueAndStageOrderByDisplayOrderAscIdAsc(stage);
  }

  @Override
  public Optional<Problem> findActiveBySlug(String slug) {
    return problemRepository.findBySlugAndActiveTrue(slug);
  }

  @Override
  public Optional<Problem> findBySlug(String slug) {
    return problemRepository.findBySlug(slug);
  }

  @Override
  public Problem save(Problem problem) {
    return problemRepository.save(problem);
  }
}
