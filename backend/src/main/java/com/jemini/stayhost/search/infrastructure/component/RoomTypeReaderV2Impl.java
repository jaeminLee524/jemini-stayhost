package com.jemini.stayhost.search.infrastructure.component;

import com.jemini.stayhost.property.domain.model.RoomType;
import com.jemini.stayhost.property.domain.model.RoomTypeStatus;
import com.jemini.stayhost.property.infrastructure.persistence.RoomTypeRepository;
import com.jemini.stayhost.search.domain.component.RoomTypeReaderV2;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RoomTypeReaderV2Impl implements RoomTypeReaderV2 {

    private final RoomTypeRepository roomTypeRepository;

    @Cacheable(value = "roomTypes", key = "#propertyId")
    @Override
    public List<RoomType> findActiveByPropertyId(final Long propertyId) {
        return roomTypeRepository.findByPropertyIdAndStatus(propertyId, RoomTypeStatus.ACTIVE);
    }

    @Override
    public List<RoomType> findActiveByPropertyIds(final List<Long> propertyIds) {
        if (propertyIds.isEmpty()) {
            return List.of();
        }
        return roomTypeRepository.findByPropertyIdInAndStatus(propertyIds, RoomTypeStatus.ACTIVE);
    }
}
