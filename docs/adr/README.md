# Architecture Decision Records

이 디렉터리는 우테코 프리코스 연습 플랫폼의 중요한 기술 결정을 기록한다. 각 문서는 구현 방법뿐 아니라 사용자에게 주는 가치와 선택에 따른 비용을 함께 설명한다.

## 상태

- `Proposed`: 검토 중
- `Accepted`: 승인되어 적용할 결정
- `Deprecated`: 더 이상 권장하지 않지만 대체 결정이 없음
- `Superseded`: 새로운 ADR로 대체됨

## 작성 규칙

1. 파일명은 `NNNN-short-title.md` 형식을 사용한다.
2. 승인된 ADR은 내용을 뒤집어 수정하지 않는다.
3. 결정을 바꾸면 새 ADR을 만들고 두 문서에 대체 관계를 연결한다.
4. 배경, 사용자 가치, 결정, 이유, 대안, 결과를 반드시 작성한다.

## 결정 목록

| ADR | 제목 | 상태 |
| --- | --- | --- |
| [0001](0001-modular-monolith-with-separate-worker.md) | Gradle 멀티모듈과 API·워커 분리 | Superseded |
| [0002](0002-mysql-backed-grading-queue.md) | MySQL 기반 채점 작업 큐 | Accepted |
| [0003](0003-runtime-test-injection-in-docker.md) | Docker 런타임 테스트 주입 | Accepted |
| [0004](0004-java-spring-jpa-mysql-stack.md) | Java·Spring Boot·JPA·MySQL 기술 스택 | Accepted |
| [0005](0005-acceptance-test-led-tdd.md) | RestAssured 중심 인수 테스트 TDD | Accepted |
| [0006](0006-git-managed-problem-definitions.md) | Git 기반 문제 정의 관리 | Superseded |
| [0007](0007-ten-concurrent-gradings.md) | 단일 서버 동시 채점 10건 | Accepted |
| [0008](0008-openapi-and-swagger-ui.md) | OpenAPI 명세와 Swagger UI | Accepted |
| [0009](0009-nstest-canonical-test-bundles.md) | NsTest 공식 테스트 번들 | Accepted |
| [0010](0010-no-submission-source-snapshot.md) | 제출 소스 스냅샷 미보관 | Accepted |
| [0011](0011-single-spring-project.md) | 단일 Spring Boot 프로젝트와 워커 실행 분리 | Accepted |
| [0012](0012-database-managed-problems.md) | DB 기반 문제와 공식 테스트 관리 | Accepted |
