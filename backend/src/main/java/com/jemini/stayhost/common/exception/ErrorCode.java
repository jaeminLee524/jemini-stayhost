package com.jemini.stayhost.common.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    // Property
    PROPERTY_NOT_FOUND("숙소를 찾을 수 없습니다."),
    ROOM_TYPE_NOT_FOUND("객실 유형을 찾을 수 없습니다."),

    // Inventory
    INVENTORY_NOT_AVAILABLE("해당 날짜에 재고 정보가 없습니다."),
    INVENTORY_INSUFFICIENT("선택한 날짜에 객실이 매진되었습니다."),
    INVENTORY_TOTAL_BELOW_RESERVED("설정하려는 재고 수가 현재 예약 건수보다 적습니다."),

    // Reservation
    RESERVATION_NOT_FOUND("예약을 찾을 수 없습니다."),
    RESERVATION_ALREADY_CANCELLED("이미 취소된 예약입니다."),
    DUPLICATE_RESERVATION("동일한 날짜에 중복 예약이 존재합니다."),

    // Partner
    PARTNER_NOT_FOUND("파트너를 찾을 수 없습니다."),
    PARTNER_SUSPENDED("정지된 파트너 계정입니다."),
    DUPLICATE_LOGIN_ID("이미 사용 중인 로그인 아이디입니다."),
    DUPLICATE_BUSINESS_NUMBER("이미 등록된 사업자번호입니다."),

    // User
    USER_NOT_FOUND("사용자를 찾을 수 없습니다."),
    DUPLICATE_EMAIL("이미 사용 중인 이메일입니다."),
    INVALID_CREDENTIALS("이메일 또는 비밀번호가 올바르지 않습니다."),

    // Validation
    INVALID_DATE_RANGE("시작일은 종료일보다 이전이어야 합니다."),
    DATE_RANGE_TOO_LONG("날짜 범위는 최대 30일까지만 가능합니다."),
    INVALID_GUEST_COUNT("유효하지 않은 인원 수입니다."),
    VALIDATION_ERROR("요청 파라미터가 유효하지 않습니다."),

    // Auth
    UNAUTHORIZED("인증이 필요합니다."),
    FORBIDDEN("접근 권한이 없습니다."),

    // Supplier
    SUPPLIER_NOT_FOUND("공급사를 찾을 수 없습니다."),
    SUPPLIER_ADAPTER_NOT_FOUND("공급사에 대한 어댑터를 찾을 수 없습니다."),

    // System
    INTERNAL_SERVER_ERROR("서버 오류가 발생했습니다.");

    private final String message;
}
