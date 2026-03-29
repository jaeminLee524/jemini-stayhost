package com.jemini.stayhost.partner.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PartnerStatus {

  PENDING("승인 대기"),
  ACTIVE("활성"),
  SUSPENDED("정지"),
  ;

  private final String description;
}
