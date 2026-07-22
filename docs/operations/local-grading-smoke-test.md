# Local Grading Smoke Test

This smoke test starts MySQL and the API with Docker Compose, then starts the
grading worker as a host process. It verifies the local process topology only;
GitHub OAuth login and repository ownership validation are intentionally out of
scope.

## Prerequisites

- Docker Desktop is running and the host Docker CLI can create containers.
- Java 21 is available for the Gradle worker process.

The worker must run on the host because it starts one constrained Docker
sandbox for each grading job. Do not give the API container access to the
Docker daemon socket.

## Build The Grader Image

Build the constrained image used by `DockerSandboxRunner` before starting the
stack:

```bash
docker build -t woowapractice/grader:java21 docker/grader
```

## Start The API And Database

From the project root, build and start the local stack with security disabled:

```bash
SECURITY_ENABLED=false docker compose up -d --build mysql api
```

The API is available at `http://localhost:8080`. Confirm it responds before
starting the worker:

```bash
curl --fail http://localhost:8080/api/problems
```

`SECURITY_ENABLED=false` is only for this local operational smoke test. The
default Compose configuration keeps OAuth security enabled. Because security is
disabled here, the GitHub OAuth login flow and the authenticated submission
ownership/fork validation are deliberately not tested.

## Start The Host Worker

In a second terminal at the project root, run the dedicated worker with a host
database URL:

```bash
DB_URL='jdbc:mysql://localhost:3306/woowapractice?serverTimezone=UTC' \
DB_USERNAME=woowapractice \
DB_PASSWORD=woowapractice \
SECURITY_ENABLED=false \
./gradlew workerRun
```

The worker runs with the `worker` profile and polls MySQL. It is a non-web
process, so it must not open an HTTP port. Its host Docker CLI access is what
allows `DockerSandboxRunner` to launch the grader image with the configured
network, CPU, memory, PID, and timeout limits.

## Scope Of This Smoke Test

This reproducible baseline verifies that MySQL, the API, and the host worker
start with the intended topology. It does not claim to complete an end-to-end
grading run. Live submission and worker-claim validation additionally require
a public GitHub fork that matches a configured starter repository and
DB-managed problem data, including an official `ApplicationTest` source.

## Stop The Smoke Test

Stop the Gradle worker with `Ctrl+C`, then stop the Compose services:

```bash
docker compose down
```

Use `docker compose down -v` only when intentionally discarding the local
MySQL data volume.
