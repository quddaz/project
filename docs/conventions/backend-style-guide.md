# Backend Style Guide

이 문서는 우테코 프리코스 연습 플랫폼의 Java 백엔드 코드 규칙이다. CrewWiki Backend Style Guide를 기반으로 하되, 승인된 아키텍처와 테스트 전략에 맞게 조정한다.

## 1. 아키텍처와 패키지

- Gradle 모듈 경계는 `domain`, `application`, `infrastructure`, `api`, `grading`, `worker`, `problem-sync`를 사용한다.
- 기능은 `user`, `problem`, `submission`, `grading` 단위로 나눈다.
- 모듈 안에서는 역할에 따라 `domain`, `application`, `infrastructure`, `presentation` 패키지를 사용한다.
- `common`에는 특정 기능의 규칙을 두지 않는다. 공통 오류 응답, 설정, 로깅처럼 둘 이상의 기능에서 실제로 공유되는 코드만 둔다.
- 엔티티는 컨트롤러에서 반환하거나 presentation 계층에 노출하지 않는다.

## 2. 테스트

- TDD의 바깥 루프는 H2를 사용하는 RestAssured 인수 테스트다.
- 컨트롤러 단위 테스트는 작성하지 않는다.
- 서비스 테스트는 통합 테스트를 우선하며 Tomcat을 시작하지 않는다.
- 외부 API와 Docker처럼 느리거나 제어하기 어려운 의존성만 대역으로 교체한다.
- 단순 CRUD와 Spring Data 파생 쿼리는 별도 리포지토리 테스트를 작성하지 않는다.
- 복합 쿼리, 잠금, 커서 페이지처럼 특수성이 있는 저장소 동작만 테스트한다.
- MySQL 전용 잠금과 `SKIP LOCKED`는 Testcontainers MySQL로 검증한다.
- `@Nested` 클래스 이름은 대상 프로덕션 메서드의 UpperCamelCase 이름을 사용한다.
- 테스트 메서드는 `productionMethod_success_condition` 또는 `productionMethod_fail_condition` 형식을 사용한다.
- 테스트 본문은 필요한 구간에 `// given`, `// when`, `// then` 주석을 사용한다.
- 여러 값을 검증할 때는 AssertJ `assertSoftly`를 사용한다.
- 행위 호출 횟수보다 상태 검증을 우선한다.
- 테스트 데이터는 Fixture의 정적 팩토리 메서드로 만든다.

## 3. 이름과 DTO

- 메서드 이름은 동사로 시작하고 불필요하게 엔티티 이름을 반복하지 않는다.
- 경로 변수는 `id` 대신 `problemId`, `submissionId`처럼 대상을 명시한다.
- 요청과 응답 타입은 `Request`, `Response` 접미사를 사용한다.
- 변경 요청에는 `RegisterRequest`, `UpdateRequest` 같은 구체적인 이름을 사용할 수 있다.
- API 엔드포인트마다 전용 DTO를 둔다. 관리자와 일반 사용자 DTO를 공유하지 않는다.
- DTO는 기본적으로 `record`를 사용한다. 프레임워크 바인딩 제약이 있을 때만 클래스를 사용한다.
- 애플리케이션 서비스는 presentation DTO 대신 Command, Query, Result 모델을 사용한다.

## 4. 객체와 메서드

- 메서드 안의 들여쓰기 단계는 하나를 목표로 한다.
- `else` 대신 조기 반환을 사용한다.
- 약어를 피하고 역할이 분명한 이름을 사용한다.
- 클래스는 하나의 책임에 집중하고 인스턴스 변수를 최소화한다.
- 컬렉션 자체에 규칙이 생길 때만 일급 컬렉션으로 분리한다.
- 의미 있는 도메인 불변식이나 행위를 보호해야 할 때만 원시값을 값 객체로 감싼다.
- 값 객체 도입은 코드 리뷰에서 필요성과 경계를 확인한다.
- 무리한 한 줄 체이닝을 피하고, 객체 그래프를 연속으로 탐색하는 코드는 의도를 드러내는 메서드로 옮긴다.
- 상태 검증은 도메인 계층, 행동·권한 검증은 애플리케이션 서비스 계층에 둔다.
- 단순 규칙은 도메인이나 서비스에 명확히 표현하고, 분기나 교체 가능성이 실제로 커질 때만 Policy로 추출한다.

## 5. Lombok과 엔티티

- 생성자 주입과 `final` 필드를 사용한다.
- Lombok은 `@Getter`, `@RequiredArgsConstructor`, `@NoArgsConstructor(access = PROTECTED)` 중심으로 제한한다.
- `@Data`, 엔티티 공개 setter, 무분별한 builder를 사용하지 않는다.
- 엔티티는 보호된 기본 생성자와 의도를 드러내는 정적 팩토리를 제공한다.
- 엔티티 상태는 `claim()`, `complete()`, `fail()`처럼 의미 있는 메서드로 변경한다.
- 필요한 getter는 클래스 하단에 둔다.

## 6. 예외와 API 오류

- `Exception`이나 일반 `RuntimeException`을 직접 던지지 않는다.
- 오류 코드를 가진 프로젝트 예외 타입을 사용한다.
- 모든 REST 오류 응답은 최소한 `code`와 `message`를 포함한다.
- 내부 예외, 호스트 경로, Docker 명령, 전체 Gradle 로그를 API에 노출하지 않는다.

## 7. 형식

- 어노테이션은 로깅, Lombok, Spring meta, Spring component 순서로 둔다.
- 클래스 선언 전에는 빈 줄 하나를 둔다.
- 메서드 매개변수가 둘 이상이면 각 매개변수를 새 줄에 둔다.
- wildcard import를 사용하지 않고 IntelliJ IDEA 기본 import 순서를 따른다.
- 상수는 `UPPER_SNAKE_CASE`로 작성한다.
- 멤버는 `public`, `protected`, `private` 순으로 배치한다.
- 식별자와 테스트 메서드 이름은 영어로 작성한다. 사용자 메시지, 테스트 `@DisplayName`, 필요한 코드 주석은 한국어로 작성한다.
- 설명이 필요 없는 코드는 주석을 추가하지 않는다.
- 코드 포맷은 Spotless, 구조 규칙은 Checkstyle로 검증한다.

## 8. 프로젝트 우선 규칙

- 이 프로젝트의 승인된 설계와 ADR이 원본 CrewWiki 가이드보다 우선한다.
- CrewWiki 전용 공통 DTO 이름은 해당 기능이 실제로 도입될 때만 사용한다.
- 불확실성을 `TODO`로 남기지 않는다. 구현을 막는 모호성은 작업 전에 질문하고, 막지 않는 선택은 설계와 기존 코드에 맞춰 결정한다.
- 새 의존성과 `build.gradle` 변경은 승인된 설계 또는 구현 계획에 명시된 경우에만 허용한다.
