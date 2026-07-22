package com.woowapractice.grading;

public record SandboxExecutionResult(int exitCode, String output, boolean timedOut) {}
