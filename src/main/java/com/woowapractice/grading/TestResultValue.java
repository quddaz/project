package com.woowapractice.grading;

public record TestResultValue(
    String testIdentifier,
    String displayName,
    TestResultStatus status,
    Long durationMillis,
    String failureMessage) {}
