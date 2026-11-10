# 업무 등록·조회 API

현재 커밋 범위는 업무 등록·프로젝트별 목록·상세 조회다.

| Method | Endpoint | 성공 상태 | 결과 |
| --- | --- | --- | --- |
| POST | `/api/projects/{projectId}/tasks` | 201 | TaskResponse |
| GET | `/api/projects/{projectId}/tasks` | 200 | TaskResponse 배열 |
| GET | `/api/projects/{projectId}/tasks/{taskId}` | 200 | TaskResponse |

등록 필수 필드: title(공백 불가·최대 120자), descriptionMarkdown(null 불가·빈 문자열 허용), assigneeName(공백 불가·최대 80자), dueDate(ISO 날짜·과거 허용), status(TODO·IN_PROGRESS·DONE).

제목과 담당자 이름은 앞뒤 공백을 제거하고 Markdown 원문은 보존한다. DONE으로 등록하면 Asia/Seoul 처리 시각을 completedAt에 기록한다. 그 외 상태의 completedAt은 null이다.

응답 필드: id, projectId, title, descriptionMarkdown, assigneeName, dueDate, status, completedAt. 기존 ApiResponse로 감싼다. 목록은 프로젝트 범위를 제한하고 dueDate ASC, id ASC로 정렬한다.

잘못된 입력·경로 ID는 400 / INVALID_REQUEST, 없는 프로젝트·업무 및 소속 불일치는 404 / RESOURCE_NOT_FOUND다. 존재하는 프로젝트의 업무가 없으면 빈 배열을 반환한다.

## 검증

```bash
./gradlew test --tests com.teamnote.task.entity.TaskTest --tests com.teamnote.task.service.TaskServiceImplTest --tests com.teamnote.task.controller.TaskControllerTest
```

Service는 Mockito, Controller는 standalone MockMvc로 검증한다. 실제 DB 저장·조회와 서버·Postman 호출은 별도 검증이 필요하다.

## 후속 작업

수정·삭제, 완료 상태 전이에 따른 시각 관리, 담당자·상태·마감일·기한 초과 JPQL 검색, DB 검증 및 Postman 결과는 후속 작업이다. 공통 Markdown Service 연결과 HTML 출력도 아직 포함하지 않는다.
