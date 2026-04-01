package com.jemini.stayhost.property.application.dto;

import lombok.Builder;

@Builder
public record PropertyImageResult(
    Long id,
    String imageUrl,
    Integer sortOrder
) {

}
