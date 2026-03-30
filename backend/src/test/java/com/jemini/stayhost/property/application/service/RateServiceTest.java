package com.jemini.stayhost.property.application.service;

import com.jemini.stayhost.common.exception.AuthorizationException;
import com.jemini.stayhost.common.exception.BusinessException;
import com.jemini.stayhost.common.exception.ErrorCode;
import com.jemini.stayhost.property.application.dto.RateBulkSetCommand;
import com.jemini.stayhost.property.application.dto.RateBulkSetResult;
import com.jemini.stayhost.property.application.dto.RateListResult;
import com.jemini.stayhost.property.domain.component.PropertyReader;
import com.jemini.stayhost.property.domain.component.RateManager;
import com.jemini.stayhost.property.domain.component.RateReader;
import com.jemini.stayhost.property.domain.component.RoomTypeReader;
import com.jemini.stayhost.property.domain.model.Property;
import com.jemini.stayhost.property.domain.model.PropertyType;
import com.jemini.stayhost.property.domain.model.Rate;
import com.jemini.stayhost.property.domain.model.RoomType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RateServiceTest {

    @InjectMocks
    private RateService rateService;

    @Mock
    private RoomTypeReader roomTypeReader;

    @Mock
    private PropertyReader propertyReader;

    @Mock
    private RateReader rateReader;

    @Mock
    private RateManager rateManager;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private static final Long PARTNER_ID = 1L;
    private static final Long PROPERTY_ID = 100L;
    private static final Long ROOM_TYPE_ID = 200L;

    @Test
    @DisplayName("요금 일괄설정 성공 - 신규생성")
    void 요금_일괄설정_성공_신규생성() {
        setupOwnership();
        given(rateReader.findByRoomTypeIdAndDateBetween(eq(ROOM_TYPE_ID), any(), any())).willReturn(List.of());

        final RateBulkSetResult result = rateService.bulkSet(ROOM_TYPE_ID, PARTNER_ID,
            RateBulkSetCommand.builder()
                .startDate(LocalDate.of(2026, 4, 1)).endDate(LocalDate.of(2026, 4, 3))
                .price(BigDecimal.valueOf(150000)).build());

        assertThat(result.appliedDates()).isEqualTo(3);
        verify(rateManager).saveAll(any());
    }

    @Test
    @DisplayName("요금 일괄설정 성공 - 기존요금 업데이트")
    void 요금_일괄설정_성공_기존요금_업데이트() {
        setupOwnership();
        final Rate existing = Rate.create(ROOM_TYPE_ID, LocalDate.of(2026, 4, 1), BigDecimal.valueOf(100000));
        given(rateReader.findByRoomTypeIdAndDateBetween(eq(ROOM_TYPE_ID), any(), any())).willReturn(List.of(existing));

        final RateBulkSetResult result = rateService.bulkSet(ROOM_TYPE_ID, PARTNER_ID,
            RateBulkSetCommand.builder()
                .startDate(LocalDate.of(2026, 4, 1)).endDate(LocalDate.of(2026, 4, 2))
                .price(BigDecimal.valueOf(200000)).build());

        assertThat(result.appliedDates()).isEqualTo(2);
        assertThat(existing.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(200000));
    }

    @Test
    @DisplayName("요금 일괄설정 - 요일필터 적용")
    void 요금_일괄설정_요일필터_적용() {
        setupOwnership();
        given(rateReader.findByRoomTypeIdAndDateBetween(eq(ROOM_TYPE_ID), any(), any())).willReturn(List.of());

        // 2026-04-06(월) ~ 2026-04-12(일), 월~금만 = 5일
        final RateBulkSetResult result = rateService.bulkSet(ROOM_TYPE_ID, PARTNER_ID,
            RateBulkSetCommand.builder()
                .startDate(LocalDate.of(2026, 4, 6)).endDate(LocalDate.of(2026, 4, 12))
                .price(BigDecimal.valueOf(150000)).daysOfWeek(List.of(1, 2, 3, 4, 5)).build());

        assertThat(result.appliedDates()).isEqualTo(5);
    }

    @Test
    @DisplayName("요금 일괄설정 - 시작일이 종료일 이후면 예외")
    void 요금_일괄설정_시작일이_종료일_이후면_예외() {
        setupOwnership();

        assertThatThrownBy(() -> rateService.bulkSet(ROOM_TYPE_ID, PARTNER_ID,
            RateBulkSetCommand.builder()
                .startDate(LocalDate.of(2026, 4, 10)).endDate(LocalDate.of(2026, 4, 1))
                .price(BigDecimal.valueOf(150000)).build()))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.INVALID_DATE_RANGE);
    }

    @Test
    @DisplayName("요금 일괄설정 - 소유권 없으면 예외")
    void 요금_일괄설정_소유권_없으면_예외() {
        given(roomTypeReader.getById(ROOM_TYPE_ID)).willReturn(createRoomType());
        given(propertyReader.getById(PROPERTY_ID)).willReturn(createProperty(PARTNER_ID));

        assertThatThrownBy(() -> rateService.bulkSet(ROOM_TYPE_ID, 999L,
            RateBulkSetCommand.builder()
                .startDate(LocalDate.of(2026, 4, 1)).endDate(LocalDate.of(2026, 4, 3))
                .price(BigDecimal.valueOf(150000)).build()))
            .isInstanceOf(AuthorizationException.class);
    }

    @Test
    @DisplayName("요금 목록조회 성공")
    void 요금_목록조회_성공() {
        setupOwnership();
        given(rateReader.findByRoomTypeIdAndDateBetween(ROOM_TYPE_ID,
            LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 3)))
            .willReturn(List.of(
                Rate.create(ROOM_TYPE_ID, LocalDate.of(2026, 4, 1), BigDecimal.valueOf(150000)),
                Rate.create(ROOM_TYPE_ID, LocalDate.of(2026, 4, 2), BigDecimal.valueOf(150000))));

        final RateListResult result = rateService.getRates(ROOM_TYPE_ID, PARTNER_ID,
            LocalDate.of(2026, 4, 1), LocalDate.of(2026, 4, 3));

        assertThat(result.rates()).hasSize(2);
    }

    private void setupOwnership() {
        given(roomTypeReader.getById(ROOM_TYPE_ID)).willReturn(createRoomType());
        given(propertyReader.getById(PROPERTY_ID)).willReturn(createProperty(PARTNER_ID));
    }

    private RoomType createRoomType() {
        return RoomType.create(PROPERTY_ID, "스탠다드", "설명", 2, BigDecimal.valueOf(120000), null, 10);
    }

    private Property createProperty(final Long partnerId) {
        return Property.create(partnerId, "테스트 호텔", PropertyType.HOTEL, "설명",
            "서울시 강남구", "서울", LocalTime.of(15, 0), LocalTime.of(11, 0), null, null, null);
    }
}
