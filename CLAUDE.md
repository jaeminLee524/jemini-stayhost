# CLAUDE.md — 프로젝트 컨텍스트

## 설계 문서
- 아키텍처/도메인/ERD/API: docs/design/ (20개)
- 코딩 원칙/네이밍/스타일: docs/design/08-code-conventions.md
- 보안(JWT/인가): docs/design/11-security-design.md
- 에러/로깅: docs/design/09-error-logging-strategy.md
- 일정/구현 스텝: docs/timeline.md

## 코드 수정 후 검증 (매번 필수)
1. `./gradlew compileJava` — 컴파일 확인
2. `./gradlew test` — 테스트 통과
3. `docker compose up` — MySQL + 앱 기동 확인
4. 검증 통과 후 commit + push

## 주의사항
- 코드/문서에 "여기어때", "채용 과제" 포함 금지
- bold 마크다운 사용 금지 — IntelliJ에서 한글 앞뒤 렌더링 안 됨
- @AuthenticationPrincipal 사용 금지 — 래퍼 타입(UserId, PartnerId) + ArgumentResolver 사용

## 커밋 규칙
Conventional Commits: `feat:`, `fix:`, `docs:`, `refactor:`, `test:`, `chore:`
