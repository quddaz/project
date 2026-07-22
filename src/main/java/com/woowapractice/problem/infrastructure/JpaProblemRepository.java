package com.woowapractice.problem.infrastructure;

import com.woowapractice.problem.domain.Problem;
import com.woowapractice.problem.domain.ProblemStage;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

interface JpaProblemRepository extends JpaRepository<Problem, Long> {

  List<Problem> findAllByOrderByIdAsc();

  List<Problem> findAllByActiveTrueAndStageOrderByDisplayOrderAscIdAsc(ProblemStage stage);

  @EntityGraph(attributePaths = "versions")
  Optional<Problem> findBySlugAndActiveTrue(String slug);

  @EntityGraph(attributePaths = "versions")
  Optional<Problem> findBySlug(String slug);
}
