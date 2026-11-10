-- 로컬 실행 시 Hibernate가 테이블을 생성한 뒤 이 파일을 실행한다.
-- IDENTITY 기본값으로 ID를 발급하여 이후 API 등록 시 시퀀스 충돌을 방지한다.
INSERT INTO projects (name, description_markdown, start_date, end_date, status) VALUES
('REST API 수행평가', '# TEAMNOTE

프로젝트·업무·회의록을 관리하는 팀 프로젝트입니다.', '2026-11-09', '2026-11-20', 'IN_PROGRESS'),
('Java 복습 스터디', '## 학습 목표

- JPA 복습
- 테스트 코드 작성', '2026-11-11', '2026-11-30', 'PLANNED'),
('지난 팀 활동', '완료 상태 검색을 확인하기 위한 예시 프로젝트입니다.', '2026-11-01', '2026-11-06', 'COMPLETED');

INSERT INTO tasks (project_id, title, description_markdown, assignee_name, due_date, status, completed_at) VALUES
(1, '프로젝트 API 구현', '프로젝트 등록·수정·삭제를 구현합니다.', '팀장', '2026-11-13', 'IN_PROGRESS', NULL),
(1, '업무 API 구현', '| 항목 | 내용 |
| --- | --- |
| 담당 | 팀원 A |
| 검증 | 정상·예외 |', '팀원 A', '2026-11-13', 'TODO', NULL),
(1, '회의록 API 구현', '회의 시간 검증과 회의록 관리를 구현합니다.', '팀원 B', '2026-11-13', 'TODO', NULL),
(1, '기획 회의 정리', '첫 회의에서 결정한 API 규칙을 정리합니다.', '팀장', '2026-11-09', 'DONE', TIMESTAMP '2026-11-09 17:00:00'),
(1, '기한 초과 검색 확인', '미완료·기한 초과 조건 검색용 데이터입니다.', '팀원 A', '2026-11-09', 'TODO', NULL),
(2, 'JPA 쿼리 예제 작성', '조건 검색용 JPQL을 작성합니다.', '팀원 A', '2026-11-18', 'TODO', NULL),
(2, '테스트 예제 작성', 'JUnit과 AssertJ를 사용합니다.', '팀원 B', '2026-11-19', 'TODO', NULL),
(3, '지난 활동 회고', '완료 업무 조회용 데이터입니다.', '팀장', '2026-11-06', 'DONE', TIMESTAMP '2026-11-06 16:00:00');

INSERT INTO meetings (project_id, title, start_at, end_at, minutes_markdown) VALUES
(1, '요구사항 확인 회의', TIMESTAMP '2026-11-10 10:00:00', TIMESTAMP '2026-11-10 10:30:00', '## 결정 사항

- 요청·응답에 DTO 사용
- Service 인터페이스·구현체 분리'),
(1, '1주차 개발 점검', TIMESTAMP '2026-11-12 15:00:00', TIMESTAMP '2026-11-12 15:30:00', '| 담당 | 점검 내용 |
| --- | --- |
| 팀장 | 프로젝트 |
| 팀원 A | 업무 |
| 팀원 B | 회의록 |'),
(2, '스터디 계획', TIMESTAMP '2026-11-11 16:00:00', TIMESTAMP '2026-11-11 16:30:00', 'Java와 JPA 학습 범위를 정합니다.'),
(3, '지난 활동 회고', TIMESTAMP '2026-11-06 15:00:00', TIMESTAMP '2026-11-06 15:30:00', '완료된 프로젝트의 회의 조회용 데이터입니다.');
