package com.woowapractice.problem.presentation;

import com.woowapractice.problem.application.ProblemAdminCommand;
import com.woowapractice.problem.domain.ProblemStage;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record ProblemAdminRequest(
    @NotBlank @Size(max = 100) String slug,
    @NotBlank @Size(max = 200) String title,
    @NotNull ProblemStage stage,
    @Min(1) int displayOrder,
    @NotBlank String description,
    @NotBlank @Size(max = 500) String starterRepositoryUrl,
    @Min(1) int version,
    @Min(8) int javaVersion,
    @NotBlank String applicationTestSource) {

  public ProblemAdminCommand toCommand() {
    return new ProblemAdminCommand(
        slug,
        title,
        stage,
        displayOrder,
        description,
        starterRepositoryUrl,
        version,
        javaVersion,
        applicationTestSource);
  }
}
