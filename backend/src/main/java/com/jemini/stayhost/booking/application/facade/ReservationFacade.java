package com.jemini.stayhost.booking.application.facade;

import com.jemini.stayhost.booking.application.dto.CreateReservationCommand;
import com.jemini.stayhost.booking.application.dto.ReservationResult;
import com.jemini.stayhost.booking.application.service.ReservationService;
import com.jemini.stayhost.booking.infrastructure.cache.InventoryCache;
import com.jemini.stayhost.property.application.service.PropertyService;
import com.jemini.stayhost.property.application.service.RoomTypeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class ReservationFacade {

    private final ReservationService reservationService;
    private final PropertyService propertyService;
    private final RoomTypeService roomTypeService;
    private final InventoryCache inventoryCache;

    /**
     * <p> 예약 생성 오케스트레이션.
     * <p> 단일 인스턴스 환경을 전제로 JVM 로컬 CAS 캐시를 1차 필터로 사용한다.
     * <p> 재고 소진 시 DB 접근 없이 빠르게 실패시켜 락 경합과 커넥션 소모를 줄인다.
     * <p> 캐시는 낙관적 처리이므로 정합성은 DB 비관적 락이 최종 보장한다.
     * <p> 다중 인스턴스 전환 시 로컬 캐시를 Redis DECRBY 등 글로벌 원자 연산으로 교체해야 한다.
     */
    public ReservationResult createReservation(
        final Long userId,
        final CreateReservationCommand command
    ) {
        propertyService.validatePropertyActive(command.propertyId());
        roomTypeService.validateGuestCount(command.roomTypeId(), command.guestCount());

        final List<LocalDate> stayDates = command.generateStayDates();

        inventoryCache.tryDecreaseAll(command.roomTypeId(), stayDates);

        try {
            return reservationService.createWithExclusiveLock(userId, command);
        } catch (final Exception e) {
            inventoryCache.rollbackAll(command.roomTypeId(), stayDates);
            throw e;
        }
    }
}
