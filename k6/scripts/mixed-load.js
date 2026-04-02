import http from "k6/http";
import { check, sleep } from "k6";
import { Trend, Rate, Counter } from "k6/metrics";

const BASE_URL = __ENV.BASE_URL || "http://localhost:8080";
const SEED_DATA = JSON.parse(open("./seed-data.json"));

// ── 커스텀 메트릭 ──

const searchDuration = new Trend("search_duration", true);
const detailDuration = new Trend("detail_duration", true);
const rateDuration = new Trend("rate_duration", true);
const reservationDuration = new Trend("reservation_duration", true);
const searchSuccess = new Rate("search_success");
const detailSuccess = new Rate("detail_success");
const rateSuccess = new Rate("rate_success");
const reservationSuccessCount = new Counter("reservation_success_count");
const reservationFailCount = new Counter("reservation_fail_count");

// ── 검색 파라미터 풀 (캐시 히트/미스 혼합) ──

const REGIONS = ["서울", "서울", "서울", "부산", "제주"];
const KEYWORDS = ["", "", "", "호텔", "테스트"];

// ── 옵션: 시나리오별 분리 ──

export const options = {
    scenarios: {
        // 검색 트래픽: 가장 높은 볼륨 (ramping)
        search_browse: {
            executor: "ramping-vus",
            startVUs: 0,
            stages: [
                { duration: "10s", target: 50 },
                { duration: "30s", target: 100 },
                { duration: "10s", target: 150 },
                { duration: "20s", target: 150 },
                { duration: "10s", target: 0 },
            ],
            exec: "searchBrowse",
            gracefulRampDown: "5s",
        },
        // 상세+요금 조회: 중간 볼륨
        detail_view: {
            executor: "ramping-vus",
            startVUs: 0,
            stages: [
                { duration: "10s", target: 20 },
                { duration: "30s", target: 50 },
                { duration: "10s", target: 80 },
                { duration: "20s", target: 80 },
                { duration: "10s", target: 0 },
            ],
            exec: "detailView",
            gracefulRampDown: "5s",
        },
        // 예약 생성: 낮은 볼륨이지만 쓰기 경합 발생
        reservation_create: {
            executor: "ramping-vus",
            startVUs: 0,
            stages: [
                { duration: "15s", target: 10 },
                { duration: "30s", target: 30 },
                { duration: "20s", target: 50 },
                { duration: "15s", target: 0 },
            ],
            exec: "reservationCreate",
            gracefulRampDown: "5s",
        },
    },
    thresholds: {
        search_duration: ["p(95)<300", "p(99)<500"],
        detail_duration: ["p(95)<300", "p(99)<500"],
        rate_duration: ["p(95)<500", "p(99)<1000"],
        reservation_duration: ["p(95)<1000", "p(99)<2000"],
        search_success: ["rate>0.99"],
        detail_success: ["rate>0.99"],
        rate_success: ["rate>0.99"],
    },
};

// ── 시나리오 1: 검색 (캐시 히트/미스 혼합) ──

export function searchBrowse() {
    const region = REGIONS[Math.floor(Math.random() * REGIONS.length)];
    const keyword = KEYWORDS[Math.floor(Math.random() * KEYWORDS.length)];
    const page = Math.floor(Math.random() * 3);

    let url = `${BASE_URL}/api/public/search/properties?page=${page}&size=10`;
    if (region) url += `&region=${encodeURIComponent(region)}`;
    if (keyword) url += `&keyword=${encodeURIComponent(keyword)}`;

    const res = http.get(url);
    searchDuration.add(res.timings.duration);
    searchSuccess.add(check(res, { "검색 200": (r) => r.status === 200 }));

    sleep(Math.random() * 0.5 + 0.2);
}

// ── 시나리오 2: 상세 + 요금 조회 ──

export function detailView() {
    const propertyId = SEED_DATA.propertyIds[Math.floor(Math.random() * SEED_DATA.propertyIds.length)];

    // 상세 조회
    const detailRes = http.get(`${BASE_URL}/api/public/properties/${propertyId}`);
    detailDuration.add(detailRes.timings.duration);
    detailSuccess.add(check(detailRes, { "상세 200": (r) => r.status === 200 }));

    sleep(Math.random() * 0.3 + 0.1);

    // 요금 조회 (동일 숙소 - 캐시 히트 기대)
    const rateRes = http.get(
        `${BASE_URL}/api/public/search/properties/${propertyId}/rates?startDate=${SEED_DATA.startDate}&endDate=${SEED_DATA.endDate}`
    );
    rateDuration.add(rateRes.timings.duration);
    rateSuccess.add(check(rateRes, { "요금 200": (r) => r.status === 200 }));

    sleep(Math.random() * 0.3 + 0.1);
}

// ── 시나리오 3: 예약 생성 ──

export function reservationCreate() {
    const ROOM_TYPES_PER_PROPERTY = Math.floor(SEED_DATA.roomTypeIds.length / SEED_DATA.propertyIds.length);
    const idx = Math.floor(Math.random() * SEED_DATA.propertyIds.length);
    const propertyId = SEED_DATA.propertyIds[idx];
    const roomTypeIdx = idx * ROOM_TYPES_PER_PROPERTY + Math.floor(Math.random() * ROOM_TYPES_PER_PROPERTY);
    const roomTypeId = SEED_DATA.roomTypeIds[roomTypeIdx];

    const res = http.post(
        `${BASE_URL}/api/reservations`,
        JSON.stringify({
            propertyId: propertyId,
            roomTypeId: roomTypeId,
            checkInDate: SEED_DATA.startDate,
            checkOutDate: SEED_DATA.endDate,
            guestName: `k6_VU${__VU}_${__ITER}`,
            guestPhone: "010-0000-0000",
            guestCount: 2,
        }),
        {
            headers: {
                "Content-Type": "application/json",
                Authorization: `Bearer ${SEED_DATA.userToken}`,
            },
        }
    );

    reservationDuration.add(res.timings.duration);

    const isSuccess = res.status === 201;
    const isExpectedFail = res.status === 400 || res.status === 409;

    check(res, {
        "예약 성공 또는 재고 부족": (r) => isSuccess || isExpectedFail,
    });

    if (isSuccess) {
        reservationSuccessCount.add(1);
    } else {
        reservationFailCount.add(1);
    }

    sleep(Math.random() * 0.5 + 0.3);
}
