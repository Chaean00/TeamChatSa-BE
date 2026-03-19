# TeamChatSa (팀찾사) Backend

조기축구 팀들이 상대 팀을 찾을 때 반복하던 비효율을 줄이기 위해 만든 **아마추어 축구팀 매칭 플랫폼**입니다.  
커뮤니티 글 검색, 지인 소개, 오픈 채팅 의존도가 높은 과정을 서비스 안으로 가져오고, 목록 조회, 지도 탐색, AI 추천까지 하나의 흐름으로 연결하는 데 초점을 맞췄습니다.

<img width="1920" height="911" alt="팀찾사1" src="https://github.com/user-attachments/assets/6d6a7736-c6ce-4135-a911-16f99ae1e057" />

<img width="1905" height="909" alt="팀찾사2" src="https://github.com/user-attachments/assets/79313f29-db23-41a3-b966-e4300191c35f" />

## 프로젝트 개요

- 매치 게시물 등록, 조회, 신청, 수락/거절까지 매칭 과정을 백엔드에서 일관되게 처리합니다.
- 목록 조회는 자주 조회되는 조건을 Redis에 선택적으로 캐싱해 반복 요청 비용을 줄였습니다.
- 지도 조회는 PostGIS 기반 공간 인덱스와 KNN 정렬을 사용해 줌 레벨에 따라 최대 40개의 마커만 빠르게 반환합니다.
- AI 추천은 사용자의 자연어 입력을 구조화한 뒤, 정형 필터와 벡터 검색을 결합해 후보를 추천합니다.

## 기술 스택

### Backend
- Java 17
- Spring Boot 3.5
- Spring Security, JWT
- Spring Data JPA, QueryDSL
- Spring AI

### Database
- PostgreSQL
- PostGIS
- pgvector
- Redis, Redisson

### Infra / Ops
- Docker
- Flyway
- Prometheus, Grafana
- GCP Storage

-----

## 주요 기능

### 1. 매치 게시물 목록 조회

- 무한 스크롤 기반으로 매치 게시물을 조회합니다.
- 날짜, 인원 수, 지역 조건을 동적으로 조합할 수 있습니다.
- 자주 조회되는 페이지와 필터 조합만 선별적으로 캐싱해 캐시 효율과 정합성을 함께 가져가도록 설계했습니다.

### 2. 위치 기반 매치 조회

- 지도 화면의 bounding box와 중심 좌표를 기준으로 매치를 조회합니다.
- 저줌에서는 화면 전체 bbox를 유지하고, 고줌에서는 중심 기준의 더 작은 bbox를 만들어 탐색 범위를 줄입니다.
- 분산 마커 선별 로직 대신 `PostGIS GiST + KNN ORDER BY <-> + LIMIT 40` 구조로 단순화해 대량 후보 정렬 비용을 줄였습니다.

### 3. 사용자 입력 기반 AI 매치 추천

- 사용자가 입력한 문장을 그대로 조건식으로 강제하지 않고, 먼저 검색 의도로 해석합니다.
- LLM이 지역, 레벨 범위, 승률 조건, 스타일 키워드를 JSON으로 파싱합니다.
- 파싱된 결과는 서비스 규칙에 맞게 다시 보정하고, 실패 시에는 규칙 기반 fallback으로 안전하게 처리합니다.
- 최종 추천은 정형 필터와 pgvector 유사도 검색을 함께 사용해 10건 이내로 반환합니다.

### 4. 팀 운영 기능

- 팀 생성, 팀원 관리, 팀 가입 신청/처리 기능을 제공합니다.
- 매치 신청 수락/거절 등 동시성 이슈가 있는 구간은 Redisson 기반 분산 락으로 보호했습니다.

-----

## 기술 선택 이유

- PostgreSQL + PostGIS  
  → 지도 기반 반경 검색과 공간 인덱스를 활용하기 위해 선택

- Spring AI + pgvector
  → LLM 모델을 보다 편하게 사용할 수 있으며, 추가적인 Vector DB 인프라를 사용하지않기 위해 선택

- QueryDSL  
  → 동적 검색 조건과 복잡한 정렬 요구사항 대응

- Redis / Redisson <br>
  → 트래픽 집중 구간(매치 리스트 API) 응답 안정화 및 분산락에 이용

- Flyway  
  → 개인 프로젝트지만 운영 환경 기준의 DB 형상 관리 경험을 위해 도입

-----

## 시스템 아키텍처
<img width="625" height="590" alt="image" src="https://github.com/user-attachments/assets/8da86c6f-65cc-4eaa-adc8-f6e1914991ad" />


-----

## 부하 테스트

### 테스트 환경

- 애플리케이션 서버: `i5-8500 (6 cores)`, `RAM 16GB`
- 애플리케이션: Spring Boot 단일 인스턴스
- 데이터 저장소: PostgreSQL + PostGIS + pgvector, Redis
- 부하 생성기: `k6`
- 모니터링: Prometheus, Grafana, 애플리케이션/DB 실행 계획 및 시스템 리소스 확인

### 테스트 방식

- 테스트 시간은 `2분`으로 고정하고, `RPS`를 높여가며 평균 응답 시간, `p95`, `p99`, 에러율, CPU 사용률 변화를 함께 확인했습니다.

### 테스트 결과

#### 매치 리스트 조회 API - `GET /api/v1/matches`

[캐시 X + 인덱스 X]

RPS 20
<p align="center">
  <img width="48%" src="https://github.com/user-attachments/assets/c3a2d1e4-d3f2-43e9-833f-559206d71c95" />
  <img width="48%" src="https://github.com/user-attachments/assets/bd88fbcd-cadf-4475-a00b-ebc071b70b28" />
</p>
<img width="298" height="135" alt="image" src="https://github.com/user-attachments/assets/10ca2bfa-5570-4d68-94ab-3212d61e52e6" />

[캐시 X + 인덱스 O]

RPS 300
<p align="center">
  <img width="48%" src="https://github.com/user-attachments/assets/6e63ac17-d6f6-4f87-a04b-ef51dcc15a7d" />
  <img width="48%" src="https://github.com/user-attachments/assets/7b9b023b-e876-4cde-90f2-72ee1f363879" />
</p>
<img width="294" height="131" alt="image" src="https://github.com/user-attachments/assets/c3f841e0-293e-49ec-a194-d1d4394e41b5" />

[캐시 O + 인덱스 O (보조 인덱스 포함)

RPS 300
<p align="center">
  <img width="48%" src="https://github.com/user-attachments/assets/41007256-5f76-4683-8571-5690fe776de4" />
  <img width="48%" src="https://github.com/user-attachments/assets/686a4d07-fce1-4a97-a6cb-08a6892d4391" />
</p>
<img width="282" height="138" alt="image" src="https://github.com/user-attachments/assets/046a0bb3-3d6b-4823-b04e-016ac0181717" />

<br/>

#### 위치 기반 조회 API - `GET /api/c1/matches/map`

RPS 1200
<p align="center">
  <img width="48%" src="https://github.com/user-attachments/assets/d11569b9-638e-4ff4-b68a-483117f62ade" />
  <img width="48%" src="https://github.com/user-attachments/assets/bc216a06-ba3f-474c-9818-61f181389cdb" />
</p>
<img width="70%" src="https://github.com/user-attachments/assets/31845268-c03b-45f3-b395-bb3211274923" />
<img width="309" height="141" alt="image" src="https://github.com/user-attachments/assets/d29a46f0-3821-4dfe-a41b-1a6231d10f07" />



