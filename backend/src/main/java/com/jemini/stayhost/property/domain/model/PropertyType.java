package com.jemini.stayhost.property.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PropertyType {

  HOTEL("호텔"),
  MOTEL("모텔"),
  PENSION("펜션"),
  RESORT("리조트"),
  GUESTHOUSE("게스트하우스"),
  ;

  private final String description;
}
