package com.jemini.stayhost.booking.domain.model;

import com.jemini.stayhost.common.exception.AuthorizationException;
import com.jemini.stayhost.common.exception.BusinessException;
import com.jemini.stayhost.common.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationTest {

    @Test
    @DisplayName("예약 생성 시 기본값이 올바르게 설정된다")
    void 예약_생성_시_기본값이_올바르게_설정된다() {
        final Reservation reservation = createReservation();

        assertThat(reservation.getReservationNumber()).startsWith("RSV-");
        assertThat(reservation.getUserId()).isEqualTo(1L);
        assertThat(reservation.getPropertyId()).isEqualTo(10L);
        assertThat(reservation.getRoomTypeId()).isEqualTo(100L);
        assertThat(reservation.getCheckInDate()).isEqualTo(LocalDate.of(2026, 5, 1));
        assertThat(reservation.getCheckOutDate()).isEqualTo(LocalDate.of(2026, 5, 3));
        assertThat(reservation.getGuestName()).isEqualTo("홍길동");
        assertThat(reservation.getGuestCount()).isEqualTo(2);
        assertThat(reservation.getBasePrice()).isEqualByComparingTo(BigDecimal.valueOf(200000));
        assertThat(reservation.getDiscountAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(reservation.getFinalPrice()).isEqualByComparingTo(BigDecimal.valueOf(200000));
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CONFIRMED);
        assertThat(reservation.getSource()).isEqualTo(ReservationSource.DIRECT);
        assertThat(reservation.getConfirmedAt()).isNotNull();
    }

    @Test
    @DisplayName("전화번호 하이픈이 제거된다")
    void 전화번호_하이픈이_제거된다() {
        final Reservation reservation = createReservation();

        assertThat(reservation.getGuestPhone()).isEqualTo("01012345678");
    }

    @Test
    @DisplayName("예약 취소 시 상태가 CANCELLED로 변경된다")
    void 예약_취소_시_상태가_CANCELLED로_변경된다() {
        final Reservation reservation = createReservation();

        reservation.cancel("단순 변심");

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
        assertThat(reservation.getCancelledAt()).isNotNull();
        assertThat(reservation.getCancelReason()).isEqualTo("단순 변심");
    }

    @Test
    @DisplayName("이미 취소된 예약을 다시 취소하면 예외가 발생한다")
    void 이미_취소된_예약을_다시_취소하면_예외가_발생한다() {
        final Reservation reservation = createReservation();
        reservation.cancel("첫 번째 취소");

        assertThatThrownBy(() -> reservation.cancel("두 번째 취소"))
            .isInstanceOf(BusinessException.class)
            .extracting("errorCode")
            .isEqualTo(ErrorCode.RESERVATION_ALREADY_CANCELLED);
    }

    @Test
    @DisplayName("본인 예약이면 검증을 통과한다")
    void 본인_예약이면_검증을_통과한다() {
        final Reservation reservation = createReservation();

        reservation.validateOwner(1L);
    }

    @Test
    @DisplayName("본인 예약이 아니면 권한 예외가 발생한다")
    void 본인_예약이_아니면_권한_예외가_발생한다() {
        final Reservation reservation = createReservation();

        assertThatThrownBy(() -> reservation.validateOwner(999L))
            .isInstanceOf(AuthorizationException.class);
    }

    @Test
    @DisplayName("일별 요금이 추가된다")
    void 일별_요금이_추가된다() {
        final Reservation reservation = createReservation();
        final ReservationDailyRate dailyRate = ReservationDailyRate.create(reservation, LocalDate.of(2026, 5, 1), BigDecimal.valueOf(100000));

        reservation.addDailyRate(dailyRate);

        assertThat(reservation.getDailyRates()).hasSize(1);
    }

    private Reservation createReservation() {
        return Reservation.create(1L, 10L, 100L, LocalDate.of(2026, 5, 1), LocalDate.of(2026, 5, 3), "홍길동", "010-1234-5678", 2, BigDecimal.valueOf(200000));
    }
}
