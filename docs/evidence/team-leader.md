# 팀장 구현 및 검증 기록

2026-11-19 · Java 21.0.13 · Spring Boot 4.1.1 · H2 2.4.240

## 주요 구현

### 프로젝트 기간 검증, JPQL 검색, 삭제 제한

- [Project](../../src/main/java/com/teamnote/project/entity/Project.java)는 등록·수정에 같은 검증을 적용한다. 같은 날 시작과 종료를 허용한다. 수정은 필수값과 기간을 모두 검증한 뒤 필드를 바꾼다.
- [ProjectRepository](../../src/main/java/com/teamnote/project/repository/ProjectRepository.java)의 `search`는 이름 부분 일치와 상태를 AND로 묶고 ID 오름차순으로 정렬한다. [Service](../../src/main/java/com/teamnote/project/service/ProjectServiceImpl.java)가 LIKE의 `!`, `%`, `_`를 이스케이프하고 검색 패턴을 파라미터로 전달한다.
- 삭제는 같은 쓰기 트랜잭션에서 프로젝트 조회 → 업무·회의 존재 확인 → 삭제·flush 순으로 수행한다. 사전 검사에서 자식을 발견하거나 두 프로젝트 참조 외래 키가 실제로 삭제를 차단하면 409를 반환한다. 동일 프로젝트를 먼저 삭제한 요청이 있으면 뒤 요청은 404로 처리한다.
- 등록·상세와 공통 오류 계약은 [등록·조회 커밋](https://github.com/azunox/java-suhang/commit/b1546ab6a712d8cc8766a28f39d3815637488d6a), 기존 목록·상세 JPQL은 [조회 전환 커밋](https://github.com/azunox/java-suhang/commit/5551a49460a50e2e39b6b73d3e36265865c136bb)에 있다.

### 공통 Markdown 변환·정제와 미리보기

- [MarkdownServiceImpl](../../src/main/java/com/teamnote/common/service/MarkdownServiceImpl.java)은 commonmark-java와 GFM Tables로 헤더·코드·표를 렌더링하고 jsoup Cleaner의 허용 목록으로 HTML을 정제한다.
- [ContentPreviewController](../../src/main/java/com/teamnote/common/controller/ContentPreviewController.java)와 Project Service가 같은 구현을 호출한다. 프로젝트는 Markdown 원문을 저장하고 응답에서 `descriptionHtml`을 생성한다. [MarkdownConfiguration](../../src/main/java/com/teamnote/common/config/MarkdownConfiguration.java)이 원시 HTML 이스케이프와 URL·태그 정책을 관리한다.
- A·B는 자기 Service 생성자에 `MarkdownService`를 주입하고 DTO 변환 시 `render(원문)`을 호출한다. 대상 필드는 업무 `descriptionHtml`, 회의 `minutesHtml`이다. 사용 예시는 [프로젝트·공통 API](../PROJECT_API.md)에 있다.

## 자동화 검증

```bash
./gradlew test --tests 'com.teamnote.project.*' --tests 'com.teamnote.common.*'
./gradlew test --tests com.teamnote.project.service.ProjectPersistenceTest
./gradlew bootJar
```

- 기본 실행은 **86건, 실패 0건**이었다. 프로젝트는 Entity 12·Controller 32·Service 15·Repository 6·영속성 4건, 공통은 17건이다. 동일 ID 동시 삭제 회귀 테스트를 추가한 뒤 ProjectPersistenceTest **5건**을 다시 실행해 전부 통과했다.
- `ProjectControllerTest.rejectsPartialUpdateBeforeCallingService`는 PUT 필수값 누락이 공통 400과 구체적인 필드 오류로 이어지는지 확인한다. 날짜 배열과 숫자 상태도 POST·PUT에서 거부한다.
- `ProjectRepositoryTest.matchesEscapedLikeCharactersLiterally`는 유사한 이름을 함께 저장한 뒤 특수 문자가 있는 이름만 반환하는지 실제 H2에서 확인한다.
- `ProjectPersistenceTest`는 정상 수정 커밋, 기간 오류 이후 값 보존, 하위 데이터 삭제 순서, 업무·회의 각 FK 위반 후 데이터 보존, 동일 ID 동시 삭제를 확인한다. FK 테스트는 사전 조회를 놓친 상황을 재현하고 DB 제약과 롤백을 실제로 실행한다.
- Markdown 서비스 테스트는 표·코드·정상 HTTPS 링크 보존, 위험 HTML 및 URL 제한, 빈 입력을 확인한다.
- 실행 JAR을 로컬 프로필로 기동해 초기 데이터 프로젝트 3개·업무 8개·회의 4개를 조회했다.

## 로컬 HTTP 및 Postman 결과

[Postman Collection](team-leader.postman_collection.json)을 가져와 기본 `baseUrl`에서 실행한다. 컬렉션이 요청을 순서대로 실행하고, 등록 응답 ID를 다음 요청에 사용한다. 기본 로컬 SQL로 구동한 JAR에서 **요청 24개, 검증 75개, 실패 0개**를 확인했다.

```bash
postman collection run docs/evidence/team-leader.postman_collection.json \
  --env-var baseUrl=http://127.0.0.1:8080 --no-report-events --timeout-request 5000
```

| 요청 | 실제 응답과 확인값 |
| --- | --- |
| 프로젝트 등록 POST | 201, 이름 앞뒤 공백 제거, 같은 시작·종료일 허용 |
| 전체 목록·상세·조건 검색 GET | 200, ID 정렬·복합 조건·특수 문자 검색·정제 HTML 일치 |
| 빈 상태 검색 GET | 400, `INVALID_REQUEST`, `fields[0].field = "status"` |
| 프로젝트 전체 수정 PUT | 200, 수정된 상태·원문·정제 HTML |
| 잘못된 기간으로 PUT | 400, `INVALID_REQUEST`. 재조회한 값은 정상 수정 응답과 일치 |
| 하위 데이터가 남은 프로젝트 DELETE | 409, `CONFLICT`. 업무·회의 상세 재조회 200 |
| 업무·회의 정리 후 프로젝트 DELETE | 200, `data: null`. 삭제한 프로젝트 재조회 404 |
| Markdown 미리보기 POST | 200, 헤더·표 출력이 프로젝트 응답과 일치. 빈 입력의 HTML은 빈 문자열, 입력 누락은 400 |

`POST /api/content/preview`에 `{"markdown":"# TEAMNOTE"}`를 보낸 실제 응답.

```json
{"success":true,"data":{"html":"<h1>TEAMNOTE</h1>"},"error":null}
```

완료 업무 또는 과거 회의가 있는 프로젝트의 삭제 응답.

```json
{"success":false,"data":null,"error":{"code":"CONFLICT","message":"업무 또는 회의가 남아 있는 프로젝트는 삭제할 수 없습니다.","fields":[]}}
```

Postman 컬렉션 외에 저장소의 현재 Task·Meeting API를 함께 기동해 프로젝트 두 개를 생성했다. 다른 프로젝트에서의 하위 상세 404와 빈 하위 목록, 같은 프로젝트에서의 업무 DONE 수정·상태 검색과 회의 제목 수정·목록 조회, 하위 데이터 정리 후 프로젝트 삭제를 실제 HTTP로 확인했다.

## A·B 통합 자료

- **A의 SQL 통합** · [프로젝트 검색 SQL](team-leader-project-search.sql)을 기존 시드 SQL 뒤에 추가한다. 프로젝트 5개·업무 8개·회의 4개로 기동하고 `릴리스_50%!` 검색에는 밑줄이 실제로 있는 행만, `name=api&status=COMPLETED`에는 비교 행만 반환되는 것을 확인했다. 기본 실행의 `db/data.sql`은 기존 3개 프로젝트를 사용한다.
- **B의 Postman 통합** · 위 컬렉션의 세 폴더와 컬렉션 수준의 공통 응답 검증을 사용한다. `leaderProjectId`, `leaderTaskId`, `leaderMeetingId` 등 팀장용 변수는 팀원 폴더와 충돌하지 않는다. 삭제 제한 확인용 하위 리소스는 이 컬렉션에서 생성하고 정리한다.

## 통합 상태

GitHub `main`의 검토 기준은 [b8940f7](https://github.com/azunox/java-suhang/commit/b8940f7ab43bbbc2988c69ec7a153b5b4a4b356e)이며 저장소 공개 상태를 확인했다. 현재 팀원 코드에서 업무·회의의 HTML 응답과 회의 기간 검색은 확인되지 않는다. 팀 SQL·Postman 통합본은 각 담당자의 변경이 반영된 뒤 확인할 수 있다.
