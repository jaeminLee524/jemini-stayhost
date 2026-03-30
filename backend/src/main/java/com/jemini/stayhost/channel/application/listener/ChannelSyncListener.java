package com.jemini.stayhost.channel.application.listener;

import java.time.LocalDate;
import java.util.List;

import com.jemini.stayhost.booking.domain.component.ReservationReader;
import com.jemini.stayhost.booking.domain.event.ReservationCreatedEvent;
import com.jemini.stayhost.booking.domain.model.Reservation;
import com.jemini.stayhost.channel.application.service.ChannelManagerService;
import com.jemini.stayhost.property.domain.event.InventoryChangedEvent;
import com.jemini.stayhost.property.domain.component.RoomTypeReader;
import com.jemini.stayhost.property.domain.model.RoomType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class ChannelSyncListener {

    private final ChannelManagerService channelManagerService;
    private final ReservationReader reservationReader;
    private final RoomTypeReader roomTypeReader;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onInventoryChanged(final InventoryChangedEvent event) {
        log.info("채널 동기화 트리거: InventoryChangedEvent roomTypeId={}", event.roomTypeId());
        final RoomType roomType = roomTypeReader.getById(event.roomTypeId());
        channelManagerService.pushInventoryToChannels(roomType.getPropertyId(), event.roomTypeId(), event.affectedDates());
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onReservationCreated(final ReservationCreatedEvent event) {
        log.info("채널 동기화 트리거: ReservationCreatedEvent reservationId={}", event.reservationId());
        final Reservation reservation = reservationReader.getById(event.reservationId());
        final List<LocalDate> stayDates = reservation.getCheckInDate().datesUntil(reservation.getCheckOutDate()).toList();
        channelManagerService.pushInventoryToChannels(reservation.getPropertyId(), reservation.getRoomTypeId(), stayDates);
    }
}
