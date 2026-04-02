package com.jemini.stayhost.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.jemini.stayhost.support.IntegrationTestBase;
import org.junit.jupiter.api.*;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("예약 취소 → 재고 복원 → 재예약 통합 테스트")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ReservationRebookIntegrationTest extends IntegrationTestBase {

    private static String partnerToken;
    private static Long propertyId;
    private static Long roomTypeId;
    private static String userToken;
    private static Long reservationId;

    private static final LocalDate CHECK_IN = LocalDate.now().plusDays(1);
    private static final LocalDate CHECK_OUT = CHECK_IN.plusDays(2);

    @BeforeAll
    void setUpData() {
        final TestPropertyData data = setupPropertyWithInventory("rebook-partner", 1);
        partnerToken = data.partnerToken();
        propertyId = data.propertyId();
        roomTypeId = data.roomTypeId();

        registerUser("rebook@test.com", "password1234");
        userToken = loginUser("rebook@test.com", "password1234");
    }

    @Test
    @Order(1)
    @DisplayName("1. 재고 1개인 객실에 예약을 생성한다")
    void 재고_1개인_객실에_예약을_생성한다() {
        final ResponseEntity<String> response = requestCreateReservation(userToken, propertyId, roomTypeId, CHECK_IN, CHECK_OUT, 2);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        reservationId = getData(response).get("id").asLong();
    }

    @Test
    @Order(2)
    @DisplayName("2. 재고 소진으로 추가 예약이 실패한다")
    void 재고_소진으로_추가_예약이_실패한다() {
        final ResponseEntity<String> response = requestCreateReservation(userToken, propertyId, roomTypeId, CHECK_IN, CHECK_OUT, 2);
        final JsonNode error = getError(response);
        assertThat(error.get("code").asText()).isEqualTo("INVENTORY_INSUFFICIENT");
    }

    @Test
    @Order(3)
    @DisplayName("3. 예약을 취소한다")
    void 예약을_취소한다() {
        final ResponseEntity<String> response = requestCancelReservation(userToken, reservationId, "단순 변심");
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();

        final JsonNode data = getData(response);
        assertThat(data.get("status").asText()).isEqualTo("CANCELLED");
    }

    @Test
    @Order(4)
    @DisplayName("4. 재고가 복원되어 재예약에 성공한다")
    void 재고가_복원되어_재예약에_성공한다() {
        final ResponseEntity<String> response = requestCreateReservation(userToken, propertyId, roomTypeId, CHECK_IN, CHECK_OUT, 2);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();

        final JsonNode data = getData(response);
        assertThat(data.get("id").asLong()).isNotEqualTo(reservationId);
        assertThat(data.get("status").asText()).isEqualTo("CONFIRMED");
    }
}
