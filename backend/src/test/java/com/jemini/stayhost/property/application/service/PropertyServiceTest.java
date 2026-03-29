package com.jemini.stayhost.property.application.service;

import com.jemini.stayhost.common.exception.AuthorizationException;
import com.jemini.stayhost.common.response.PageResult;
import com.jemini.stayhost.property.application.dto.PropertyCreateCommand;
import com.jemini.stayhost.property.application.dto.PropertyResult;
import com.jemini.stayhost.property.application.dto.PropertyUpdateCommand;
import com.jemini.stayhost.property.domain.component.PropertyManager;
import com.jemini.stayhost.property.domain.component.PropertyReader;
import com.jemini.stayhost.property.domain.model.Property;
import com.jemini.stayhost.property.domain.model.PropertyStatus;
import com.jemini.stayhost.property.domain.model.PropertyType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PropertyServiceTest {

    @InjectMocks
    private PropertyService propertyService;

    @Mock
    private PropertyReader propertyReader;

    @Mock
    private PropertyManager propertyManager;

    private static final Long PARTNER_ID = 1L;
    private static final Long PROPERTY_ID = 100L;

    @Test
    @DisplayName("숙소 등록 성공")
    void 숙소_등록_성공() {
        final PropertyCreateCommand command = PropertyCreateCommand.builder()
            .name("테스트 호텔").type("HOTEL").description("설명").address("서울")
            .region("서울").checkInTime(LocalTime.of(15, 0)).checkOutTime(LocalTime.of(11, 0)).build();
        given(propertyManager.save(any(Property.class))).willAnswer(invocation -> invocation.getArgument(0));

        final PropertyResult result = propertyService.createProperty(PARTNER_ID, command);

        assertThat(result.name()).isEqualTo("테스트 호텔");
        verify(propertyManager).save(any(Property.class));
    }

    @Test
    @DisplayName("내 숙소 목록조회 성공")
    void 내숙소_목록조회_성공() {
        final Property property = createProperty(PARTNER_ID);
        given(propertyReader.findByPartnerId(PARTNER_ID, PageRequest.of(0, 20)))
            .willReturn(new PageImpl<>(List.of(property)));

        final PageResult<PropertyResult> result = propertyService.getMyProperties(PARTNER_ID, PageRequest.of(0, 20));

        assertThat(result.content()).hasSize(1);
    }

    @Test
    @DisplayName("숙소 상세조회 성공")
    void 숙소_상세조회_성공() {
        final Property property = createProperty(PARTNER_ID);
        given(propertyReader.getById(PROPERTY_ID)).willReturn(property);

        final PropertyResult result = propertyService.getProperty(PROPERTY_ID, PARTNER_ID);

        assertThat(result.name()).isEqualTo("테스트 호텔");
    }

    @Test
    @DisplayName("숙소 상세조회 - 소유권 없으면 예외")
    void 숙소_상세조회_소유권_없으면_예외() {
        final Property property = createProperty(PARTNER_ID);
        given(propertyReader.getById(PROPERTY_ID)).willReturn(property);

        assertThatThrownBy(() -> propertyService.getProperty(PROPERTY_ID, 999L))
            .isInstanceOf(AuthorizationException.class);
    }

    @Test
    @DisplayName("숙소 수정 성공")
    void 숙소_수정_성공() {
        final Property property = createProperty(PARTNER_ID);
        given(propertyReader.getById(PROPERTY_ID)).willReturn(property);
        final PropertyUpdateCommand command = PropertyUpdateCommand.builder()
            .name("수정됨").description("새설명").checkInTime(LocalTime.of(14, 0))
            .checkOutTime(LocalTime.of(12, 0)).thumbnailUrl("https://new.jpg").build();

        final PropertyResult result = propertyService.updateProperty(PROPERTY_ID, PARTNER_ID, command);

        assertThat(result.name()).isEqualTo("수정됨");
    }

    @Test
    @DisplayName("숙소 수정 - 소유권 없으면 예외")
    void 숙소_수정_소유권_없으면_예외() {
        final Property property = createProperty(PARTNER_ID);
        given(propertyReader.getById(PROPERTY_ID)).willReturn(property);

        assertThatThrownBy(() -> propertyService.updateProperty(PROPERTY_ID, 999L,
            PropertyUpdateCommand.builder().name("수정").build()))
            .isInstanceOf(AuthorizationException.class);
    }

    @Test
    @DisplayName("숙소 상태변경 성공")
    void 숙소_상태변경_성공() {
        final Property property = createProperty(PARTNER_ID);
        given(propertyReader.getById(PROPERTY_ID)).willReturn(property);

        final PropertyResult result = propertyService.changeStatus(PROPERTY_ID, PARTNER_ID, PropertyStatus.ACTIVE);

        assertThat(result.status()).isEqualTo("ACTIVE");
    }

    private Property createProperty(final Long partnerId) {
        return Property.create(partnerId, "테스트 호텔", PropertyType.HOTEL, "설명",
            "서울시 강남구", "서울", LocalTime.of(15, 0), LocalTime.of(11, 0), null, null, null);
    }
}
