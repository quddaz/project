# 우아한테크코스 프리코스 연습 플랫폼 설계

## 1. 목적

여러 사용자가 우아한테크코스 프리코스 형식의 문제를 연습하는 웹 서비스를 만든다. 사용자는 GitHub로 로그인하고 공식 스타터 저장소를 fork한 공개 저장소 URL을 제출한다. 서버는 제출 시점의 commit SHA를 고정하고, 격리된 Docker 환경에서 서버가 관리하는 공개 테스트를 주입해 채점한다.

MVP는 개인 학습 흐름과 안정적인 채점에 집중한다. 점수, 사용자 비교, 리더보드, 그룹, 기수 구분, 비공개 저장소, AI 피드백은 제외한다. 향후 AI 피드백과 워커 확장을 추가할 수 있도록 API와 채점 실행의 경계를 분리한다.

## 2. 기술 및 배포 기준

- Java 21, Spring Boot, Gradle Groovy DSL
- Spring Data JPA, MySQL 8, Flyway
- GitHub OAuth 전용 로그인
- 단일 Linux 서버의 Docker Compose 배포
- API와 채점 워커를 별도 Spring Boot 프로세스 및 컨테이너로 실행
- Redis나 메시지 브로커 없이 MySQL 작업 테이블을 비동기 큐로 사용
- 초기 동시 채점 1~3건을 기준으로 하되 워커만 수평 확장 가능

## 3. 시스템 구조

Gradle 멀티모듈 단일 저장소를 사용한다.

- `domain`: JPA 엔티티, 값 객체, 상태 전이와 핵심 도메인 규칙
- `application`: 문제 조회, 제출 접수, 채점 상태 전이 등 유스케이스와 트랜잭션 경계
- `infrastructure`: JPA 저장소 구현, GitHub 클라이언트, 문제 정의 로더
- `grading`: 작업 선점, Git clone, 테스트 주입, Docker 실행 계약과 구현
- `api`: REST API, OAuth, 공통 오류 응답, 최소 서버 렌더링 화면
- `worker`: 채점 작업을 소비하는 별도 실행 애플리케이션
- `problem-sync`: Git의 문제 정의를 검증하고 DB에 반영하는 배포용 CLI

의존 방향은 `api/worker -> application -> domain`으로 제한한다. `domain`과 `application`은 HTTP, Thymeleaf, Docker 명령 등 표현 및 실행 기술에 의존하지 않는다. API는 Docker를 직접 실행하지 않고 MySQL에 제출과 채점 작업을 같은 트랜잭션으로 기록한다.

## 4. 문제 정의와 분류

문제는 기수에 귀속하지 않고 차수만 표시한다.

- `ROUND_1`: 1차
- `ROUND_2`: 2차
- `ROUND_3`: 3차
- `ROUND_4`: 4차
- `ROUND_5`: 5차
- `FINAL`: 최종 테스트

같은 차수에 여러 문제가 생길 수 있으므로 `display_order`를 둔다. 문제 정의는 Git 저장소에서 다음 구조로 관리한다.

```text
problems/<slug>/
  problem.yaml
  README.md
  tests/
```

`problem.yaml`은 제목, 차수, 표시 순서, 스타터 저장소 URL, Java 버전, 실행 제한, 문제 버전을 포함한다. `README.md`는 문제 설명이며 `tests/`는 채점 시 주입할 공개 테스트다. 배포 담당자가 `problem-sync` 모듈의 Gradle 실행 명령을 호출하면 모든 정의를 검증한 뒤 하나의 트랜잭션으로 DB에 반영한다. 애플리케이션 시작 시 자동 동기화하지 않고 관리자 수정 API도 제공하지 않는다.

## 5. 데이터 모델

### users

- GitHub의 불변 사용자 ID를 `github_id`로 저장하고 유니크 제약을 둔다.
- 로그인명과 표시명은 변경 가능한 프로필 값으로 취급한다.
- 최소 권한 구분을 위한 `role`을 저장한다.

### problems와 problem_versions

- `problems`: `slug`, 제목, 설명, `stage`, `display_order`, 스타터 저장소 URL, 활성 여부
- `problem_versions`: 문제 버전, Java 버전, 테스트 번들 참조, 설정 checksum, 게시 시각
- 제출은 항상 특정 `problem_version`을 참조한다.

### submissions

- 사용자, 문제 버전, 저장소 URL, 제출 시 확정한 commit SHA를 저장한다.
- 상태는 `QUEUED`, `RUNNING`, `PASSED`, `FAILED`, `ERROR` 문자열 enum이다.
- 제출 및 완료 시각을 UTC로 저장한다.

`FAILED`는 컴파일, 테스트, 프로젝트 구조, 자원 제한 등 사용자 제출 원인의 최종 결과다. `ERROR`는 GitHub, Docker, 워커 장애 등 플랫폼 원인의 최종 결과다.

### grading_jobs

- 제출 하나당 작업 하나만 존재하도록 `submission_id`에 유니크 제약을 둔다.
- 상태, 실행 가능 시각, 선점 시각, worker ID, 시도 횟수, 제한된 마지막 오류를 저장한다.
- 워커는 `FOR UPDATE SKIP LOCKED`로 작업을 선점하고 lease를 기록한다.

### test_results

- 제출 ID, 안정적인 테스트 식별자, 표시명, 성공 여부, 실행시간, 제한된 실패 요약을 저장한다.
- `(submission_id, test_identifier)`에 유니크 제약을 둔다.
- 전체 빌드 로그, 내부 예외, 호스트 경로는 영구 결과나 사용자 응답에 포함하지 않는다.

## 6. 인덱스

실제 조회 패턴에 맞춰 다음 인덱스를 둔다.

| 테이블 | 인덱스 또는 제약 | 목적 |
| --- | --- | --- |
| `users` | `UNIQUE(github_id)` | OAuth 사용자 멱등 생성 |
| `problems` | `UNIQUE(slug)` | 안정적인 문제 식별 |
| `problems` | `(active, stage, display_order, id)` | 활성 문제의 차수별 정렬 조회 |
| `problem_versions` | `UNIQUE(problem_id, version)` | 문제 버전 고정 |
| `submissions` | `(user_id, submitted_at DESC, id DESC)` | 내 제출 이력 커서 조회 |
| `submissions` | `(user_id, problem_version_id, submitted_at DESC)` | 문제별 최근 결과와 제출 간격 확인 |
| `grading_jobs` | `UNIQUE(submission_id)` | 중복 채점 작업 방지 |
| `grading_jobs` | `(status, available_at, id)` | 대기 작업 선점 |
| `test_results` | `UNIQUE(submission_id, test_identifier)` | 테스트 결과 중복 방지 |

인덱스의 실제 사용 여부는 MySQL 실행 계획으로 검증한다. H2 실행 계획을 MySQL 인덱스 검증 근거로 사용하지 않는다.

## 7. 제출과 채점 흐름

1. 로그인 사용자가 문제에 공개 GitHub 저장소 URL을 제출한다.
2. API는 URL, 공개 접근 가능 여부, 로그인 사용자 소유권, 공식 스타터 저장소의 fork 관계를 검증한다.
3. API는 GitHub 기본 브랜치의 HEAD commit SHA를 조회해 고정한다.
4. 사용자에게 실행 중인 제출이 없어야 하며 같은 문제의 직전 제출 후 30초가 지나야 한다.
5. API는 `submission`과 `grading_job`을 하나의 트랜잭션으로 생성한다.
6. 워커는 실행 가능한 작업을 행 잠금과 `SKIP LOCKED`로 선점하고 제출을 `RUNNING`으로 바꾼다.
7. 워커는 저장소를 임시 공간에 얕게 clone하고 확정된 SHA를 checkout한다.
8. 서버가 보관한 해당 문제 버전의 공개 테스트를 실행 시점에 주입한다.
9. 일회성 Docker 샌드박스에서 고정 Gradle 명령을 실행한다.
10. 테스트별 결과와 최종 상태를 저장하고 컨테이너 및 임시 작업공간을 폐기한다.

프로젝트 구조를 판별하는 별도 형식 검사기는 만들지 않는다. Gradle wrapper 누락, 컴파일 오류, 잘못된 빌드 설정, 테스트 주입이 불가능한 구조는 `FAILED`로 처리한다. 저장소 계약 검증과 보안 검증은 형식 검사와 별개로 반드시 수행한다.

## 8. 재시도와 장애 복구

GitHub의 일시적인 5xx 또는 rate limit, Docker 데몬의 일시적인 실패, 워커 종료와 lease 만료는 플랫폼 오류로 재시도한다. 최대 3회까지 지수형 지연을 적용하고 이후 `ERROR`로 종료한다.

컴파일 실패, 테스트 실패, 프로젝트 구조 오류, CPU·메모리·실행시간 초과는 재시도하지 않는다. lease가 만료된 `RUNNING` 작업은 다른 워커가 다시 선점할 수 있어야 하며, 상태 전이는 중복 실행에도 결과가 깨지지 않도록 멱등성을 보장한다.

## 9. 샌드박스 보안

채점 컨테이너는 비신뢰 코드를 실행한다. 다음 제한을 기본 적용한다.

- 비특권 사용자와 모든 불필요한 Linux capability 제거
- 읽기 전용 루트 파일시스템과 제한된 임시 쓰기 볼륨
- 외부 네트워크 차단
- CPU, 메모리, 프로세스 수, 실행시간, 출력 크기 제한
- 저장소 최대 크기와 clone 시간 제한
- 경로 이탈 및 심볼릭 링크를 이용한 테스트 주입 경로 공격 방지
- 호스트의 민감한 디렉터리와 Docker 제어 권한을 채점 컨테이너에 노출하지 않음

의존성은 검증된 채점 이미지 또는 통제된 Gradle 캐시로 제공한다. 채점 중 임의의 외부 의존성을 내려받지 않는다. 단일 서버 MVP에서도 API 컨테이너는 Docker 실행 권한을 갖지 않으며, 실행 책임은 워커에만 둔다.

## 10. REST API와 최소 화면

### API

- `GET /api/problems?stage=ROUND_1`
- `GET /api/problems/{slug}`
- `POST /api/problems/{slug}/submissions`
- `GET /api/submissions/{id}`
- `GET /api/me/submissions?cursor=...`
- `GET /api/me/progress`
- `/oauth2/authorization/github`

요청 및 응답 DTO는 presentation 계층에만 둔다. 애플리케이션 서비스는 Command, Query, Result 모델을 사용해 HTTP와 뷰에 의존하지 않는다. 제출 상태 조회 API는 소유자만 접근할 수 있다.

### 최소 화면

- 차수별 문제 목록과 개인 진행 상태
- Markdown 문제 상세와 스타터 저장소 링크
- 저장소 URL 제출 폼
- 2초 폴링 기반 채점 상태와 테스트별 결과
- 개인 제출 이력

별도 SPA나 관리자 UI는 MVP에 포함하지 않는다.

## 11. 오류 응답

REST 오류는 `code`, `message`, `fieldErrors`, `traceId`를 갖는 공통 형식을 사용한다. 오류 코드는 클라이언트가 분기할 수 있는 안정적인 문자열로 관리한다. 내부 예외, Docker 명령, 전체 Gradle 로그, 호스트 경로는 노출하지 않는다.

저장소 계약 오류와 제출 제한은 요청 단계에서 4xx로 반환한다. 접수 후 사용자 코드에서 발생한 오류는 제출 상태 `FAILED`와 제한된 실패 요약으로 제공한다. 플랫폼 장애는 재시도 중에는 진행 상태로 유지하고 최종 실패 시 `ERROR`로 제공한다.

## 12. 코드 컨벤션

- Java 21과 Gradle Groovy DSL을 사용한다.
- 기능별 `user`, `problem`, `submission`, `grading` 패키지를 우선하고 내부에서 계층 경계를 지킨다.
- 서비스 클래스에는 `@Transactional(readOnly = true)`를 기본 적용하고 쓰기 메서드에만 `@Transactional`을 지정한다.
- 생성자 주입만 사용한다.
- Lombok은 `@Getter`, `@RequiredArgsConstructor`, `@NoArgsConstructor(access = PROTECTED)` 중심으로 제한한다.
- `@Data`, 엔티티 공개 setter, 무분별한 builder를 사용하지 않는다.
- 엔티티 변경은 `claim()`, `complete()`, `fail()`처럼 의도가 드러나는 메서드로 수행한다.
- 컨트롤러는 요청 검증과 변환만 담당하고 비즈니스 규칙을 두지 않는다.
- 단순 규칙은 서비스나 도메인 객체에 명확히 표현한다. 분기가 커지거나 구현 교체가 실제로 필요할 때만 Policy로 추출한다.
- 식별자는 영어를 사용하고 테스트 시나리오는 한국어 `@DisplayName`을 허용한다.
- 시간은 UTC로 저장하고 API는 ISO-8601로 반환한다.
- enum은 문자열로 저장하고 스키마 변경은 Flyway로만 수행한다.
- Spotless와 Checkstyle로 우아한테크코스 스타일의 포맷과 금지 규칙을 자동 검사한다.

## 13. 테스트 전략

TDD의 바깥 루프는 RestAssured 기반 인수 테스트다. 인수 테스트는 REST 요청부터 응답과 서버 DB 상태까지 사용자 시나리오를 검증하며 H2를 사용한다.

- 컨트롤러 단위 테스트는 작성하지 않는다.
- 서비스는 통합 테스트를 우선한다.
- 외부 API와 Docker처럼 느리거나 제어하기 어려운 의존성만 대역으로 교체한다.
- 단순 CRUD와 Spring Data 파생 쿼리는 별도 리포지토리 테스트를 작성하지 않는다.
- 복합 쿼리, 잠금, 커서 페이지처럼 특수성이 있는 저장소 동작만 테스트한다.
- 작업 선점과 `SKIP LOCKED` 동시성은 H2가 아닌 Testcontainers MySQL로 검증한다.
- 내부 로직의 복잡성이 생길 때만 도메인 또는 서비스 단위 테스트를 인수 테스트 안쪽 루프로 추가한다.

사용자 문제 채점 테스트는 플랫폼 서버 테스트와 완전히 분리한다. 채점 컨테이너에서는 서버가 주입한 JUnit 공개 테스트를 실행하며 H2나 플랫폼 DB를 사용하지 않는다.

## 14. 완료 기준

- GitHub 로그인 사용자가 차수별 문제를 조회할 수 있다.
- 공식 스타터 저장소를 fork한 본인 소유 공개 저장소 URL을 제출할 수 있다.
- 제출 시점의 commit SHA가 저장되고 동일 SHA로 재현 가능하다.
- 사용자당 동시 채점은 1건이고 같은 문제의 제출 간격은 30초다.
- 워커가 MySQL 큐에서 작업을 선점하고 격리된 Docker 컨테이너에서 테스트를 실행한다.
- 서버 보관 공개 테스트가 실행 시 주입되며 사용자 저장소의 테스트 변경이 채점에 영향을 주지 않는다.
- 사용자는 대기, 실행, 통과, 실패, 플랫폼 오류 상태와 테스트별 결과를 확인할 수 있다.
- API와 워커를 별도로 실행하고 워커 인스턴스를 추가해 처리량을 확장할 수 있다.
- RestAssured 인수 테스트와 MySQL 작업 선점 동시성 테스트가 통과한다.

## 15. 후속 범위

실제 사용량과 실패 패턴을 확인한 뒤 다음 기능을 별도 설계한다.

- AI 코드 피드백과 비용·개인정보 정책
- 비공개 GitHub 저장소와 GitHub App
- 그룹, 기수, 리더보드와 점수
- Redis 또는 전용 메시지 브로커 전환
- 별도 채점 워커 서버 또는 실행 클러스터

## 16. 아키텍처 의사결정 기록

중요한 기술 선택은 `docs/adr/`에 ADR로 기록한다. ADR은 상태, 배경, 사용자 가치, 결정, 선택 이유, 대안, 결과를 포함한다. 이미 승인된 ADR의 방향을 바꿀 때는 기존 문서를 덮어쓰지 않고 새 ADR을 작성해 이전 ADR을 `Superseded`로 연결한다.

다음 변화는 ADR 작성 대상이다.

- 서비스 또는 배포 경계의 변경
- 데이터베이스, 큐, 인증, 채점 격리 기술의 도입이나 교체
- 데이터 정합성, 보안, 확장성에 영향을 주는 결정
- 테스트 전략과 운영 장애 대응 방식의 중요한 변경

단순 라이브러리 패치, 코드 포맷, 지역적인 리팩터링은 ADR 대상이 아니다. 초기 결정은 `docs/adr/README.md`의 색인과 ADR 0001~0006에 기록한다.
