package com.jemini.stayhost.property.infrastructure.persistence;

import com.jemini.stayhost.property.domain.model.RoomType;
import com.jemini.stayhost.property.domain.model.RoomTypeStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {

  List<RoomType> findByPropertyId(Long propertyId);

  List<RoomType> findByPropertyIdAndStatus(Long propertyId, RoomTypeStatus status);

  List<RoomType> findByPropertyIdInAndStatus(List<Long> propertyIds, RoomTypeStatus status);
}
