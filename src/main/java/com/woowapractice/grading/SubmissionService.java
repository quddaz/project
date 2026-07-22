package com.woowapractice.grading;

import com.woowapractice.problem.application.ProblemCatalog;
import com.woowapractice.problem.application.ProblemNotFoundException;
import com.woowapractice.problem.domain.ProblemVersion;
import com.woowapractice.user.User;
import com.woowapractice.user.UserRepository;
import java.util.List;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.core.user.OAuth2User;
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
  private final GitHubRepositoryClient gitHubRepositoryClient;
  private final UserRepository userRepository;

  @Transactional
  public Submission create(String slug, String repositoryUrl, String commitSha) {
    validateRepository(repositoryUrl);
    String resolvedCommitSha = resolveCommitSha(repositoryUrl, commitSha);
    ProblemVersion version =
        problemCatalog
            .findActiveBySlug(slug)
            .map(problem -> problem.currentVersion())
            .orElseThrow(() -> new ProblemNotFoundException(slug));
    Submission submission =
        submissionRepository.save(
            Submission.create(currentUser(), version, repositoryUrl, resolvedCommitSha));
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

  private void validateRepository(String repositoryUrl) {
    if (!GITHUB_REPOSITORY.matcher(repositoryUrl).matches()) {
      throw new InvalidRepositoryException();
    }
  }

  private String resolveCommitSha(String repositoryUrl, String commitSha) {
    if (commitSha == null || commitSha.isBlank()) {
      return gitHubRepositoryClient.resolveDefaultBranchCommit(repositoryUrl);
    }
    if (!COMMIT_SHA.matcher(commitSha).matches()) {
      throw new InvalidRepositoryException();
    }
    return commitSha;
  }

  private User currentUser() {
    Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
    if (authentication == null
        || !(authentication.getPrincipal() instanceof OAuth2User principal)) {
      return null;
    }
    String githubId = String.valueOf(principal.getAttribute("id"));
    String login = String.valueOf(principal.getAttribute("login"));
    String name = principal.getAttribute("name");
    return userRepository
        .findByGithubId(githubId)
        .map(
            user -> {
              user.updateProfile(login, name);
              return user;
            })
        .orElseGet(() -> userRepository.save(User.create(githubId, login, name)));
  }
}
