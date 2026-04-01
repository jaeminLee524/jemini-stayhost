import http from "k6/http";

const BASE_URL = __ENV.BASE_URL || "http://localhost:8080";

const JSON_HEADERS = { "Content-Type": "application/json" };

function authHeaders(token) {
    return {
        "Content-Type": "application/json",
        Authorization: `Bearer ${token}`,
    };
}

function extractData(response) {
    const body = JSON.parse(response.body);
    return body.data;
}

// ── 파트너 시딩 ──

export function registerPartner(loginId, password, businessName) {
    const uniqueSuffix = Date.now() + Math.floor(Math.random() * 100000);
    const res = http.post(
        `${BASE_URL}/api/extranet/partners`,
        JSON.stringify({
            businessName: businessName,
            businessNumber: `${uniqueSuffix}`,
            representative: "테스트대표",
            phone: "02-1234-5678",
            email: `${loginId}@test.com`,
            bankName: "국민은행",
            bankAccount: "123456-78-901234",
            loginId: loginId,
            password: password,
            adminName: "테스트관리자",
        }),
        { headers: JSON_HEADERS }
    );
    return extractData(res);
}

export function loginPartner(loginId, password) {
    const res = http.post(
        `${BASE_URL}/api/public/extranet/auth/login`,
        JSON.stringify({ loginId, password }),
        { headers: JSON_HEADERS }
    );
    return extractData(res).accessToken;
}

export function createProperty(token, name) {
    const res = http.post(
        `${BASE_URL}/api/extranet/properties`,
        JSON.stringify({
            name: name,
            type: "HOTEL",
            description: "k6 부하 테스트용 숙소",
            address: "서울특별시 강남구 테헤란로 123",
            region: "서울",
            latitude: 37.4979,
            longitude: 127.0276,
            checkInTime: "15:00",
            checkOutTime: "11:00",
            thumbnailUrl: "https://cdn.example.com/thumb.jpg",
        }),
        { headers: authHeaders(token) }
    );
    return extractData(res).id;
}

export function activateProperty(token, propertyId) {
    http.patch(
        `${BASE_URL}/api/extranet/properties/${propertyId}/status`,
        JSON.stringify({ status: "ACTIVE" }),
        { headers: authHeaders(token) }
    );
}

export function createRoomType(token, propertyId, name, maxOccupancy, basePrice, totalRoomCount) {
    const res = http.post(
        `${BASE_URL}/api/extranet/properties/${propertyId}/room-types`,
        JSON.stringify({
            name: name,
            description: "k6 테스트 객실",
            maxOccupancy: maxOccupancy,
            basePrice: basePrice,
            amenities: ["WiFi", "TV", "에어컨"],
            totalRoomCount: totalRoomCount,
        }),
        { headers: authHeaders(token) }
    );
    return extractData(res).id;
}

export function setRates(token, roomTypeId, startDate, endDate, price) {
    http.put(
        `${BASE_URL}/api/extranet/room-types/${roomTypeId}/rates`,
        JSON.stringify({ startDate, endDate, price }),
        { headers: authHeaders(token) }
    );
}

export function setInventory(token, roomTypeId, startDate, endDate, totalCount) {
    http.put(
        `${BASE_URL}/api/extranet/room-types/${roomTypeId}/inventory`,
        JSON.stringify({ startDate, endDate, totalCount }),
        { headers: authHeaders(token) }
    );
}

// ── 고객 시딩 ──

export function registerUser(email, password) {
    const res = http.post(
        `${BASE_URL}/api/public/users/signup`,
        JSON.stringify({
            email: email,
            password: password,
            name: "테스트유저",
            phone: "010-1234-5678",
        }),
        { headers: JSON_HEADERS }
    );
    return extractData(res);
}

export function loginUser(email, password) {
    const res = http.post(
        `${BASE_URL}/api/public/users/login`,
        JSON.stringify({ email, password }),
        { headers: JSON_HEADERS }
    );
    return extractData(res).accessToken;
}

// ── 공통 유틸 ──

export function formatDate(date) {
    return date.toISOString().split("T")[0];
}

export function futureDate(daysFromNow) {
    const d = new Date();
    d.setDate(d.getDate() + daysFromNow);
    return formatDate(d);
}

export { BASE_URL, JSON_HEADERS, authHeaders, extractData };
