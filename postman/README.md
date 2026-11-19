# TEAMNOTE Postman 실행

`TEAMNOTE.postman_collection.json`과 `TEAMNOTE.local.postman_environment.json`을 Postman으로 가져온다. Java 21에서 `./gradlew bootRun`으로 로컬 H2 서버를 실행한 뒤 `baseUrl`을 지정한다. 기본 주소는 `http://localhost:8080`이다.

컬렉션 순서는 Content Preview → Projects → Tasks → Meetings다. Projects 검증은 미리보기에서 만든 HTML 값을 재사용하므로 순서를 유지한다. Tasks는 팀원 A의 `origin/feat/task-completion` 전달 자료에서 가져왔으며 해당 브랜치의 업무 HTML 기능이 필요하다.

- Projects·Content Preview는 팀장 `ccf12c5`의 검증 자료를 사용한다.
- Tasks는 팀원 A `1911e74`의 요청·검증과 상대 날짜 초기화를 보존한다. B가 통합한 마지막 두 요청이 임시 프로젝트를 정리한다.
- Meetings 41개 요청은 B가 작성했다. 등록 응답에서 프로젝트·회의 ID를 저장하며 실패 요청은 정상 ID를 덮어쓰지 않는다.
- 팀장 변수는 `leader*`, 회의 변수는 `b*`, A 변수는 기존 이름을 사용해 충돌을 피한다. 환경 파일의 동적 ID 초기값은 비어 있다.
- 회의 SQL 전달 파일은 `docs/evidence/member-b-meetings.sql`이다. 회의 컬렉션은 검증할 데이터를 직접 생성하므로 해당 SQL의 선행 실행이 필요하지 않다.

전체 실행은 아래 명령을 사용한다. 결과를 클라우드에 전송하지 않는 CLI 옵션을 지정했다.

```bash
postman collection run postman/TEAMNOTE.postman_collection.json \
  --env-var baseUrl=http://localhost:8080 --no-report-events --timeout-request 5000
```

팀원 A의 HTML 변경이 아직 병합되기 전에는 준비된 도메인만 선택해서 실행할 수 있다.

```bash
postman collection run postman/TEAMNOTE.postman_collection.json \
  -i 'Content Preview' -i Projects -i Meetings \
  --env-var baseUrl=http://localhost:8080 --no-report-events --timeout-request 5000
```

회의만 실행하려면 `TEAMNOTE.meetings.postman_collection.json`을 사용한다. 컬렉션은 임시 데이터를 생성하고 정상 종료 시 삭제한다. 중간 실패 후에는 서버를 재시작해 메모리 H2를 초기화한 뒤 처음부터 실행한다. 팀원 A의 SQL 샘플 요청은 기본 프로젝트 1의 초기 데이터가 있는 로컬 프로필을 사용한다.

실제 검증 범위·명령·실행 결과는 [팀원 B 근거](../docs/evidence/member-b.md)에 기록한다.
