package com.jemini.stayhost.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.jemini.stayhost.support.IntegrationTestBase;
import org.junit.jupiter.api.*;
import org.springframework.http.*;
import org.springframework.test.annotation.DirtiesContext;

import java.time.LocalDate;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PartnerPropertyFlowIntegrationTest extends IntegrationTestBase {

    private static final String LOGIN_ID = "partner_flow";
    private static final String PASSWORD = "password1234";

    private Long partnerId;
    private String partnerToken;
    private Long propertyId;
    private Long roomTypeId;
    private LocalDate rateStart;
    private LocalDate rateEnd;

    @Test
    @Order(1)
    void 파트너_가입_성공() {
        partnerId = registerPartner(LOGIN_ID, PASSWORD, "테스트 호텔업체");
        assertThat(partnerId).isPositive();
    }

    @Test
    @Order(2)
    void 파트너_로그인_성공() {
        partnerToken = loginPartner(LOGIN_ID, PASSWORD);
        assertThat(partnerToken).isNotBlank();
    }

    @Test
    @Order(3)
    void 내_정보_조회_성공() {
        final ResponseEntity<String> response = restTemplate.exchange(
                "/api/extranet/partners/me", HttpMethod.GET, authEntity(partnerToken), String.class);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        final JsonNode data = getData(response);
        assertThat(data.get("businessName").asText()).isEqualTo("테스트 호텔업체");
        assertThat(data.get("status").asText()).isEqualTo("ACTIVE");
    }

    @Test
    @Order(4)
    void 숙소_등록_성공() {
        propertyId = createProperty(partnerToken, "스테이호스트 강남점");
        assertThat(propertyId).isPositive();
    }

    @Test
    @Order(5)
    void 숙소_상태_변경_ACTIVE() {
        activateProperty(partnerToken, propertyId);

        final ResponseEntity<String> response = restTemplate.exchange(
                "/api/extranet/properties/" + propertyId, HttpMethod.GET, authEntity(partnerToken), String.class);

        final JsonNode data = getData(response);
        assertThat(data.get("status").asText()).isEqualTo("ACTIVE");
    }

    @Test
    @Order(6)
    void 객실유형_등록_성공() {
        roomTypeId = createRoomType(partnerToken, propertyId, "디럭스 트윈", 2, 150000, 5);
        assertThat(roomTypeId).isPositive();
    }

    @Test
    @Order(7)
    void 요금_설정_성공() {
        rateStart = LocalDate.now().plusDays(1);
        rateEnd = rateStart.plusDays(7);
        setRates(partnerToken, roomTypeId, rateStart, rateEnd, 120000);
    }

    @Test
    @Order(8)
    void 재고_설정_성공() {
        setInventory(partnerToken, roomTypeId, rateStart, rateEnd, 5);
    }

    @Test
    @Order(9)
    void 요금_조회_성공() {
        final ResponseEntity<String> response = restTemplate.exchange(
                "/api/extranet/room-types/" + roomTypeId + "/rates?startDate=" + rateStart + "&endDate=" + rateEnd,
                HttpMethod.GET, authEntity(partnerToken), String.class);

        System.out.println("[DEBUG] 요금 조회 status=" + response.getStatusCode() + " body=" + response.getBody());
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        final JsonNode data = getData(response);
        final JsonNode rates = data.get("rates");
        assertThat(rates).isNotEmpty();

        for (final JsonNode rate : rates) {
            assertThat(rate.get("price").asInt()).isEqualTo(120000);
        }
    }

    @Test
    @Order(10)
    void 재고_조회_성공() {
        final ResponseEntity<String> response = restTemplate.exchange(
                "/api/extranet/room-types/" + roomTypeId + "/inventory?startDate=" + rateStart + "&endDate=" + rateEnd,
                HttpMethod.GET, authEntity(partnerToken), String.class);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        final JsonNode data = getData(response);
        final JsonNode inventory = data.get("inventory");
        assertThat(inventory).isNotEmpty();

        for (final JsonNode inv : inventory) {
            assertThat(inv.get("totalCount").asInt()).isEqualTo(5);
            assertThat(inv.get("availableCount").asInt()).isEqualTo(5);
        }
    }

    @Test
    @Order(11)
    void 숙소_정보_수정_성공() {
        final Map<String, Object> body = Map.of(
                "name", "스테이호스트 강남점 (리뉴얼)",
                "description", "리뉴얼 오픈",
                "checkInTime", "14:00",
                "checkOutTime", "12:00",
                "thumbnailUrl", "https://example.com/new-thumb.jpg"
        );
        final ResponseEntity<String> response = restTemplate.exchange(
                "/api/extranet/properties/" + propertyId, HttpMethod.PUT, entity(body, partnerToken), String.class);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        final JsonNode data = getData(response);
        assertThat(data.get("name").asText()).isEqualTo("스테이호스트 강남점 (리뉴얼)");
    }

    @Test
    @Order(12)
    void 내_숙소_목록_조회_성공() {
        final ResponseEntity<String> response = restTemplate.exchange(
                "/api/extranet/properties", HttpMethod.GET, authEntity(partnerToken), String.class);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        final JsonNode data = getData(response);
        assertThat(data.get("content")).hasSize(1);
    }
}
