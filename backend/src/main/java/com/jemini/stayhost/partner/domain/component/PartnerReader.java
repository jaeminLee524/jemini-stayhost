package com.jemini.stayhost.partner.domain.component;

import com.jemini.stayhost.partner.domain.model.Partner;

public interface PartnerReader {

    Partner getById(Long id);

    Partner getByLoginId(String loginId);

    boolean existsByLoginId(String loginId);

    boolean existsByBusinessNumber(String businessNumber);
}
