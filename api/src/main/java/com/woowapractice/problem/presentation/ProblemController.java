package com.woowapractice.problem.presentation;

import com.woowapractice.common.presentation.ApiErrorResponse;
import com.woowapractice.problem.application.ProblemQueryService;
import com.woowapractice.problem.domain.ProblemStage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/problems")
public class ProblemController {

  private final ProblemQueryService problemQueryService;

  @Operation(summary = "차수별 활성 문제 목록 조회")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "문제 목록 조회 성공",
        content = @Content(schema = @Schema(implementation = ProblemResponse.ListResponse.class))),
    @ApiResponse(
        responseCode = "400",
        description = "유효하지 않은 차수",
        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
  })
  @GetMapping
  public ProblemResponse.ListResponse findAll(
      @Parameter(description = "프리코스 차수") @RequestParam ProblemStage stage) {
    return ProblemResponse.ListResponse.from(problemQueryService.findAll(stage));
  }

  @Operation(summary = "활성 문제 상세 조회")
  @ApiResponses({
    @ApiResponse(
        responseCode = "200",
        description = "문제 상세 조회 성공",
        content = @Content(schema = @Schema(implementation = ProblemResponse.Detail.class))),
    @ApiResponse(
        responseCode = "404",
        description = "문제를 찾을 수 없음",
        content = @Content(schema = @Schema(implementation = ApiErrorResponse.class)))
  })
  @GetMapping("/{slug}")
  public ProblemResponse.Detail findBySlug(@PathVariable String slug) {
    return ProblemResponse.Detail.from(problemQueryService.findBySlug(slug));
  }
}
