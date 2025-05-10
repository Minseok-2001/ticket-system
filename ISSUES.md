# ISSUES.md

## 환경 설정

- [ ] AWS 인프라 설정 (EC2, EKS, RDS, ElastiCache)
- [ ] Kafka 브로커 설정 및 토픽 구성
- [ ] Redis 설정 (대기열, 캐싱, 세션 관리용)
- [ ] MySQL 스키마 설계 및 구현
- [ ] Kubernetes 클러스터 구성
- [ ] CI/CD 파이프라인 구축 (Jenkins)

## 사용자 인증

- [ ] JWT 기반 인증 시스템 구현
- [ ] Spring Security 설정
- [ ] Redis 세션 관리 구현 (TTL 설정)
- [ ] 로그인/로그아웃 API 개발

## 대기열 시스템

- [ ] Redis Sorted Set을 사용한 대기열 구현
- [ ] 분산 락(Redlock 알고리즘) 구현
- [ ] 대기열 순번 관리 및 상태 조회 API 개발
- [ ] 실시간 대기 순번 및 예상 시간 표시 기능

## 티켓 예매 기능

- [ ] 티켓 재고 관리 시스템 개발
- [ ] Kafka를 통한 예매 요청 비동기 처리 구현
- [ ] 티켓 예매 API 개발 (`/api/tickets/reserve`)
- [ ] 예매 상태 조회 API 개발 (`/api/tickets/status`)
- [ ] 더미 결제 시스템 연동

## 알림 서비스

- [ ] 디바이스 토큰 등록 API 개발 (`/api/notifications/register`)
- [ ] Redis를 사용한 디바이스 토큰 관리 구현
- [ ] Kafka 이벤트 발행 및 소비 로직 개발
- [ ] AWS SNS 연동 및 푸시 알림 전송 구현

## 모니터링 및 로깅

- [ ] Prometheus 설정 및 메트릭 수집
- [ ] Grafana 대시보드 구성
- [ ] TPS, latency, 알림 성공률 메트릭 설정
- [ ] ELK Stack 구성 및 로그 분석 시스템 설정

## 테스트

- [ ] 단위 테스트 작성 (JUnit)
- [ ] 부하 테스트 시나리오 작성 (JMeter)
- [ ] 대기열 처리 성능 테스트
- [ ] 알림 전송 성능 테스트

## UI/UX

- [ ] 기본 예매 UI 개발 (이벤트 목록, 티켓 선택 화면)
- [ ] 대기열 순번 표시 UI 개발
- [ ] 알림 링크 및 재진입 페이지 개발
- [ ] 모바일/웹 반응형 디자인 적용

## 문서화

- [ ] API 문서화 (OpenAPI 스펙)
- [ ] 시스템 아키텍처 다이어그램 작성
- [ ] 개발자 가이드 작성
- [ ] 운영자 가이드 작성
