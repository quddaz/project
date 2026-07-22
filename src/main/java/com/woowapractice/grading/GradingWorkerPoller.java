package com.woowapractice.grading;

import java.time.Instant;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@EnableScheduling
@Profile("worker")
@RequiredArgsConstructor
@Component
public class GradingWorkerPoller {

  private final GradingJobRepository gradingJobRepository;
  private final GradingWorkerService gradingWorkerService;
  private final String workerId = UUID.randomUUID().toString();

  @Scheduled(fixedDelayString = "${grading.worker.poll-ms:1000}")
  public void poll() {
    GradingJob job = claimNext();
    if (job != null) {
      gradingWorkerService.execute(job.getSubmission());
    }
  }

  @Transactional
  protected GradingJob claimNext() {
    return gradingJobRepository
        .findFirstByStatusAndAvailableAtLessThanEqualOrderByIdAsc(
            GradingJobStatus.QUEUED, Instant.now())
        .map(
            job -> {
              job.claim(workerId);
              return gradingJobRepository.save(job);
            })
        .orElse(null);
  }
}
