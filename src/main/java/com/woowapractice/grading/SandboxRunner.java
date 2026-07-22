package com.woowapractice.grading;

import java.nio.file.Path;

public interface SandboxRunner {

  SandboxExecutionResult run(Path workspace);
}
