# 팀원 A 전달 자료

## 팀장: 공통 Markdown 계약

- 파일: `src/main/java/com/teamnote/common/service/MarkdownService.java`
- 생성자 주입: 기존 Parser, HtmlRenderer, Cleaner Bean.
- 호출: `String render(String markdown)`; 입력은 DTO·Entity에서 null을 허용하지 않음. 빈 문자열 출력은 빈 문자열.
- 출력: commonmark HTML 변환 후 jsoup 정제한 body fragment. 저장 원문은 변경하지 않음.
- A 응답은 `TaskResponse.descriptionHtml`에 연결 완료. 팀장은 프로젝트·미리보기, B는 minutesHtml에 같은 Service를 재사용할 수 있다.
- 선행 의존성으로 최소 구현을 포함했으며 팀장·회의의 API·DTO·공통 설정·스키마는 변경하지 않았다. 기존 팀장 Service가 별도로 있으면 이 계약과 맞춰 하나만 남긴다.

## 팀원 B: 업무 Postman 통합

- [Collection](../postman/teamnote-member-a.postman_collection.json)
- [Environment](../postman/teamnote-local.postman_environment.json)
- [Newman 원본 결과](../postman/member-a-results.json)
- [실행 요약](../postman/member-a-summary.md): 요청 30개·검증 78개 통과.
- 변수: baseUrl, projectId, otherProjectId, lateId, doneId, todayId, futureId, foreignId, completedAt, yesterday, today, tomorrow. 팀 전체 collection에 합칠 때 같은 이름 변수 충돌을 확인한다.
- 준비 단계는 테스트 프로젝트 생성용 POST 두 번을 포함한다. 업무 기능 검증만 담당하며 팀장·회의 API 검증을 대신하지 않는다.

## 통합 SQL

- 경로: `src/main/resources/db/data.sql`
- 기반: 원격 main b8940f7의 프로젝트 3개·업무 8개·회의 4개.
- A 추가: 프로젝트 1의 업무 3개(오늘·미래·지난 완료 샘플). 총 프로젝트 3개·업무 11개·회의 4개.
- 고정 날짜 샘플 기준은 2026-11-19. 시간이 지나면 overdue 결과는 달라진다. 자동화 DB 테스트와 collection은 고정/상대 기준일로 각각 분리했다.
- local H2 전체 SQL 초기화 성공 및 업무 API 조회 확인. 팀장·B의 별도 미전달 SQL 자료까지 받았다고 주장하지 않는다.

본 문서는 전달 가능한 자료를 정리한 것이며 팀원에게 실제 메시지를 보내지는 않았다.
