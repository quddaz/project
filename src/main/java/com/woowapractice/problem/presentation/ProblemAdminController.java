package com.woowapractice.problem.presentation;

import com.woowapractice.problem.application.ProblemAdminService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
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
}
