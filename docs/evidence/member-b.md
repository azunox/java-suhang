# 팀원 B 회의 기능 구현·검증 근거

2026년 11월 19일 · Java 21.0.10 · Spring Boot 4.1.1 · H2 2.4.240

## 담당 구현

- 회의 CRUD 5개, 시간 검증과 프로젝트 소속 검증
- JPQL 기간 겹침 검색. `m.startAt < to AND m.endAt > from`으로 맞닿기만 한 경계를 제외하며 프로젝트 ID를 항상 제한
- 공통 MarkdownService를 재사용해 등록·목록·상세·수정의 `minutesHtml` 생성
- 실제 H2 회귀 테스트, 회의 검증 SQL, Postman 자료 통합

API 계약은 [회의 API 문서](../MEETING_API.md)에 있다. 대표 구현은 시간 검증·실패 수정의 원본 보존과 JPQL 기간 검색·HTML 출력이다.

## 개발 커밋

| 날짜 | 메시지 | 근거 |
| --- | --- | --- |
| 2026-11-10 | `feat : 회의 등록과 프로젝트별 조회 API 구현` | [a516d99](https://github.com/azunox/java-suhang/commit/a516d999315516bd434baccac005e506b3ef59f8) |
| 2026-11-10 | `feat : 회의 수정과 시간 검증 및 삭제 처리 구현` | [4ae5139](https://github.com/azunox/java-suhang/commit/4ae513914c8b3f4af2fed9cb210618239562e897) |
| 2026-11-19 | `feat : 회의 기간 검색과 회의록 HTML 출력 구현` | [1aacb8a](https://github.com/azunox/java-suhang/commit/1aacb8a8fab7c76d6ede115e30adc3ff0e26d8f8) |
| 2026-11-19 | `chore : 회의 회귀 검증과 팀 Postman 자료 통합` | [브랜치 개발 이력](https://github.com/azunox/java-suhang/commits/feat/meeting-completion) |

## 실제 검증

```bash
./gradlew test bootJar
postman collection run postman/TEAMNOTE.postman_collection.json \
  -i 'Content Preview' -i Projects -i Meetings \
  --env-var baseUrl=http://127.0.0.1:18082 --no-report-events --timeout-request 5000
```

| 검증 대상 | 결과 | 전제 |
| --- | --- | --- |
| 회의 제출 브랜치 전체 자동화 테스트 | 161건, 실패·오류·스킵 0 | 팀장 main `ccf12c5` + B 변경 |
| 회의 테스트 | Controller 26·Repository 7·Service 4·영속성 4 = 41건 | 모의 Repository와 실제 H2 테스트를 함께 포함 |
| 실행 JAR | bootJar 성공, Java 21 기동 | local 프로필, 더미 SQL 자동 초기화 |
| Projects·Meetings·Content Preview | 요청 65개·검증 193개 통과, exit 0 | 127.0.0.1:18082의 제출 브랜치 |
| 전체 팀 Postman | 요청 97개·검증 312개, 검증 6개 실패, exit 1 | 업무 HTML 기능이 없는 현재 main 기반 |
| 별도 A+B 통합 전체 자동화 테스트 | 170건, 실패·오류·스킵 0 | 별도 worktree에 A `1911e74`를 임시 병합 |
| 별도 A+B 통합 Postman | 요청 97개·검증 312개 모두 통과, exit 0 | 위 통합 JAR을 127.0.0.1:18083에서 실행 |

업무 HTML 관련 6개 실패는 TaskResponse에 `descriptionHtml`이 아직 없는 main에서 재현됐다. 팀원 A의 `feat/task-completion`을 합친 별도 작업 폴더에서는 같은 컬렉션이 통과했다. A 브랜치 내용은 B 제출 브랜치에 추가하지 않았다. 팀 최종 병합 후 컬렉션을 다시 실행한다.

- [자동화 테스트 상세](member-b-tests.json)
- [제출 브랜치 Postman 성공 로그](member-b-postman-core.txt)
- [업무 병합 전 전체 컬렉션 실패 로그](member-b-postman-main.txt)
- [A+B 별도 통합 Postman 성공 로그](member-b-postman-integration.txt)
- [실제 HTTP 요청·응답 13건](member-b-http.json). Postman 로그와 별도로 Python urllib로 기록했다.

## 회귀 테스트와 SQL

`MeetingRepositoryTest`는 조회 구간의 좌우 경계·전체 포함·걸침·한쪽 조건 생략·프로젝트 범위·정렬을 실제 H2로 검증한다. `executesHandoffSqlAndFindsExpectedOverlapRows`는 [A에게 통합할 SQL](member-b-meetings.sql)을 직접 실행하고 겹침 결과 네 건을 확인했다.

`MeetingPersistenceTest`는 Service 트랜잭션이 종료된 뒤 다시 조회해 수정 저장과 잘못된 수정의 값 보존을 확인한다. 다른 프로젝트의 수정·삭제 차단, 단일 회의 삭제 후 Project 보존, 재삭제 404, 빈 회의록 HTML도 검증했다.

## 트러블슈팅

### 시각 입력의 JSON 배열

기본 Jackson LocalDateTime 변환은 숫자 배열을 허용했다. 1주차에 회의 DTO 필드의 엄격한 문자열 파서를 구현하고, 배열 입력이 저장소 호출 전에 400으로 처리되는지 검증했다. 2주차 검색 DTO에도 오프셋 없는 명시적 포맷을 적용해 잘못된 달력 날짜·오프셋을 400으로 검증했다.

### HTML 검증의 텍스트 오인

첫 Postman 스크립트는 HTML 문자열에서 `onerror`를 정규식으로 검색해, 이스케이프된 원시 HTML 텍스트도 실패로 처리했다. 파싱된 DOM에서 실행 가능한 태그·이벤트 속성과 링크 프로토콜을 검사하도록 바꿨다. 같은 위험 입력과 정상 표·코드·링크가 포함된 회의 응답이 검증을 통과했다.

### 통합 컬렉션 실행 순서

팀장 프로젝트 검증은 미리보기 응답을 변수에 저장해 비교하므로 Content Preview 폴더를 Projects보다 먼저 실행하도록 통합했다. 팀장·A의 검증 스크립트와 변수는 보존했다. 회의 변수는 `b*` 접두사로 격리했다.

## 자료 통합 출처

- Projects·Content Preview는 팀장 main `ccf12c5`의 컬렉션
- Tasks는 팀원 A `1911e74f4e7d54f2b06efaa153f1d54ce342f3f8`의 전달 컬렉션. 업무 구현과 기존 검증의 작성자는 A다.
- Meetings와 공통 실행 순서·자료 통합·추가 정리는 B의 작업이다.
- 더미 SQL 통합 담당자는 A다. B의 SQL은 독립 파일과 실행 테스트로 전달 가능한 상태다.

개인 보고서에는 현재 사용자가 확인할 학번·이름과 회고를 보완하고 최종 PDF로 내보낸다. 이 문서는 실제 개발·검증 근거이며 최종 PDF 제출을 대신하지 않는다.
