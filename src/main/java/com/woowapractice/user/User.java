package com.woowapractice.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "users")
public class User {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "github_id", nullable = false, unique = true, length = 100)
  private String githubId;

  @Column(name = "github_login", nullable = false, length = 100)
  private String githubLogin;

  @Column(name = "display_name", length = 200)
  private String displayName;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private UserRole role;

  @Column(name = "created_at", nullable = false)
  private Instant createdAt;

  @Column(name = "updated_at", nullable = false)
  private Instant updatedAt;

  private User(String githubId, String githubLogin, String displayName) {
    this.githubId = githubId;
    this.githubLogin = githubLogin;
    this.displayName = displayName;
    this.role = UserRole.USER;
    this.createdAt = Instant.now();
    this.updatedAt = this.createdAt;
  }

  public static User create(String githubId, String githubLogin, String displayName) {
    return new User(githubId, githubLogin, displayName);
  }

  public void updateProfile(String githubLogin, String displayName) {
    this.githubLogin = githubLogin;
    this.displayName = displayName;
    this.updatedAt = Instant.now();
  }
}
