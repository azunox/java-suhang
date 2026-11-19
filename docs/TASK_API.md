# 업무 관리 API

담당 범위: 업무 CRUD, 담당자·상태·마감일 검색, 완료 시각 관리. 프로젝트·회의 기능과 공통 정책은 변경하지 않는다.

## 구현한 API

기본 경로: `/api/projects/{projectId}/tasks`. 모든 경로 ID는 양수다.

| API | Method | Endpoint | 성공 상태 | 결과 |
| --- | --- | --- | --- | --- |
| 업무 등록 | POST | 기본 경로 | 201 | TaskResponse |
| 업무 목록·검색 | GET | 기본 경로 | 200 | TaskResponse 배열 |
| 업무 상세 | GET | 기본 경로 + `/{taskId}` | 200 | TaskResponse |
| 업무 전체 수정 | PUT | 기본 경로 + `/{taskId}` | 200 | TaskResponse |
| 업무 삭제 | DELETE | 기본 경로 + `/{taskId}` | 200 | null |

기존 `ApiResponse(success, data, error)`를 사용한다. 잘못된 입력은 400 / `INVALID_REQUEST`, 없는 프로젝트·업무 또는 소속 불일치는 404 / `RESOURCE_NOT_FOUND`다. 있는 프로젝트에 검색 결과가 없으면 200과 빈 배열을 반환한다.

## 등록·수정

POST와 PUT은 동일한 필수 필드를 받는다. PUT은 전체 수정이다.

```json
{
  "title": "업무 검색 API 구현",
  "descriptionMarkdown": "# 작업 내용",
  "assigneeName": "박승아",
  "dueDate": "2026-11-18",
  "status": "IN_PROGRESS"
}
```

- title: 공백만 있는 값 불가, 최대 120자
- assigneeName: 공백만 있는 값 불가, 최대 80자
- descriptionMarkdown: null 불가, 빈 문자열 허용, 공백·줄바꿈 보존
- dueDate: yyyy-MM-dd 형식의 JSON 문자열, 과거 날짜 허용. 배열·숫자·boolean은 400
- status: TODO, IN_PROGRESS, DONE 중 하나의 JSON 문자열. 숫자·빈 문자열·다른 타입은 400

제목·담당자 이름은 앞뒤 공백을 제거해 저장한다. projectId는 경로에서 받으며 수정 시 프로젝트를 옮기지 않는다. completedAt은 서버가 Asia/Seoul 시각으로 결정한다. DONE 최초 진입 시 기록하고 DONE 유지 시 보존하며 완료 해제 시 비운다. 재완료하면 새 시각을 기록한다.

응답 필드: id, projectId, title, descriptionMarkdown, descriptionHtml, assigneeName, dueDate, status, completedAt.

`descriptionHtml`은 공통 `MarkdownService.render(String markdown)`으로 생성한다. 기존 Parser → HtmlRenderer → Cleaner Bean을 주입받아 Markdown → HTML 변환 → jsoup 정제 순서로 처리한다. 원문은 그대로 저장하며 표·코드는 보존하고 위험 HTML·URL은 제한한다. 빈 원문의 HTML은 빈 문자열이다. 목록·상세·등록·수정 응답에 동일하게 적용한다.

팀장 PR #7이 병합된 최신 main의 `MarkdownService` 인터페이스·`MarkdownServiceImpl`을 그대로 재사용한다. 팀장·B도 같은 `render(String)` 계약을 사용한다. 이 변경은 공통 HTML 정책·Bean·라이브러리 버전·팀장/B API를 바꾸지 않는다.

## 검색

GET query parameter로 받으며 모든 조건을 AND로 결합한다.

| 파라미터 | 조건 |
| --- | --- |
| assigneeName | 앞뒤 공백 제거 후 정확히 일치, 공백만 있으면 조건 없음, 최대 80자 |
| status | TODO·IN_PROGRESS·DONE 문자열 일치. 생략만 조건 없음으로 처리하며 빈 값·공백은 400 |
| dueDateFrom | 마감일 하한 포함 |
| dueDateTo | 마감일 상한 포함 |
| overdue | true: 오늘 이전 마감이며 미완료 / false: 오늘 이후·오늘 마감 또는 완료 / 생략: 전체 |

오늘은 요청 처리 시 Asia/Seoul 날짜로 계산한다. 검색 시작일이 종료일보다 늦으면 400이다. 프로젝트 범위는 항상 적용하며 DB에서 `dueDate ASC, id ASC`로 정렬한다.

예시: `/api/projects/1/tasks?status=TODO&dueDateFrom=2026-11-09&dueDateTo=2026-11-20&overdue=true`.

## 검증과 재현

```bash
./gradlew test --tests 'com.teamnote.task.*'
./gradlew bootJar
java -jar build/libs/teamnote-0.0.1-SNAPSHOT.jar --server.port=18081
```

- 기존 Entity·Service·Controller 테스트에 비문자열 날짜·상태, 빈 검색 상태를 거부하는 회귀 테스트와 유효한 상태 문자열 허용 검증을 추가했다.
- `TaskRepositoryTest` 6건: 생성자 주입을 사용하는 `@DataJpaTest`에서 실제 H2로 조건 조합, 날짜 양쪽·한쪽 경계, 정렬, 소속, 빈 결과, overdue 생략·true·false를 검증한다. 기준일은 2026-11-19로 고정한다.
- `TaskApiIntegrationTest` 3건: 실제 Service·JPA·Markdown Bean과 MockMvc를 연결해 CRUD·소속·오류·표·코드·위험 HTML·빈 원문을 검증한다. 테스트 트랜잭션은 종료 시 롤백한다.
- 실제 HTTP와 트랜잭션 커밋은 별도의 Postman collection/Newman 실행으로 확인한다.

Postman에서 [collection](postman/teamnote-member-a.postman_collection.json)과 [environment](postman/teamnote-local.postman_environment.json)를 가져와 순서대로 실행한다. CLI는 서버 실행 후 아래 명령을 사용한다.

```bash
sh scripts/run-task-postman.sh
```

Node.js/npm이 필요하며 최초 실행 시 Newman을 내려받는다. collection의 준비 단계는 독립된 프로젝트 두 개를 만든다. 업무 생성·조회·검색·수정·삭제와 완료 시각 유지/해제, 소속 제한·입력 오류·HTML 정제·초기 SQL을 검증하고 생성 업무를 삭제한다. 최신 main에 병합된 프로젝트 DELETE로 검증용 프로젝트 두 개도 정리하며 이후 GET 404까지 확인한다. 로컬 메모리 H2에서 실행한다. 실행 날짜는 Asia/Seoul 기준으로 계산한다.

SQL은 `src/main/resources/db/data.sql`에 최신 main의 프로젝트·회의·업무 데이터를 유지하고 A의 2026-11-19·20 마감 및 지난 완료 샘플을 추가했다. 고정 날짜 SQL의 overdue 결과는 실행일에 따라 달라진다. B에게 전달할 자료와 공통 Service 계약은 [A 전달 자료](evidence/member-a-handoff.md)에 정리한다.
