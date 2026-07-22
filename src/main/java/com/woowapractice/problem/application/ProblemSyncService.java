package com.woowapractice.problem.application;

import com.woowapractice.problem.domain.Problem;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ProblemSyncService {

  private final ProblemCatalog problemCatalog;

  @Transactional
  public void synchronize(List<ProblemSyncCommand> commands) {
    commands.forEach(this::upsert);
  }

  private void upsert(ProblemSyncCommand command) {
    Problem problem =
        problemCatalog
            .findBySlug(command.slug())
            .orElseGet(
                () ->
                    Problem.create(
                        command.slug(),
                        command.title(),
                        command.stage(),
                        command.displayOrder(),
                        command.descriptionMarkdown(),
                        command.starterRepositoryUrl()));
    problem.synchronize(
        command.title(),
        command.stage(),
        command.displayOrder(),
        command.descriptionMarkdown(),
        command.starterRepositoryUrl());
    problem.publishVersion(
        command.version(),
        command.javaVersion(),
        command.testBundleRef(),
        command.configChecksum());
    problemCatalog.save(problem);
  }
}
