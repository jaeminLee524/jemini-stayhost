package com.jemini.stayhost.support;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jemini.stayhost.partner.domain.model.Partner;
import com.jemini.stayhost.partner.infrastructure.persistence.PartnerRepository;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public abstract class IntegrationTestBase {

    @Autowired
    protected TestRestTemplate restTemplate;

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    private PartnerRepository partnerRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeAll
    void cleanDatabase() {
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 0");
        jdbcTemplate.queryForList(
                "SELECT TABLE_NAME FROM INFORMATION_SCHEMA.TABLES WHERE TABLE_SCHEMA = DATABASE() AND TABLE_TYPE = 'BASE TABLE'",
                String.class)
            .forEach(table -> jdbcTemplate.execute("TRUNCATE TABLE " + table));
        jdbcTemplate.execute("SET FOREIGN_KEY_CHECKS = 1");
    }

    // ===== HTTP Helpers =====

    protected HttpHeaders headers() {
        final HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return headers;
    }

    protected HttpHeaders headers(final String token) {
        final HttpHeaders headers = headers();
        headers.setBearerAuth(token);
        return headers;
    }

    protected <T> HttpEntity<T> entity(final T body) {
        return new HttpEntity<>(body, headers());
    }

    protected <T> HttpEntity<T> entity(final T body, final String token) {
        return new HttpEntity<>(body, headers(token));
    }

    protected HttpEntity<Void> authEntity(final String token) {
        return new HttpEntity<>(headers(token));
    }

    protected JsonNode getData(final ResponseEntity<String> response) {
        try {
            final JsonNode root = objectMapper.readTree(response.getBody());
            assertThat(root.get("result").asText()).isEqualTo("SUCCESS");
            return root.get("data");
        } catch (final Exception e) {
            throw new RuntimeException("응답 파싱 실패: " + response.getBody(), e);
        }
    }

    protected JsonNode getError(final ResponseEntity<String> response) {
        try {
            final JsonNode root = objectMapper.readTree(response.getBody());
            assertThat(root.get("result").asText()).isEqualTo("ERROR");
            return root.get("error");
        } catch (final Exception e) {
            throw new RuntimeException("에러 응답 파싱 실패: " + response.getBody(), e);
        }
    }

    // ===== Partner Helpers =====

    protected Long registerPartner(
            final String loginId,
            final String password,
            final String businessName
    ) {
        final String uniqueSuffix = String.valueOf(System.nanoTime() % 100000);
        final Map<String, Object> body = Map.of(
                "businessName", businessName,
                "businessNumber", "123-45-" + uniqueSuffix,
                "representative", "대표자",
                "phone", "010-1234-5678",
                "email", loginId + "@test.com",
                "bankName", "국민은행",
                "bankAccount", "123-456-789",
                "loginId", loginId,
                "password", password
        );
        final ResponseEntity<String> response = restTemplate.postForEntity("/api/extranet/partners", entity(body), String.class);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        final Long partnerId = getData(response).get("partnerId").asLong();

        final Partner partner = partnerRepository.findById(partnerId).orElseThrow();
        partner.activate();
        partnerRepository.save(partner);

        return partnerId;
    }

    protected String loginPartner(final String loginId, final String password) {
        final Map<String, Object> body = Map.of("loginId", loginId, "password", password);
        final ResponseEntity<String> response = restTemplate.postForEntity("/api/public/extranet/auth/login", entity(body), String.class);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        return getData(response).get("accessToken").asText();
    }

    // ===== User Helpers =====

    protected Long registerUser(final String email, final String password) {
        final Map<String, Object> body = Map.of(
                "email", email,
                "password", password,
                "name", "테스트유저",
                "phone", "010-9876-5432"
        );
        final ResponseEntity<String> response = restTemplate.postForEntity("/api/public/users/signup", entity(body), String.class);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        return getData(response).get("id").asLong();
    }

    protected String loginUser(final String email, final String password) {
        final Map<String, Object> body = Map.of("email", email, "password", password);
        final ResponseEntity<String> response = restTemplate.postForEntity("/api/public/users/login", entity(body), String.class);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        return getData(response).get("accessToken").asText();
    }

    // ===== Property Helpers =====

    protected Long createProperty(final String token, final String name) {
        final Map<String, Object> body = Map.of(
                "name", name,
                "type", "HOTEL",
                "description", "통합 테스트 숙소",
                "address", "서울시 강남구 테헤란로 123",
                "region", "서울",
                "checkInTime", "15:00",
                "checkOutTime", "11:00",
                "latitude", 37.5012,
                "longitude", 127.0396,
                "thumbnailUrl", "https://example.com/thumb.jpg"
        );
        final ResponseEntity<String> response = restTemplate.postForEntity("/api/extranet/properties", entity(body, token), String.class);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        return getData(response).get("id").asLong();
    }

    protected void activateProperty(final String token, final Long propertyId) {
        final Map<String, Object> body = Map.of("status", "ACTIVE");
        final ResponseEntity<String> response = restTemplate.exchange(
                "/api/extranet/properties/" + propertyId + "/status", HttpMethod.PATCH, entity(body, token), String.class);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    // ===== RoomType Helpers =====

    protected Long createRoomType(
            final String token,
            final Long propertyId,
            final String name,
            final int maxOccupancy,
            final int basePrice,
            final int totalRoomCount
    ) {
        final Map<String, Object> body = Map.of(
                "name", name,
                "description", "통합 테스트 객실",
                "maxOccupancy", maxOccupancy,
                "basePrice", basePrice,
                "amenities", List.of("WiFi", "TV"),
                "totalRoomCount", totalRoomCount
        );
        final ResponseEntity<String> response = restTemplate.postForEntity(
                "/api/extranet/properties/" + propertyId + "/room-types", entity(body, token), String.class);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        return getData(response).get("id").asLong();
    }

    // ===== Rate & Inventory Helpers =====

    protected void setRates(
            final String token,
            final Long roomTypeId,
            final LocalDate startDate,
            final LocalDate endDate,
            final int price
    ) {
        final Map<String, Object> body = Map.of(
                "startDate", startDate.toString(),
                "endDate", endDate.toString(),
                "price", price
        );
        final ResponseEntity<String> response = restTemplate.exchange(
                "/api/extranet/room-types/" + roomTypeId + "/rates", HttpMethod.PUT, entity(body, token), String.class);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    protected void setInventory(
            final String token,
            final Long roomTypeId,
            final LocalDate startDate,
            final LocalDate endDate,
            final int totalCount
    ) {
        final Map<String, Object> body = Map.of(
                "startDate", startDate.toString(),
                "endDate", endDate.toString(),
                "totalCount", totalCount
        );
        final ResponseEntity<String> response = restTemplate.exchange(
                "/api/extranet/room-types/" + roomTypeId + "/inventory", HttpMethod.PUT, entity(body, token), String.class);
        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    // ===== Reservation Helpers =====

    protected ResponseEntity<String> requestCreateReservation(
            final String token,
            final Long propertyId,
            final Long roomTypeId,
            final LocalDate checkIn,
            final LocalDate checkOut,
            final int guestCount
    ) {
        final Map<String, Object> body = Map.ofEntries(
                Map.entry("propertyId", propertyId),
                Map.entry("roomTypeId", roomTypeId),
                Map.entry("checkInDate", checkIn.toString()),
                Map.entry("checkOutDate", checkOut.toString()),
                Map.entry("guestName", "테스트게스트"),
                Map.entry("guestPhone", "010-1111-2222"),
                Map.entry("guestCount", guestCount)
        );
        return restTemplate.postForEntity("/api/reservations", entity(body, token), String.class);
    }

    protected ResponseEntity<String> requestCancelReservation(
            final String token,
            final Long reservationId,
            final String reason
    ) {
        final Map<String, Object> body = Map.of("cancelReason", reason);
        return restTemplate.postForEntity("/api/reservations/" + reservationId + "/cancel", entity(body, token), String.class);
    }

    // ===== Common Setup =====

    protected record TestPropertyData(String partnerToken, Long propertyId, Long roomTypeId) {
    }

    protected TestPropertyData setupPropertyWithInventory(
            final String partnerLoginId,
            final int inventoryCount
    ) {
        final String password = "password1234";
        registerPartner(partnerLoginId, password, partnerLoginId + " 호텔업체");
        final String partnerToken = loginPartner(partnerLoginId, password);
        final Long propertyId = createProperty(partnerToken, "테스트 호텔");
        activateProperty(partnerToken, propertyId);
        final Long roomTypeId = createRoomType(partnerToken, propertyId, "디럭스 더블", 4, 100000, 10);

        final LocalDate start = LocalDate.now().plusDays(1);
        final LocalDate end = start.plusDays(13);
        setRates(partnerToken, roomTypeId, start, end, 120000);
        setInventory(partnerToken, roomTypeId, start, end, inventoryCount);

        return new TestPropertyData(partnerToken, propertyId, roomTypeId);
    }
}
