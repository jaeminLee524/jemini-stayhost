package com.jemini.stayhost.channel.domain.dto;

import java.time.LocalDate;

public record InventoryUpdate(
    String externalRoomId,
    LocalDate date,
    int availableCount
) {
}
