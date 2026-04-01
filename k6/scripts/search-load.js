import http from "k6/http";
import { check, sleep } from "k6";
import { Trend, Rate } from "k6/metrics";

const BASE_URL = __ENV.BASE_URL || "http://localhost:8080";
const SEED_DATA = JSON.parse(open("./seed-data.json"));

// ── 커스텀 메트릭 ──

const searchDuration = new Trend("search_duration", true);
const detailDuration = new Trend("detail_duration", true);
const rateDuration = new Trend("rate_duration", true);
const searchSuccess = new Rate("search_success");

// ── 옵션 ──

export const options = {
    scenarios: {
        search_load: {
            executor: "constant-vus",
            vus: 100,
            duration: "30s",
        },
    },
    thresholds: {
        search_duration: ["p(99)<500"],
        detail_duration: ["p(99)<500"],
        rate_duration: ["p(99)<500"],
        search_success: ["rate>0.99"],
    },
};

// ── VU 시나리오: 검색 → 상세 → 요금 조회 ──

export default function () {
    // 1. 숙소 검색
    const searchRes = http.get(`${BASE_URL}/api/public/search/properties`);
    searchDuration.add(searchRes.timings.duration);
    const searchOk = check(searchRes, {
        "검색 200": (r) => r.status === 200,
    });
    searchSuccess.add(searchOk);

    sleep(0.5);

    // 2. 랜덤 숙소 상세 조회
    const randomIdx = Math.floor(Math.random() * SEED_DATA.propertyIds.length);
    const propertyId = SEED_DATA.propertyIds[randomIdx];
    const detailRes = http.get(`${BASE_URL}/api/public/properties/${propertyId}`);
    detailDuration.add(detailRes.timings.duration);
    check(detailRes, {
        "상세 200": (r) => r.status === 200,
    });

    sleep(0.3);

    // 3. 요금 조회
    const rateRes = http.get(
        `${BASE_URL}/api/public/search/properties/${propertyId}/rates?startDate=${SEED_DATA.startDate}&endDate=${SEED_DATA.endDate}`
    );
    rateDuration.add(rateRes.timings.duration);
    check(rateRes, {
        "요금 200": (r) => r.status === 200,
    });

    sleep(0.2);
}
