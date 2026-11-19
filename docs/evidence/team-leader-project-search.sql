-- 팀원 A의 통합용 프로젝트 검색 데이터
-- 기존 프로젝트 3개와 하위 업무·회의 INSERT 뒤에 추가한다.
-- 기존 ID 매핑을 유지하며 이 두 프로젝트에는 하위 데이터가 없다.
INSERT INTO projects (name, description_markdown, start_date, end_date, status) VALUES
('API 릴리스_50%!', '| 구분 | 값 |
| --- | --- |
| 진행률 | 50% |', '2026-11-19', '2026-11-19', 'PLANNED'),
('API 릴리스X50Y!', 'LIKE 메타 문자와 구분하기 위한 비교용 프로젝트입니다.', '2026-11-19', '2026-11-19', 'COMPLETED');
