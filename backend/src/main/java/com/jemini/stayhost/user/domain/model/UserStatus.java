package com.jemini.stayhost.user.domain.model;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum UserStatus {

  ACTIVE("활성"),
  INACTIVE("탈퇴"),
  ;

  private final String description;
}
