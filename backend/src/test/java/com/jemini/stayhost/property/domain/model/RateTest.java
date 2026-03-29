package com.jemini.stayhost.property.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class RateTest {

    @Test
    @DisplayName("요금 생성 성공")
    void 요금_생성_성공() {
        final Rate rate = Rate.create(1L, LocalDate.of(2026, 4, 1), BigDecimal.valueOf(150000));

        assertThat(rate.getRoomTypeId()).isEqualTo(1L);
        assertThat(rate.getDate()).isEqualTo(LocalDate.of(2026, 4, 1));
        assertThat(rate.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(150000));
    }

    @Test
    @DisplayName("요금 가격수정 성공")
    void 요금_가격수정_성공() {
        final Rate rate = Rate.create(1L, LocalDate.of(2026, 4, 1), BigDecimal.valueOf(150000));

        rate.updatePrice(BigDecimal.valueOf(200000));

        assertThat(rate.getPrice()).isEqualByComparingTo(BigDecimal.valueOf(200000));
    }
}
