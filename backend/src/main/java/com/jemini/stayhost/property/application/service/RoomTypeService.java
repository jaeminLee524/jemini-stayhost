package com.jemini.stayhost.property.application.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jemini.stayhost.property.application.dto.RoomTypeCreateCommand;
import com.jemini.stayhost.property.application.dto.RoomTypeResult;
import com.jemini.stayhost.property.application.dto.RoomTypeUpdateCommand;
import com.jemini.stayhost.property.domain.component.PropertyReader;
import com.jemini.stayhost.property.domain.component.RoomTypeManager;
import com.jemini.stayhost.property.domain.component.RoomTypeReader;
import com.jemini.stayhost.property.domain.model.Property;
import com.jemini.stayhost.property.domain.model.RoomType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomTypeService {

    private final RoomTypeReader roomTypeReader;
    private final RoomTypeManager roomTypeManager;
    private final PropertyReader propertyReader;
    private final ObjectMapper objectMapper;

    /**
     * 객실 유형 등록. 숙소 소유권을 검증한다.
     */
    @Transactional
    public RoomTypeResult createRoomType(
        final Long propertyId,
        final Long partnerId,
        final RoomTypeCreateCommand command
    ) {
        final Property property = propertyReader.getById(propertyId);
        property.validateOwner(partnerId);

        final RoomType roomType = buildRoomType(propertyId, command);
        final RoomType saved = roomTypeManager.save(roomType);

        return RoomTypeResult.from(saved);
    }

    /**
     * 숙소의 객실 유형 목록 조회. 소유권을 검증한다.
     */
    @Transactional(readOnly = true)
    public List<RoomTypeResult> getRoomTypes(final Long propertyId, final Long partnerId) {
        final Property property = propertyReader.getById(propertyId);
        property.validateOwner(partnerId);

        return roomTypeReader.findByPropertyId(propertyId).stream()
            .map(RoomTypeResult::from)
            .toList();
    }

    /**
     * 객실 유형 수정. 숙소 소유권을 검증한다.
     */
    @Transactional
    public RoomTypeResult updateRoomType(
        final Long roomTypeId,
        final Long partnerId,
        final RoomTypeUpdateCommand command
    ) {
        final RoomType roomType = roomTypeReader.getById(roomTypeId);

        validateRoomTypeOwner(roomType, partnerId);
        roomType.update(command.name(), command.description(), command.maxOccupancy(), command.basePrice(), toJson(command.amenities()));

        return RoomTypeResult.from(roomType);
    }

    private void validateRoomTypeOwner(final RoomType roomType, final Long partnerId) {
        final Property property = propertyReader.getById(roomType.getPropertyId());
        property.validateOwner(partnerId);
    }

    private RoomType buildRoomType(final Long propertyId, final RoomTypeCreateCommand command) {
        return RoomType.create(
            propertyId,
            command.name(),
            command.description(),
            command.maxOccupancy(),
            command.basePrice(),
            toJson(command.amenities()),
            command.totalRoomCount()
        );
    }

    private String toJson(final List<String> amenities) {
        if (amenities == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(amenities);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("어메니티 JSON 변환에 실패했습니다.", e);
        }
    }
}
