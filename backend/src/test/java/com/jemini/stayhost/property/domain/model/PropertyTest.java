package com.jemini.stayhost.property.domain.model;

import com.jemini.stayhost.common.exception.AuthorizationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PropertyTest {

    @Test
    @DisplayName("숙소 생성 성공 - INACTIVE 상태")
    void 숙소_생성_성공_INACTIVE_상태() {
        final Property property = createProperty(1L);

        assertThat(property.getStatus()).isEqualTo(PropertyStatus.INACTIVE);
        assertThat(property.getName()).isEqualTo("테스트 호텔");
        assertThat(property.getPartnerId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("숙소 정보수정 성공")
    void 숙소_정보수정_성공() {
        final Property property = createProperty(1L);

        property.update("수정된 호텔", "새로운 설명", LocalTime.of(14, 0), LocalTime.of(12, 0), "https://new-thumb.jpg");

        assertThat(property.getName()).isEqualTo("수정된 호텔");
        assertThat(property.getDescription()).isEqualTo("새로운 설명");
        assertThat(property.getCheckInTime()).isEqualTo(LocalTime.of(14, 0));
        assertThat(property.getCheckOutTime()).isEqualTo(LocalTime.of(12, 0));
        assertThat(property.getThumbnailUrl()).isEqualTo("https://new-thumb.jpg");
    }

    @Test
    @DisplayName("숙소 상태변경 성공")
    void 숙소_상태변경_성공() {
        final Property property = createProperty(1L);

        property.changeStatus(PropertyStatus.ACTIVE);

        assertThat(property.getStatus()).isEqualTo(PropertyStatus.ACTIVE);
    }

    @Test
    @DisplayName("소유권 검증 - 본인이면 통과")
    void 소유권_검증_본인이면_통과() {
        final Property property = createProperty(1L);

        property.validateOwner(1L);
    }

    @Test
    @DisplayName("소유권 검증 - 타인이면 예외")
    void 소유권_검증_타인이면_예외() {
        final Property property = createProperty(1L);

        assertThatThrownBy(() -> property.validateOwner(999L))
            .isInstanceOf(AuthorizationException.class);
    }

    private Property createProperty(final Long partnerId) {
        return Property.create(
            partnerId, "테스트 호텔", PropertyType.HOTEL, "설명",
            "서울시 강남구", "서울", LocalTime.of(15, 0), LocalTime.of(11, 0),
            null, null, null
        );
    }
}
