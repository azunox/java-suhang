-- 팀원 A가 db/data.sql 뒤에 통합할 회의 검증 데이터. 새 프로젝트 두 개를 사용한다.
-- 고정된 2026-11-19 10:00~11:00 검색의 경계·다른 소속·Markdown 사례다.
INSERT INTO projects (name, description_markdown, start_date, end_date, status) VALUES
('B 회의 검색 검증', '', DATE '2026-11-19', DATE '2026-11-20', 'IN_PROGRESS'),
('B 다른 소속 검증', '', DATE '2026-11-19', DATE '2026-11-20', 'PLANNED');

INSERT INTO meetings (project_id, title, start_at, end_at, minutes_markdown)
SELECT id, '끝 경계', TIMESTAMP '2026-11-19 09:00:00', TIMESTAMP '2026-11-19 10:00:00', ''
FROM projects WHERE name = 'B 회의 검색 검증';
INSERT INTO meetings (project_id, title, start_at, end_at, minutes_markdown)
SELECT id, '시작 걸침', TIMESTAMP '2026-11-19 09:30:00', TIMESTAMP '2026-11-19 10:30:00', ''
FROM projects WHERE name = 'B 회의 검색 검증';
INSERT INTO meetings (project_id, title, start_at, end_at, minutes_markdown)
SELECT id, '동일 구간', TIMESTAMP '2026-11-19 10:00:00', TIMESTAMP '2026-11-19 11:00:00',
'# 회의록

| 담당 | 작업 |
| --- | --- |
| B | 검색 검증 |

<script>alert(1)</script>'
FROM projects WHERE name = 'B 회의 검색 검증';
INSERT INTO meetings (project_id, title, start_at, end_at, minutes_markdown)
SELECT id, '종료 걸침', TIMESTAMP '2026-11-19 10:30:00', TIMESTAMP '2026-11-19 11:30:00', ''
FROM projects WHERE name = 'B 회의 검색 검증';
INSERT INTO meetings (project_id, title, start_at, end_at, minutes_markdown)
SELECT id, '전체 포함', TIMESTAMP '2026-11-19 09:00:00', TIMESTAMP '2026-11-19 12:00:00', ''
FROM projects WHERE name = 'B 회의 검색 검증';
INSERT INTO meetings (project_id, title, start_at, end_at, minutes_markdown)
SELECT id, '시작 경계', TIMESTAMP '2026-11-19 11:00:00', TIMESTAMP '2026-11-19 12:00:00', ''
FROM projects WHERE name = 'B 회의 검색 검증';
INSERT INTO meetings (project_id, title, start_at, end_at, minutes_markdown)
SELECT id, '다른 소속', TIMESTAMP '2026-11-19 10:00:00', TIMESTAMP '2026-11-19 11:00:00', ''
FROM projects WHERE name = 'B 다른 소속 검증';
