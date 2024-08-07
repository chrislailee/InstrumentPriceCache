package com.adaton.service;

import com.adaton.model.InstrumentPrice;

import java.time.LocalDate;
import java.util.Map;

public interface IInstrumentPriceCache {

    void publishInstrumentPrice(
            String vendorId,
            String instrumentId,
            LocalDate priceDate,
            Double price);

    Map<String, InstrumentPrice> getInstrumentPrice(
            String instrumentId,
            LocalDate priceDate);

    Map<String, InstrumentPrice> getAllInstrumentPricesForVendor(
            String vendorId,
            LocalDate priceDate);

}
