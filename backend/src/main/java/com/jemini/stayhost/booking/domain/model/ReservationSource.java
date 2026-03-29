package com.jemini.stayhost.booking.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ReservationSource {

  DIRECT("자체 플랫폼 직접 예약"),
  CHANNEL("외부 채널(Expedia, Agoda 등) 통한 예약"),
  SUPPLIER("외부 공급자 재고 기반 예약"),
  ;

  private final String description;
}
