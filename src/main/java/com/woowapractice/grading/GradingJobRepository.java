package com.woowapractice.grading;

import java.time.Instant;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GradingJobRepository extends JpaRepository<GradingJob, Long> {

  Optional<GradingJob> findFirstByStatusAndAvailableAtLessThanEqualOrderByIdAsc(
      GradingJobStatus status, Instant now);
}
