package com.jemini.stayhost.search.application.dto;

import com.jemini.stayhost.property.domain.model.PropertyImage;
import lombok.Builder;

@Builder
public record PropertyImageEntryResult(
    String imageUrl,
    int sortOrder
) {

    public static PropertyImageEntryResult from(final PropertyImage image) {
        return PropertyImageEntryResult.builder()
            .imageUrl(image.getImageUrl())
            .sortOrder(image.getSortOrder())
            .build();
    }
}
