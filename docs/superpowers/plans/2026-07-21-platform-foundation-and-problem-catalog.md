# Platform Foundation and Problem Catalog Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Create the Java/Gradle project foundation and deliver a working problem catalog that is populated from versioned YAML/Markdown definitions and exposed through REST endpoints.

**Architecture:** Use a Gradle multi-module repository with dependencies flowing from `api` and `problem-sync` through `application` to `domain`; JPA implementations live in `infrastructure`. This is the first of four implementation plans: catalog foundation, authentication/submissions, grading execution, then minimal UI and capacity hardening.

**Tech Stack:** Java 21, Spring Boot 4.1.0, Gradle 9.6.1 Groovy DSL, Spring Data JPA, Flyway, MySQL 8, H2, RestAssured, JUnit 5, AssertJ, Lombok, Jackson YAML, springdoc-openapi 3.0.3, Swagger UI, Spotless

## Global Constraints

- Source code is Java 21; build scripts use Gradle Groovy DSL.
- Use `@Transactional(readOnly = true)` on service classes and `@Transactional` only on write methods.
- Presentation DTOs must not cross into the application layer.
- Use Lombok only for focused boilerplate; do not use `@Data`, public entity setters, or unrestricted builders.
- Use RestAssured acceptance tests as the outer TDD loop with H2 for platform-server tests.
- Do not write controller unit tests or tests for plain Spring Data CRUD.
- Store enums as strings, store time in UTC, and change schemas only through Flyway.
- Keep policies inline until branching or genuine replaceability makes extraction useful.
- Package root is `com.woowapractice`.
- Commit after every task passes its stated verification.

## Scope Decomposition

This plan implements only the first independently testable vertical slice. Follow it with these separate plans:

1. GitHub OAuth, repository ownership/fork validation, submission intake, and personal history.
2. MySQL job claiming, worker leases, Docker sandboxing, runtime test injection, and grading results.
3. Minimal server-rendered screens, Docker Compose deployment, ten-concurrent-grading load tests, and operational hardening.

## File Map

```text
settings.gradle                         Gradle module registry
build.gradle                            Shared Java, test, formatting conventions
gradle/wrapper/*                        Pinned Gradle 9.6.1 wrapper
domain/                                 Problem aggregate and stage enum
application/                            Catalog query and synchronization use cases
infrastructure/                         JPA entities/repositories and Flyway migrations
api/                                    Spring Boot API and RestAssured acceptance tests
problem-sync/                           YAML/Markdown parser and deployment CLI
problems/                               Version-controlled problem definitions
```

---

### Task 1: Bootstrap the Multi-Module Build and API Test Harness

**Files:**
- Create: `settings.gradle`
- Create: `build.gradle`
- Create: `domain/build.gradle`
- Create: `application/build.gradle`
- Create: `infrastructure/build.gradle`
- Create: `api/build.gradle`
- Create: `grading/build.gradle`
- Create: `worker/build.gradle`
- Create: `problem-sync/build.gradle`
- Create: `api/src/main/java/com/woowapractice/api/ApiApplication.java`
- Create: `api/src/main/resources/application.yml`
- Create: `api/src/test/resources/application-test.yml`
- Test: `api/src/test/java/com/woowapractice/api/ApiApplicationAcceptanceTest.java`

**Interfaces:**
- Consumes: No application code; this is the repository foundation.
- Produces: Boot entry point `ApiApplication`, shared Java 21 conventions, and an H2-backed RestAssured test context used by later tasks.

- [ ] **Step 1: Create the failing application-context acceptance test**

```java
package com.woowapractice.api;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ActiveProfiles;

@ActiveProfiles("test")
@SpringBootTest
class ApiApplicationAcceptanceTest {

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    void 애플리케이션_컨텍스트를_실행한다() {
        assertThat(applicationContext).isNotNull();
    }
}
```

- [ ] **Step 2: Run the test and confirm the project is not bootstrapped**

Run: `./gradlew :api:test --tests '*ApiApplicationAcceptanceTest'`

Expected: FAIL because the Gradle wrapper or `api` module does not exist.

- [ ] **Step 3: Add the Gradle build and Spring Boot entry point**

Create `settings.gradle`:

```groovy
rootProject.name = 'woowacourse-practice'

include 'domain', 'application', 'infrastructure', 'api', 'grading', 'worker', 'problem-sync'
```

Create root `build.gradle`:

```groovy
plugins {
    id 'java'
    id 'org.springframework.boot' version '4.1.0' apply false
    id 'io.spring.dependency-management' version '1.1.7' apply false
    id 'com.diffplug.spotless' version '8.0.0' apply false
}

allprojects {
    group = 'com.woowapractice'
    version = '0.0.1-SNAPSHOT'

    repositories {
        mavenCentral()
    }
}

subprojects {
    apply plugin: 'java'
    apply plugin: 'io.spring.dependency-management'
    apply plugin: 'com.diffplug.spotless'

    java {
        toolchain {
            languageVersion = JavaLanguageVersion.of(21)
        }
    }

    dependencyManagement {
        imports {
            mavenBom org.springframework.boot.gradle.plugin.SpringBootPlugin.BOM_COORDINATES
        }
    }

    configurations {
        compileOnly {
            extendsFrom annotationProcessor
        }
    }

    dependencies {
        compileOnly 'org.projectlombok:lombok'
        annotationProcessor 'org.projectlombok:lombok'
        testCompileOnly 'org.projectlombok:lombok'
        testAnnotationProcessor 'org.projectlombok:lombok'
        testImplementation 'org.junit.jupiter:junit-jupiter'
        testImplementation 'org.assertj:assertj-core'
    }

    tasks.withType(Test).configureEach {
        useJUnitPlatform()
    }

    spotless {
        java {
            googleJavaFormat()
            removeUnusedImports()
            trimTrailingWhitespace()
            endWithNewline()
        }
    }
}
```

Use these module dependencies:

```groovy
// domain/build.gradle
dependencies {
    implementation 'jakarta.persistence:jakarta.persistence-api'
}

// application/build.gradle
dependencies {
    implementation project(':domain')
    implementation 'org.springframework:spring-context'
    implementation 'org.springframework:spring-tx'
}

// infrastructure/build.gradle
dependencies {
    implementation project(':domain')
    implementation project(':application')
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    implementation 'org.flywaydb:flyway-core'
    implementation 'org.flywaydb:flyway-mysql'
    runtimeOnly 'com.mysql:mysql-connector-j'
}

// api/build.gradle
plugins {
    id 'org.springframework.boot'
}
dependencies {
    implementation project(':application')
    implementation project(':infrastructure')
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-validation'
    implementation 'org.springdoc:springdoc-openapi-starter-webmvc-ui:3.0.3'
    runtimeOnly 'com.mysql:mysql-connector-j'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'io.rest-assured:rest-assured'
    testRuntimeOnly 'com.h2database:h2'
}

// grading/build.gradle
dependencies {
    implementation project(':application')
}

// worker/build.gradle
plugins {
    id 'org.springframework.boot'
}
dependencies {
    implementation project(':grading')
    implementation project(':infrastructure')
    implementation 'org.springframework.boot:spring-boot-starter'
}

// problem-sync/build.gradle
plugins {
    id 'org.springframework.boot'
}
dependencies {
    implementation project(':application')
    implementation project(':infrastructure')
    implementation 'org.springframework.boot:spring-boot-starter'
    implementation 'com.fasterxml.jackson.dataformat:jackson-dataformat-yaml'
    runtimeOnly 'com.mysql:mysql-connector-j'
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testRuntimeOnly 'com.h2database:h2'
}
```

Create the application entry point:

```java
package com.woowapractice.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.woowapractice")
public class ApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(ApiApplication.class, args);
    }
}
```

Create `api/src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: ${DB_URL:jdbc:mysql://localhost:3306/woowapractice?serverTimezone=UTC}
    username: ${DB_USERNAME:woowapractice}
    password: ${DB_PASSWORD:woowapractice}
  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false
    properties:
      hibernate.jdbc.time_zone: UTC
  flyway:
    enabled: true
```

Create `api/src/test/resources/application-test.yml`:

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:acceptance;MODE=MySQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1
    username: sa
    password:
  jpa:
    hibernate:
      ddl-auto: validate
    open-in-view: false
  flyway:
    enabled: true
```

Generate and pin the wrapper with `gradle wrapper --gradle-version 9.6.1`.

- [ ] **Step 4: Run the foundation verification**

Run: `./gradlew :api:test --tests '*ApiApplicationAcceptanceTest' spotlessCheck`

Expected: PASS with one application-context test and no formatting violations.

- [ ] **Step 5: Commit the foundation**

```bash
git add settings.gradle build.gradle gradle gradlew gradlew.bat domain application infrastructure api grading worker problem-sync
git commit -m "build: bootstrap platform modules"
```

---

### Task 2: Persist the Versioned Problem Catalog

**Files:**
- Create: `domain/src/main/java/com/woowapractice/problem/domain/ProblemStage.java`
- Create: `domain/src/main/java/com/woowapractice/problem/domain/Problem.java`
- Create: `domain/src/main/java/com/woowapractice/problem/domain/ProblemVersion.java`
- Create: `application/src/main/java/com/woowapractice/problem/application/ProblemCatalog.java`
- Create: `infrastructure/src/main/java/com/woowapractice/problem/infrastructure/JpaProblemRepository.java`
- Create: `infrastructure/src/main/java/com/woowapractice/problem/infrastructure/ProblemCatalogAdapter.java`
- Create: `infrastructure/src/main/resources/db/migration/V1__create_problem_catalog.sql`
- Test: `api/src/test/java/com/woowapractice/problem/ProblemCatalogPersistenceAcceptanceTest.java`

**Interfaces:**
- Consumes: Spring/JPA/Flyway foundation from Task 1.
- Produces: `ProblemCatalog.findActiveByStage(ProblemStage)`, `ProblemCatalog.findActiveBySlug(String)`, and `ProblemCatalog.save(Problem)`.

- [ ] **Step 1: Write a failing persistence acceptance test**

```java
package com.woowapractice.problem;

import static org.assertj.core.api.Assertions.assertThat;

import com.woowapractice.problem.application.ProblemCatalog;
import com.woowapractice.problem.domain.Problem;
import com.woowapractice.problem.domain.ProblemStage;
import com.woowapractice.problem.domain.ProblemVersion;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@ActiveProfiles("test")
@SpringBootTest
@Transactional
class ProblemCatalogPersistenceAcceptanceTest {

    @Autowired
    private ProblemCatalog problemCatalog;

    @Test
    void 활성_문제를_차수와_표시순서로_조회한다() {
        Problem problem = Problem.create(
                "racing-car",
                "자동차 경주",
                ProblemStage.ROUND_2,
                1,
                "기능 요구사항",
                "https://github.com/example/java-racingcar");
        problem.addVersion(ProblemVersion.create(1, 21, "racing-car/v1", "checksum"));
        problemCatalog.save(problem);

        List<Problem> result = problemCatalog.findActiveByStage(ProblemStage.ROUND_2);

        assertThat(result).extracting(Problem::getSlug).containsExactly("racing-car");
    }
}
```

- [ ] **Step 2: Run the persistence test and verify the missing types**

Run: `./gradlew :api:test --tests '*ProblemCatalogPersistenceAcceptanceTest'`

Expected: FAIL at test compilation because `Problem`, `ProblemVersion`, and `ProblemCatalog` do not exist.

- [ ] **Step 3: Implement the aggregate and catalog port**

Implement `ProblemStage`:

```java
package com.woowapractice.problem.domain;

public enum ProblemStage {
    ROUND_1,
    ROUND_2,
    ROUND_3,
    ROUND_4,
    ROUND_5,
    FINAL
}
```

Implement `Problem` with JPA mappings, `Problem.create(...)`, `addVersion(...)`, getters, protected no-args construction, `active = true`, and an ordered `@OneToMany(mappedBy = "problem", cascade = ALL, orphanRemoval = true)` version list. Implement `ProblemVersion.create(int version, int javaVersion, String testBundleRef, String configChecksum)` and set its problem only through `Problem.addVersion`.

The catalog port must be exact:

```java
package com.woowapractice.problem.application;

import com.woowapractice.problem.domain.Problem;
import com.woowapractice.problem.domain.ProblemStage;
import java.util.List;
import java.util.Optional;

public interface ProblemCatalog {

    List<Problem> findActiveByStage(ProblemStage stage);

    Optional<Problem> findActiveBySlug(String slug);

    Problem save(Problem problem);
}
```

Implement `JpaProblemRepository` as a package-private Spring Data repository with methods `findAllByActiveTrueAndStageOrderByDisplayOrderAscIdAsc` and `findBySlugAndActiveTrue`. Implement the public `ProblemCatalogAdapter` to delegate to it.

Create the migration:

```sql
CREATE TABLE problems (
    id BIGINT NOT NULL AUTO_INCREMENT,
    slug VARCHAR(100) NOT NULL,
    title VARCHAR(200) NOT NULL,
    stage VARCHAR(20) NOT NULL,
    display_order INT NOT NULL,
    description_md TEXT NOT NULL,
    starter_repo_url VARCHAR(500) NOT NULL,
    active BOOLEAN NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT uk_problems_slug UNIQUE (slug),
    INDEX idx_problems_active_stage_order (active, stage, display_order, id)
);

CREATE TABLE problem_versions (
    id BIGINT NOT NULL AUTO_INCREMENT,
    problem_id BIGINT NOT NULL,
    version INT NOT NULL,
    java_version INT NOT NULL,
    test_bundle_ref VARCHAR(300) NOT NULL,
    config_checksum VARCHAR(64) NOT NULL,
    published_at TIMESTAMP(6) NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_problem_versions_problem FOREIGN KEY (problem_id) REFERENCES problems (id),
    CONSTRAINT uk_problem_versions_problem_version UNIQUE (problem_id, version)
);
```

- [ ] **Step 4: Run persistence and schema verification**

Run: `./gradlew :api:test --tests '*ProblemCatalogPersistenceAcceptanceTest'`

Expected: PASS; Flyway creates both tables and JPA validation succeeds.

- [ ] **Step 5: Commit the catalog persistence**

```bash
git add domain application infrastructure api/src/test
git commit -m "feat: persist versioned problem catalog"
```

---

### Task 3: Expose Problem List and Detail REST APIs

**Files:**
- Create: `application/src/main/java/com/woowapractice/problem/application/ProblemQueryService.java`
- Create: `application/src/main/java/com/woowapractice/problem/application/ProblemSummary.java`
- Create: `application/src/main/java/com/woowapractice/problem/application/ProblemDetail.java`
- Create: `application/src/main/java/com/woowapractice/problem/application/ProblemNotFoundException.java`
- Create: `api/src/main/java/com/woowapractice/problem/presentation/ProblemController.java`
- Create: `api/src/main/java/com/woowapractice/problem/presentation/ProblemResponse.java`
- Create: `api/src/main/java/com/woowapractice/common/presentation/OpenApiConfig.java`
- Create: `api/src/main/java/com/woowapractice/common/presentation/ApiErrorResponse.java`
- Create: `api/src/main/java/com/woowapractice/common/presentation/GlobalExceptionHandler.java`
- Test: `api/src/test/java/com/woowapractice/problem/ProblemApiAcceptanceTest.java`
- Test: `api/src/test/java/com/woowapractice/support/DatabaseCleaner.java`
- Test: `api/src/test/java/com/woowapractice/support/ProblemFixture.java`

**Interfaces:**
- Consumes: `ProblemCatalog` from Task 2.
- Produces: `GET /api/problems?stage=ROUND_2` and `GET /api/problems/{slug}` with stable response and error contracts.
- Produces: `GET /v3/api-docs` and `GET /swagger-ui/index.html` for executable OpenAPI documentation.

- [ ] **Step 1: Write failing RestAssured scenarios**

```java
@Test
void 차수별_활성_문제_목록을_조회한다() {
    problemFixture.save("racing-car", "자동차 경주", "ROUND_2", 1, true);

    given()
            .queryParam("stage", "ROUND_2")
    .when()
            .get("/api/problems")
    .then()
            .statusCode(200)
            .body("problems.slug", contains("racing-car"))
            .body("problems[0].stage", equalTo("ROUND_2"));
}

@Test
void 존재하지_않는_문제는_안정적인_오류코드를_반환한다() {
    given()
    .when()
            .get("/api/problems/not-found")
    .then()
            .statusCode(404)
            .body("code", equalTo("PROBLEM_NOT_FOUND"))
            .body("traceId", not(emptyString()));
}

@Test
void 공개_API와_오류응답을_OpenAPI로_제공한다() {
    given()
    .when()
            .get("/v3/api-docs")
    .then()
            .statusCode(200)
            .body("openapi", startsWith("3.1"))
            .body("info.title", equalTo("Woowa Practice API"))
            .body("paths.'/api/problems'", notNullValue())
            .body("components.schemas.ApiErrorResponse", notNullValue());
}

@Test
void Swagger_UI를_제공한다() {
    given()
    .when()
            .get("/swagger-ui/index.html")
    .then()
            .statusCode(200);
}
```

Configure RestAssured from `@LocalServerPort` in `@BeforeEach`, clean tables through `JdbcTemplate`, and insert fixtures through `ProblemFixture`; do not autowire the package-private Spring Data repository into the test.

- [ ] **Step 2: Run the API tests and verify 404/missing routes**

Run: `./gradlew :api:test --tests '*ProblemApiAcceptanceTest'`

Expected: FAIL because `/api/problems` endpoints and error mapping do not exist.

- [ ] **Step 3: Implement the application query service**

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProblemQueryService {

    private final ProblemCatalog problemCatalog;

    public List<ProblemSummary> findAll(ProblemStage stage) {
        return problemCatalog.findActiveByStage(stage).stream()
                .map(ProblemSummary::from)
                .toList();
    }

    public ProblemDetail findBySlug(String slug) {
        return problemCatalog.findActiveBySlug(slug)
                .map(ProblemDetail::from)
                .orElseThrow(() -> new ProblemNotFoundException(slug));
    }
}
```

Define application records without HTTP types:

```java
public record ProblemSummary(String slug, String title, ProblemStage stage, int displayOrder) {
    public static ProblemSummary from(Problem problem) {
        return new ProblemSummary(problem.getSlug(), problem.getTitle(), problem.getStage(), problem.getDisplayOrder());
    }
}

public record ProblemDetail(
        String slug,
        String title,
        ProblemStage stage,
        int displayOrder,
        String descriptionMarkdown,
        String starterRepositoryUrl,
        int version,
        int javaVersion) {
    public static ProblemDetail from(Problem problem) {
        ProblemVersion current = problem.currentVersion();
        return new ProblemDetail(problem.getSlug(), problem.getTitle(), problem.getStage(),
                problem.getDisplayOrder(), problem.getDescriptionMarkdown(),
                problem.getStarterRepositoryUrl(), current.getVersion(), current.getJavaVersion());
    }
}
```

- [ ] **Step 4: Implement presentation mapping and common errors**

The controller must delegate only:

```java
@RestController
@RequestMapping("/api/problems")
@RequiredArgsConstructor
public class ProblemController {

    private final ProblemQueryService problemQueryService;

    @GetMapping
    public ProblemResponse.ListResponse findAll(@RequestParam ProblemStage stage) {
        return ProblemResponse.ListResponse.from(problemQueryService.findAll(stage));
    }

    @GetMapping("/{slug}")
    public ProblemResponse.Detail findBySlug(@PathVariable String slug) {
        return ProblemResponse.Detail.from(problemQueryService.findBySlug(slug));
    }
}
```

Use this stable error shape:

```java
public record ApiErrorResponse(
        String code,
        String message,
        List<FieldError> fieldErrors,
        String traceId) {

    public record FieldError(String field, String reason) {}
}
```

Map `ProblemNotFoundException` to HTTP 404 and `MethodArgumentTypeMismatchException` to HTTP 400 with code `INVALID_STAGE`. Read the trace ID from MDC when available and otherwise generate a UUID for the response.

Create the OpenAPI metadata configuration:

```java
@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI woowaPracticeOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Woowa Practice API")
                .version("v1")
                .description("우아한테크코스 프리코스 연습 플랫폼 API"));
    }
}
```

Annotate controller operations with `@Operation` and declare 200, 400, and 404 responses with `@ApiResponse`. Reference `ProblemResponse` and `ApiErrorResponse` schemas instead of duplicating inline response shapes. Configure `springdoc.api-docs.version: openapi_3_1` and keep the default `/v3/api-docs` and `/swagger-ui/index.html` paths.

- [ ] **Step 5: Run all API tests and formatting**

Run: `./gradlew :api:test spotlessCheck`

Expected: PASS for context, persistence, list, detail, not-found, invalid-stage, OpenAPI document, and Swagger UI scenarios.

- [ ] **Step 6: Commit the REST catalog**

```bash
git add application api
git commit -m "feat: expose problem catalog api"
```

---

### Task 4: Synchronize Git-Managed Problem Definitions

**Files:**
- Create: `application/src/main/java/com/woowapractice/problem/application/ProblemSyncCommand.java`
- Create: `application/src/main/java/com/woowapractice/problem/application/ProblemSyncService.java`
- Create: `problem-sync/src/main/java/com/woowapractice/sync/ProblemSyncApplication.java`
- Create: `problem-sync/src/main/java/com/woowapractice/sync/ProblemDefinitionLoader.java`
- Create: `problem-sync/src/main/java/com/woowapractice/sync/ProblemDefinitionFile.java`
- Create: `problem-sync/src/main/resources/application.yml`
- Create: `problem-sync/src/test/java/com/woowapractice/sync/ProblemDefinitionLoaderTest.java`
- Create: `problem-sync/src/test/java/com/woowapractice/sync/ProblemSyncAcceptanceTest.java`
- Create: `problems/racing-car/problem.yaml`
- Create: `problems/racing-car/README.md`
- Create: `problems/racing-car/tests/.gitkeep`

**Interfaces:**
- Consumes: `ProblemCatalog.save(Problem)` and problem aggregate factories from Task 2.
- Produces: `ProblemDefinitionLoader.load(Path): List<ProblemSyncCommand>` and `ProblemSyncService.synchronize(List<ProblemSyncCommand>)`.

- [ ] **Step 1: Write failing YAML and Markdown loader tests**

```java
@TempDir
Path tempDirectory;

@Test
void yaml과_markdown을_하나의_동기화_명령으로_읽는다() throws IOException {
    Path problem = Files.createDirectories(tempDirectory.resolve("racing-car"));
    Files.writeString(problem.resolve("problem.yaml"), """
            slug: racing-car
            title: 자동차 경주
            stage: ROUND_2
            displayOrder: 1
            starterRepositoryUrl: https://github.com/example/java-racingcar
            version: 1
            javaVersion: 21
            testBundleRef: racing-car/v1
            """);
    Files.writeString(problem.resolve("README.md"), "# 자동차 경주");

    List<ProblemSyncCommand> commands = loader.load(tempDirectory);

    assertThat(commands).singleElement().satisfies(command -> {
        assertThat(command.slug()).isEqualTo("racing-car");
        assertThat(command.descriptionMarkdown()).isEqualTo("# 자동차 경주");
        assertThat(command.configChecksum()).hasSize(64);
    });
}
```

Add a second test asserting duplicate slug, missing README, invalid stage, non-positive version, and non-positive display order each throw `InvalidProblemDefinitionException` with the offending path.

- [ ] **Step 2: Run loader tests and verify missing implementation**

Run: `./gradlew :problem-sync:test --tests '*ProblemDefinitionLoaderTest'`

Expected: FAIL because the loader and command types do not exist.

- [ ] **Step 3: Implement deterministic definition loading**

Define the command exactly:

```java
public record ProblemSyncCommand(
        String slug,
        String title,
        ProblemStage stage,
        int displayOrder,
        String descriptionMarkdown,
        String starterRepositoryUrl,
        int version,
        int javaVersion,
        String testBundleRef,
        String configChecksum) {}
```

`ProblemDefinitionLoader.load(Path root)` must sort child directories by name, deserialize each `problem.yaml` with Jackson YAML, read the sibling `README.md` as UTF-8, validate all required values, reject duplicate slugs, and calculate SHA-256 over the exact YAML bytes, README bytes, and sorted relative test file paths plus bytes. Return an immutable list only after every definition passes validation.

- [ ] **Step 4: Write the failing transactional synchronization test**

```java
@Test
void 검증된_문제_목록을_한_트랜잭션으로_동기화한다() {
    syncService.synchronize(List.of(racingCarCommand()));

    Problem saved = problemCatalog.findActiveBySlug("racing-car").orElseThrow();
    assertThat(saved.getStage()).isEqualTo(ProblemStage.ROUND_2);
    assertThat(saved.currentVersion().getVersion()).isEqualTo(1);
}
```

Add a rollback scenario with two otherwise valid commands whose second command reuses an existing `(problem_id, version)` with a different checksum. Assert that the conflict throws `ProblemVersionConflictException` and that changes made by the first command in the same synchronization call are rolled back.

- [ ] **Step 5: Implement synchronization and CLI execution**

Implement the write boundary:

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProblemSyncService {

    private final ProblemCatalog problemCatalog;

    @Transactional
    public void synchronize(List<ProblemSyncCommand> commands) {
        commands.forEach(this::upsert);
    }

    private void upsert(ProblemSyncCommand command) {
        Problem problem = problemCatalog.findBySlug(command.slug())
                .orElseGet(() -> Problem.create(command.slug(), command.title(), command.stage(),
                        command.displayOrder(), command.descriptionMarkdown(), command.starterRepositoryUrl()));
        problem.synchronize(command.title(), command.stage(), command.displayOrder(),
                command.descriptionMarkdown(), command.starterRepositoryUrl());
        problem.publishVersion(command.version(), command.javaVersion(),
                command.testBundleRef(), command.configChecksum());
        problemCatalog.save(problem);
    }
}
```

Add `ProblemCatalog.findBySlug(String)` for synchronization of inactive records. `publishVersion` must be idempotent when the version and checksum match and must reject reuse of the same version with a different checksum.

The CLI entry point must require exactly one non-option argument for the definitions root, set `WebApplicationType.NONE`, call loader then service, log the synchronized count, and exit non-zero on validation or persistence failure. Do not synchronize during API startup.

- [ ] **Step 6: Add the first versioned example definition**

Create `problems/racing-car/problem.yaml`:

```yaml
slug: racing-car
title: 자동차 경주
stage: ROUND_2
displayOrder: 1
starterRepositoryUrl: https://github.com/example/java-racingcar
version: 1
javaVersion: 21
testBundleRef: racing-car/v1
```

Create `README.md` with this exact content. Keep `tests/.gitkeep` until the grading plan adds executable public tests.

```markdown
# 자동차 경주

자동차 경주 문제 정의의 동기화와 조회 흐름을 검증하기 위한 개발용 문제입니다.
```

- [ ] **Step 7: Run the complete first-slice verification**

Run: `./gradlew clean test spotlessCheck`

Expected: PASS for all modules, RestAssured catalog scenarios, loader validation, and transactional synchronization.

- [ ] **Step 8: Commit the problem synchronization slice**

```bash
git add application domain problem-sync problems
git commit -m "feat: synchronize git managed problems"
```

---

## Plan Self-Review Checklist

- The first slice covers the approved module boundaries, Java/Gradle conventions, JPA/Flyway schema ownership, H2 server acceptance tests, problem stages, versioned tests metadata, indexes, REST error shape, OpenAPI/Swagger UI, and Git-managed definitions.
- Authentication, submissions, grading jobs, Docker execution, minimal screens, and ten-concurrent-grading validation are deliberately assigned to the three follow-up plans listed under Scope Decomposition.
- All public method names used by later tasks are declared in an earlier task or explicitly added in the consuming task.
- No controller unit test or plain CRUD repository test is introduced.
