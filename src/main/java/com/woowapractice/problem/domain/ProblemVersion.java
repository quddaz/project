package com.woowapractice.problem.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "problem_versions")
public class ProblemVersion {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @ManyToOne(fetch = FetchType.LAZY, optional = false)
  @JoinColumn(name = "problem_id", nullable = false)
  private Problem problem;

  @Column(nullable = false)
  private int version;

  @Column(name = "java_version", nullable = false)
  private int javaVersion;

  @Column(name = "test_bundle_ref", nullable = false, length = 300)
  private String testBundleRef;

  @Column(name = "config_checksum", nullable = false, length = 64)
  private String configChecksum;

  @Column(name = "published_at", nullable = false)
  private Instant publishedAt;

  private ProblemVersion(
      int version, int javaVersion, String testBundleRef, String configChecksum) {
    this.version = version;
    this.javaVersion = javaVersion;
    this.testBundleRef = testBundleRef;
    this.configChecksum = configChecksum;
    this.publishedAt = Instant.now();
  }

  public static ProblemVersion create(
      int version, int javaVersion, String testBundleRef, String configChecksum) {
    return new ProblemVersion(version, javaVersion, testBundleRef, configChecksum);
  }

  boolean hasVersion(int version) {
    return this.version == version;
  }

  boolean hasChecksum(String configChecksum) {
    return this.configChecksum.equals(configChecksum);
  }

  void assignTo(Problem problem) {
    this.problem = problem;
  }

  boolean isAssigned() {
    return problem != null;
  }
}
