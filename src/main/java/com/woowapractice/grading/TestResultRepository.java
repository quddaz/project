package com.woowapractice.grading;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TestResultRepository extends JpaRepository<TestResult, Long> {

  List<TestResult> findAllBySubmissionIdOrderByIdAsc(Long submissionId);
}
