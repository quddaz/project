package com.woowapractice.grading;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
public class SubmissionController {

  private final SubmissionService submissionService;

  @Operation(summary = "GitHub 커밋 제출 및 채점 예약")
  @PostMapping("/api/problems/{slug}/submissions")
  @ResponseStatus(HttpStatus.ACCEPTED)
  public SubmissionResponse.Detail create(
      @PathVariable String slug, @Valid @RequestBody SubmissionRequest request) {
    Submission submission =
        submissionService.create(slug, request.repositoryUrl(), request.commitSha());
    return SubmissionResponse.Detail.from(submission, List.of());
  }

  @Operation(summary = "제출 채점 결과 조회")
  @GetMapping("/api/submissions/{id}")
  public SubmissionResponse.Detail find(@PathVariable Long id) {
    Submission submission = submissionService.find(id);
    return SubmissionResponse.Detail.from(submission, submissionService.findResults(id));
  }
}
