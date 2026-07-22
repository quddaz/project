package com.woowapractice.feedback;

import com.woowapractice.grading.Submission;
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
@Table(name = "submission_feedback")
public class SubmissionFeedback {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @OneToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "submission_id", nullable = false, unique = true)
  private Submission submission;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private FeedbackStatus status;

  @Column(nullable = false, length = 50)
  private String provider;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String summary;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String strengths;

  @Column(nullable = false, columnDefinition = "TEXT")
  private String improvements;

  @Column(name = "generated_at")
  private Instant generatedAt;

  @Column(name = "error_message", length = 1000)
  private String errorMessage;

  private SubmissionFeedback(Submission submission, FeedbackDraft draft) {
    this.submission = submission;
    this.status = FeedbackStatus.GENERATED;
    this.provider = "rule-based";
    this.summary = draft.summary();
    this.strengths = draft.strengths();
    this.improvements = draft.improvements();
    this.generatedAt = Instant.now();
  }

  public static SubmissionFeedback generated(Submission submission, FeedbackDraft draft) {
    return new SubmissionFeedback(submission, draft);
  }
}
