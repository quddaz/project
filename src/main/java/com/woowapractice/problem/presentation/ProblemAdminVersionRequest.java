package com.woowapractice.problem.presentation;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

public record ProblemAdminVersionRequest(
    @Min(1) int version, @Min(8) int javaVersion, @NotBlank String applicationTestSource) {}
