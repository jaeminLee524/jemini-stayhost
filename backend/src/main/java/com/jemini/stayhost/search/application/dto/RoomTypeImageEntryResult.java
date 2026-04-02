package com.jemini.stayhost.search.application.dto;

import lombok.Builder;

@Builder
public record RoomTypeImageEntryResult(
    String imageUrl,
    int sortOrder
) {

}
