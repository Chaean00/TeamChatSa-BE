# TeamChatSa (팀찾사) Backend

조기축구 팀들이 상대팀을 찾기 위해 반복하는 비효율을 줄이기 위해 만든 **아마추어 축구팀 매칭 플랫폼**입니다.
- 네이버 카페 / 커뮤니티 글 검색 / 지인 추천 등의 과정을 플랫폼화

<img width="1920" height="911" alt="팀찾사1" src="https://github.com/user-attachments/assets/6d6a7736-c6ce-4135-a911-16f99ae1e057" />

<img width="1905" height="909" alt="팀찾사2" src="https://github.com/user-attachments/assets/79313f29-db23-41a3-b966-e4300191c35f" />

## 기술 스택

### Backend
- Java 17, Spring Boot 3
- Spring Security, JWT
- JPA, QueryDSL

### Database
- PostgreSQL + PostGIS
- Redis + Redisson

### Infra / Ops
- GCP
- Docker
- Prometheus + Grafana
- Flyway

-----

## 핵심 기능
- JWT 기반 인증/인가 및 카카오 로그인
- 지도 기반의 팀 검색 (PostGIS)
- 목록 기반의 팀 검색
- 팀 생성 및 팀원 관리

-----

## 기술 선택 이유

- PostgreSQL + PostGIS  
  → 지도 기반 반경 검색과 공간 인덱스를 활용하기 위해 선택

- QueryDSL  
  → 동적 검색 조건과 복잡한 정렬 요구사항 대응

- Redis / Redisson <br>
  → 트래픽 집중 구간(매치 리스트 API) 응답 안정화 및 분산락에 이용

- Flyway  
  → 개인 프로젝트지만 운영 환경 기준의 DB 형상 관리 경험을 위해 도입

-----

## 시스템 아키텍처
<img width="788" height="650" alt="팀찾사_아키텍처" src="https://github.com/user-attachments/assets/121722a0-6965-4618-88b7-a51381d6707f" />

-----

## 성능 개선 사례

### 1️⃣ 로그인 API 성능 개선
- 문제
  - 48만 명 규모 부하 테스트에서 DB 커넥션 병목 발생
  - p95 응답 시간 2.57s

- 원인
  - 단순 조회 로직에도 @Transactional 적용

- 해결
  - Read-only 인증 로직에서 트랜잭션 제거
  - 인증 로직과 DB 접근 책임 분리

- 결과
  - RPS 17.7 → 28.4 (+60%)
  - p95 2.57s → 0.99s
  - CPU 사용률 안정화


### 2️⃣ 지도 검색 API 최적화
- 문제
  - ST_Within만을 사용해 공간 필터링을 수행하여 GIST 인덱스 (공간인덱스)가 적극적으로 사용되지않고, 불필요한 캐스팅으로 공간 인덱스를 활용하지 못하는 문제점을 발견
- 해결
  - SQL 쿼리 최적화
    - && 조건 및 ST_Intersects 사용으로 공간 인덱스를 효과적으로 사용하도록 변경

- 결과
  - 평균 응답 시간 282ms -> 79ms (-70%)
  - p95 1520ms -> 395ms (-74%)
  - p99 2655ms -> 740ms (-72%)

