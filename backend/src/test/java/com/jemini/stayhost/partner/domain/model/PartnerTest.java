package com.jemini.stayhost.partner.domain.model;

import com.jemini.stayhost.common.exception.AuthorizationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PartnerTest {

    @Test
    @DisplayName("파트너 생성 성공 - PENDING 상태")
    void 파트너_생성_성공_PENDING_상태() {
        final Partner partner = createPartner();

        assertThat(partner.getStatus()).isEqualTo(PartnerStatus.PENDING);
        assertThat(partner.getBusinessName()).isEqualTo("테스트 숙소");
        assertThat(partner.getLoginId()).isEqualTo("testlogin");
    }

    @Test
    @DisplayName("파트너 활성화 성공")
    void 파트너_활성화_성공() {
        final Partner partner = createPartner();

        partner.activate();

        assertThat(partner.getStatus()).isEqualTo(PartnerStatus.ACTIVE);
    }

    @Test
    @DisplayName("파트너 정지 성공")
    void 파트너_정지_성공() {
        final Partner partner = createPartner();

        partner.suspend();

        assertThat(partner.getStatus()).isEqualTo(PartnerStatus.SUSPENDED);
    }

    @Test
    @DisplayName("파트너 정보수정 성공")
    void 파트너_정보수정_성공() {
        final Partner partner = createPartner();

        partner.update("010-9999-9999", "new@test.com", "신한은행", "111-222-333");

        assertThat(partner.getPhone()).isEqualTo("01099999999");
        assertThat(partner.getEmail()).isEqualTo("new@test.com");
        assertThat(partner.getBankName()).isEqualTo("신한은행");
        assertThat(partner.getBankAccount()).isEqualTo("111-222-333");
    }

    @Test
    @DisplayName("활성상태 검증 - ACTIVE이면 통과")
    void 활성상태_검증_ACTIVE이면_통과() {
        final Partner partner = createPartner();
        partner.activate();

        partner.validateActive();
    }

    @Test
    @DisplayName("활성상태 검증 - PENDING이면 예외")
    void 활성상태_검증_PENDING이면_예외() {
        final Partner partner = createPartner();

        assertThatThrownBy(partner::validateActive)
            .isInstanceOf(AuthorizationException.class);
    }

    @Test
    @DisplayName("활성상태 검증 - SUSPENDED이면 예외")
    void 활성상태_검증_SUSPENDED이면_예외() {
        final Partner partner = createPartner();
        partner.suspend();

        assertThatThrownBy(partner::validateActive)
            .isInstanceOf(AuthorizationException.class);
    }

    private Partner createPartner() {
        return Partner.create(
            "테스트 숙소", "123-45-67890", "홍길동",
            "010-1234-5678", "test@test.com", "testlogin", "encodedPassword",
            null, null
        );
    }
}
