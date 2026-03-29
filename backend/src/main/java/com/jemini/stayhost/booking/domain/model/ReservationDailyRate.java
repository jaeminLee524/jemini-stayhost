package com.jemini.stayhost.booking.domain.model;

import com.jemini.stayhost.common.domain.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "reservation_daily_rate")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ReservationDailyRate extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reservation_id", nullable = false)
    private Reservation reservation;

    @Column(nullable = false)
    private LocalDate date;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    public static ReservationDailyRate create(
            final Reservation reservation,
            final LocalDate date,
            final BigDecimal price
    ) {
        final ReservationDailyRate dailyRate = new ReservationDailyRate();
        dailyRate.reservation = reservation;
        dailyRate.date = date;
        dailyRate.price = price;
        return dailyRate;
    }
}
