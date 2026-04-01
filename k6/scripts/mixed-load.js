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
const reservationSuccessCount = new Counter("reservation_success_count");
const reservationFailCount = new Counter("reservation_fail_count");

// ── 옵션 ──

export const options = {
    scenarios: {
        mixed_load: {
            executor: "constant-vus",
            vus: 200,
            duration: "60s",
        },
    },
    thresholds: {
        search_duration: ["p(99)<500"],
        reservation_duration: ["p(99)<2000"],
        search_success: ["rate>0.99"],
    },
};

// ── VU 시나리오: 검색 70% + 예약 30% ──

export default function () {
    if (Math.random() < 0.7) {
        searchFlow();
    } else {
        reservationFlow();
    }
}

function searchFlow() {
    // 1. 숙소 검색
    const searchRes = http.get(`${BASE_URL}/api/public/search/properties`);
    searchDuration.add(searchRes.timings.duration);
    const ok = check(searchRes, { "검색 200": (r) => r.status === 200 });
    searchSuccess.add(ok);

    sleep(0.3);

    // 2. 랜덤 숙소 상세
    const propertyId = SEED_DATA.propertyIds[Math.floor(Math.random() * SEED_DATA.propertyIds.length)];
    const detailRes = http.get(`${BASE_URL}/api/public/properties/${propertyId}`);
    detailDuration.add(detailRes.timings.duration);
    check(detailRes, { "상세 200": (r) => r.status === 200 });

    sleep(0.2);

    // 3. 요금 조회
    const rateRes = http.get(
        `${BASE_URL}/api/public/search/properties/${propertyId}/rates?startDate=${SEED_DATA.startDate}&endDate=${SEED_DATA.endDate}`
    );
    rateDuration.add(rateRes.timings.duration);
    check(rateRes, { "요금 200": (r) => r.status === 200 });

    sleep(0.2);
}

function reservationFlow() {
    const propertyId = SEED_DATA.propertyIds[Math.floor(Math.random() * SEED_DATA.propertyIds.length)];
    const roomTypeId = SEED_DATA.roomTypeIds[Math.floor(Math.random() * SEED_DATA.roomTypeIds.length)];

    const res = http.post(
        `${BASE_URL}/api/reservations`,
        JSON.stringify({
            propertyId: propertyId,
            roomTypeId: roomTypeId,
            checkInDate: SEED_DATA.startDate,
            checkOutDate: SEED_DATA.endDate,
            guestName: `k6혼합_VU${__VU}`,
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

    sleep(0.1);
}
