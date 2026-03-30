package com.jemini.stayhost.partner.application.service;

import com.jemini.stayhost.common.exception.AuthenticationException;
import com.jemini.stayhost.common.exception.BusinessException;
import com.jemini.stayhost.common.exception.ErrorCode;
import com.jemini.stayhost.common.security.JwtProvider;
import com.jemini.stayhost.partner.application.dto.*;
import com.jemini.stayhost.partner.domain.component.PartnerManager;
import com.jemini.stayhost.partner.domain.component.PartnerReader;
import com.jemini.stayhost.partner.domain.model.Partner;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PartnerService {

    private static final String ROLE_PARTNER = "PARTNER";
    private static final String CONTEXT_PARTNER = "PARTNER";
    private static final long SECONDS_DIVISOR = 1000;

    private final PartnerReader partnerReader;
    private final PartnerManager partnerManager;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    /**
     * 파트너 등록. 로그인 아이디/사업자번호 중복 검증 후 PENDING 상태로 생성한다.
     */
    @Transactional
    public PartnerResult register(final PartnerRegisterCommand command) {
        validateDuplicate(command);

        final Partner partner = createPartner(command);
        final Partner saved = partnerManager.save(partner);

        return PartnerResult.from(saved);
    }

    /**
     * 파트너 로그인. 비밀번호 검증 + 활성 상태 확인 후 JWT를 발급한다.
     */
    @Transactional(readOnly = true)
    public PartnerLoginResult login(final PartnerLoginCommand command) {
        final Partner partner = partnerReader.getByLoginId(command.loginId());

        validatePassword(command.password(), partner.getPassword());
        partner.validateActive();

        return createLoginResult(partner);
    }

    /**
     * 파트너 정보 조회.
     */
    @Transactional(readOnly = true)
    public PartnerResult getPartner(final Long partnerId) {
        final Partner partner = partnerReader.getById(partnerId);

        return PartnerResult.from(partner);
    }

    /**
     * 파트너 정보 수정. 사업자번호는 수정 불가.
     */
    @Transactional
    public PartnerResult updatePartner(final Long partnerId, final PartnerUpdateCommand command) {
        final Partner partner = partnerReader.getById(partnerId);

        partner.update(command.phone(), command.email(), command.bankName(), command.bankAccount());

        return PartnerResult.from(partner);
    }

    private void validateDuplicate(final PartnerRegisterCommand command) {
        if (partnerReader.existsByLoginId(command.loginId())) {
            throw new BusinessException(ErrorCode.DUPLICATE_LOGIN_ID);
        }
        if (partnerReader.existsByBusinessNumber(command.businessNumber())) {
            throw new BusinessException(ErrorCode.DUPLICATE_BUSINESS_NUMBER);
        }
    }

    private Partner createPartner(final PartnerRegisterCommand command) {
        return Partner.create(
            command.businessName(),
            command.businessNumber(),
            command.representative(),
            command.phone(),
            command.email(),
            command.loginId(),
            passwordEncoder.encode(command.password()),
            command.bankName(),
            command.bankAccount()
        );
    }

    private void validatePassword(final String rawPassword, final String encodedPassword) {
        if (!passwordEncoder.matches(rawPassword, encodedPassword)) {
            throw new AuthenticationException("로그인 아이디 또는 비밀번호가 올바르지 않습니다.");
        }
    }

    private PartnerLoginResult createLoginResult(final Partner partner) {
        final String token = jwtProvider.generateToken(partner.getId(), ROLE_PARTNER, CONTEXT_PARTNER);

        return PartnerLoginResult.create(
            token,
            jwtProvider.getExpiration() / SECONDS_DIVISOR,
            partner.getId(),
            partner.getBusinessName(),
            partner.getStatus().name()
        );
    }
}
