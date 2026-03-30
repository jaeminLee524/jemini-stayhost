package com.jemini.stayhost.booking.domain.model;

import com.jemini.stayhost.common.domain.BaseEntity;
import com.jemini.stayhost.common.exception.AuthorizationException;
import com.jemini.stayhost.common.exception.BusinessException;
import com.jemini.stayhost.common.exception.ErrorCode;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "reservation")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String reservationNumber;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long propertyId;

    @Column(nullable = false)
    private Long roomTypeId;

    @Column(nullable = false)
    private LocalDate checkInDate;

    @Column(nullable = false)
    private LocalDate checkOutDate;

    @Column(nullable = false, length = 100)
    private String guestName;

    @Column(length = 20)
    private String guestPhone;

    @Column(nullable = false)
    private Integer guestCount;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal basePrice;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal discountAmount;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal finalPrice;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationStatus status;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ReservationSource source;

    private LocalDateTime cancelledAt;

    @Column(length = 500)
    private String cancelReason;

    private LocalDateTime confirmedAt;

    @OneToMany(mappedBy = "reservation", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReservationDailyRate> dailyRates = new ArrayList<>();

    public static Reservation create(
        final Long userId,
        final Long propertyId,
        final Long roomTypeId,
        final LocalDate checkInDate,
        final LocalDate checkOutDate,
        final String guestName,
        final String guestPhone,
        final int guestCount,
        final BigDecimal totalPrice
    ) {
        final Reservation reservation = new Reservation();
        reservation.reservationNumber = generateReservationNumber();
        reservation.userId = userId;
        reservation.propertyId = propertyId;
        reservation.roomTypeId = roomTypeId;
        reservation.checkInDate = checkInDate;
        reservation.checkOutDate = checkOutDate;
        reservation.guestName = guestName;
        reservation.guestPhone = stripHyphen(guestPhone);
        reservation.guestCount = guestCount;
        reservation.basePrice = totalPrice;
        reservation.discountAmount = BigDecimal.ZERO;
        reservation.finalPrice = totalPrice;
        reservation.status = ReservationStatus.CONFIRMED;
        reservation.source = ReservationSource.DIRECT;
        reservation.confirmedAt = LocalDateTime.now();
        return reservation;
    }

    public void cancel(final String reason) {
        if (this.status == ReservationStatus.CANCELLED) {
            throw new BusinessException(ErrorCode.RESERVATION_ALREADY_CANCELLED);
        }
        this.status = ReservationStatus.CANCELLED;
        this.cancelledAt = LocalDateTime.now();
        this.cancelReason = reason;
    }

    public void validateOwner(final Long requestingUserId) {
        if (!this.userId.equals(requestingUserId)) {
            throw new AuthorizationException("본인의 예약만 조회할 수 있습니다.");
        }
    }

    public void addDailyRate(final ReservationDailyRate dailyRate) {
        this.dailyRates.add(dailyRate);
    }

    private static String stripHyphen(final String value) {
        return value != null ? value.replace("-", "") : null;
    }

    private static String generateReservationNumber() {
        return "RSV-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
