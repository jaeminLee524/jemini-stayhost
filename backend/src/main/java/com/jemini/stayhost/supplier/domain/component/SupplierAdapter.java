package com.jemini.stayhost.supplier.domain.component;

import com.jemini.stayhost.supplier.domain.dto.SupplierBookingRequest;
import com.jemini.stayhost.supplier.domain.dto.SupplierBookingResult;
import com.jemini.stayhost.supplier.domain.dto.SupplierInventoryData;
import com.jemini.stayhost.supplier.domain.dto.SupplierPropertyData;
import com.jemini.stayhost.supplier.domain.dto.SupplierRateData;

import java.time.LocalDate;
import java.util.List;

public interface SupplierAdapter {

    String getSupplierCode();

    List<SupplierPropertyData> fetchProperties();

    List<SupplierRateData> fetchRates(String externalPropertyId, LocalDate from, LocalDate to);

    List<SupplierInventoryData> fetchInventory(String externalPropertyId, LocalDate from, LocalDate to);

    SupplierBookingResult createBooking(SupplierBookingRequest request);

    SupplierBookingResult cancelBooking(String externalBookingId);
}
