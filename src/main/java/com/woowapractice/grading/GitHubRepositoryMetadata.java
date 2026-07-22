package com.woowapractice.grading;

public record GitHubRepositoryMetadata(
    String owner, String name, boolean isPrivate, boolean fork, String parentFullName) {}
