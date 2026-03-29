package com.jemini.stayhost.property.infrastructure.component;

import com.jemini.stayhost.property.domain.component.PropertyManager;
import com.jemini.stayhost.property.domain.model.Property;
import com.jemini.stayhost.property.infrastructure.persistence.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class PropertyManagerImpl implements PropertyManager {

  private final PropertyRepository propertyRepository;

  @Override
  public Property save(final Property property) {
    return propertyRepository.save(property);
  }
}
