package com.woowapractice.problem.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "problems")
public class Problem {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, unique = true, length = 100)
  private String slug;

  @Column(nullable = false, length = 200)
  private String title;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private ProblemStage stage;

  @Column(name = "display_order", nullable = false)
  private int displayOrder;

  @Column(name = "description_md", nullable = false, columnDefinition = "TEXT")
  private String descriptionMarkdown;

  @Column(name = "starter_repo_url", nullable = false, length = 500)
  private String starterRepositoryUrl;

  @Column(nullable = false)
  private boolean active;

  @OneToMany(mappedBy = "problem", cascade = CascadeType.ALL, orphanRemoval = true)
  @OrderBy("version ASC")
  private List<ProblemVersion> versions = new ArrayList<>();

  private Problem(
      String slug,
      String title,
      ProblemStage stage,
      int displayOrder,
      String descriptionMarkdown,
      String starterRepositoryUrl) {
    this.slug = slug;
    this.title = title;
    this.stage = stage;
    this.displayOrder = displayOrder;
    this.descriptionMarkdown = descriptionMarkdown;
    this.starterRepositoryUrl = starterRepositoryUrl;
    this.active = true;
  }

  public static Problem create(
      String slug,
      String title,
      ProblemStage stage,
      int displayOrder,
      String descriptionMarkdown,
      String starterRepositoryUrl) {
    return new Problem(slug, title, stage, displayOrder, descriptionMarkdown, starterRepositoryUrl);
  }

  public void addVersion(ProblemVersion version) {
    if (version.isAssigned()) {
      throw new ProblemDomainException(ProblemErrorCode.PROBLEM_VERSION_ALREADY_ASSOCIATED);
    }
    version.assignTo(this);
    versions.add(version);
  }

  public ProblemVersion currentVersion() {
    return versions.stream().max(Comparator.comparingInt(ProblemVersion::getVersion)).orElseThrow();
  }

  public List<ProblemVersion> getVersions() {
    return Collections.unmodifiableList(versions);
  }
}
