package com.woowapractice.problem.application;

import com.woowapractice.problem.domain.ProblemStage;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ProblemQueryService {

  private final ProblemCatalog problemCatalog;

  public List<ProblemSummary> findAll(ProblemStage stage) {
    return problemCatalog.findActiveByStage(stage).stream().map(ProblemSummary::from).toList();
  }

  public ProblemDetail findBySlug(String slug) {
    return problemCatalog
        .findActiveBySlug(slug)
        .map(ProblemDetail::from)
        .orElseThrow(() -> new ProblemNotFoundException(slug));
  }
}
