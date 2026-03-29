package com.jemini.stayhost.partner.infrastructure.component;

import com.jemini.stayhost.partner.domain.component.PartnerManager;
import com.jemini.stayhost.partner.domain.model.Partner;
import com.jemini.stayhost.partner.infrastructure.persistence.PartnerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PartnerManagerImpl implements PartnerManager {

  private final PartnerRepository partnerRepository;

  @Override
  public Partner save(final Partner partner) {
    return partnerRepository.save(partner);
  }
}
