package com.jemini.stayhost.supplier.infrastructure.component;

import com.jemini.stayhost.supplier.domain.component.SupplierAdapter;
import com.jemini.stayhost.supplier.domain.dto.SupplierBookingRequest;
import com.jemini.stayhost.supplier.domain.dto.SupplierBookingResult;
import com.jemini.stayhost.supplier.domain.dto.SupplierInventoryData;
import com.jemini.stayhost.supplier.domain.dto.SupplierPropertyData;
import com.jemini.stayhost.supplier.domain.dto.SupplierRateData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Slf4j
@Component
public class MockSupplierAdapter implements SupplierAdapter {

    private static final String SUPPLIER_CODE = "MOCK_SUPPLIER";
    private static final BigDecimal MOCK_RATE = BigDecimal.valueOf(120_000);
    private static final int MOCK_AVAILABLE_COUNT = 5;

    @Override
    public String getSupplierCode() {
        return SUPPLIER_CODE;
    }

    @Override
    public List<SupplierPropertyData> fetchProperties() {
        log.info("[MockSupplierAdapter] fetchProperties 호출 — 가상 숙소 3건 반환");
        return List.of(
            new SupplierPropertyData("EXT-001", "목업 호텔 A", "서울 강남구", "서울", "HOTEL", "{\"id\":\"EXT-001\",\"name\":\"목업 호텔 A\"}"),
            new SupplierPropertyData("EXT-002", "목업 펜션 B", "강원 속초시", "강원", "PENSION", "{\"id\":\"EXT-002\",\"name\":\"목업 펜션 B\"}"),
            new SupplierPropertyData("EXT-003", "목업 리조트 C", "제주 서귀포시", "제주", "RESORT", "{\"id\":\"EXT-003\",\"name\":\"목업 리조트 C\"}")
        );
    }

    @Override
    public List<SupplierRateData> fetchRates(final String externalPropertyId, final LocalDate from, final LocalDate to) {
        log.info("[MockSupplierAdapter] fetchRates: property={}, {}~{}", externalPropertyId, from, to);
        return from.datesUntil(to.plusDays(1))
            .map(date -> new SupplierRateData("EXT-ROOM-001", date, MOCK_RATE, "KRW"))
            .toList();
    }

    @Override
    public List<SupplierInventoryData> fetchInventory(final String externalPropertyId, final LocalDate from, final LocalDate to) {
        log.info("[MockSupplierAdapter] fetchInventory: property={}", externalPropertyId);
        return from.datesUntil(to.plusDays(1))
            .map(date -> new SupplierInventoryData("EXT-ROOM-001", date, MOCK_AVAILABLE_COUNT))
            .toList();
    }

    @Override
    public SupplierBookingResult createBooking(final SupplierBookingRequest request) {
        final String mockBookingId = "MOCK-BK-" + System.currentTimeMillis();
        log.info("[MockSupplierAdapter] createBooking → externalBookingId={}", mockBookingId);
        return new SupplierBookingResult(true, mockBookingId, null);
    }

    @Override
    public SupplierBookingResult cancelBooking(final String externalBookingId) {
        log.info("[MockSupplierAdapter] cancelBooking: externalBookingId={}", externalBookingId);
        return new SupplierBookingResult(true, externalBookingId, null);
    }
}
