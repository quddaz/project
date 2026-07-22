package com.woowapractice.sync;

import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.woowapractice.problem.application.ProblemSyncCommand;
import com.woowapractice.problem.domain.ProblemStage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.HexFormat;
import java.util.List;
import java.util.Set;
import org.springframework.stereotype.Component;

@Component
public class ProblemDefinitionLoader {

  private static final String YAML_FILE_NAME = "problem.yaml";
  private static final String README_FILE_NAME = "README.md";
  private static final String TESTS_DIRECTORY_NAME = "tests";
  private static final String SHA_256 = "SHA-256";

  private final ObjectMapper objectMapper;

  public ProblemDefinitionLoader() {
    YAMLFactory yamlFactory =
        YAMLFactory.builder().enable(StreamReadFeature.STRICT_DUPLICATE_DETECTION).build();
    this.objectMapper =
        new ObjectMapper(yamlFactory)
            .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .enable(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES);
  }

  public List<ProblemSyncCommand> load(Path root) {
    List<Path> problemDirectories = findProblemDirectories(root);
    List<ProblemSyncCommand> commands = new ArrayList<>();
    Set<String> slugs = new HashSet<>();
    for (Path problemDirectory : problemDirectories) {
      ProblemSyncCommand command = loadDefinition(problemDirectory);
      if (!slugs.add(command.slug())) {
        throw new InvalidProblemDefinitionException(problemDirectory.resolve(YAML_FILE_NAME));
      }
      commands.add(command);
    }
    return List.copyOf(commands);
  }

  private List<Path> findProblemDirectories(Path root) {
    try (var children = Files.list(root)) {
      return children
          .filter(Files::isDirectory)
          .sorted(Comparator.comparing(path -> path.getFileName().toString()))
          .toList();
    } catch (IOException exception) {
      throw new InvalidProblemDefinitionException(root, exception);
    }
  }

  private ProblemSyncCommand loadDefinition(Path problemDirectory) {
    Path yamlPath = problemDirectory.resolve(YAML_FILE_NAME);
    Path readmePath = problemDirectory.resolve(README_FILE_NAME);
    byte[] yamlBytes = readRequiredFile(yamlPath);
    byte[] readmeBytes = readRequiredFile(readmePath);
    ProblemDefinitionFile definition = deserialize(yamlPath, yamlBytes);
    ProblemStage stage = validate(yamlPath, definition);
    String descriptionMarkdown = decodeUtf8(readmePath, readmeBytes);
    requireText(readmePath, descriptionMarkdown);
    return new ProblemSyncCommand(
        definition.slug(),
        definition.title(),
        stage,
        definition.displayOrder(),
        descriptionMarkdown,
        definition.starterRepositoryUrl(),
        definition.version(),
        definition.javaVersion(),
        definition.testBundleRef(),
        calculateChecksum(problemDirectory, yamlBytes, readmeBytes));
  }

  private byte[] readRequiredFile(Path path) {
    try {
      if (!Files.isRegularFile(path)) {
        throw new InvalidProblemDefinitionException(path);
      }
      return Files.readAllBytes(path);
    } catch (IOException exception) {
      throw new InvalidProblemDefinitionException(path, exception);
    }
  }

  private ProblemDefinitionFile deserialize(Path yamlPath, byte[] yamlBytes) {
    try {
      return objectMapper.readValue(yamlBytes, ProblemDefinitionFile.class);
    } catch (IOException exception) {
      throw new InvalidProblemDefinitionException(yamlPath, exception);
    }
  }

  private String decodeUtf8(Path path, byte[] bytes) {
    try {
      return StandardCharsets.UTF_8.newDecoder().decode(ByteBuffer.wrap(bytes)).toString();
    } catch (CharacterCodingException exception) {
      throw new InvalidProblemDefinitionException(path, exception);
    }
  }

  private ProblemStage validate(Path yamlPath, ProblemDefinitionFile definition) {
    if (definition == null) {
      throw new InvalidProblemDefinitionException(yamlPath);
    }
    requireText(yamlPath, definition.slug());
    requireText(yamlPath, definition.title());
    requireText(yamlPath, definition.stage());
    requireText(yamlPath, definition.starterRepositoryUrl());
    requireText(yamlPath, definition.testBundleRef());
    requirePositive(yamlPath, definition.displayOrder());
    requirePositive(yamlPath, definition.version());
    requirePositive(yamlPath, definition.javaVersion());
    try {
      return ProblemStage.valueOf(definition.stage());
    } catch (IllegalArgumentException exception) {
      throw new InvalidProblemDefinitionException(yamlPath, exception);
    }
  }

  private void requireText(Path path, String value) {
    if (value == null || value.isBlank()) {
      throw new InvalidProblemDefinitionException(path);
    }
  }

  private void requirePositive(Path path, int value) {
    if (value <= 0) {
      throw new InvalidProblemDefinitionException(path);
    }
  }

  private String calculateChecksum(Path problemDirectory, byte[] yamlBytes, byte[] readmeBytes) {
    MessageDigest digest = createDigest(problemDirectory);
    digest.update(yamlBytes);
    digest.update(readmeBytes);
    for (Path testFile : findTestFiles(problemDirectory)) {
      Path relativePath = problemDirectory.relativize(testFile);
      digest.update(relativePath.toString().replace('\\', '/').getBytes(StandardCharsets.UTF_8));
      digest.update(readRequiredFile(testFile));
    }
    return HexFormat.of().formatHex(digest.digest());
  }

  private MessageDigest createDigest(Path problemDirectory) {
    try {
      return MessageDigest.getInstance(SHA_256);
    } catch (NoSuchAlgorithmException exception) {
      throw new InvalidProblemDefinitionException(problemDirectory, exception);
    }
  }

  private List<Path> findTestFiles(Path problemDirectory) {
    Path testsDirectory = problemDirectory.resolve(TESTS_DIRECTORY_NAME);
    if (!Files.exists(testsDirectory)) {
      return List.of();
    }
    try (var paths = Files.walk(testsDirectory)) {
      return paths
          .filter(Files::isRegularFile)
          .sorted(
              Comparator.comparing(
                  path -> problemDirectory.relativize(path).toString().replace('\\', '/')))
          .toList();
    } catch (IOException exception) {
      throw new InvalidProblemDefinitionException(testsDirectory, exception);
    }
  }
}
