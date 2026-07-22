package com.woowapractice.problem.application;

import com.woowapractice.problem.domain.Problem;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
@Service
public class ProblemAdminService {

  private final ProblemCatalog problemCatalog;
  private final ProblemTestSourceValidator testSourceValidator;

  @Transactional
  public ProblemDetail create(ProblemAdminCommand command) {
    testSourceValidator.validate(command.applicationTestSource());
    if (problemCatalog.findBySlug(command.slug()).isPresent()) {
      throw new ProblemAlreadyExistsException();
    }

    Problem problem =
        Problem.create(
            command.slug(),
            command.title(),
            command.stage(),
            command.displayOrder(),
            command.description(),
            command.starterRepositoryUrl());
    String checksum = checksum(command.applicationTestSource());
    problem.publishVersion(
        command.version(),
        command.javaVersion(),
        "db:" + command.slug() + "/v" + command.version(),
        command.applicationTestSource(),
        checksum);
    problemCatalog.save(problem);
    return ProblemDetail.from(problem);
  }

  @Transactional
  public ProblemDetail publishVersion(
      String slug, int version, int javaVersion, String applicationTestSource) {
    testSourceValidator.validate(applicationTestSource);
    Problem problem =
        problemCatalog.findBySlug(slug).orElseThrow(() -> new ProblemNotFoundException(slug));
    String checksum = checksum(applicationTestSource);
    problem.publishVersion(
        version, javaVersion, "db:" + slug + "/v" + version, applicationTestSource, checksum);
    problemCatalog.save(problem);
    return ProblemDetail.from(problem);
  }

  public List<ProblemDetail> findAll() {
    return problemCatalog.findAll().stream().map(ProblemDetail::from).toList();
  }

  private String checksum(String source) {
    try {
      byte[] digest =
          MessageDigest.getInstance("SHA-256").digest(source.getBytes(StandardCharsets.UTF_8));
      StringBuilder checksum = new StringBuilder();
      for (byte value : digest) {
        checksum.append(String.format("%02x", value));
      }
      return checksum.toString();
    } catch (NoSuchAlgorithmException exception) {
      throw new IllegalStateException("SHA-256 is unavailable", exception);
    }
  }
}
