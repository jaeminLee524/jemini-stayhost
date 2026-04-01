package com.jemini.stayhost.supplier.domain.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SupplierRateData(
    String externalRoomId,
    LocalDate date,
    BigDecimal price,
    String currency
) {

}
