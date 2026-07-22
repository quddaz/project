# Host-Managed Grading Worker Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Run API and MySQL with Docker Compose while executing the Docker-capable grading worker as a dedicated host process.

**Architecture:** `docker-compose.yml` becomes the local API and database topology only. The existing `GradingWorkerApplication` remains the worker entry point and is launched with `./gradlew workerRun`, so only the host process receives Docker daemon access. A small operations document records the required startup order and development security flag.

**Tech Stack:** Docker Compose, Docker CLI, Java 21, Spring Boot 4, Gradle.

## Global Constraints

- Do not add an AI provider or any AI-generated feedback behavior.
- Do not mount the Docker daemon socket into an application container.
- Keep Docker sandbox network, CPU, memory, PID, and timeout limits unchanged.
- Keep OAuth enabled by default; use `SECURITY_ENABLED=false` only for local operational verification.
- Use a single root Gradle build and the existing `workerRun` task.

---

### Task 1: Make Compose API-and-database only

**Files:**
- Modify: `docker-compose.yml`
- Modify: `docs/adr/0013-host-managed-grading-worker.md`

**Interfaces:**
- Consumes: `GradingWorkerApplication` through the existing Gradle `workerRun` task.
- Produces: A Compose topology exposing `mysql` and `api`, with no Docker-capable worker container.

- [x] **Step 1: Confirm the current topology contains a Docker-capable worker container**

Run: `docker compose config`

Expected: the rendered configuration contains the `worker` service and confirms the configuration is parseable.

- [x] **Step 2: Remove the worker service and make local security configuration overridable**

Delete the complete `worker` service block. Retain `mysql`, `api`, the named MySQL volume, healthcheck, DB environment variables, and API port mapping. In the API environment use `${GITHUB_CLIENT_ID:-local}`, `${GITHUB_CLIENT_SECRET:-local}`, and `${SECURITY_ENABLED:-true}` so production remains secure by default and the documented `SECURITY_ENABLED=false` smoke test does not need OAuth credentials.

- [x] **Step 3: Mark the approved ADR accepted**

Change the ADR status from `Proposed` to `Accepted` after the topology matches the decision.

- [ ] **Step 4: Validate the rendered Compose topology (blocked: Docker CLI unavailable)**

Run: `docker compose config --services`

Expected: exactly `mysql` and `api` are listed; `worker` is absent.

- [x] **Step 5: Commit**

```bash
git add docker-compose.yml docs/adr/0013-host-managed-grading-worker.md docs/adr/README.md
git commit -m "chore: run grading worker on host"
```

### Task 2: Document and execute a local operational smoke test

**Files:**
- Create: `docs/operations/local-grading-smoke-test.md`

**Interfaces:**
- Consumes: `docker compose up -d mysql api`, `./gradlew workerRun`, `SECURITY_ENABLED=false`, and the API on `http://localhost:8080`.
- Produces: Reproducible commands for starting and stopping the local stack without GitHub OAuth credentials.

- [x] **Step 1: Write the operational commands**

Document these exact command shapes:

```bash
SECURITY_ENABLED=false docker compose up -d --build mysql api
DB_URL='jdbc:mysql://localhost:3306/woowapractice?serverTimezone=UTC' \
DB_USERNAME=woowapractice \
DB_PASSWORD=woowapractice \
SECURITY_ENABLED=false \
./gradlew workerRun
docker compose down
```

State that the worker requires host Docker access and that GitHub OAuth validation is intentionally outside this smoke test.

- [ ] **Step 2: Build application and grader images (blocked: Docker CLI unavailable)**

Run: `docker compose build api` and `docker build -t woowapractice/grader:java21 docker/grader`

Expected: both images build successfully.

- [ ] **Step 3: Start MySQL and API with development security disabled (blocked: Docker CLI unavailable)**

Run: `SECURITY_ENABLED=false docker compose up -d mysql api`

Expected: MySQL becomes healthy and API responds to `GET /api/problems`.

- [ ] **Step 4: Start the host worker and verify process startup (blocked: Docker CLI unavailable)**

Run the documented `workerRun` command with host database variables and inspect its startup log.

Expected: it connects to MySQL, enables the worker profile, and starts polling without starting an HTTP server.

- [x] **Step 5: Stop smoke-test processes and record any external blockers**

Run: `docker compose down`

Expected: Compose services stop cleanly. If Docker Desktop, image pull, or GitHub access prevents a live test, record the exact blocker without changing the security model.

- [x] **Step 6: Commit**

```bash
git add docs/operations/local-grading-smoke-test.md
git commit -m "docs: add local grading smoke test"
```

## Self-Review

1. Spec coverage: Task 1 removes Docker daemon authority from Compose; Task 2 supplies the local API, MySQL, and host-worker verification flow. Live Docker validation is blocked because the Docker CLI is unavailable. AI is excluded.
2. Placeholder scan: no placeholder implementation is present; the live Docker checks remain explicitly blocked and unchecked.
3. Type consistency: all commands use the existing `workerRun` task, existing `GradingWorkerApplication`, and Docker Compose service names.
