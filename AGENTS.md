# TEAMNOTE AI 개발 규칙

이 문서는 이 저장소에서 작업하는 AI와 팀원에게 적용하는 공통 개발 기준입니다. 작업 전 이 파일, [README](README.md), [개발 계획](docs/DEVELOPMENT.md), 수정 대상과 인접 코드를 읽습니다. 다른 AI 도구를 사용할 때도 이 파일을 작업 지침으로 제공하세요.

## 1. 작업 원칙과 현재 상태

- 설명·진행 보고·PR 본문은 간결한 한국어로 작성합니다. 코드 식별자와 기술 용어는 English를 사용합니다.
- 사용자의 명시적 요청을 우선합니다. 수행평가 충족 여부는 안내서 원문으로 판단하고, 내부 계획을 원문의 필수 조건으로 잘못 설명하지 않습니다.
- 현재 저장소에는 실행 환경, 공통 설정·응답·MVC 예외 처리, Entity·기본 Repository, 더미 SQL, 초기 공통 테스트가 있습니다. CRUD·검색 쿼리·Service·도메인 DTO·미리보기 API는 구현 대상입니다. 작업 시점의 실제 코드를 다시 확인합니다.
- 한 작업·한 커밋은 하나의 논리적 목적에 집중합니다. 관련 없는 리팩터링·파일 이동·일괄 포맷 변경을 섞지 않습니다.
- 기존 구조와 공통 코드를 재사용합니다. 새 추상화·모듈·라이브러리는 필요성과 영향이 명확할 때만 도입합니다.
- 본인이 수정하지 않은 작업, 실행하지 않은 테스트, 미구현 기능을 완료했다고 보고하지 않습니다.

## 2. 위험에 따른 진행과 협의

| 위험 | 예시 | 진행 방식 |
| --- | --- | --- |
| 낮음 | 오탈자·문서·작은 주석·import 정리 | 자율 진행하고 정적 검토로 확인 |
| 중간 | 기존 Service 로직·DTO·쿼리·Controller 동작 수정 | 목적·영향·접근·검증 범위를 짧게 설명한 뒤 진행 |
| 높음 | 인증·권한, 트랜잭션 경계·저장 흐름, 스키마·Entity 관계, 동시성·캐싱, 패키지·의존 방향 변경, 구조적 리팩터링 | 목적·영향 파일·위험·접근·검증 계획을 제시하고 명시적 승인 후 구현 |

- 이미 명시적으로 승인된 계획과 범위는 같은 승인을 반복해서 요청하지 않습니다.
- 예상하지 못한 부작용·의존 관계·규칙 충돌·큰 범위 확대가 발견되면 상황과 최소 해결안을 설명하고 다음 진행을 협의합니다.
- 다른 도메인에 영향을 주는 공통 API 계약·응답 형식·예외 정책·HTML 정책·의존성 변경은 팀장과 합의한 범위에서 반영합니다. 영향 없는 문구·import 수정까지 별도 승인 대상으로 확대하지 않습니다.

## 3. 담당 범위와 공통 파일

| 담당 | 기본 작업 영역 | 주요 책임 |
| --- | --- | --- |
| 팀장 | `project` | 프로젝트 CRUD·검색·기간 검증·삭제 제한, 공통 Markdown 처리 Service·미리보기 API, 통합 |
| 팀원 A | `task` | 업무 CRUD·검색·완료 시각·소속 검증, 업무 설명의 표 변환·정제 적용·검증, SQL 통합 |
| 팀원 B | `meeting` | 회의 CRUD·검색·시간·소속 검증, 회의록 변환·정제 적용·검증, Postman 자료 통합 |

- 각자 자기 도메인의 Controller·Service 인터페이스/구현체·Repository·DTO·테스트·SQL·Postman 결과를 작성합니다.
- `common/**`, `build.gradle`, `settings.gradle`, `gradle/**`, `application*.yml`, `db/data.sql`, 공통 문서는 여러 작업이 만나는 파일입니다. 필요한 수정만 하고, 변경 이유와 다른 담당자에게 미치는 영향을 작업 결과·PR에 기록합니다.
- 다른 도메인의 계약을 임의로 바꾸거나 담당자의 코드를 덮어쓰지 않습니다. 공통 기능은 한 번 구현해 재사용하고 도메인마다 복사하지 않습니다.
- 통합 담당자도 다른 팀원의 실제 구현·검증·개인 보고서 작성까지 대신 맡지는 않습니다.

## 4. 버전·서식·이름

- Java **21**, Spring Boot **4.1.1**, Gradle Wrapper **9.8.2**를 유지합니다. 전역 Gradle 대신 `./gradlew`를 사용합니다. Windows는 `gradlew.bat`을 사용합니다.
- commonmark-java와 GFM Tables는 **0.32.0**, jsoup은 **1.24.1**입니다. Boot BOM 관리 의존성에 버전을 개별 지정하거나 기존 버전을 임의로 올리지 않습니다.
- 현재는 생성자·getter를 직접 작성하는 코드 스타일입니다. Lombok 등 새로운 코드 생성 의존성을 독자적으로 추가하지 않습니다.
- `.editorconfig`와 `.gitattributes`를 따릅니다. Java·Gradle은 공백 4칸, YAML은 2칸, UTF-8·LF·마지막 개행을 사용합니다. `.bat`의 CRLF 예외를 유지합니다.
- 패키지는 소문자, 클래스·record는 PascalCase, 메서드·변수는 camelCase, enum·상수는 UPPER_SNAKE_CASE입니다. wildcard import와 필드 주입을 사용하지 않습니다.
- import는 기존처럼 JDK, 프로젝트·외부 라이브러리, 마지막 static import 순서로 묶고 각 묶음 안에서 정리합니다.
- 새 DTO는 Java `record`를 기본으로 사용합니다. 다음 이름을 공통 기준으로 사용합니다.

| 역할 | 프로젝트 도메인 예시 |
| --- | --- |
| Controller | `ProjectController` |
| Service 인터페이스·구현체 | `ProjectService` · `ProjectServiceImpl` |
| Repository·Entity | `ProjectRepository` · `Project` |
| 등록·수정 Request | `ProjectCreateRequest` · `ProjectUpdateRequest` |
| 검색 조건·Response | `ProjectSearchRequest` · `ProjectResponse` |
| 테스트 | `ProjectControllerTest` · `ProjectServiceImplTest` · `ProjectRepositoryTest` |

- `task`, `meeting`에도 같은 접미사를 적용합니다. `service/impl` 등 새 하위 패키지를 도메인마다 다르게 만들지 않고 인터페이스·구현체를 기존 `service`에 둡니다.
- 매핑된 필드명을 API에서도 camelCase로 맞춥니다. `startDate`/`endDate`, `dueDate`/`completedAt`, `startAt`/`endAt`, `descriptionMarkdown`, `minutesMarkdown`, `assigneeName`을 임의로 다른 이름으로 바꾸지 않습니다.
- 의미 없는 축약, `data1`, `tempService`, `Manager`·`Util`에 여러 책임을 모으는 이름을 피합니다. 주석은 의도·도메인 이유·예외 조건을 설명하고, 임시 코드의 한계·후속 방향·위험을 숨기지 않습니다.

## 5. 계층과 의존 방향

기본 흐름은 **Controller → Service 인터페이스/구현체 → Repository**입니다.

- **Controller:** URL·HTTP 메서드·입력 DTO·입력 검증·상태코드·응답을 담당합니다. Repository 호출, Entity 참조, 비즈니스 판단, 트랜잭션을 넣지 않습니다.
- **Service:** 비즈니스 규칙·트랜잭션·Entity 접근·DTO 변환을 담당합니다. 인터페이스와 구현체를 분리하고 생성자 주입을 사용합니다. `ResponseEntity`, HTTP 요청 객체, `ApiResponse` 생성은 Controller에 둡니다.
- **Repository:** JPA 데이터 접근과 쿼리를 담당합니다. Controller·Service에 의존하지 않습니다.
- **DTO:** 입력·출력 값만 표현합니다. Entity 필드, Entity 타입을 받는 생성자·`from(Entity)` 메서드, Repository 접근을 두지 않습니다. Entity→DTO 변환은 Service 처리 범위에서 수행합니다.
- **Entity:** JPA 매핑과 자기 상태를 표현합니다. 상태 변경 메서드에는 그 상태에 직접 관련된 규칙을 둘 수 있지만, Service·Repository·Controller를 호출하지 않습니다.
- 조회 트랜잭션은 `@Transactional(readOnly = true)`, 쓰기 트랜잭션은 `@Transactional`을 기본으로 하며 Service에 선언합니다. 기존 경계 변경은 위험 기준에 따라 검토합니다.
- `open-in-view=false`를 유지합니다. 지연 로딩과 DTO 변환은 Service의 유효한 처리 범위 안에서 끝냅니다.
- 업무·회의 Service에서 프로젝트의 존재·소속 확인이 필요하면 기존 Project 관계·Repository를 재사용합니다. 도메인 Service를 서로 순환 참조시키거나 Project Entity를 Controller까지 전달하지 않습니다.
- 작은 Entity 변경까지 범용 Mapper·Factory·Facade·BaseService를 새로 만들지 않습니다. 파일 추가는 책임 분리에 실제로 도움이 될 때 선택합니다.

## 6. API·DTO 계약

| 대상 | 기본 경로 |
| --- | --- |
| 프로젝트 | `/api/projects` |
| 업무 | `/api/projects/{projectId}/tasks` |
| 회의 | `/api/projects/{projectId}/meetings` |
| Markdown 미리보기 | `POST /api/content/preview` |

- 각 도메인은 `POST` 등록, `GET` 목록·`/{대상Id}` 상세, `PUT /{대상Id}` 수정, `DELETE /{대상Id}` 삭제를 구현합니다. 업무 상세의 변수는 `taskId`, 회의 상세는 `meetingId`를 사용합니다.
- 기존 기획의 수정 메서드는 `PUT`입니다. `PATCH`로 대체하거나 `/create`, `/update`, `/delete` 동사 경로를 추가하지 않습니다.
- JSON 등록·수정·미리보기 요청은 Request DTO와 `@Valid @RequestBody`로 받습니다. GET 검색 조건은 검색 DTO와 query parameter로 받으며 GET body를 사용하지 않습니다.
- 필수 여부·길이·형식은 DTO에서, 존재·소속·기간 관계·상태 변경은 Service/Entity에서 검증합니다. 날짜는 `LocalDate`, 시각은 `LocalDateTime`과 ISO 형식을 사용합니다.
- DB 문자열 제한과 DTO 제한을 맞춥니다. 현재 프로젝트 이름·업무 제목·회의 제목은 120자, 담당자 이름은 80자입니다. 새 필수 조건·공통 길이 제한은 기획과 계약을 함께 확인합니다.
- 제목·이름·검색 키워드의 공백 처리 기준을 일관되게 적용합니다. Markdown에는 의미 있는 공백·줄바꿈이 있으므로 일반 문자열 정규화를 무조건 적용하지 않습니다.
- 목록은 `ApiResponse<List<대상Response>>`, 상세·등록·수정은 `ApiResponse<대상Response>`를 기본으로 합니다. 페이지 응답 등 새 계약이 필요하면 공통 기준을 먼저 맞춥니다.
- 성공·오류의 바깥 형식은 기존 `ApiResponse<T>(success, data, error)`를 재사용합니다. `Map` 임시 응답이나 도메인별 별도 응답 봉투를 만들지 않습니다.

```json
{"success":true,"data":{"id":1},"error":null}
```

```json
{"success":false,"data":null,"error":{"code":"RESOURCE_NOT_FOUND","message":"요청한 대상을 찾을 수 없습니다.","fields":[]}}
```

| 상황 | 상태코드·공통 처리 |
| --- | --- |
| 등록 성공 | `201` + 생성 결과 DTO |
| 목록·상세·수정 성공 | `200` + 결과 DTO |
| 삭제 성공 | `200` + `ApiResponse<Void>`의 `data: null` |
| 입력·기간 오류 | `400` / `INVALID_REQUEST` |
| 없는 대상·다른 프로젝트 소속 대상 | `404` / `RESOURCE_NOT_FOUND` |
| 하위 데이터가 있는 프로젝트 삭제 | `409` / `CONFLICT` |
| 허용하지 않은 메서드·형식 | `405`·`415`와 기존 MVC 처리 |
| 예상하지 못한 서버 오류 | `500`, 내부 정보는 로그에만 기록 |

- JSON 오류는 기존 `GlobalExceptionHandler`·`ApiError(code, message, fields)`로 통합합니다. Controller별 try/catch·Advice를 복제하지 않고, 도메인 실패를 `200`이나 일반 `500`으로 숨기지 않습니다.
- 현재 공통 처리는 MVC 예외 중심입니다. 도메인 예외의 공통 타입·매핑이 아직 없으면 첫 도입 작업에서 팀장과 기준을 확정해 공유하고, 다른 도메인도 동일한 방식을 사용합니다. 존재하지 않는 공통 클래스를 이미 구현된 것처럼 가정하지 않습니다.
- MVC 예외의 원래 상태와 `Allow` 등의 헤더를 보존하고, 스택 트레이스·SQL·내부 경로를 응답 메시지에 노출하지 않습니다.

## 7. 도메인·JPA·SQL 기준

- 프로젝트는 `startDate <= endDate`, 회의는 `startAt < endAt`을 검증합니다.
- 업무가 `DONE`이 되면 완료 시각을 기록하고, 완료 상태를 해제하면 완료 시각을 비웁니다. 상태 enum 값은 기존 `ProjectStatus`, `TaskStatus`를 재사용합니다.
- 업무·회의는 식별자와 `projectId`의 소속 관계를 함께 검증해 조회·수정·삭제합니다. 다른 프로젝트의 데이터가 섞이지 않도록 목록 쿼리도 `projectId`로 제한합니다.
- 하위 업무·회의가 있는 프로젝트는 삭제를 제한합니다. `CascadeType.REMOVE`, 무조건적인 하위 일괄 삭제, 삭제 검증 우회를 추가하지 않습니다. 사전 검사와 DB 외래 키 제약을 함께 고려합니다.
- 기한 초과 업무와 지난 회의도 조회 대상입니다. 기획에 없는 과거 날짜 입력 금지·회의 중복 금지 등 새 업무 규칙을 임의로 추가하지 않습니다.
- 기존 `Long` ID·`IDENTITY`, 문자열 enum 매핑, `LAZY` 프로젝트 관계, `projects`·`tasks`·`meetings` 테이블을 유지합니다. JPA는 `jakarta.persistence`, 검증은 `jakarta.validation`을 사용합니다.
- Entity 필드에 일괄 setter를 열지 않고 생성·변경 동작을 의도 있는 생성자·메서드로 제공합니다.
- 기획상 각자 조건 검색용 **JPQL 1개 이상**을 구현합니다. 안내서 자체의 필수 조건은 Query Method·JPQL·QueryDSL 중 1개 이상이며, 기본 `findById`만으로 쿼리 요구를 충족했다고 판단하지 않습니다.
- 검색 값은 파라미터로 바인딩합니다. 검색을 위해 전체 데이터를 읽어 메모리에서 필터링하거나 사용자 입력으로 쿼리 문자열을 이어 붙이지 않습니다.
- 필요한 조회 범위를 확인하고, 실제로 문제가 확인된 N+1 등만 관련 범위에서 개선합니다. 근거 없이 캐시·QueryDSL·새 조회 계층을 도입하지 않습니다.
- 더미 SQL은 `src/main/resources/db/data.sql`을 사용합니다. FK 순서·enum·필드명과 정합성을 맞추고 현재처럼 ID를 직접 지정하지 않습니다.
- `local`의 메모리 H2와 `test` 프로필을 구분합니다. 테스트는 로컬 더미 SQL에 의존하지 않고 필요한 데이터를 직접 준비합니다. 실행 프로필·DDL 정책·SQL 로딩 순서를 조용히 바꾸지 않습니다.

## 8. Markdown·HTML 공통 처리

- 기존 `MarkdownConfiguration`의 Parser·HtmlRenderer·Cleaner Bean을 재사용합니다. 도메인마다 별도 Parser나 허용 목록을 만들지 않습니다.
- 처리 순서는 **Markdown 원문 → commonmark-java HTML 변환 → jsoup 정제 → Response DTO**입니다. 원문 저장과 정제된 HTML 출력을 구분합니다.
- 미리보기와 세 도메인의 출력에 같은 처리 정책을 적용합니다. 실제 처리 Service가 구현되면 그 Service를 공통으로 사용합니다.
- 원시 HTML 이스케이프·위험 URL 제한·허용 태그/속성 정책을 유지합니다. 변환만 한 HTML이나 원문을 정제된 HTML 필드에 넣지 않습니다.
- 표·코드 같은 허용 표현의 보존과 위험 속성·URL 제거를 함께 검증합니다. `escapeHtml(false)`, 모든 태그/프로토콜 허용 등 정책 완화는 독자적으로 적용하지 않습니다.
- 의존성·Bean이 있다는 사실과 핵심 API에 실제 적용했다는 사실을 구분해 보고합니다.

## 9. 검증과 수행평가 근거

- 최소한의 적절한 검증을 선택합니다. 문서·단순 서식은 정적 검토, 로직·API 변경은 관련 단위 테스트를 우선합니다. 컴파일 위험이 있을 때 컴파일을 추가합니다.
- 전체 테스트, 통합·E2E·Docker 테스트, 장시간 빌드 등 비용이 큰 검증은 실행 전에 범위와 이유를 설명하고 확인받습니다. 해당 범위가 이미 명시적으로 승인됐다면 다시 묻지 않습니다.
- 선택한 테스트는 기본 1회 실행합니다. 실패 시 실제 원인을 분석해 최소 수정하고 실패·직접 관련 테스트만 다시 실행합니다.
- 실행하지 않은 테스트는 이유·수행한 정적 검토·필요한 후속 검증을 결과에 씁니다. 빈 테스트, getter만 검증하는 테스트, 구현을 그대로 복사한 검증을 개수 채우기용으로 추가하지 않습니다.
- 새 테스트는 해당 main 패키지와 같은 test 패키지에 두고 `*Test`와 동작을 설명하는 camelCase 메서드명을 사용합니다. JUnit Jupiter·AssertJ·MockMvc를 사용하고 기존 Boot 4 테스트 의존성을 유지합니다.
- Controller는 상태코드·응답·입력 오류, Service는 실제 규칙·예외, Repository는 조건 검색·소속 범위를 검증합니다. 시간에 의존하는 테스트는 재현할 수 있는 기준 시각·데이터를 사용합니다.
- 안내서 최소 조건은 **팀 전체 3건 이상**, **Controller 1건 이상**, **Service 또는 Repository 1건 이상**, **실패·예외 검증 1건 이상**입니다. 만점 준비 계획은 **개인별 정상·실패를 포함한 6건 이상**이며 두 기준을 구분합니다.
- 현재 공통 설정 테스트만으로 도메인 테스트 조건을 모두 충족했다고 보고하지 않습니다.

관련 테스트만 실행하는 명령 예시입니다. 실제 존재하는 클래스명으로 선택합니다.

```bash
./gradlew test --tests com.teamnote.task.service.TaskServiceImplTest
```

현재 존재하는 공통 설정 검증:

```bash
./gradlew test \
  --tests com.teamnote.common.config.MarkdownConfigurationTest \
  --tests com.teamnote.common.exception.GlobalExceptionHandlerTest
```

컴파일 또는 JAR 확인이 필요한 경우:

```bash
./gradlew compileJava compileTestJava
./gradlew bootJar
```

- 개인별 주요 구현 2건 이상에 목적·핵심 코드 링크·호출 흐름·기술적 판단·검증 근거를 남깁니다. 라이브러리 도입 이유·실제 API 결과와 테스트 결과도 함께 기록합니다.
- 새 API는 API명·Method·Endpoint·설명·상태코드와 Postman 결과를 담당자가 준비합니다. 문서의 예정·구현 완료·검증 완료 상태를 구분해 갱신합니다.

## 10. 브랜치·커밋·PR

- 변경 전에 `git status --short --branch`를 확인합니다. 기존 수정·미추적 파일을 다른 사람의 변경으로 취급하고 임의로 지우거나 함께 커밋하지 않습니다.
- 실제 기준 브랜치를 확인해 목적별 브랜치를 사용합니다. 이미 작업 목적과 맞는 브랜치는 재사용합니다. 사용자나 저장소 흐름의 명시적 허용 없이 `main`에서 구현하지 않습니다.
- 브랜치는 kebab-case로 작성합니다. 예: `feat/project-crud`, `feat/task-crud`, `feat/meeting-crud`, `fix/meeting-period-validation`.
- 커밋 형식은 **`type : 변경 목적`**, 허용 type은 **`feat`, `fix`, `refactor`, `chore`, `docs`, `hotfix`**입니다. 실제 변경 내용과 메시지를 일치시킵니다.
- 작업 파일을 검토해 해당 목적의 파일만 선택해 stage합니다. 명시적 요청 범위에서 commit·push·PR을 진행하며, 다른 사람 변경을 되돌리는 reset·force push를 임의로 수행하지 않습니다.
- 개인별 **11/9~11/13 2건 이상**, **11/16~11/20 2건 이상**, 총 4건 이상의 유효 커밋이 필요합니다. 같은 작업의 인위적인 분할이나 날짜·기여 이력 조작을 하지 않습니다.
- PR·Issue 작성 전 `.github/pull_request_template.md`, `.github/ISSUE_TEMPLATE/*` 등 실제 저장소 템플릿을 확인하고 존재하면 우선합니다. 템플릿이 없으면 아래 정보 중 관련 항목만 작성합니다.

  - 해결한 요구사항과 바뀐 동작
  - API·DTO·공통 파일·스키마에 미치는 영향
  - 실제 검증 명령·결과와 미검증 항목
  - 관련 코드·Postman·보고서 근거

- 작업 완료 보고는 **무엇을 바꿨는지, 왜 필요한지, 어떻게 확인했는지, 남은 사항**을 한국어로 짧게 정리합니다. 코드·프로젝트 파일을 바꿨다면 권장 커밋 type·메시지와 해당 브랜치를 안내합니다.

## 11. 작업물 제출 전 확인

- [ ] 담당 범위 안에서 작업했고 다른 팀원의 변경을 보존했는가?
- [ ] 기존 이름·경로·필드명·enum·응답·예외 계약을 따랐는가?
- [ ] Controller/DTO에 Entity나 Repository 접근이 새지 않았는가?
- [ ] Service 인터페이스/구현체, 쿼리, 소속·기간·상태 규칙을 구현했는가?
- [ ] 관련 정상·실패 검증과 실제 결과를 준비했는가?
- [ ] 공통 파일 변경과 호환성 영향을 기록했는가?
- [ ] SQL·API 명세·Postman·개인 보고서 근거를 실제 상태와 맞췄는가?
- [ ] 실행하지 않은 검증이나 남은 구현을 완료로 표시하지 않았는가?

문서만 수정한 작업에는 해당되는 문서·링크·규칙 정합성만 확인하고 불필요한 코드 테스트를 실행하지 않습니다.
