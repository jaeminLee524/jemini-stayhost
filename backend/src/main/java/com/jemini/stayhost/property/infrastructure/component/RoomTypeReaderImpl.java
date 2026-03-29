package com.jemini.stayhost.property.infrastructure.component;

import com.jemini.stayhost.common.exception.ErrorCode;
import com.jemini.stayhost.common.exception.NotFoundException;
import com.jemini.stayhost.property.domain.component.RoomTypeReader;
import com.jemini.stayhost.property.domain.model.RoomType;
import com.jemini.stayhost.property.infrastructure.persistence.RoomTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RoomTypeReaderImpl implements RoomTypeReader {

  private final RoomTypeRepository roomTypeRepository;

  @Override
  public RoomType getById(final Long id) {
    return roomTypeRepository.findById(id)
        .orElseThrow(() -> new NotFoundException(ErrorCode.ROOM_TYPE_NOT_FOUND));
  }

  @Override
  public List<RoomType> findByPropertyId(final Long propertyId) {
    return roomTypeRepository.findByPropertyId(propertyId);
  }
}
