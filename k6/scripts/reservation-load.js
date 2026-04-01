import http from "k6/http";
import { check, sleep } from "k6";
import { Trend, Rate, Counter } from "k6/metrics";

const BASE_URL = __ENV.BASE_URL || "http://localhost:8080";
const SEED_DATA = JSON.parse(open("./seed-data.json"));

// ── 커스텀 메트릭 ──

const reservationDuration = new Trend("reservation_duration", true);
const reservationSuccess = new Rate("reservation_success");
const successCount = new Counter("reservation_success_count");
const failCount = new Counter("reservation_fail_count");

// ── 옵션 ──

export const options = {
    scenarios: {
        reservation_load: {
            executor: "constant-vus",
            vus: 50,
            duration: "10s",
        },
    },
    thresholds: {
        reservation_duration: ["p(99)<2000"],
        reservation_success: ["rate>0.0"],
    },
};

// ── VU 시나리오: 예약 생성 ──

export default function () {
    const propertyId = SEED_DATA.propertyIds[Math.floor(Math.random() * SEED_DATA.propertyIds.length)];
    const roomTypeId = SEED_DATA.roomTypeIds[Math.floor(Math.random() * SEED_DATA.roomTypeIds.length)];

    const res = http.post(
        `${BASE_URL}/api/reservations`,
        JSON.stringify({
            propertyId: propertyId,
            roomTypeId: roomTypeId,
            checkInDate: SEED_DATA.startDate,
            checkOutDate: SEED_DATA.endDate,
            guestName: `k6유저_VU${__VU}`,
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
        reservationSuccess.add(true);
        successCount.add(1);
    } else {
        reservationSuccess.add(false);
        failCount.add(1);
    }

    sleep(0.1);
}
