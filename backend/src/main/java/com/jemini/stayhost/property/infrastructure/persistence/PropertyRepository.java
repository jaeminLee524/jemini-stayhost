package com.jemini.stayhost.property.infrastructure.persistence;

import com.jemini.stayhost.property.domain.model.Property;
import com.jemini.stayhost.property.domain.model.PropertyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PropertyRepository extends JpaRepository<Property, Long> {

  Page<Property> findByPartnerId(Long partnerId, Pageable pageable);

  Optional<Property> findByIdAndStatus(Long id, PropertyStatus status);
}
