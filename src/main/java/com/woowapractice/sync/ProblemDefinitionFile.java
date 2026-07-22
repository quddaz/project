package com.woowapractice.sync;

record ProblemDefinitionFile(
    String slug,
    String title,
    String stage,
    int displayOrder,
    String starterRepositoryUrl,
    int version,
    int javaVersion,
    String testBundleRef) {}
