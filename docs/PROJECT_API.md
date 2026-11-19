# 프로젝트 API와 공통 기능 사용법

프로젝트 CRUD·조건 검색, 공통 도메인 예외, Markdown 미리보기를 사용할 수 있다.

## 프로젝트 API

| Method | Endpoint | 성공 응답 |
| --- | --- | --- |
| POST | `/api/projects` | 201, `ApiResponse<ProjectResponse>` |
| GET | `/api/projects?name={name}&status={status}` | 200, `ApiResponse<List<ProjectResponse>>`, 조건 선택 |
| GET | `/api/projects/{projectId}` | 200, `ApiResponse<ProjectResponse>` |
| PUT | `/api/projects/{projectId}` | 200, `ApiResponse<ProjectResponse>` |
| DELETE | `/api/projects/{projectId}` | 200, `ApiResponse<Void>` |

등록·전체 수정의 공통 요청 예시.

```json
{
  "name": "REST API 수행평가",
  "descriptionMarkdown": "# TEAMNOTE\n\n프로젝트 설명  \n",
  "startDate": "2026-11-09",
  "endDate": "2026-11-20",
  "status": "IN_PROGRESS"
}
```

- 다섯 필드는 모두 필수다. 이름은 공백만 입력할 수 없으며 입력값 기준 최대 120자다. 저장 시 이름의 앞뒤 공백을 제거한다.
- 설명은 빈 문자열을 허용하고 원문의 공백·줄바꿈을 보존한다.
- 기간은 `startDate <= endDate`다. 같은 날짜와 과거 날짜를 허용하고 상태를 날짜로 자동 변경하지 않는다.
- 상태는 `PLANNED`, `IN_PROGRESS`, `COMPLETED` 중 하나다.
- 응답의 `data`는 `id`, `name`, `descriptionMarkdown`, `descriptionHtml`, `startDate`, `endDate`, `status`를 포함한다. 생성된 ID는 `data.id`에서 확인한다.
- `GET /api/projects?name=REST&status=IN_PROGRESS`는 대소문자를 구분하지 않는 이름 부분 검색과 상태를 AND로 적용한다. 이름은 최대 120자이며 앞뒤 공백을 제거한다. 빈 이름은 조건에서 제외한다. 상태는 생략하거나 허용된 세 값 중 하나를 지정한다. 목록은 `id ASC`이며 일치하는 데이터가 없으면 `data: []`다.
- 이름의 `%`, `_`, `!`는 문자 그대로 검색된다. URL에서는 퍼센트를 `%25`로 인코딩한다.
- 상세·수정·삭제의 ID는 양수다. 형식 오류·0 이하 값은 400, 존재하지 않는 ID는 404다.
- PUT에서 기간 오류가 발생하면 400을 반환하며 기존 값은 유지된다.
- 완료 업무나 과거 회의도 프로젝트 삭제를 제한한다. 하위 데이터가 하나라도 있으면 409를 반환한다. 하위 데이터를 정리한 후 DELETE에 성공하면 `{"success":true,"data":null,"error":null}`을 반환한다.

## 공통 Markdown과 미리보기

`POST /api/content/preview`에 `Content-Type: application/json`으로 요청한다.

```json
{"markdown":"# TEAMNOTE"}
```

성공 시 200과 `{"success":true,"data":{"html":"<h1>TEAMNOTE</h1>"},"error":null}`을 반환한다. 빈 문자열은 빈 HTML로 변환하고, 필드 누락이나 null은 400 / `INVALID_REQUEST`로 처리한다. 미리보기는 DB에 데이터를 저장하지 않는다.

업무·회의 Service에서는 생성자로 `com.teamnote.common.service.MarkdownService`를 주입받아 응답 DTO를 만들 때 `markdownService.render(원문)`을 호출한다. 업무는 `descriptionHtml`, 회의는 `minutesHtml`에 결과를 담는다. 프로젝트와 미리보기는 같은 구현을 사용한다. commonmark-java로 GFM 표를 포함한 Markdown을 변환하고 jsoup으로 HTML을 정제한다.

## 공통 예외

패키지는 `com.teamnote.common.exception`이며 생성자는 모두 공개 가능한 오류 메시지 하나를 받는다.

| 예외 | HTTP 상태 | `error.code` | 용도 |
| --- | --- | --- | --- |
| `InvalidRequestException` | 400 | `INVALID_REQUEST` | 기간 등 비즈니스 입력 규칙 위반 |
| `ResourceNotFoundException` | 404 | `RESOURCE_NOT_FOUND` | 없는 대상·다른 프로젝트 소속 대상 |
| `ConflictException` | 409 | `CONFLICT` | 데이터 상태와 충돌하는 작업 |

Service에서 대상이 없을 때의 사용 예시:

```java
Project project = projectRepository.findProjectById(projectId)
        .orElseThrow(() -> new ResourceNotFoundException("프로젝트를 찾을 수 없습니다."));
```

`GlobalExceptionHandler`가 HTTP 상태와 공통 JSON을 생성한다. Service에서 HTTP 응답을 만들거나 도메인별 Advice를 복제하지 않는다.

```json
{
  "success": false,
  "data": null,
  "error": {
    "code": "RESOURCE_NOT_FOUND",
    "message": "프로젝트를 찾을 수 없습니다.",
    "fields": []
  }
}
```

입력 DTO 검증 오류는 `error.fields`에 필드별 오류를 담는다. 도메인 예외는 빈 배열을 사용한다. 예외 메시지는 응답에 사용되므로 SQL·내부 경로 등 내부 정보를 넣지 않는다. 예상하지 못한 예외는 기존처럼 일반 메시지와 500으로 처리한다.

## Project 테스트 데이터 준비

단위 테스트에서는 공개 생성 메서드로 유효한 Project를 준비한다. setter나 별도 생성자를 추가하지 않는다.

```java
Project project = Project.create(
        "테스트 프로젝트",
        "",
        LocalDate.of(2026, 11, 9),
        LocalDate.of(2026, 11, 20),
        ProjectStatus.PLANNED
);
```

생성 직후 ID는 아직 없다. DB를 사용하는 Repository 테스트에서는 테스트 트랜잭션 안에서 저장한 뒤 실제 ID를 사용한다.

```java
Project savedProject = projectRepository.saveAndFlush(project);
Long projectId = savedProject.getId();
```

`test` 프로필은 로컬 더미 SQL을 실행하지 않는다. 각 테스트가 필요한 Project를 직접 준비한다. HTTP 요청에서는 등록 성공 응답의 `data.id`를 다음 요청의 경로에 사용한다.

## 관련 검증

Project와 공통 기능 테스트를 실행한다. ProjectRepositoryTest와 ProjectPersistenceTest는 H2에 연결한다.

```bash
./gradlew test \
  --tests 'com.teamnote.project.*' \
  --tests 'com.teamnote.common.*'
```

로컬 HTTP 시나리오와 실제 검증 결과는 [팀장 검증 기록](evidence/team-leader.md)을 참고한다.
