# 팀원 A Postman 실행 결과

- 실행 시각: 2026-11-19T12:10:53+09:00 (Asia/Seoul)
- 실행 도구: Newman (Postman collection CLI runner)
- 실행 대상: Java 21.0.11 / Spring Boot 4.1.1 / local H2 / 127.0.0.1:18081
- 요청: 30개, 실패 0개
- 검증: 78개, 실패 0개

실제 요청·응답 본문과 assertion 결과는 [원본 결과 JSON](member-a-results.json)에 들어 있다. Postman GUI의 수동 실행 화면을 캡처한 자료는 아니다.

| 요청 | Method | 실제 상태 | 검증 |
| --- | --- | --- | --- |
| 준비 / 업무 프로젝트 | POST | 201 | 2/2 |
| 준비 / 다른 프로젝트 | POST | 201 | 2/2 |
| 등록 / 지난 미완료 | POST | 201 | 3/3 |
| 등록 / 지난 완료 | POST | 201 | 3/3 |
| 등록 / 오늘 마감 | POST | 201 | 3/3 |
| 등록 / 미래 마감 | POST | 201 | 3/3 |
| 등록 / 다른 소속 | POST | 201 | 2/2 |
| 검색 / overdue 생략 | GET | 200 | 3/3 |
| 검색 / overdue=true | GET | 200 | 3/3 |
| 검색 / overdue=false | GET | 200 | 3/3 |
| 검색 / 조합과 날짜 양쪽 경계 | GET | 200 | 3/3 |
| 검색 / 빈 결과 | GET | 200 | 3/3 |
| 검색 / 잘못된 기간 | GET | 400 | 3/3 |
| 검색 / 잘못된 enum | GET | 400 | 2/2 |
| 소속 / 상세 차단 | GET | 404 | 2/2 |
| 소속 / 수정 차단 | PUT | 404 | 2/2 |
| 소속 / 삭제 차단 | DELETE | 404 | 2/2 |
| 실패 / 필수값 누락 | POST | 400 | 2/2 |
| 기술 / 표·코드·HTML 정제 | PUT | 200 | 4/4 |
| 완료 / 최초 DONE 진입 | PUT | 200 | 3/3 |
| 완료 / DONE 유지 | PUT | 200 | 3/3 |
| 완료 / 완료 해제 | PUT | 200 | 3/3 |
| 상세 / 수정 후 재조회 | GET | 200 | 3/3 |
| 삭제 / 업무 삭제 | DELETE | 200 | 3/3 |
| 삭제 / 재조회 404 | GET | 404 | 2/2 |
| SQL / 통합 초기 데이터 확인 | GET | 200 | 3/3 |
| 정리 / lateId | DELETE | 200 | 2/2 |
| 정리 / doneId | DELETE | 200 | 2/2 |
| 정리 / futureId | DELETE | 200 | 2/2 |
| 정리 / foreignId | DELETE | 200 | 2/2 |
