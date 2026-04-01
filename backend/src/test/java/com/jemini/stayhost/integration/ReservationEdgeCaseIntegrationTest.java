package com.jemini.stayhost.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.jemini.stayhost.support.IntegrationTestBase;
import org.junit.jupiter.api.*;
import org.springframework.http.*;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ReservationEdgeCaseIntegrationTest extends IntegrationTestBase {

    private TestPropertyData activeProperty;
    private Long inactivePropertyId;
    private Long inactiveRoomTypeId;
    private String userToken;
    private String user2Token;
    private String partner2Token;

    @BeforeAll
    void setUp() {
        // 활성 숙소 (재고 1)
        activeProperty = setupPropertyWithInventory("edge_case", 1);

        // 비활성 숙소 (같은 파트너)
        inactivePropertyId = createProperty(activeProperty.partnerToken(), "비활성 호텔");
        inactiveRoomTypeId = createRoomType(activeProperty.partnerToken(), inactivePropertyId, "스탠다드", 2, 80000, 3);
        final LocalDate start = LocalDate.now().plusDays(1);
        final LocalDate end = start.plusDays(13);
        setInventory(activeProperty.partnerToken(), inactiveRoomTypeId, start, end, 3);

        // 다른 파트너
        registerPartner("edge_partner2", "password1234", "다른 업체");
        partner2Token = loginPartner("edge_partner2", "password1234");

        // 사용자 1
        registerUser("edge1@test.com", "password1234");
        userToken = loginUser("edge1@test.com", "password1234");

        // 사용자 2
        registerUser("edge2@test.com", "password1234");
        user2Token = loginUser("edge2@test.com", "password1234");
    }

    @Test
    @Order(1)
    @DisplayName("재고 소진 후 추가 예약 실패")
    void 재고_소진_후_추가_예약_실패() {
        final LocalDate checkIn = LocalDate.now().plusDays(1);
        final LocalDate checkOut = checkIn.plusDays(1);

        // 첫 번째 예약 성공 (재고 1 → 0)
        final ResponseEntity<String> success = requestCreateReservation(
                userToken, activeProperty.propertyId(), activeProperty.roomTypeId(), checkIn, checkOut, 2);
        assertThat(success.getStatusCode().is2xxSuccessful()).isTrue();
        assertThat(getData(success).get("status").asText()).isEqualTo("CONFIRMED");

        // 두 번째 예약 실패 (재고 부족)
        final ResponseEntity<String> fail = requestCreateReservation(
                user2Token, activeProperty.propertyId(), activeProperty.roomTypeId(), checkIn, checkOut, 2);
        assertThat(fail.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        final JsonNode error = getError(fail);
        assertThat(error.get("code").asText()).isEqualTo("INVENTORY_INSUFFICIENT");
    }

    @Test
    @Order(2)
    @DisplayName("이미 취소된 예약 재취소 실패")
    void 이미_취소된_예약_재취소_실패() {
        final LocalDate checkIn = LocalDate.now().plusDays(3);
        final LocalDate checkOut = checkIn.plusDays(1);

        // 예약 생성
        final ResponseEntity<String> created = requestCreateReservation(
                userToken, activeProperty.propertyId(), activeProperty.roomTypeId(), checkIn, checkOut, 2);
        assertThat(created.getStatusCode().is2xxSuccessful()).isTrue();
        final Long reservationId = getData(created).get("id").asLong();

        // 첫 번째 취소 성공
        final ResponseEntity<String> cancelled = requestCancelReservation(userToken, reservationId, "취소 사유");
        assertThat(cancelled.getStatusCode().is2xxSuccessful()).isTrue();

        // 두 번째 취소 실패
        final ResponseEntity<String> fail = requestCancelReservation(userToken, reservationId, "다시 취소");
        assertThat(fail.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        final JsonNode error = getError(fail);
        assertThat(error.get("code").asText()).isEqualTo("RESERVATION_ALREADY_CANCELLED");
    }

    @Test
    @Order(3)
    @DisplayName("다른 회원의 예약 조회 실패")
    void 다른_회원의_예약_조회_실패() {
        final LocalDate checkIn = LocalDate.now().plusDays(5);
        final LocalDate checkOut = checkIn.plusDays(1);

        // 사용자 1이 예약
        final ResponseEntity<String> created = requestCreateReservation(
                userToken, activeProperty.propertyId(), activeProperty.roomTypeId(), checkIn, checkOut, 2);
        assertThat(created.getStatusCode().is2xxSuccessful()).isTrue();
        final Long reservationId = getData(created).get("id").asLong();

        // 사용자 2가 조회 시도
        final ResponseEntity<String> response = restTemplate.exchange(
                "/api/reservations/" + reservationId, HttpMethod.GET, authEntity(user2Token), String.class);
        assertThat(response.getStatusCode()).isIn(HttpStatus.FORBIDDEN, HttpStatus.BAD_REQUEST);
    }

    @Test
    @Order(4)
    @DisplayName("다른 파트너의 숙소 수정 실패")
    void 다른_파트너의_숙소_수정_실패() {
        final Map<String, Object> body = Map.of(
                "name", "해킹 시도",
                "description", "다른 파트너가 수정",
                "checkInTime", "14:00",
                "checkOutTime", "12:00",
                "thumbnailUrl", "https://hack.com/thumb.jpg"
        );
        final ResponseEntity<String> response = restTemplate.exchange(
                "/api/extranet/properties/" + activeProperty.propertyId(),
                HttpMethod.PUT, entity(body, partner2Token), String.class);
        assertThat(response.getStatusCode()).isIn(HttpStatus.FORBIDDEN, HttpStatus.BAD_REQUEST);
    }

    @Test
    @Order(5)
    @DisplayName("최대 수용인원 초과 예약 실패")
    void 최대_수용인원_초과_예약_실패() {
        final LocalDate checkIn = LocalDate.now().plusDays(7);
        final LocalDate checkOut = checkIn.plusDays(1);

        // maxOccupancy=4 인 객실에 guestCount=10 으로 예약
        final ResponseEntity<String> response = requestCreateReservation(
                userToken, activeProperty.propertyId(), activeProperty.roomTypeId(), checkIn, checkOut, 10);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    @Order(6)
    @DisplayName("인증없이 예약 시도시 401")
    void 인증없이_예약_시도시_401() {
        final LocalDate checkIn = LocalDate.now().plusDays(8);
        final LocalDate checkOut = checkIn.plusDays(1);

        final Map<String, Object> body = Map.ofEntries(
                Map.entry("propertyId", activeProperty.propertyId()),
                Map.entry("roomTypeId", activeProperty.roomTypeId()),
                Map.entry("checkInDate", checkIn.toString()),
                Map.entry("checkOutDate", checkOut.toString()),
                Map.entry("guestName", "무인증"),
                Map.entry("guestPhone", "010-0000-0000"),
                Map.entry("guestCount", 2)
        );
        final ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/reservations", entity(body), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    @Order(7)
    @DisplayName("체크아웃이 체크인 이전이면 예약 실패")
    void 체크아웃이_체크인_이전이면_예약_실패() {
        final LocalDate checkIn = LocalDate.now().plusDays(9);
        final LocalDate checkOut = checkIn.minusDays(1);

        final ResponseEntity<String> response = requestCreateReservation(
                userToken, activeProperty.propertyId(), activeProperty.roomTypeId(), checkIn, checkOut, 2);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        final JsonNode error = getError(response);
        assertThat(error.get("code").asText()).isEqualTo("INVALID_DATE_RANGE");
    }

    @Test
    @Order(8)
    @DisplayName("다른 회원의 예약 취소 실패")
    void 다른_회원의_예약_취소_실패() {
        final LocalDate checkIn = LocalDate.now().plusDays(9);
        final LocalDate checkOut = checkIn.plusDays(1);

        // 사용자 1이 예약 생성
        final ResponseEntity<String> created = requestCreateReservation(
                userToken, activeProperty.propertyId(), activeProperty.roomTypeId(), checkIn, checkOut, 2);
        assertThat(created.getStatusCode().is2xxSuccessful()).isTrue();
        final Long reservationId = getData(created).get("id").asLong();

        // 사용자 2가 취소 시도
        final ResponseEntity<String> response = requestCancelReservation(user2Token, reservationId, "남의 예약 취소");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.FORBIDDEN);
        final JsonNode error = getError(response);
        assertThat(error.get("code").asText()).isEqualTo("FORBIDDEN");
    }

    @Test
    @Order(9)
    @DisplayName("존재하지 않는 예약 조회시 404")
    void 존재하지_않는_예약_조회시_404() {
        final ResponseEntity<String> response = restTemplate.exchange(
                "/api/reservations/999999", HttpMethod.GET, authEntity(userToken), String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        final JsonNode error = getError(response);
        assertThat(error.get("code").asText()).isEqualTo("RESERVATION_NOT_FOUND");
    }

    @Test
    @Order(10)
    @DisplayName("비활성 숙소의 객실 예약 시도")
    void 비활성_숙소의_객실_예약_시도() {
        final LocalDate checkIn = LocalDate.now().plusDays(10);
        final LocalDate checkOut = checkIn.plusDays(1);

        // 비활성 숙소(INACTIVE)의 객실에 예약 시도 - 재고는 설정됨
        final ResponseEntity<String> response = requestCreateReservation(
                userToken, inactivePropertyId, inactiveRoomTypeId, checkIn, checkOut, 2);

        // 비활성 숙소에 대한 예약은 차단되어야 함
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        final JsonNode error = getError(response);
        assertThat(error.get("code").asText()).isEqualTo("PROPERTY_NOT_ACTIVE");
    }
}
