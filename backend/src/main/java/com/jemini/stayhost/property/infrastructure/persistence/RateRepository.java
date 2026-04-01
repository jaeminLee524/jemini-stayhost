package com.jemini.stayhost.property.infrastructure.persistence;

import com.jemini.stayhost.property.domain.model.Rate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RateRepository extends JpaRepository<Rate, Long> {

    Optional<Rate> findByRoomTypeIdAndDate(Long roomTypeId, LocalDate date);

    List<Rate> findByRoomTypeIdAndDateBetween(Long roomTypeId, LocalDate startDate, LocalDate endDate);

    List<Rate> findByRoomTypeIdInAndDateBetween(List<Long> roomTypeIds, LocalDate startDate, LocalDate endDate);
}
