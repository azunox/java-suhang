# 회의 API

프로젝트별 회의 등록·목록·상세·전체 수정·삭제를 제공한다. 요청과 응답의 날짜·시각은 Asia/Seoul의 현지 시각이며 오프셋 없이 초까지 작성한다.

| Method | 경로 | 성공 |
| --- | --- | --- |
| POST | `/api/projects/{projectId}/meetings` | 201 |
| GET | `/api/projects/{projectId}/meetings` | 200 |
| GET | `/api/projects/{projectId}/meetings/{meetingId}` | 200 |
| PUT | `/api/projects/{projectId}/meetings/{meetingId}` | 200 |
| DELETE | `/api/projects/{projectId}/meetings/{meetingId}` | 200 |

POST·PUT 본문은 아래 네 필드를 모두 받는다. 제목은 필수이며 최대 120자다. 회의록은 빈 문자열을 허용한다. 시작 시각은 종료 시각보다 빨라야 한다. 과거 회의와 자정을 넘기는 회의도 허용한다.

```json
{
  "title": "개발 점검",
  "startAt": "2026-11-19T23:30:00",
  "endAt": "2026-11-20T00:30:00",
  "minutesMarkdown": "| 담당 | 작업 |\n| --- | --- |\n| B | 검색 |"
}
```

응답 DTO는 `id`, `projectId`, `title`, `startAt`, `endAt`, `minutesMarkdown`, `minutesHtml`을 포함한다. 회의록 원문은 편집용으로 보존하며 HTML은 공통 MarkdownService가 commonmark-java/GFM 변환 후 jsoup으로 정제한다. 등록·목록·상세·수정에 같은 변환을 사용한다. 빈 원문의 HTML은 빈 문자열이다.

목록의 `from`, `to`는 선택 query parameter다. `from < to`를 만족해야 한다. 검색 구간은 `[from, to)`이며 회의와 겹치는 구간을 검색한다.

- `from`만 있으면 `meeting.endAt > from`
- `to`만 있으면 `meeting.startAt < to`
- 둘 다 있으면 위 조건을 AND로 적용
- 둘 다 없으면 해당 프로젝트의 전체 회의

항상 `projectId`를 제한하며 `startAt ASC, id ASC`로 정렬한다. 예를 들어 `?from=2026-11-19T10:00:00&to=2026-11-19T11:00:00`은 10시에 끝나는 회의와 11시에 시작하는 회의를 제외하고, 조회 구간을 가로지르는 회의를 포함한다.

잘못된 ID·필수값·시각·검색 기간은 400/`INVALID_REQUEST`, 없는 프로젝트·회의·소속 불일치는 404/`RESOURCE_NOT_FOUND`다. 존재하는 프로젝트에 검색 결과가 없으면 200과 빈 배열을 반환한다. 삭제는 `{"success":true,"data":null,"error":null}`을 반환한다.

검증 명령과 실제 결과는 [개인 검증 근거](evidence/member-b.md), 재현할 요청은 [Postman 실행 안내](../postman/README.md)를 참고한다.
