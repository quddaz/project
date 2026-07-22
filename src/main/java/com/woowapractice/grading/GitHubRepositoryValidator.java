package com.woowapractice.grading;

import java.net.URI;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class GitHubRepositoryValidator {

  private final GitHubRepositoryClient gitHubRepositoryClient;

  public void validate(String repositoryUrl, String starterRepositoryUrl, String githubLogin) {
    GitHubRepositoryMetadata repository = gitHubRepositoryClient.getRepository(repositoryUrl);
    if (repository.isPrivate()
        || (githubLogin != null && !githubLogin.equals(repository.owner()))) {
      throw new InvalidRepositoryException();
    }
    if (!repository.fork() || !sameRepository(repository.parentFullName(), starterRepositoryUrl)) {
      throw new InvalidRepositoryException();
    }
  }

  private boolean sameRepository(String parentFullName, String starterRepositoryUrl) {
    if (parentFullName == null) {
      return false;
    }
    URI uri = URI.create(starterRepositoryUrl);
    String path = uri.getPath().replaceFirst("^/", "").replaceFirst("/+$", "");
    if (path.toLowerCase(Locale.ROOT).endsWith(".git")) {
      path = path.substring(0, path.length() - 4);
    }
    return parentFullName.equalsIgnoreCase(path);
  }
}
