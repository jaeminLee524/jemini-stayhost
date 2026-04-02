# k6 부하 테스트

## 사전 준비

```bash
# k6 설치 (macOS)
brew install k6

# Docker Compose로 앱 기동
docker compose up -d
```

## 실행 방법

```bash
# 1. 테스트 데이터 시딩 (필수 - seed-data.json 생성)
bash k6/scripts/seed.sh

# 2. 부하 테스트 실행
k6 run k6/scripts/mixed-load.js       # 종합 부하 (검색 70% + 상세 + 예약)
k6 run k6/scripts/search-load.js      # 검색 전용
k6 run k6/scripts/reservation-load.js # 예약 전용
```

## 스크립트 구성

| 파일 | 설명 | 최대 VUs | 시간 |
|------|------|---------|------|
| mixed-load.js | 검색/상세/예약 동시 실행 (ramping) | 280 | 80s |
| search-load.js | 검색 → 상세 → 요금 조회 플로우 | 100 | 30s |
| reservation-load.js | 예약 생성 집중 | 50 | 10s |
| helpers.js | API 호출 헬퍼 함수 | - | - |
| seed.sh | 테스트 데이터 시딩 스크립트 | - | - |

## 커스텀 옵션

```bash
# 다른 서버 대상으로 실행
k6 run -e BASE_URL=http://your-server:8080 k6/scripts/mixed-load.js
```

## 테스트 결과

최신 결과는 `docs/test/k6-load-test-report.md` 참고.
