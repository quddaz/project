package com.woowapractice.sync;

import com.woowapractice.problem.application.ProblemSyncCommand;
import com.woowapractice.problem.application.ProblemSyncService;
import java.nio.file.Path;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.AutoConfigurationPackage;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@Slf4j
@RequiredArgsConstructor
@AutoConfigurationPackage(basePackages = "com.woowapractice")
@SpringBootApplication(scanBasePackages = "com.woowapractice")
public class ProblemSyncApplication implements ApplicationRunner {

  private final ProblemDefinitionLoader definitionLoader;
  private final ProblemSyncService syncService;

  public static void main(String[] args) {
    createSpringApplication().run(args);
  }

  @Override
  public void run(ApplicationArguments arguments) {
    List<String> definitionRoots = arguments.getNonOptionArgs();
    if (definitionRoots.size() != 1) {
      throw new InvalidProblemSyncArgumentsException();
    }
    List<ProblemSyncCommand> commands = definitionLoader.load(Path.of(definitionRoots.getFirst()));
    syncService.synchronize(commands);
    log.info("Synchronized {} problem definitions", commands.size());
  }

  static SpringApplication createSpringApplication() {
    SpringApplication application = new SpringApplication(ProblemSyncApplication.class);
    application.setWebApplicationType(WebApplicationType.NONE);
    return application;
  }
}
