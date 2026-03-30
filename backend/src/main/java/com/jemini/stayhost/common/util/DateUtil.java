package com.jemini.stayhost.common.util;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

public final class DateUtil {

    private DateUtil() {
    }

    /**
     * 시작일~종료일 사이 일수 (양쪽 포함).
     */
    public static long dayCountInclusive(final LocalDate startDate, final LocalDate endDate) {
        return ChronoUnit.DAYS.between(startDate, endDate) + 1;
    }

    /**
     * 시작일~종료일 날짜 목록 (양쪽 포함).
     */
    public static List<LocalDate> dateRangeInclusive(final LocalDate startDate, final LocalDate endDate) {
        return startDate.datesUntil(endDate.plusDays(1)).toList();
    }
}
