package com.jemini.stayhost.search.domain.component;

import com.jemini.stayhost.property.domain.model.Property;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PropertyReaderV2 {

    Property getActiveById(Long id);

    Page<Property> searchActive(String region, String keyword, Pageable pageable);
}
