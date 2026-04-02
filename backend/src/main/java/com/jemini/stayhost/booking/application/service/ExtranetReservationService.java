package com.jemini.stayhost.booking.application.service;

import com.jemini.stayhost.booking.application.dto.ExtranetReservationSearch;
import com.jemini.stayhost.booking.application.dto.ReservationResult;
import com.jemini.stayhost.booking.domain.component.ReservationReader;
import com.jemini.stayhost.booking.domain.model.Reservation;
import com.jemini.stayhost.common.response.PageResult;
import com.jemini.stayhost.property.domain.component.PropertyReader;
import com.jemini.stayhost.property.domain.component.RoomTypeReader;
import com.jemini.stayhost.property.domain.model.Property;
import com.jemini.stayhost.property.domain.model.RoomType;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ExtranetReservationService {

    private final ReservationReader reservationReader;
    private final PropertyReader propertyReader;
    private final RoomTypeReader roomTypeReader;

    @Transactional(readOnly = true)
    public PageResult<ReservationResult> getReservations(
        final Long partnerId,
        final ExtranetReservationSearch search,
        final Pageable pageable
    ) {
        final List<Long> partnerPropertyIds = propertyReader.findByPartnerId(partnerId, Pageable.unpaged())
            .stream()
            .map(Property::getId)
            .toList();

        if (partnerPropertyIds.isEmpty()) {
            return PageResult.from(Page.empty(pageable));
        }

        final Page<Reservation> reservationPage = reservationReader.findByPropertyIdsWithFilters(
            partnerPropertyIds, search.propertyId(), search.status(), search.checkInFrom(), search.checkInTo(), pageable);

        return PageResult.from(reservationPage.map(this::toResultFromReservation));
    }

    @Transactional(readOnly = true)
    public ReservationResult getReservation(
        final Long reservationId,
        final Long partnerId
    ) {
        final Reservation reservation = reservationReader.getById(reservationId);
        final Property property = propertyReader.getById(reservation.getPropertyId());

        property.validateOwner(partnerId);

        final RoomType roomType = roomTypeReader.getById(reservation.getRoomTypeId());
        return toResult(reservation, property, roomType);
    }

    private ReservationResult toResultFromReservation(final Reservation reservation) {
        final Property property = propertyReader.getById(reservation.getPropertyId());
        final RoomType roomType = roomTypeReader.getById(reservation.getRoomTypeId());

        return toResult(reservation, property, roomType);
    }

    private ReservationResult toResult(
        final Reservation reservation,
        final Property property,
        final RoomType roomType
    ) {
        return ReservationResult.from(
            reservation,
            property.getName(),
            property.getAddress(),
            roomType.getName(),
            property.getThumbnailUrl(),
            property.getCheckInTime(),
            property.getCheckOutTime()
        );
    }
}
