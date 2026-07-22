package com.woowapractice.feedback;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/api/submissions/{submissionId}/feedback")
public class FeedbackController {

  private final FeedbackService feedbackService;

  @PostMapping
  public FeedbackResponse generate(@PathVariable Long submissionId) {
    return FeedbackResponse.from(feedbackService.generate(submissionId));
  }

  @GetMapping
  public FeedbackResponse find(@PathVariable Long submissionId) {
    return FeedbackResponse.from(feedbackService.find(submissionId));
  }
}
