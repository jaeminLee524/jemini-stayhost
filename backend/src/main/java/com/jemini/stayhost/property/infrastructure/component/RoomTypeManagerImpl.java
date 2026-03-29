package com.jemini.stayhost.property.infrastructure.component;

import com.jemini.stayhost.property.domain.component.RoomTypeManager;
import com.jemini.stayhost.property.domain.model.RoomType;
import com.jemini.stayhost.property.infrastructure.persistence.RoomTypeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RoomTypeManagerImpl implements RoomTypeManager {

  private final RoomTypeRepository roomTypeRepository;

  @Override
  public RoomType save(final RoomType roomType) {
    return roomTypeRepository.save(roomType);
  }
}
