# news-alpha-backend
AI가 뉴스 데이터를 분석해 시장 흐름과 투자 신호를 발견하는 서비스

## Stack
### Backend
- Spring Boot : 백엔드 API 서버
- Spring AI : LLM 연동 및 RAG 구현
- Spring Security & JWT : 인증 및 권한 관리
- JPA : ORM
- Querydsl : 타입 안전 동적 쿼리

### Database
- PostgreSQL : 메인 DB
- pgvector : 뉴스 임베딩 벡터 저장 및 유사도 검색
- Redis : 캐싱 및 리프레시 토큰

### Infra
- AWS : 클라우드
- Nginx : 리버스 프록시 및 정적 리소스 서빙
- Docker & Docker Compose : 컨테이너 기반 배포
- Github Actions : CI/CD

### Monitoring
- Prometheus : 메트릭 수집
- Grafana : 메트릭 시각화 및 모니터링 대시보드
- Loki : 로그 수집

### Frontend
- React & TypeScript : UI 컴포넌트 기반 SPA
- Vite : 빠른 개발 서버 및 번들링
- TanStack Query : 서버 상태 캐싱 및 동기화
- Zustand : 로그인 토큰 등의 클라이언트 전역 상태 관리
- React Hook Form & Zod : Form 입력 처리 및 유효성 검사
- Axios : API 요청 및 JWT 인터셉터

## 핵심기능
- 자동 뉴스 수집 — 네이버 뉴스 API를 통해 최신 뉴스 데이터 자동 수집
- 맥락 기반 뉴스 분석 — RAG를 활용해 뉴스 맥락을 이해하고 시장 흐름 분석
