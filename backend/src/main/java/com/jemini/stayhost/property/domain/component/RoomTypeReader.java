package com.jemini.stayhost.property.domain.component;

import com.jemini.stayhost.property.domain.model.RoomType;

import java.util.List;

public interface RoomTypeReader {

  RoomType getById(Long id);

  List<RoomType> findByPropertyId(Long propertyId);
}
