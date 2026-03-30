package com.jemini.stayhost.booking.application.dto;

import com.jemini.stayhost.common.exception.BusinessException;
import com.jemini.stayhost.common.exception.ErrorCode;
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

    public List<LocalDate> generateStayDates() {
        if (!this.checkInDate.isBefore(checkOutDate)) {
            throw new BusinessException(ErrorCode.INVALID_DATE_RANGE);
        }

        return checkInDate.datesUntil(checkOutDate).toList();
    }
}
