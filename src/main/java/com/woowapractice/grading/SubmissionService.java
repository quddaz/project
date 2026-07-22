package com.woowapractice.grading;

import com.woowapractice.problem.application.ProblemCatalog;
import com.woowapractice.problem.application.ProblemNotFoundException;
import com.woowapractice.problem.domain.ProblemVersion;
import java.util.List;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class SubmissionService {

  private static final Pattern COMMIT_SHA = Pattern.compile("[0-9a-fA-F]{40}");
  private static final Pattern GITHUB_REPOSITORY =
      Pattern.compile("https://github\\.com/[^/\\s]+/[^/\\s#?]+/?");

  private final ProblemCatalog problemCatalog;
  private final SubmissionRepository submissionRepository;
  private final GradingJobRepository gradingJobRepository;
  private final TestResultRepository testResultRepository;

  @Transactional
  public Submission create(String slug, String repositoryUrl, String commitSha) {
    validateRepository(repositoryUrl, commitSha);
    ProblemVersion version =
        problemCatalog
            .findActiveBySlug(slug)
            .map(problem -> problem.currentVersion())
            .orElseThrow(() -> new ProblemNotFoundException(slug));
    Submission submission =
        submissionRepository.save(Submission.create(version, repositoryUrl, commitSha));
    gradingJobRepository.save(GradingJob.create(submission));
    return submission;
  }

  public Submission find(Long id) {
    return submissionRepository.findById(id).orElseThrow(() -> new SubmissionNotFoundException(id));
  }

  public List<TestResult> findResults(Long id) {
    find(id);
    return testResultRepository.findAllBySubmissionIdOrderByIdAsc(id);
  }

  private void validateRepository(String repositoryUrl, String commitSha) {
    if (!GITHUB_REPOSITORY.matcher(repositoryUrl).matches()
        || !COMMIT_SHA.matcher(commitSha).matches()) {
      throw new InvalidRepositoryException();
    }
  }
}
