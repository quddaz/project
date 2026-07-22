package com.woowapractice.grading;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {

  Optional<Submission> findByIdAndProblemVersionProblemSlug(Long id, String slug);
}
