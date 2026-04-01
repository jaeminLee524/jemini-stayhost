package com.jemini.stayhost.search.domain.component;

import com.jemini.stayhost.property.domain.model.RoomType;

import java.util.List;

public interface RoomTypeReaderV2 {

    List<RoomType> findActiveByPropertyId(Long propertyId);

    List<RoomType> findActiveByPropertyIds(List<Long> propertyIds);
}
