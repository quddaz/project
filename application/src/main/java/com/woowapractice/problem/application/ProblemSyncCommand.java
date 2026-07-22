package com.woowapractice.problem.application;

import com.woowapractice.problem.domain.ProblemStage;

public record ProblemSyncCommand(
    String slug,
    String title,
    ProblemStage stage,
    int displayOrder,
    String descriptionMarkdown,
    String starterRepositoryUrl,
    int version,
    int javaVersion,
    String testBundleRef,
    String configChecksum) {}
