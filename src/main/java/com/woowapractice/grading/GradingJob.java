package com.woowapractice.grading;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "grading_jobs")
public class GradingJob {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "submission_id", nullable = false, unique = true)
  private Submission submission;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private GradingJobStatus status;

  @Column(name = "available_at", nullable = false)
  private Instant availableAt;

  @Column(name = "claimed_at")
  private Instant claimedAt;

  @Column(name = "worker_id", length = 100)
  private String workerId;

  @Column(nullable = false)
  private int attempt;

  @Column(name = "last_error", length = 1000)
  private String lastError;

  private GradingJob(Submission submission) {
    this.submission = submission;
    this.status = GradingJobStatus.QUEUED;
    this.availableAt = Instant.now();
  }

  public static GradingJob create(Submission submission) {
    return new GradingJob(submission);
  }
}
