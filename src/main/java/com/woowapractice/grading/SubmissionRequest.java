package com.woowapractice.grading;

import jakarta.validation.constraints.NotBlank;

public record SubmissionRequest(@NotBlank String repositoryUrl, String commitSha) {}
