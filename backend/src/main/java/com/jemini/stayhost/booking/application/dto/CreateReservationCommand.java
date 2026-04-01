package com.jemini.stayhost.booking.application.dto;

import com.jemini.stayhost.common.exception.BusinessException;
import com.jemini.stayhost.common.exception.ErrorCode;
import com.jemini.stayhost.common.util.DateUtil;
import java.util.List;
import lombok.Builder;

import java.time.LocalDate;

@Builder
public record CreateReservationCommand(
    Long propertyId,
    Long roomTypeId,
    LocalDate checkInDate,
    LocalDate checkOutDate,
    String guestName,
    String guestPhone,
    int guestCount
) {

    /**
     * 숙박 날짜 리스트 생성 (체크인 날짜 포함, 체크아웃 날짜 제외)
     */
    public List<LocalDate> generateStayDates() {
        if (!this.checkInDate.isBefore(this.checkOutDate)) {
            throw new BusinessException(ErrorCode.INVALID_DATE_RANGE);
        }

        return DateUtil.dateRangeExclusive(this.checkInDate, this.checkOutDate);
    }
}
