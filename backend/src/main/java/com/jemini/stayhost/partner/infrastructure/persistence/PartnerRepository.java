package com.jemini.stayhost.partner.infrastructure.persistence;

import com.jemini.stayhost.partner.domain.model.Partner;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PartnerRepository extends JpaRepository<Partner, Long> {

  Optional<Partner> findByLoginId(String loginId);

  boolean existsByLoginId(String loginId);

  boolean existsByBusinessNumber(String businessNumber);
}
