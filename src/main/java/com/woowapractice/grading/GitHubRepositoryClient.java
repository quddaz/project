package com.woowapractice.grading;

public interface GitHubRepositoryClient {

  String resolveDefaultBranchCommit(String repositoryUrl);

  GitHubRepositoryMetadata getRepository(String repositoryUrl);
}
