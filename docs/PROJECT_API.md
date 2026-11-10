# 프로젝트 등록·조회와 공통 예외 사용법

팀장 명세의 커밋 1 구현 범위다. 프로젝트 등록·목록·상세와 공통 도메인 예외를 사용할 수 있다.

## 프로젝트 API

| Method | Endpoint | 성공 응답 |
| --- | --- | --- |
| POST | `/api/projects` | 201, `ApiResponse<ProjectResponse>` |
| GET | `/api/projects` | 200, `ApiResponse<List<ProjectResponse>>` |
| GET | `/api/projects/{projectId}` | 200, `ApiResponse<ProjectResponse>` |

등록 요청 예시:

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
- 응답의 `data`는 `id`, `name`, `descriptionMarkdown`, `startDate`, `endDate`, `status`를 포함한다. 생성된 ID는 `data.id`에서 확인한다.
- 목록은 DB에서 `id ASC`로 정렬한다. 결과가 없으면 `data: []`다.
- 상세의 ID는 양수다. 형식 오류·0 이하 값은 400, 존재하지 않는 ID는 404다.
- 수정·삭제·조건 검색은 커밋 2, `descriptionHtml`·Markdown 미리보기는 커밋 3에서 추가할 예정이다.

## 공통 예외

패키지는 `com.teamnote.common.exception`이며 생성자는 모두 공개 가능한 오류 메시지 하나를 받는다.

| 예외 | HTTP 상태 | `error.code` | 용도 |
| --- | --- | --- | --- |
| `InvalidRequestException` | 400 | `INVALID_REQUEST` | 기간 등 비즈니스 입력 규칙 위반 |
| `ResourceNotFoundException` | 404 | `RESOURCE_NOT_FOUND` | 없는 대상·다른 프로젝트 소속 대상 |
| `ConflictException` | 409 | `CONFLICT` | 데이터 상태와 충돌하는 작업 |

Service에서 대상이 없을 때의 사용 예시:

```java
Project project = projectRepository.findById(projectId)
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

관련 단위 테스트를 선택하는 명령:

```bash
./gradlew test \
  --tests com.teamnote.common.exception.GlobalExceptionHandlerTest \
  --tests com.teamnote.project.entity.ProjectTest \
  --tests com.teamnote.project.service.ProjectServiceImplTest \
  --tests com.teamnote.project.controller.ProjectControllerTest
```

2026-11-10 작업에서 각 대상을 선택 실행한 결과는 공통 예외 8건, Entity 12건, Service 6건, Controller 18건 통과다. Service 테스트의 초기 Mockito 설정 오류 2건을 수정하고 해당 클래스만 재실행했다. Java 21에서 main·test 소스 컴파일도 완료했다.

이 검증은 MockMvc·Mockito와 실제 Entity 로직을 사용한다. 실제 DB 저장·트랜잭션·서버 기동·Postman·팀 전체 통합 검증은 실행하지 않았다. DB 검증이 필요하면 저장소 `AGENTS.md`에 따라 목적과 실행 범위를 확인한다.
