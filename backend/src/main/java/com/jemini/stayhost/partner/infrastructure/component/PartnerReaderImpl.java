package com.jemini.stayhost.partner.infrastructure.component;

import com.jemini.stayhost.common.exception.ErrorCode;
import com.jemini.stayhost.common.exception.NotFoundException;
import com.jemini.stayhost.partner.domain.component.PartnerReader;
import com.jemini.stayhost.partner.domain.model.Partner;
import com.jemini.stayhost.partner.infrastructure.persistence.PartnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PartnerReaderImpl implements PartnerReader {

  private final PartnerRepository partnerRepository;

  @Override
  public Partner getById(final Long id) {
    return partnerRepository.findById(id)
        .orElseThrow(() -> new NotFoundException(ErrorCode.PARTNER_NOT_FOUND));
  }

  @Override
  public Partner getByLoginId(final String loginId) {
    return partnerRepository.findByLoginId(loginId)
        .orElseThrow(() -> new NotFoundException(ErrorCode.PARTNER_NOT_FOUND));
  }

  @Override
  public boolean existsByLoginId(final String loginId) {
    return partnerRepository.existsByLoginId(loginId);
  }

  @Override
  public boolean existsByBusinessNumber(final String businessNumber) {
    return partnerRepository.existsByBusinessNumber(businessNumber);
  }
}
