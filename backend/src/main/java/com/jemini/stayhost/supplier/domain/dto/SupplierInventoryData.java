package com.jemini.stayhost.supplier.domain.dto;

import java.time.LocalDate;

public record SupplierInventoryData(
    String externalRoomId,
    LocalDate date,
    int availableCount
) {

}
