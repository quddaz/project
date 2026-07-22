package com.woowapractice.problem.application;

import com.woowapractice.problem.domain.ProblemStage;

public record ProblemAdminCommand(
    String slug,
    String title,
    ProblemStage stage,
    int displayOrder,
    String description,
    String starterRepositoryUrl,
    int version,
    int javaVersion,
    String applicationTestSource) {}
