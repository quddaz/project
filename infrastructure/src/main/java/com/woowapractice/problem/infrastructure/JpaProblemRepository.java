package com.woowapractice.problem.infrastructure;

import com.woowapractice.problem.domain.Problem;
import com.woowapractice.problem.domain.ProblemStage;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

interface JpaProblemRepository extends JpaRepository<Problem, Long> {

  List<Problem> findAllByActiveTrueAndStageOrderByDisplayOrderAscIdAsc(ProblemStage stage);

  Optional<Problem> findBySlugAndActiveTrue(String slug);
}
