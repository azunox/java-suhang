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
- dueDate: ISO 날짜, 과거 날짜 허용
- status: TODO, IN_PROGRESS, DONE

제목·담당자 이름은 앞뒤 공백을 제거해 저장한다. projectId는 경로에서 받으며 수정 시 프로젝트를 옮기지 않는다. completedAt은 서버가 Asia/Seoul 시각으로 결정한다. DONE 최초 진입 시 기록하고 DONE 유지 시 보존하며 완료 해제 시 비운다. 재완료하면 새 시각을 기록한다.

응답 필드: id, projectId, title, descriptionMarkdown, assigneeName, dueDate, status, completedAt.

공통 Markdown Service가 아직 없어 descriptionHtml은 제공하지 않는다. Markdown 원문을 HTML로 표시하지 않는다.

## 검색

GET query parameter로 받으며 모든 조건을 AND로 결합한다.

| 파라미터 | 조건 |
| --- | --- |
| assigneeName | 앞뒤 공백 제거 후 정확히 일치, 공백만 있으면 조건 없음, 최대 80자 |
| status | enum 일치 |
| dueDateFrom | 마감일 하한 포함 |
| dueDateTo | 마감일 상한 포함 |
| overdue | true: 오늘 이전 마감이며 미완료 / false: 오늘 이후·오늘 마감 또는 완료 / 생략: 전체 |

오늘은 요청 처리 시 Asia/Seoul 날짜로 계산한다. 검색 시작일이 종료일보다 늦으면 400이다. 프로젝트 범위는 항상 적용하며 DB에서 `dueDate ASC, id ASC`로 정렬한다.

예시: `/api/projects/1/tasks?status=TODO&dueDateFrom=2026-11-09&dueDateTo=2026-11-20&overdue=true`.

## 검증 범위

TaskTest는 고정 시각으로 완료 상태 전이·원문 보존·잘못된 수정 시 상태 보존을 검증한다. TaskServiceImplTest는 등록·수정·삭제·존재/소속 검증·검색 조건 전달을, TaskControllerTest는 HTTP 상태·공통 응답·입력 오류를 검증한다.

```bash
./gradlew test --tests com.teamnote.task.entity.TaskTest --tests com.teamnote.task.service.TaskServiceImplTest --tests com.teamnote.task.controller.TaskControllerTest
```

Service 테스트는 Mockito, Controller 테스트는 standalone MockMvc를 사용한다. 실제 DB 쿼리·트랜잭션 커밋·삭제 후 재조회 검증과는 구분한다. JPQL의 DB 실행, Postman 실호출·결과 수집은 아직 수행하지 않았다. SQL·팀 자료 통합·개인 보고서·HTML 출력은 이번 작업 범위에 포함하지 않는다.
