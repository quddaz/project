package com.woowapractice.grading;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(
    name = "test_results",
    uniqueConstraints = @UniqueConstraint(columnNames = {"submission_id", "test_identifier"}))
public class TestResult {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "submission_id", nullable = false)
  private Submission submission;

  @Column(name = "test_identifier", nullable = false, length = 300)
  private String testIdentifier;

  @Column(name = "display_name", nullable = false, length = 300)
  private String displayName;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private TestResultStatus status;

  @Column(name = "duration_ms")
  private Long durationMillis;

  @Column(name = "failure_message", length = 2000)
  private String failureMessage;

  private TestResult(Submission submission, TestResultValue value) {
    this.submission = submission;
    this.testIdentifier = value.testIdentifier();
    this.displayName = value.displayName();
    this.status = value.status();
    this.durationMillis = value.durationMillis();
    this.failureMessage = value.failureMessage();
  }

  public static TestResult create(Submission submission, TestResultValue value) {
    return new TestResult(submission, value);
  }
}
