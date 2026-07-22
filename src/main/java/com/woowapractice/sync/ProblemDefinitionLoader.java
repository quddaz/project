package com.woowapractice.sync;

import com.fasterxml.jackson.core.StreamReadFeature;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.woowapractice.problem.application.ProblemSyncCommand;
import com.woowapractice.problem.domain.ProblemStage;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.LinkOption;
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
  private static final byte YAML_COMPONENT_TAG = 1;
  private static final byte README_COMPONENT_TAG = 2;
  private static final byte TEST_PATH_COMPONENT_TAG = 3;
  private static final byte TEST_PAYLOAD_COMPONENT_TAG = 4;

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
    Path normalizedRoot = normalizeRoot(root);
    List<Path> problemDirectories = findProblemDirectories(normalizedRoot);
    List<ProblemSyncCommand> commands = new ArrayList<>();
    Set<String> slugs = new HashSet<>();
    for (Path problemDirectory : problemDirectories) {
      ProblemSyncCommand command = loadDefinition(normalizedRoot, problemDirectory);
      if (!slugs.add(command.slug())) {
        throw new InvalidProblemDefinitionException(problemDirectory.resolve(YAML_FILE_NAME));
      }
      commands.add(command);
    }
    return List.copyOf(commands);
  }

  private Path normalizeRoot(Path root) {
    Path normalizedRoot = root.toAbsolutePath().normalize();
    if (Files.isSymbolicLink(normalizedRoot)
        || !Files.isDirectory(normalizedRoot, LinkOption.NOFOLLOW_LINKS)) {
      throw new InvalidProblemDefinitionException(normalizedRoot);
    }
    return normalizedRoot;
  }

  private List<Path> findProblemDirectories(Path root) {
    try (var children = Files.list(root)) {
      return children
          .map(path -> validateVisitedPath(root, path))
          .filter(path -> Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS))
          .sorted(Comparator.comparing(path -> path.getFileName().toString()))
          .toList();
    } catch (IOException exception) {
      throw new InvalidProblemDefinitionException(root, exception);
    }
  }

  private ProblemSyncCommand loadDefinition(Path root, Path problemDirectory) {
    Path yamlPath = problemDirectory.resolve(YAML_FILE_NAME);
    Path readmePath = problemDirectory.resolve(README_FILE_NAME);
    byte[] yamlBytes = readRequiredFile(root, yamlPath);
    byte[] readmeBytes = readRequiredFile(root, readmePath);
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
        calculateChecksum(root, problemDirectory, yamlBytes, readmeBytes));
  }

  private byte[] readRequiredFile(Path root, Path path) {
    Path normalizedPath = validateVisitedPath(root, path);
    try {
      if (!Files.isRegularFile(normalizedPath, LinkOption.NOFOLLOW_LINKS)) {
        throw new InvalidProblemDefinitionException(normalizedPath);
      }
      return Files.readAllBytes(normalizedPath);
    } catch (IOException exception) {
      throw new InvalidProblemDefinitionException(normalizedPath, exception);
    }
  }

  private Path validateVisitedPath(Path root, Path path) {
    Path normalizedPath = path.toAbsolutePath().normalize();
    if (!normalizedPath.startsWith(root) || Files.isSymbolicLink(normalizedPath)) {
      throw new InvalidProblemDefinitionException(normalizedPath);
    }
    return normalizedPath;
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

  private String calculateChecksum(
      Path root, Path problemDirectory, byte[] yamlBytes, byte[] readmeBytes) {
    MessageDigest digest = createDigest(problemDirectory);
    updateDigest(digest, YAML_COMPONENT_TAG, yamlBytes);
    updateDigest(digest, README_COMPONENT_TAG, readmeBytes);
    for (Path testFile : findTestFiles(root, problemDirectory)) {
      Path relativePath = problemDirectory.relativize(testFile);
      byte[] pathBytes =
          relativePath.toString().replace('\\', '/').getBytes(StandardCharsets.UTF_8);
      updateDigest(digest, TEST_PATH_COMPONENT_TAG, pathBytes);
      updateDigest(digest, TEST_PAYLOAD_COMPONENT_TAG, readRequiredFile(root, testFile));
    }
    return HexFormat.of().formatHex(digest.digest());
  }

  private void updateDigest(MessageDigest digest, byte componentTag, byte[] component) {
    byte[] length =
        ByteBuffer.allocate(Long.BYTES)
            .order(ByteOrder.BIG_ENDIAN)
            .putLong(component.length)
            .array();
    digest.update(componentTag);
    digest.update(length);
    digest.update(component);
  }

  private MessageDigest createDigest(Path problemDirectory) {
    try {
      return MessageDigest.getInstance(SHA_256);
    } catch (NoSuchAlgorithmException exception) {
      throw new InvalidProblemDefinitionException(problemDirectory, exception);
    }
  }

  private List<Path> findTestFiles(Path root, Path problemDirectory) {
    Path testsDirectory = validateVisitedPath(root, problemDirectory.resolve(TESTS_DIRECTORY_NAME));
    if (!Files.exists(testsDirectory, LinkOption.NOFOLLOW_LINKS)) {
      return List.of();
    }
    if (!Files.isDirectory(testsDirectory, LinkOption.NOFOLLOW_LINKS)) {
      throw new InvalidProblemDefinitionException(testsDirectory);
    }
    try (var paths = Files.walk(testsDirectory)) {
      return paths
          .map(path -> validateTestPath(root, path))
          .filter(path -> Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS))
          .sorted(
              Comparator.comparing(
                  path -> problemDirectory.relativize(path).toString().replace('\\', '/')))
          .toList();
    } catch (IOException exception) {
      throw new InvalidProblemDefinitionException(testsDirectory, exception);
    }
  }

  private Path validateTestPath(Path root, Path path) {
    Path normalizedPath = validateVisitedPath(root, path);
    if (!Files.isDirectory(normalizedPath, LinkOption.NOFOLLOW_LINKS)
        && !Files.isRegularFile(normalizedPath, LinkOption.NOFOLLOW_LINKS)) {
      throw new InvalidProblemDefinitionException(normalizedPath);
    }
    return normalizedPath;
  }
}
