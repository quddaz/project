package com.woowapractice.grading;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

@RequiredArgsConstructor
@Component
public class HttpGitHubRepositoryClient implements GitHubRepositoryClient {

  private static final Pattern REPOSITORY =
      Pattern.compile("https://github\\.com/([^/]+)/([^/#?]+?)/?");
  private final ObjectMapper objectMapper;
  private final HttpClient httpClient =
      HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

  @Override
  public String resolveDefaultBranchCommit(String repositoryUrl) {
    Matcher matcher = REPOSITORY.matcher(repositoryUrl);
    if (!matcher.matches()) {
      throw new InvalidRepositoryException();
    }
    String repositoryApi =
        "https://api.github.com/repos/" + matcher.group(1) + "/" + matcher.group(2);
    try {
      JsonNode repository = get(repositoryApi);
      String branch = repository.path("default_branch").asText();
      if (branch.isBlank()) {
        throw new GitHubClientException("GitHub 기본 브랜치를 찾을 수 없습니다.");
      }
      return get(repositoryApi + "/commits/" + URI.create(branch).toString()).path("sha").asText();
    } catch (GitHubClientException exception) {
      throw exception;
    } catch (Exception exception) {
      throw new GitHubClientException("GitHub 커밋을 조회하지 못했습니다.", exception);
    }
  }

  private JsonNode get(String url) throws Exception {
    HttpRequest request =
        HttpRequest.newBuilder(URI.create(url))
            .timeout(Duration.ofSeconds(10))
            .header("Accept", "application/vnd.github+json")
            .header("User-Agent", "woowacourse-practice")
            .GET()
            .build();
    HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
    if (response.statusCode() / 100 != 2) {
      throw new GitHubClientException("GitHub 저장소 또는 커밋을 조회할 수 없습니다.");
    }
    return objectMapper.readTree(response.body());
  }
}
