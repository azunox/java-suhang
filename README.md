# TEAMNOTE

학교 팀 프로젝트의 프로젝트·업무·회의록을 관리하는 REST API 프로젝트입니다.

현재는 초기 세팅 위에 **프로젝트 등록·목록·상세 API와 공통 도메인 예외 처리**를 구현한 단계입니다. 사용법과 테스트 데이터 준비 방법은 [프로젝트 API·공통 예외 계약](docs/PROJECT_API.md)을 참고합니다. 프로젝트 수정·삭제·검색, 업무·회의 API와 Markdown 미리보기는 [개발 계획](docs/DEVELOPMENT.md)에 따라 구현합니다.

팀원과 AI는 작업 전에 [공통 개발 규칙(AGENTS.md)](AGENTS.md)을 읽고 명명·계층·API·예외·검증·Git 기준을 따릅니다. AI 도구가 이 파일을 자동으로 읽지 않으면 작업 지침으로 직접 제공합니다.

## 개발 환경과 의존성

| 구분 | 버전·설정 |
| --- | --- |
| Java | 21, Java Toolchain·컴파일 release·Gradle Daemon JVM 기준에 지정 |
| Spring Boot | **4.1.1** — 수행평가 지정 버전 |
| Gradle Wrapper | **9.8.2**, 공식 배포 SHA-256 검증 |
| Spring Web MVC·JPA·Validation | Spring Boot 4.1.1 BOM 관리 |
| H2 | 2.4.240, Spring Boot BOM 관리 |
| JUnit Jupiter | 6.0.3, Spring Boot BOM 관리 |
| AssertJ | 3.27.7, Spring Boot BOM 관리 |
| MockMvc | Spring Framework 7.0.9, Spring Boot BOM 관리 |
| commonmark-java | **0.32.0** |
| CommonMark GFM Tables | **0.32.0**, 코어와 같은 버전 |
| jsoup | **1.24.1** |

Spring Boot 관리 대상 의존성은 개별 버전을 덮어쓰지 않습니다. 추가 라이브러리는 `build.gradle`에 명시적으로 고정했습니다. 스냅샷·마일스톤 저장소는 사용하지 않습니다.

Boot 4 구조에 맞게 `spring-boot-starter-webmvc`와 Web MVC·JPA·Validation 테스트 스타터를 사용합니다. H2 Console은 별도 `spring-boot-h2console` 모듈을 포함했습니다.

확인한 공식 자료:

- [Spring Boot 4.1.1 릴리스](https://spring.io/blog/2026/08/20/spring-boot-4-1-1-available-now/)
- [Spring Boot 4.1.1 의존성 BOM](https://repo.maven.apache.org/maven2/org/springframework/boot/spring-boot-dependencies/4.1.1/spring-boot-dependencies-4.1.1.pom)
- [commonmark-java 릴리스](https://github.com/commonmark/commonmark-java/releases)
- [jsoup 1.24.1 릴리스](https://jsoup.org/news/release-1.24.1)

## 실행 방법

JDK 21을 설치한 뒤 프로젝트 루트에서 실행합니다. Gradle은 별도로 설치하지 않아도 됩니다.

```bash
./gradlew bootRun
```

Windows에서는 `gradlew.bat bootRun`을 사용합니다. 기본 프로필은 `local`입니다. 서버는 `http://localhost:8080`에서 실행됩니다. 포트 변경은 환경 변수로 지정할 수 있습니다.

```bash
SERVER_PORT=8081 ./gradlew bootRun
```

별도 외부 계정·API 키·DB 설치는 필요하지 않습니다. 기본 H2 계정은 `sa`, 비밀번호는 빈 값입니다.

실행 JAR 생성:

```bash
./gradlew bootJar
# 직접 JAR를 실행하는 터미널에서도 Java 21을 선택했는지 확인합니다.
java -version
java -jar build/libs/teamnote-0.0.1-SNAPSHOT.jar
```

Gradle Wrapper 실행 결과의 Launcher JVM은 현재 터미널의 Java 버전일 수 있습니다. 실제 Gradle Daemon은 `gradle/gradle-daemon-jvm.properties`로 Java 21을 선택하고, 소스·테스트·실행도 Toolchain 21을 사용합니다. JDK 21이 없으면 먼저 설치해야 합니다.

## IntelliJ IDEA에서 열기

1. 프로젝트 폴더 또는 `build.gradle`을 엽니다.
2. Gradle 프로젝트로 가져옵니다.
3. Project SDK와 Gradle JVM을 JDK 21로 지정합니다.
4. Gradle 배포는 프로젝트의 Wrapper를 사용합니다.
5. `com.teamnote.TeamnoteApplication` 또는 Gradle `bootRun`으로 실행합니다.

## H2와 더미 데이터

| 항목 | 값 |
| --- | --- |
| Console | `http://localhost:8080/h2-console` |
| JDBC URL | `jdbc:h2:mem:teamnote` |
| 사용자 | `sa` |
| 비밀번호 | 빈 값 |
| 초기 데이터 | `src/main/resources/db/data.sql` |

로컬 실행 시 Hibernate가 스키마를 만든 다음 SQL을 실행합니다. 초기 데이터는 프로젝트 3개·업무 8개·회의 4개입니다. 상태·담당자·날짜·기한 초과·다른 프로젝트 조회 조건을 개발할 수 있도록 구성했습니다.

로컬 DB는 메모리 방식이며 **애플리케이션을 종료하면 데이터가 사라지고, 다음 실행 시 더미 데이터로 다시 시작**합니다. Hibernate의 `create-drop`은 로컬·테스트 프로필에만 지정했습니다. 운영 배포를 위한 영속 DB 설정은 별도 범위입니다.

Console에서 초기 데이터 확인:

```sql
SELECT 'projects' AS table_name, COUNT(*) AS row_count FROM projects
UNION ALL
SELECT 'tasks', COUNT(*) FROM tasks
UNION ALL
SELECT 'meetings', COUNT(*) FROM meetings;
```

Entity의 ID는 `IDENTITY`이고 SQL에서 ID를 직접 지정하지 않아, 이후 등록 기능을 개발할 때 초기 데이터와 ID가 충돌하지 않습니다.

## 패키지와 책임

```text
com.teamnote
├── TeamnoteApplication
├── common
│   ├── config       # Markdown 변환·HTML 정제 Bean 설정
│   ├── exception    # MVC·도메인 오류의 공통 JSON 처리
│   └── response     # ApiResponse·ApiError
├── project
│   ├── controller
│   ├── dto
│   ├── entity
│   ├── repository
│   └── service
├── task             # project와 같은 계층 구조
└── meeting          # project와 같은 계층 구조
```

- Controller는 요청·검증·응답을 처리합니다.
- Service는 인터페이스와 구현체로 분리하고 비즈니스 처리·트랜잭션·Entity↔DTO 변환을 담당합니다.
- Repository는 데이터 접근을 담당합니다.
- Entity는 Service·Repository 내부에서 사용하고 API에 직접 노출하지 않습니다.
- 비어 있는 계층 폴더의 `.gitkeep`은 Git에서 폴더를 보존하기 위한 파일이며, 해당 계층 구현 후 삭제할 수 있습니다.
- `open-in-view=false`이므로 필요한 Entity 접근과 DTO 변환을 Service 처리 범위 안에서 완료합니다.

## 공통 응답과 Markdown 설정

성공 응답은 `ApiResponse.success(responseDto)`로 생성합니다.

```json
{"success":true,"data":{"id":1},"error":null}
```

오류 응답은 MVC가 정한 HTTP 상태와 헤더를 보존하면서 같은 형식으로 반환합니다. 잘못된 요청·없는 경로·지원하지 않는 메서드 등 프레임워크 오류를 처리합니다. 도메인 실패는 공통 `InvalidRequestException`(400), `ResourceNotFoundException`(404), `ConflictException`(409)을 사용하고 기존 `GlobalExceptionHandler`에서 같은 JSON으로 처리합니다.

```json
{"success":false,"data":null,"error":{"code":"INVALID_REQUEST","message":"요청 값이 올바르지 않습니다.","fields":[{"field":"title","message":"공백일 수 없습니다"}]}}
```

`MarkdownConfiguration`에는 Parser·HtmlRenderer·Cleaner Bean을 등록했습니다. GFM 표 지원, 원시 HTML 이스케이프, 위험 URL 제한, HTML 태그·속성 허용 목록을 구성했습니다. 실제 `Markdown 입력 → 변환 → 정제 → Response DTO` 처리 Service와 API 연결은 개발 대상입니다.

## 테스트

공통 설정 검증 대상만 실행:

```bash
./gradlew test \
  --tests com.teamnote.common.config.MarkdownConfigurationTest \
  --tests com.teamnote.common.exception.GlobalExceptionHandlerTest
```

- 표·코드가 변환·정제 후 보존되는지 검증
- 허용되지 않은 속성·URL 제거와 정상 HTTPS 링크 유지 검증
- 원시 HTML의 실행 가능한 태그 이스케이프 검증
- 입력 검증 실패의 상태코드·오류 JSON 검증
- 허용하지 않는 메서드의 `405`·`Allow` 헤더·오류 JSON 검증

프로젝트 등록·조회와 공통 예외의 관련 단위 테스트 실행:

```bash
./gradlew test \
  --tests com.teamnote.common.exception.GlobalExceptionHandlerTest \
  --tests com.teamnote.project.entity.ProjectTest \
  --tests com.teamnote.project.service.ProjectServiceImplTest \
  --tests com.teamnote.project.controller.ProjectControllerTest
```

프로젝트 이름·필수값·기간, 등록·조회·빈 목록·없는 대상, 잘못된 ID·enum·날짜, 공통 오류 응답을 검증합니다. Controller는 MockMvc, Service는 Mockito를 사용하므로 실제 DB 저장·트랜잭션 검증과는 구분합니다.

Gradle 테스트에는 `test` 프로필을 적용합니다. 테스트 DB는 `teamnote-test`로 구분하고 로컬 더미 SQL은 자동 실행하지 않습니다. Repository 테스트에서 필요한 데이터는 해당 테스트가 직접 준비합니다.

Markdown 설정 테스트는 초기 공통 설정 검증용입니다. 프로젝트 테스트는 현재 구현한 등록·조회 범위를 검증하며, 남은 도메인 기능과 개인별 검증 근거는 각 구현에 맞춰 추가해야 합니다.

## 초기 세팅 검증 결과

2026-11-10 기준:

- 지정한 공통 설정 테스트 **5건 통과**, 실패·오류·스킵 0건
- Java·테스트 소스 컴파일 및 `bootJar` 생성 성공
- 컴파일 결과 Java 21 바이트코드(major version 65) 확인
- 실제 의존성 해석 결과가 위 버전 표와 일치함을 확인
- Temurin **21.0.13**에서 생성한 JAR 직접 실행 성공, Spring Boot **4.1.1** 기동 확인
- JPA Repository 3개 검색, H2 스키마·SQL 초기화 오류 없이 기동
- 미구현 경로의 **404 + 공통 오류 JSON**, H2 Console의 **200** 응답 확인

실행 확인에는 임시 포트를 사용했으며, 확인 후 해당 애플리케이션을 종료했습니다. 도메인 API 통합 테스트나 전체 수행평가 기능 검증을 완료했다는 의미는 아닙니다.

## 수행평가 자료

- [수행평가 안내서](https://app.notion.com/p/2026-REST-API-898e7505474382f69c5881f8d4b931e9)
- [개인 보고서 양식](https://app.notion.com/p/40ee75054743806f972aefcfbe90f0bf)
- [담당 범위·구현 순서·남은 요구사항](docs/DEVELOPMENT.md)

초기 생성 브랜치는 `chore/teamnote-project-setup`이며, 세팅 시점에 초기 커밋·원격 저장소 연결·push는 수행하지 않았습니다. 팀 저장소 공개 전환과 개인별 유효 커밋·PDF 제출 조건은 개발 계획에서 확인합니다.
