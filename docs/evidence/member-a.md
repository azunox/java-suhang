# 팀원 A 개인 보고서 근거 — 박승아

## 2.2. 개인별 GitHub 커밋 이력

수행평가 기간의 개인별 유효 커밋을 각 주차에서 최소 2건, 총 4건 이상 제시한다. 실제 개발 커밋의 날짜와 메시지 원문을 아래에 기록한다. 2026-11-19 원격 main을 가져와 기존 세 개발 커밋이 포함된 것을 확인했다. 추가 구현과 DB 검증은 2026-11-19 로컬 커밋으로 기록했다. 아직 push 전이므로 GitHub 링크와 구분한다.

| 주차 | 커밋 날짜 | 커밋 메시지(원문) | GitHub 커밋 링크 |
| --- | --- | --- | --- |
| 1주차 (11/9 ~ 11/13) | 2026-11-10 | feat : 업무 등록과 프로젝트별 조회 API 구현 | [7eb8ef1](https://github.com/azunox/java-suhang/commit/7eb8ef1dc0f3c80280178db3c5debf53695e9a10) |
| 1주차 (11/9 ~ 11/13) | 2026-11-10 | feat : 업무 수정과 완료 상태 및 삭제 처리 구현 | [d7c3fe7](https://github.com/azunox/java-suhang/commit/d7c3fe7c80350d859f43b0d74d36b599a9366029) |
| 1주차 (11/9 ~ 11/13) | 2026-11-10 | feat : 업무 담당자 상태 마감일 조건 검색 구현 | [8df0dfa](https://github.com/azunox/java-suhang/commit/8df0dfa285a657593a0dbd7b376cbf3a4b173a0c) |
| 2주차 (11/16 ~ 11/20) | 2026-11-19 | feat : 업무 Markdown HTML 응답과 통합 검증 추가 | 로컬 커밋 `f50bf8c` (push 전) |
| 2주차 (11/16 ~ 11/20) | 2026-11-19 | chore : 업무 검색 DB 검증과 마감일 경계 SQL 보강 | 로컬 커밋 `2ea2f4b` (push 전) |

전체 SHA:

- 등록·조회: `7eb8ef1dc0f3c80280178db3c5debf53695e9a10`
- 수정·삭제: `d7c3fe7c80350d859f43b0d74d36b599a9366029`
- 조건 검색: `8df0dfa285a657593a0dbd7b376cbf3a4b173a0c`
- HTML 응답·통합 검증: `f50bf8ce8e727ce9627519c9e06286d48ae0e704`
- 검색 DB 검증·SQL: `2ea2f4b3f5834b3069f655985b061725a60bd61e`

업무 API 명세·검증 근거를 정리하는 문서 커밋은 위 개발 커밋과 별도로 관리한다. 처음 세 개발 커밋은 1주차 작업이다. 2026-11-19의 두 추가 커밋은 서로 다른 목적의 실제 구현·검증 작업이다. 원격 제출 근거는 push 후 실제 GitHub 링크로 확인한다. 날짜를 바꾸거나 미래 작업을 완료 이력으로 작성하지 않는다.

## 구현 범위와 검증

2026-11-10 각 기능 범위를 분리한 상태에서 아래 명령을 실행했다.

```bash
./gradlew test --tests com.teamnote.task.entity.TaskTest --tests com.teamnote.task.service.TaskServiceImplTest --tests com.teamnote.task.controller.TaskControllerTest
```

| 코드 범위 | Controller | Entity | Service | 합계 | 결과 |
| --- | --- | --- | --- | --- | --- |
| 등록·목록·상세 | 14 | 3 | 7 | 24 | 통과 |
| 수정·삭제·완료 상태 처리까지 | 15 | 4 | 9 | 28 | 통과 |
| 조건 검색까지 포함한 최종 코드 | 19 | 4 | 10 | 33 | 통과 |

각 실행은 BUILD SUCCESSFUL이며 실패·오류·스킵은 0건이다. main·test Java 소스 컴파일도 성공했다. 위 합계는 각 시점의 전체 관련 테스트 수이며 서로 더해 고유 테스트 수로 계산하지 않는다.

- Entity: 고정 시각으로 완료 진입·유지·해제·재진입, Markdown 원문 보존, 잘못된 수정 시 기존 상태 보존 확인
- Service: 프로젝트 존재·업무 소속 검증, CRUD 처리, 검색 조건 전달·범위 오류 확인
- Controller: HTTP 상태·공통 JSON, 정상 요청·필수값·길이·형식·경로 ID 오류 확인

Service는 Mockito, Controller는 standalone MockMvc 검증이다. Repository 쿼리·실제 트랜잭션·서버 호출을 검증한 결과로 해석하지 않는다.

## 2026-11-19 추가 구현·검증 근거

이번 구현·DB 검증은 `feat/task-completion` 브랜치의 `f50bf8c`, `2ea2f4b`에 나누어 커밋했다. Postman·보고서 자료는 별도 문서 커밋으로 관리한다. 기반은 원격 main `b8940f7`이며 팀원 B의 회의 CRUD를 그대로 보존했다.

### 1. 업무 Markdown HTML 응답 연결

- 코드: [TaskServiceImpl](../../src/main/java/com/teamnote/task/service/TaskServiceImpl.java), [TaskResponse](../../src/main/java/com/teamnote/task/dto/TaskResponse.java)
- 흐름: 요청 DTO → Task Entity에 원문 저장 → Service에서 공통 MarkdownService 호출 → descriptionHtml을 포함한 응답 DTO.
- 판단: 원문을 변경하지 않고 출력 시 변환한다. 도메인별 Parser·정제 정책 복제를 피한다. 기존 공통 Bean을 재사용하므로 라이브러리 버전과 허용 목록을 바꾸지 않는다.
- 근거: API 통합 테스트에서 표·코드·정상 HTTPS 링크 보존, script 태그 이스케이프, javascript URL 제거, 빈 원문과 재조회 HTML을 확인했다. Newman 실호출에서도 같은 동작을 확인했다.
- 추가 기술: commonmark-java와 GFM Tables는 표·코드 변환, jsoup은 허용 목록에 따른 HTML 정제에 사용했다. 의존성 추가만 한 결과와 실제 업무 API 사용 결과를 구분한다.
- 기준 main에 공통 Service가 없어 선행 의존성으로 포함했고, PR 준비 시 팀장 PR #7의 인터페이스·구현체와 파일 내용을 동일하게 맞췄다. 이 부분을 A의 단독 도메인 기능으로 부풀리거나 팀장·B API까지 구현했다고 기록하지 않는다.

### 2. 검색 JPQL의 실제 DB 검증과 완료 상태 실호출

- 코드: [TaskRepositoryTest](../../src/test/java/com/teamnote/task/repository/TaskRepositoryTest.java), [TaskApiIntegrationTest](../../src/test/java/com/teamnote/task/integration/TaskApiIntegrationTest.java)
- 기존 JPQL을 변경하지 않고 실제 H2에서 실행해 담당자·상태·마감일 AND 검색, 양쪽/한쪽 날짜 경계, 날짜·ID 정렬, 소속 범위, 빈 결과를 확인했다.
- 고정 기준일 2026-11-19에 overdue 생략·true·false를 각각 실행했다. 오늘 마감과 지난 완료 업무는 overdue=true에서 제외한다.
- MockMvc는 실제 Service·Repository·공통 라이브러리를 연결한다. 별도 Newman 실행은 생성→완료→완료 유지→완료 해제→재조회→삭제→404를 요청별 트랜잭션으로 확인한다.
- 결과: 업무 자동화 42건 통과, JAR 빌드 성공, Newman 요청 30개·검증 78개 통과. [자동화 요약](member-a-test-results.md), [HTTP 실행 요약](../postman/member-a-summary.md).

### 3. SQL·Postman 통합 자료

- 최신 main의 프로젝트·회의 SQL을 유지하고 오늘·미래·지난 완료 샘플 3개를 추가했다. IDENTITY/FK 순서와 enum·completedAt 관계를 유지했다.
- local 프로필의 실제 서버 기동으로 전체 SQL 실행을 확인했다. Postman collection은 초기 프로젝트 1의 팀장·A·B 업무와 표 HTML을 확인한다.
- B에게 넘길 collection·environment·실행 원본 JSON과 공통 계약은 [전달 자료](member-a-handoff.md)에 정리했다. 실제 메시지 전송은 하지 않았다.

## 제출 전에 직접 확인할 항목

- 팀장에게 공통 MarkdownService 계약을 공유하고 팀장 구현과 중복이 생기지 않게 통합한다.
- B에게 업무 Postman 자료를 전달해 팀 전체 collection과 합친다.
- 로컬 커밋을 push한 뒤 2주차 표에 접근 가능한 실제 GitHub 링크를 넣는다. 날짜·기여 이력을 임의로 만들지 않는다.
- 개인 보고서의 설명을 실제 이해·수행한 내용으로 확인하고 PDF로 제출한다. 팀 전체 기능의 완료 여부와 개인 A의 검증을 구분한다.

API 계약은 [업무 API 문서](../TASK_API.md)를 참고한다.
