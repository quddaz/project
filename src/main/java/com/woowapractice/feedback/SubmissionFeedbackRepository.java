package com.woowapractice.feedback;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SubmissionFeedbackRepository extends JpaRepository<SubmissionFeedback, Long> {

  Optional<SubmissionFeedback> findBySubmissionId(Long submissionId);
}
