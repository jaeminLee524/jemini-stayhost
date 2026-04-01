package com.jemini.stayhost.property.domain.component;

import com.jemini.stayhost.property.domain.model.Property;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PropertyReader {

    Property getById(Long id);

    Property getActiveById(Long id);

    Page<Property> findByPartnerId(Long partnerId, Pageable pageable);

    Page<Property> searchActive(String region, String keyword, Pageable pageable);
}
