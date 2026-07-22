package com.woowapractice.grading;

import com.woowapractice.problem.domain.ProblemVersion;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "submissions")
public class Submission {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "problem_version_id", nullable = false)
  private ProblemVersion problemVersion;

  @Column(name = "repository_url", nullable = false, length = 500)
  private String repositoryUrl;

  @Column(name = "commit_sha", nullable = false, length = 40)
  private String commitSha;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private SubmissionStatus status;

  @Column(name = "submitted_at", nullable = false)
  private Instant submittedAt;

  @Column(name = "completed_at")
  private Instant completedAt;

  @Column(name = "error_message", length = 1000)
  private String errorMessage;

  private Submission(ProblemVersion problemVersion, String repositoryUrl, String commitSha) {
    this.problemVersion = problemVersion;
    this.repositoryUrl = repositoryUrl;
    this.commitSha = commitSha;
    this.status = SubmissionStatus.QUEUED;
    this.submittedAt = Instant.now();
  }

  public static Submission create(
      ProblemVersion problemVersion, String repositoryUrl, String commitSha) {
    return new Submission(problemVersion, repositoryUrl, commitSha);
  }

  public void start() {
    this.status = SubmissionStatus.RUNNING;
  }

  public void complete(SubmissionStatus status, String errorMessage) {
    this.status = status;
    this.errorMessage = errorMessage;
    this.completedAt = Instant.now();
  }
}
