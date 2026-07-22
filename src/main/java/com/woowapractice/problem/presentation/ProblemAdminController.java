package com.woowapractice.problem.presentation;

import com.woowapractice.problem.application.ProblemAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/admin/problems")
public class ProblemAdminController {

  private final ProblemAdminService problemAdminService;

  @Operation(summary = "관리자 문제 등록")
  @ApiResponse(responseCode = "201", description = "문제 등록 성공")
  @PostMapping
  @ResponseStatus(HttpStatus.CREATED)
  public ProblemResponse.Detail create(@Valid @RequestBody ProblemAdminRequest request) {
    return ProblemResponse.Detail.from(problemAdminService.create(request.toCommand()));
  }

  @GetMapping
  public List<ProblemResponse.Detail> findAll() {
    return problemAdminService.findAll().stream().map(ProblemResponse.Detail::from).toList();
  }

  @PostMapping("/{slug}/versions")
  public ProblemResponse.Detail publishVersion(
      @PathVariable String slug, @Valid @RequestBody ProblemAdminVersionRequest request) {
    return ProblemResponse.Detail.from(
        problemAdminService.publishVersion(
            slug, request.version(), request.javaVersion(), request.applicationTestSource()));
  }
}
