package com.jemini.stayhost.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.jemini.stayhost.support.IntegrationTestBase;
import org.junit.jupiter.api.*;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("파트너 숙소 등록 → 검색 API 노출 통합 테스트")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class PropertySearchIntegrationTest extends IntegrationTestBase {

    private static String partnerToken;
    private static Long propertyId;
    private static Long roomTypeId;

    @BeforeAll
    void setUpData() {
        final String password = "password1234";
        registerPartner("search-partner", password, "검색테스트 호텔업체");
        partnerToken = loginPartner("search-partner", password);
    }

    @Test
    @Order(1)
    @DisplayName("1. INACTIVE 숙소는 검색에 노출되지 않는다")
    void INACTIVE_숙소는_검색에_노출되지_않는다() {
        propertyId = createProperty(partnerToken, "검색테스트 호텔");

        final ResponseEntity<String> response = restTemplate.getForEntity("/api/public/search/properties?region=서울&page=0&size=100", String.class);
        final JsonNode data = getData(response);

        final boolean found = hasPropertyInResults(data, propertyId);
        assertThat(found).isFalse();
    }

    @Test
    @Order(2)
    @DisplayName("2. 숙소 활성화 후 검색에 노출된다")
    void 숙소_활성화_후_검색에_노출된다() {
        activateProperty(partnerToken, propertyId);

        final ResponseEntity<String> response = restTemplate.getForEntity("/api/public/search/properties?region=서울&page=0&size=100", String.class);
        final JsonNode data = getData(response);

        final boolean found = hasPropertyInResults(data, propertyId);
        assertThat(found).isTrue();
    }

    @Test
    @Order(3)
    @DisplayName("3. 객실 등록 후 검색 결과에 객실 정보가 포함된다")
    void 객실_등록_후_검색_결과에_객실_정보가_포함된다() {
        roomTypeId = createRoomType(partnerToken, propertyId, "디럭스 더블", 4, 100000, 10);

        final ResponseEntity<String> response = restTemplate.getForEntity("/api/public/search/properties?region=서울&page=0&size=100", String.class);
        final JsonNode data = getData(response);

        final JsonNode property = findPropertyInResults(data, propertyId);
        assertThat(property).isNotNull();
        assertThat(property.get("availableRoomTypes").asInt()).isGreaterThanOrEqualTo(1);
        assertThat(property.get("minPrice")).isNotNull();
    }

    @Test
    @Order(4)
    @DisplayName("4. 숙소 상세 조회 시 객실 유형 목록이 포함된다")
    void 숙소_상세_조회_시_객실_유형_목록이_포함된다() {
        final ResponseEntity<String> response = restTemplate.getForEntity("/api/public/properties/" + propertyId, String.class);
        final JsonNode data = getData(response);

        assertThat(data.get("id").asLong()).isEqualTo(propertyId);
        assertThat(data.get("name").asText()).isEqualTo("검색테스트 호텔");
        assertThat(data.get("roomTypes")).isNotNull();
        assertThat(data.get("roomTypes").size()).isGreaterThanOrEqualTo(1);
    }

    @Test
    @Order(5)
    @DisplayName("5. 요금/재고 설정 후 날짜별 요금을 조회할 수 있다")
    void 요금_재고_설정_후_날짜별_요금을_조회할_수_있다() {
        final LocalDate start = LocalDate.now().plusDays(1);
        final LocalDate end = start.plusDays(2);
        setRates(partnerToken, roomTypeId, start, end, 120000);
        setInventory(partnerToken, roomTypeId, start, end, 5);

        final ResponseEntity<String> response = restTemplate.getForEntity(
            "/api/public/search/properties/" + propertyId + "/rates?startDate=" + start + "&endDate=" + end, String.class);
        final JsonNode data = getData(response);

        assertThat(data.get("propertyId").asLong()).isEqualTo(propertyId);
        assertThat(data.get("roomTypes").size()).isGreaterThanOrEqualTo(1);

        final JsonNode rates = data.get("roomTypes").get(0).get("rates");
        assertThat(rates.size()).isEqualTo(3);
    }

    @Test
    @Order(6)
    @DisplayName("6. 키워드로 숙소를 검색할 수 있다")
    void 키워드로_숙소를_검색할_수_있다() {
        final ResponseEntity<String> response = restTemplate.getForEntity("/api/public/search/properties?keyword=검색테스트&page=0&size=10", String.class);
        final JsonNode data = getData(response);

        final boolean found = hasPropertyInResults(data, propertyId);
        assertThat(found).isTrue();
    }

    private boolean hasPropertyInResults(final JsonNode data, final Long targetPropertyId) {
        return findPropertyInResults(data, targetPropertyId) != null;
    }

    private JsonNode findPropertyInResults(final JsonNode data, final Long targetPropertyId) {
        final JsonNode content = data.get("content");
        if (content == null || !content.isArray()) {
            return null;
        }
        for (final JsonNode node : content) {
            if (node.get("id").asLong() == targetPropertyId) {
                return node;
            }
        }
        return null;
    }
}
