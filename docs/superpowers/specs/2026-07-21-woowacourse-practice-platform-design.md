# 우아한테크코스 프리코스 연습 플랫폼 설계

## 1. 목적

여러 사용자가 우아한테크코스 프리코스 형식의 문제를 연습하는 웹 서비스를 만든다. 사용자는 GitHub로 로그인하고 공식 스타터 저장소를 fork한 공개 저장소 URL을 제출한다. 서버는 제출 시점의 commit SHA를 고정하고, 격리된 Docker 환경에서 서버가 관리하는 공개 테스트를 주입해 채점한다.

MVP는 개인 학습 흐름과 안정적인 채점에 집중한다. 점수, 사용자 비교, 리더보드, 그룹, 기수 구분, 비공개 저장소, AI 피드백은 제외한다. 향후 AI 피드백과 워커 확장을 추가할 수 있도록 API와 채점 실행의 경계를 분리한다.

## 2. 기술 및 배포 기준

- Java 21, Spring Boot, Gradle Groovy DSL
- Spring Data JPA, MySQL 8, Flyway
- GitHub OAuth 전용 로그인
- 단일 Linux 서버의 Docker Compose 배포
- API와 채점 워커를 같은 Spring Boot 프로젝트에서 별도 프로세스 및 컨테이너로 실행
- Redis나 메시지 브로커 없이 MySQL 작업 테이블을 비동기 큐로 사용
- 단일 서버에서 동시 채점 10건을 지원하고 워커만 수평 확장 가능

## 3. 시스템 구조

단일 Gradle Spring Boot 프로젝트를 사용한다. 기능별 패키지는 `domain`, `application`, `infrastructure`, `presentation`, `grading`, `sync`로 나누되 Gradle 모듈로 분리하지 않는다. API와 워커는 같은 코드베이스를 공유하고 실행 프로필 또는 별도 main class로 구분한다. API는 Docker를 직접 실행하지 않고 MySQL에 제출과 채점 작업을 같은 트랜잭션으로 기록한다.

## 4. 문제 정의와 분류

문제는 기수에 귀속하지 않고 차수만 표시한다.

- `ROUND_1`: 1차
- `ROUND_2`: 2차
- `ROUND_3`: 3차
- `ROUND_4`: 4차
- `ROUND_5`: 5차
- `FINAL`: 최종 테스트

같은 차수에 여러 문제가 생길 수 있으므로 `display_order`를 둔다. 문제 설명과 공식 `ApplicationTest.java` 소스는 관리자 등록 API와 관리자 페이지를 통해 DB에 저장한다. YAML, README, 별도 문제 저장소는 사용하지 않는다. 등록 시 테스트 소스의 `ApplicationTest`·`NsTest`·`runMain` 계약과 크기를 검증하고 SHA-256 checksum을 생성한다. 공개된 문제 버전은 수정하지 않고 새 버전을 등록한다.

제출 저장소와 공식 테스트 번들은 하나의 고정된 프리코스 실행 환경을 따른다. 제출 프로젝트는 `src/main/java/Application.java`의 `Application.main(String[])` 진입점과 `src/test/java/ApplicationTest.java` 테스트 위치를 사용한다. 공식 테스트는 `NsTest`를 상속하고 `run(...)`, `output()`, `runException(...)`, `assertRandomNumberInRangeTest(...)`로 입력·출력·예외·랜덤 동작을 검증한다. 워커는 공식 테스트를 주입한 뒤 `./gradlew test --tests ApplicationTest`를 실행한다. 문제별 차이는 Gradle 모듈이나 실행 스크립트가 아니라 공식 `ApplicationTest`의 입력, 기대 출력, 예외 조건이다.

## 5. 데이터 모델

### users

- GitHub의 불변 사용자 ID를 `github_id`로 저장하고 유니크 제약을 둔다.
- 로그인명과 표시명은 변경 가능한 프로필 값으로 취급한다.
- 최소 권한 구분을 위한 `role`을 저장한다.

### problems와 problem_versions

- `problems`: `slug`, 제목, 설명, `stage`, `display_order`, 스타터 저장소 URL, 활성 여부
- `problem_versions`: 문제 버전, Java 버전, 공식 `ApplicationTest.java` 소스, 테스트 checksum, 게시 시각
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
8. DB에 저장된 해당 문제 버전의 공식 `ApplicationTest`와 `NsTest` 지원 코드를 제출 프로젝트에 주입한다.
9. 일회성 Docker 샌드박스에서 `./gradlew test --tests ApplicationTest`를 실행한다.
10. 테스트별 결과와 최종 상태를 저장하고 컨테이너 및 임시 작업공간을 폐기한다.

프로젝트 구조를 판별하는 별도 형식 검사기는 만들지 않는다. Gradle wrapper 누락, 컴파일 오류, 잘못된 빌드 설정, 테스트 주입이 불가능한 구조는 `FAILED`로 처리한다. 저장소 계약 검증과 보안 검증은 형식 검사와 별개로 반드시 수행한다.

## 8. 재시도와 장애 복구

GitHub의 일시적인 5xx 또는 rate limit, Docker 데몬의 일시적인 실패, 워커 종료와 lease 만료는 플랫폼 오류로 재시도한다. 최대 3회까지 지수형 지연을 적용하고 이후 `ERROR`로 종료한다.

컴파일 실패, 테스트 실패, 프로젝트 구조 오류, CPU·메모리·실행시간 초과는 재시도하지 않는다. lease가 만료된 `RUNNING` 작업은 다른 워커가 다시 선점할 수 있어야 하며, 상태 전이는 중복 실행에도 결과가 깨지지 않도록 멱등성을 보장한다.

## 9. 동시 채점 용량

MVP의 용량 목표는 한 서버에서 채점 컨테이너 10개를 동시에 실행하는 것이다. 워커는 설정 가능한 실행 슬롯을 가지며 기본값을 10으로 둔다. 작업은 빈 슬롯이 생긴 뒤에만 MySQL에서 선점하므로, 실행 한도를 넘는 제출은 컨테이너를 추가로 만들지 않고 `QUEUED` 상태로 대기한다.

초기 서버 권장 기준은 12 vCPU, RAM 16GB 이상의 Linux 서버와 충분한 SSD 공간이다. 채점 컨테이너 하나에는 CPU 1코어, 메모리 1GB, 전체 실행시간 120초를 기본 한도로 부여한다. API와 MySQL을 위해 최소 2 vCPU와 4GB 메모리를 예약하고 채점 프로세스가 이 예약분을 사용하지 못하도록 제한한다. 실제 문제의 컴파일 및 테스트 특성에 따라 측정 후 한도를 조정한다.

부하 검증은 서로 다른 사용자 10명이 동시에 제출한 상황을 재현한다. 정상 자원 상태에서 10건이 모두 실행 슬롯을 획득하고, 채점 부하 중에도 문제 목록과 제출 상태 조회 API의 p95 응답시간이 500ms 이내인지 확인한다. 자원 부족이나 11번째 이상의 제출은 실패시키지 않고 큐에서 대기시킨다.

여러 워커 서버로 확장할 때는 워커별 슬롯 수의 합을 전체 동시 실행량으로 관리한다. 단일 서버의 자원 한도를 넘겨 워커 프로세스만 복제하지 않는다.

## 10. 샌드박스 보안

채점 컨테이너는 비신뢰 코드를 실행한다. 다음 제한을 기본 적용한다.

- 비특권 사용자와 모든 불필요한 Linux capability 제거
- 읽기 전용 루트 파일시스템과 제한된 임시 쓰기 볼륨
- 외부 네트워크 차단
- CPU, 메모리, 프로세스 수, 실행시간, 출력 크기 제한
- 저장소 최대 크기와 clone 시간 제한
- 경로 이탈 및 심볼릭 링크를 이용한 테스트 주입 경로 공격 방지
- 호스트의 민감한 디렉터리와 Docker 제어 권한을 채점 컨테이너에 노출하지 않음

의존성은 검증된 채점 이미지 또는 통제된 Gradle 캐시로 제공한다. 채점 중 임의의 외부 의존성을 내려받지 않는다. 단일 서버 MVP에서도 API 컨테이너는 Docker 실행 권한을 갖지 않으며, 실행 책임은 워커에만 둔다.

## 11. REST API와 최소 화면

### API

- `GET /api/problems?stage=ROUND_1`
- `GET /api/problems/{slug}`
- `POST /api/problems/{slug}/submissions`
- `GET /api/submissions/{id}`
- `GET /api/me/submissions?cursor=...`
- `GET /api/me/progress`
- `/oauth2/authorization/github`
- `GET /v3/api-docs`: OpenAPI 3.1 JSON 명세
- `GET /swagger-ui/index.html`: Swagger UI

요청 및 응답 DTO는 presentation 계층에만 둔다. 애플리케이션 서비스는 Command, Query, Result 모델을 사용해 HTTP와 뷰에 의존하지 않는다. 제출 상태 조회 API는 소유자만 접근할 수 있다.

springdoc-openapi로 REST 명세를 애플리케이션에서 생성한다. 각 API는 요약, 요청 조건, 성공 응답, 공통 오류 응답, enum 허용 값을 문서화한다. Swagger UI에서 GitHub OAuth가 필요한 API의 인증 요구사항을 확인할 수 있게 하고, 운영 환경에서도 문서와 UI를 제공하되 채점 내부 API나 관리용 엔드포인트는 문서에 노출하지 않는다.

### 최소 화면

- 차수별 문제 목록과 개인 진행 상태
- Markdown 문제 상세와 스타터 저장소 링크
- 저장소 URL 제출 폼
- 2초 폴링 기반 채점 상태와 테스트별 결과
- 개인 제출 이력

별도 SPA나 관리자 UI는 MVP에 포함하지 않는다.

## 12. 오류 응답

REST 오류는 `code`, `message`, `fieldErrors`, `traceId`를 갖는 공통 형식을 사용한다. 오류 코드는 클라이언트가 분기할 수 있는 안정적인 문자열로 관리한다. 내부 예외, Docker 명령, 전체 Gradle 로그, 호스트 경로는 노출하지 않는다.

저장소 계약 오류와 제출 제한은 요청 단계에서 4xx로 반환한다. 접수 후 사용자 코드에서 발생한 오류는 제출 상태 `FAILED`와 제한된 실패 요약으로 제공한다. 플랫폼 장애는 재시도 중에는 진행 상태로 유지하고 최종 실패 시 `ERROR`로 제공한다.

## 13. 코드 컨벤션

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

## 14. 테스트 전략

TDD의 바깥 루프는 RestAssured 기반 인수 테스트다. 인수 테스트는 REST 요청부터 응답과 서버 DB 상태까지 사용자 시나리오를 검증하며 H2를 사용한다.

- 컨트롤러 단위 테스트는 작성하지 않는다.
- 서비스는 통합 테스트를 우선한다.
- 외부 API와 Docker처럼 느리거나 제어하기 어려운 의존성만 대역으로 교체한다.
- 단순 CRUD와 Spring Data 파생 쿼리는 별도 리포지토리 테스트를 작성하지 않는다.
- 복합 쿼리, 잠금, 커서 페이지처럼 특수성이 있는 저장소 동작만 테스트한다.
- 작업 선점과 `SKIP LOCKED` 동시성은 H2가 아닌 Testcontainers MySQL로 검증한다.
- 내부 로직의 복잡성이 생길 때만 도메인 또는 서비스 단위 테스트를 인수 테스트 안쪽 루프로 추가한다.

사용자 문제 채점 테스트는 플랫폼 서버 테스트와 완전히 분리한다. 채점 컨테이너에서는 서버가 보관한 공식 `ApplicationTest` 번들을 실행하며 H2나 플랫폼 DB를 사용하지 않는다. 사용자 fork의 테스트는 로컬 피드백용으로 선택적으로 실행할 수 있지만 최종 판정에는 사용하지 않는다.

## 15. 완료 기준

- GitHub 로그인 사용자가 차수별 문제를 조회할 수 있다.
- 공식 스타터 저장소를 fork한 본인 소유 공개 저장소 URL을 제출할 수 있다.
- 제출 시점의 commit SHA가 저장되고 동일 SHA로 재현 가능하다.
- 사용자당 동시 채점은 1건이고 같은 문제의 제출 간격은 30초다.
- 워커가 MySQL 큐에서 작업을 선점하고 격리된 Docker 컨테이너에서 테스트를 실행한다.
- 단일 서버에서 10건을 동시에 채점하며 초과 제출은 `QUEUED` 상태로 대기한다.
- 동시 채점 10건 중에도 문제 및 제출 상태 조회 API의 p95 응답시간이 500ms 이내다.
- 서버 보관 공식 `ApplicationTest`가 실행 시 주입되며 사용자 저장소의 테스트 변경이 채점에 영향을 주지 않는다.
- 사용자는 대기, 실행, 통과, 실패, 플랫폼 오류 상태와 테스트별 결과를 확인할 수 있다.
- API와 워커를 별도로 실행하고 워커 인스턴스를 추가해 처리량을 확장할 수 있다.
- RestAssured 인수 테스트와 MySQL 작업 선점 동시성 테스트가 통과한다.
- `/v3/api-docs`와 Swagger UI가 제공되고 공개 REST API 및 공통 오류 응답이 명세에 포함된다.

## 16. 후속 범위

실제 사용량과 실패 패턴을 확인한 뒤 다음 기능을 별도 설계한다.

- AI 코드 피드백과 비용·개인정보 정책
- 비공개 GitHub 저장소와 GitHub App
- 그룹, 기수, 리더보드와 점수
- Redis 또는 전용 메시지 브로커 전환
- 별도 채점 워커 서버 또는 실행 클러스터

## 17. 아키텍처 의사결정 기록

중요한 기술 선택은 `docs/adr/`에 ADR로 기록한다. ADR은 상태, 배경, 사용자 가치, 결정, 선택 이유, 대안, 결과를 포함한다. 이미 승인된 ADR의 방향을 바꿀 때는 기존 문서를 덮어쓰지 않고 새 ADR을 작성해 이전 ADR을 `Superseded`로 연결한다.

다음 변화는 ADR 작성 대상이다.

- 서비스 또는 배포 경계의 변경
- 데이터베이스, 큐, 인증, 채점 격리 기술의 도입이나 교체
- 데이터 정합성, 보안, 확장성에 영향을 주는 결정
- 테스트 전략과 운영 장애 대응 방식의 중요한 변경

단순 라이브러리 패치, 코드 포맷, 지역적인 리팩터링은 ADR 대상이 아니다. 초기 결정은 `docs/adr/README.md`의 색인과 ADR 0001~0009에 기록한다.
