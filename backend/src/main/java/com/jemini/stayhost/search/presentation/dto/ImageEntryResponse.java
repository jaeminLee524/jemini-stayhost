package com.jemini.stayhost.search.presentation.dto;

import lombok.Builder;

@Builder
public record ImageEntryResponse(
    String imageUrl,
    int sortOrder
) {

}
