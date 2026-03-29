package com.jemini.stayhost.partner.application.service;

import com.jemini.stayhost.common.exception.AuthenticationException;
import com.jemini.stayhost.common.exception.AuthorizationException;
import com.jemini.stayhost.common.exception.BusinessException;
import com.jemini.stayhost.common.exception.ErrorCode;
import com.jemini.stayhost.common.security.JwtProvider;
import com.jemini.stayhost.partner.application.dto.PartnerLoginCommand;
import com.jemini.stayhost.partner.application.dto.PartnerLoginResult;
import com.jemini.stayhost.partner.application.dto.PartnerRegisterCommand;
import com.jemini.stayhost.partner.application.dto.PartnerResult;
import com.jemini.stayhost.partner.application.dto.PartnerUpdateCommand;
import com.jemini.stayhost.partner.domain.component.PartnerManager;
import com.jemini.stayhost.partner.domain.component.PartnerReader;
import com.jemini.stayhost.partner.domain.model.Partner;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PartnerServiceTest {

    @InjectMocks
    private PartnerService partnerService;

    @Mock
    private PartnerReader partnerReader;

    @Mock
    private PartnerManager partnerManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @Test
    @DisplayName("파트너 등록 성공")
    void 파트너_등록_성공() {
        final PartnerRegisterCommand command = PartnerRegisterCommand.builder()
            .businessName("테스트").businessNumber("123-45-67890").representative("홍길동")
            .phone("010-1234-5678").email("test@test.com").loginId("testlogin").password("password123")
            .build();
        given(partnerReader.existsByLoginId("testlogin")).willReturn(false);
        given(partnerReader.existsByBusinessNumber("123-45-67890")).willReturn(false);
        given(passwordEncoder.encode("password123")).willReturn("encoded");
        given(partnerManager.save(any(Partner.class))).willAnswer(invocation -> invocation.getArgument(0));

        final PartnerResult result = partnerService.register(command);

        assertThat(result.businessName()).isEqualTo("테스트");
        verify(partnerManager).save(any(Partner.class));
    }

    @Test
    @DisplayName("파트너 등록 - 로그인아이디 중복이면 예외")
    void 파트너_등록_로그인아이디_중복이면_예외() {
        final PartnerRegisterCommand command = PartnerRegisterCommand.builder()
            .loginId("duplicate").businessNumber("123-45-67890").build();
        given(partnerReader.existsByLoginId("duplicate")).willReturn(true);

        assertThatThrownBy(() -> partnerService.register(command))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_LOGIN_ID);
    }

    @Test
    @DisplayName("파트너 등록 - 사업자번호 중복이면 예외")
    void 파트너_등록_사업자번호_중복이면_예외() {
        final PartnerRegisterCommand command = PartnerRegisterCommand.builder()
            .loginId("newlogin").businessNumber("duplicate-bn").build();
        given(partnerReader.existsByLoginId("newlogin")).willReturn(false);
        given(partnerReader.existsByBusinessNumber("duplicate-bn")).willReturn(true);

        assertThatThrownBy(() -> partnerService.register(command))
            .isInstanceOf(BusinessException.class)
            .hasFieldOrPropertyWithValue("errorCode", ErrorCode.DUPLICATE_BUSINESS_NUMBER);
    }

    @Test
    @DisplayName("파트너 로그인 성공 - JWT 발급")
    void 파트너_로그인_성공_JWT_발급() {
        final Partner partner = createActivePartner();
        given(partnerReader.getByLoginId("testlogin")).willReturn(partner);
        given(passwordEncoder.matches("password123", "encoded")).willReturn(true);
        given(jwtProvider.generateToken(any(), anyString(), anyString())).willReturn("jwt-token");
        given(jwtProvider.getExpiration()).willReturn(1800000L);

        final PartnerLoginResult result = partnerService.login(
            PartnerLoginCommand.builder().loginId("testlogin").password("password123").build());

        assertThat(result.accessToken()).isEqualTo("jwt-token");
    }

    @Test
    @DisplayName("파트너 로그인 - 비밀번호 불일치 예외")
    void 파트너_로그인_비밀번호_불일치_예외() {
        final Partner partner = createActivePartner();
        given(partnerReader.getByLoginId("testlogin")).willReturn(partner);
        given(passwordEncoder.matches("wrong", "encoded")).willReturn(false);

        assertThatThrownBy(() -> partnerService.login(
            PartnerLoginCommand.builder().loginId("testlogin").password("wrong").build()))
            .isInstanceOf(AuthenticationException.class);
    }

    @Test
    @DisplayName("파트너 로그인 - 비활성 상태 예외")
    void 파트너_로그인_비활성_상태_예외() {
        final Partner partner = Partner.create(
            "테스트", "123-45-67890", "홍길동", "010-1234-5678", "test@test.com", "testlogin", "encoded",
            null, null);
        given(partnerReader.getByLoginId("testlogin")).willReturn(partner);
        given(passwordEncoder.matches("password123", "encoded")).willReturn(true);

        assertThatThrownBy(() -> partnerService.login(
            PartnerLoginCommand.builder().loginId("testlogin").password("password123").build()))
            .isInstanceOf(AuthorizationException.class);
    }

    @Test
    @DisplayName("파트너 조회 성공")
    void 파트너_조회_성공() {
        final Partner partner = createActivePartner();
        given(partnerReader.getById(1L)).willReturn(partner);

        final PartnerResult result = partnerService.getPartner(1L);

        assertThat(result.businessName()).isEqualTo("테스트");
    }

    @Test
    @DisplayName("파트너 정보수정 성공")
    void 파트너_정보수정_성공() {
        final Partner partner = createActivePartner();
        given(partnerReader.getById(1L)).willReturn(partner);

        partnerService.updatePartner(1L,
            PartnerUpdateCommand.builder().phone("010-9999").email("new@test.com")
                .bankName("신한").bankAccount("111").build());

        assertThat(partner.getPhone()).isEqualTo("010-9999");
    }

    private Partner createActivePartner() {
        final Partner partner = Partner.create(
            "테스트", "123-45-67890", "홍길동", "010-1234-5678", "test@test.com", "testlogin", "encoded",
            "국민은행", "123-456");
        partner.activate();
        return partner;
    }
}
