# CLAUDE.md — 프로젝트 컨텍스트

## 설계 문서
- 아키텍처/도메인/ERD/API: docs/design/ (11개)
- 코딩 원칙/네이밍/스타일: docs/design/07-code-conventions.md
- 보안(JWT/인가): docs/design/10-security-design.md
- 에러/로깅: docs/design/08-error-logging-strategy.md
- 모니터링: docs/design/09-monitoring-design.md
- 테스트 전략: docs/design/11-test-strategy.md
- 일정/구현 스텝: docs/timeline.md
- 기능별 설계 고민: docs/journal/design-decisions.md

## 코드 수정 후 검증 (매번 필수)
1. `cd backend && ./gradlew compileJava` — 컴파일 확인
2. `cd backend && ./gradlew test` — 테스트 통과
3. `docker compose up` — MySQL + 앱 기동 확인
4. 검증 통과 후 commit + push