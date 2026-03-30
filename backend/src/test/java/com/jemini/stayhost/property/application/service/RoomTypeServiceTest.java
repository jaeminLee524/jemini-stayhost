package com.jemini.stayhost.property.application.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jemini.stayhost.common.exception.AuthorizationException;
import com.jemini.stayhost.property.application.dto.RoomTypeCreateCommand;
import com.jemini.stayhost.property.application.dto.RoomTypeResult;
import com.jemini.stayhost.property.application.dto.RoomTypeUpdateCommand;
import com.jemini.stayhost.property.domain.component.PropertyReader;
import com.jemini.stayhost.property.domain.component.RoomTypeManager;
import com.jemini.stayhost.property.domain.component.RoomTypeReader;
import com.jemini.stayhost.property.domain.model.Property;
import com.jemini.stayhost.property.domain.model.PropertyType;
import com.jemini.stayhost.property.domain.model.RoomType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RoomTypeServiceTest {

    @InjectMocks
    private RoomTypeService roomTypeService;

    @Mock
    private RoomTypeReader roomTypeReader;

    @Mock
    private RoomTypeManager roomTypeManager;

    @Mock
    private PropertyReader propertyReader;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private static final Long PARTNER_ID = 1L;
    private static final Long PROPERTY_ID = 100L;
    private static final Long ROOM_TYPE_ID = 200L;

    @Test
    @DisplayName("객실유형 등록 성공")
    void 객실유형_등록_성공() {
        given(propertyReader.getById(PROPERTY_ID)).willReturn(createProperty(PARTNER_ID));
        given(roomTypeManager.save(any(RoomType.class))).willAnswer(invocation -> invocation.getArgument(0));

        final RoomTypeResult result = roomTypeService.createRoomType(PROPERTY_ID, PARTNER_ID,
            RoomTypeCreateCommand.builder().name("스탠다드").description("설명")
                .maxOccupancy(2).basePrice(BigDecimal.valueOf(120000)).totalRoomCount(10).build());

        assertThat(result.name()).isEqualTo("스탠다드");
        verify(roomTypeManager).save(any(RoomType.class));
    }

    @Test
    @DisplayName("객실유형 등록 - 소유권 없으면 예외")
    void 객실유형_등록_소유권_없으면_예외() {
        given(propertyReader.getById(PROPERTY_ID)).willReturn(createProperty(PARTNER_ID));

        assertThatThrownBy(() -> roomTypeService.createRoomType(PROPERTY_ID, 999L,
            RoomTypeCreateCommand.builder().name("스탠다드").maxOccupancy(2)
                .basePrice(BigDecimal.valueOf(120000)).totalRoomCount(10).build()))
            .isInstanceOf(AuthorizationException.class);
    }

    @Test
    @DisplayName("객실유형 목록조회 성공")
    void 객실유형_목록조회_성공() {
        given(propertyReader.getById(PROPERTY_ID)).willReturn(createProperty(PARTNER_ID));
        given(roomTypeReader.findByPropertyId(PROPERTY_ID)).willReturn(List.of(
            RoomType.create(PROPERTY_ID, "스탠다드", "설명", 2, BigDecimal.valueOf(120000), null, 10)));

        final List<RoomTypeResult> results = roomTypeService.getRoomTypes(PROPERTY_ID, PARTNER_ID);

        assertThat(results).hasSize(1);
    }

    @Test
    @DisplayName("객실유형 수정 성공")
    void 객실유형_수정_성공() {
        final RoomType roomType = RoomType.create(PROPERTY_ID, "스탠다드", "설명", 2, BigDecimal.valueOf(120000), null, 10);
        given(roomTypeReader.getById(ROOM_TYPE_ID)).willReturn(roomType);
        given(propertyReader.getById(PROPERTY_ID)).willReturn(createProperty(PARTNER_ID));

        final RoomTypeResult result = roomTypeService.updateRoomType(ROOM_TYPE_ID, PARTNER_ID,
            RoomTypeUpdateCommand.builder().name("디럭스").description("업그레이드")
                .maxOccupancy(3).basePrice(BigDecimal.valueOf(200000)).build());

        assertThat(result.name()).isEqualTo("디럭스");
    }

    @Test
    @DisplayName("객실유형 수정 - 소유권 없으면 예외")
    void 객실유형_수정_소유권_없으면_예외() {
        final RoomType roomType = RoomType.create(PROPERTY_ID, "스탠다드", "설명", 2, BigDecimal.valueOf(120000), null, 10);
        given(roomTypeReader.getById(ROOM_TYPE_ID)).willReturn(roomType);
        given(propertyReader.getById(PROPERTY_ID)).willReturn(createProperty(PARTNER_ID));

        assertThatThrownBy(() -> roomTypeService.updateRoomType(ROOM_TYPE_ID, 999L,
            RoomTypeUpdateCommand.builder().name("디럭스").maxOccupancy(3)
                .basePrice(BigDecimal.valueOf(200000)).build()))
            .isInstanceOf(AuthorizationException.class);
    }

    private Property createProperty(final Long partnerId) {
        return Property.create(partnerId, "테스트 호텔", PropertyType.HOTEL, "설명",
            "서울시 강남구", "서울", LocalTime.of(15, 0), LocalTime.of(11, 0), null, null, null);
    }
}
