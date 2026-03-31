package com.jemini.stayhost.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.jemini.stayhost.support.IntegrationTestBase;
import org.junit.jupiter.api.*;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserReservationFlowIntegrationTest extends IntegrationTestBase {

    private static final String USER_EMAIL = "reservation_flow@test.com";
    private static final String USER_PASSWORD = "password1234";

    private TestPropertyData propertyData;
    private String userToken;
    private Long reservationId;
    private LocalDate checkIn;
    private LocalDate checkOut;

    @BeforeAll
    void setUp() {
        propertyData = setupPropertyWithInventory("reservation_flow", 5);
        checkIn = LocalDate.now().plusDays(3);
        checkOut = checkIn.plusDays(2);
    }

    @Test
    @Order(1)
    @DisplayName("회원가입 성공")
    void 회원가입_성공() {
        final Long userId = registerUser(USER_EMAIL, USER_PASSWORD);
        assertThat(userId).isPositive();
    }

    @Test
    @Order(2)
    @DisplayName("로그인 성공")
    void 로그인_성공() {
        userToken = loginUser(USER_EMAIL, USER_PASSWORD);
        assertThat(userToken).isNotBlank();
    }

    @Test
    @Order(3)
    @DisplayName("숙소 검색 성공")
    void 숙소_검색_성공() {
        final ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/public/search/properties?region=서울", String.class);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        final JsonNode data = getData(response);
        assertThat(data.get("content")).isNotEmpty();
    }

    @Test
    @Order(4)
    @DisplayName("숙소 상세 조회 성공")
    void 숙소_상세_조회_성공() {
        final ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/public/properties/" + propertyData.propertyId(), String.class);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        final JsonNode data = getData(response);
        assertThat(data.get("name").asText()).isEqualTo("테스트 호텔");
        assertThat(data.get("roomTypes")).isNotEmpty();
    }

    @Test
    @Order(5)
    @DisplayName("요금 조회 성공")
    void 요금_조회_성공() {
        final ResponseEntity<String> response = restTemplate.getForEntity(
                "/api/public/search/properties/" + propertyData.propertyId() + "/rates?startDate=" + checkIn + "&endDate=" + checkOut,
                String.class);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        final JsonNode data = getData(response);
        assertThat(data.get("roomTypes")).isNotEmpty();
    }

    @Test
    @Order(6)
    @DisplayName("예약 생성 성공")
    void 예약_생성_성공() {
        final ResponseEntity<String> response = requestCreateReservation(
                userToken, propertyData.propertyId(), propertyData.roomTypeId(), checkIn, checkOut, 2);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        final JsonNode data = getData(response);
        reservationId = data.get("id").asLong();
        assertThat(data.get("status").asText()).isEqualTo("CONFIRMED");
        assertThat(data.get("reservationNumber").asText()).isNotBlank();
        assertThat(data.get("finalPrice").asInt()).isGreaterThan(0);
        assertThat(data.get("dailyRates")).hasSize(2);
    }

    @Test
    @Order(7)
    @DisplayName("내 예약 목록 조회 성공")
    void 내_예약_목록_조회_성공() {
        final ResponseEntity<String> response = restTemplate.exchange(
                "/api/reservations", HttpMethod.GET, authEntity(userToken), String.class);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        final JsonNode data = getData(response);
        assertThat(data.get("content")).hasSize(1);
        assertThat(data.get("content").get(0).get("status").asText()).isEqualTo("CONFIRMED");
    }

    @Test
    @Order(8)
    @DisplayName("예약 상세 조회 성공")
    void 예약_상세_조회_성공() {
        final ResponseEntity<String> response = restTemplate.exchange(
                "/api/reservations/" + reservationId, HttpMethod.GET, authEntity(userToken), String.class);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        final JsonNode data = getData(response);
        assertThat(data.get("id").asLong()).isEqualTo(reservationId);
        assertThat(data.get("status").asText()).isEqualTo("CONFIRMED");
        assertThat(data.get("guestName").asText()).isEqualTo("테스트게스트");
        assertThat(data.get("checkInDate").asText()).isEqualTo(checkIn.toString());
        assertThat(data.get("checkOutDate").asText()).isEqualTo(checkOut.toString());
    }

    @Test
    @Order(9)
    @DisplayName("예약 취소 성공")
    void 예약_취소_성공() {
        final ResponseEntity<String> response = requestCancelReservation(userToken, reservationId, "개인 사정으로 취소");

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        final JsonNode data = getData(response);
        assertThat(data.get("status").asText()).isEqualTo("CANCELLED");
        assertThat(data.get("cancelReason").asText()).isEqualTo("개인 사정으로 취소");
    }

    @Test
    @Order(10)
    @DisplayName("취소 후 재고 복구 확인")
    void 취소_후_재고_복구_확인() {
        final ResponseEntity<String> response = restTemplate.exchange(
                "/api/extranet/room-types/" + propertyData.roomTypeId() + "/inventory?startDate=" + checkIn + "&endDate=" + checkOut,
                HttpMethod.GET, authEntity(propertyData.partnerToken()), String.class);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        final JsonNode inventory = getData(response).get("inventory");

        for (final JsonNode inv : inventory) {
            assertThat(inv.get("availableCount").asInt()).isEqualTo(5);
            assertThat(inv.get("reservedCount").asInt()).isEqualTo(0);
        }
    }

    @Test
    @Order(11)
    @DisplayName("취소된 예약 상태 확인")
    void 취소된_예약_상태_확인() {
        final ResponseEntity<String> response = restTemplate.exchange(
                "/api/reservations/" + reservationId, HttpMethod.GET, authEntity(userToken), String.class);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        final JsonNode data = getData(response);
        assertThat(data.get("status").asText()).isEqualTo("CANCELLED");
    }
}
