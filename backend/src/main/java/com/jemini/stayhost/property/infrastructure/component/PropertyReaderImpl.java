package com.jemini.stayhost.property.infrastructure.component;

import com.jemini.stayhost.common.exception.ErrorCode;
import com.jemini.stayhost.common.exception.NotFoundException;
import com.jemini.stayhost.property.domain.component.PropertyReader;
import com.jemini.stayhost.property.domain.model.Property;
import com.jemini.stayhost.property.infrastructure.persistence.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PropertyReaderImpl implements PropertyReader {

  private final PropertyRepository propertyRepository;

  @Override
  public Property getById(final Long id) {
    return propertyRepository.findById(id)
        .orElseThrow(() -> new NotFoundException(ErrorCode.PROPERTY_NOT_FOUND));
  }

  @Override
  public Page<Property> findByPartnerId(final Long partnerId, final Pageable pageable) {
    return propertyRepository.findByPartnerId(partnerId, pageable);
  }
}
