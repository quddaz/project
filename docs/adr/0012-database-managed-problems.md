# ADR 0012: DB 기반 문제와 공식 테스트 관리

- 상태: Accepted
- 날짜: 2026-07-22
- 대체: ADR 0006의 Git 기반 문제 정의 관리

## 결정

문제 메타데이터, 설명, 버전별 공식 `ApplicationTest.java` 소스를 MySQL에 저장한다. 관리자는 `/admin/problems` 페이지 또는 관리자 등록 API로 문제를 등록한다. YAML, README, Git 문제 동기화 CLI는 사용하지 않는다.

## 등록 검증

등록 요청은 slug·차수·버전·Java 버전·저장소 URL을 검증하고, 테스트 소스가 `ApplicationTest`, `extends NsTest`, `runMain` 계약을 포함하는지와 최대 크기를 확인한다. 통과한 소스의 SHA-256 checksum을 생성해 버전과 함께 보관한다. 실제 정답 프로젝트에서의 컴파일·실행 검증은 채점 워커의 공식 테스트 번들 검증 단계에서 수행한다.

## 채점 시 사용

워커는 문제 버전의 `application_test_source`를 임시 작업 디렉터리의 `src/test/java/ApplicationTest.java`로 복원하고 고정된 Docker 환경에서 실행한다. 소스는 사용자 제출 소스가 아니므로 관리자 권한으로만 변경할 수 있다.

## 결과

문제 추가는 관리자 작업이지만 DB 등록 API로 자동화할 수 있다. 공개 버전은 불변으로 취급하고 테스트를 바꾸려면 새 버전을 만든다. 문제 설명과 테스트 기준을 애플리케이션과 함께 배포할 수 있어 별도 Git 동기화 프로세스가 사라진다.
