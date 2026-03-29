package com.jemini.stayhost.property.infrastructure.persistence;

import com.jemini.stayhost.property.domain.model.Rate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface RateRepository extends JpaRepository<Rate, Long> {

  List<Rate> findByRoomTypeIdAndDateBetween(Long roomTypeId, LocalDate startDate, LocalDate endDate);
}
