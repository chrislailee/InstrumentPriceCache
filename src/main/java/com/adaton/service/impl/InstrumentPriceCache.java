package com.adaton.service.impl;

import com.adaton.model.InstrumentPrice;
import com.adaton.service.IInstrumentPriceCache;

import java.time.LocalDate;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InstrumentPriceCache implements IInstrumentPriceCache {

    // Price date, instrument ID, list of prices for each vendor
    private ConcurrentHashMap<LocalDate, ConcurrentHashMap<String, ConcurrentHashMap<String, InstrumentPrice>>> cachedPricesByInstrument;
    // Price date, vendor ID, list of prices for each instrument
    private ConcurrentHashMap<LocalDate, ConcurrentHashMap<String, ConcurrentHashMap<String, InstrumentPrice>>> cachedPricesByVendor;

    public InstrumentPriceCache() {
        this.cachedPricesByInstrument = new ConcurrentHashMap<>();
        this.cachedPricesByVendor = new ConcurrentHashMap<>();
    }

    @Override
    public void publishInstrumentPrice(
            String vendorId,
            String instrumentId,
            LocalDate priceDate,
            Double price) {

        final InstrumentPrice instrumentPrice =
                new InstrumentPrice(
                        vendorId,
                        instrumentId,
                        priceDate,
                        price
                );

        cachedPricesByInstrument.compute(
                priceDate,
                (date, pricesForDate) -> {
                    if (pricesForDate == null) {
                        pricesForDate = new ConcurrentHashMap<>();
                    }

                    pricesForDate.compute(
                            instrumentId,
                            (instrumentId_, vendorPricesForInstr) -> {
                                if (vendorPricesForInstr == null) {
                                    vendorPricesForInstr = new ConcurrentHashMap<>();
                                }
                                vendorPricesForInstr.put(vendorId, instrumentPrice);
                                return vendorPricesForInstr;
                            }
                    );

                    return pricesForDate;
                }
        );

        cachedPricesByVendor.compute(
                priceDate,
                (date, pricesForDate) -> {
                    if (pricesForDate == null) {
                        pricesForDate = new ConcurrentHashMap<>();
                    }

                    pricesForDate.compute(
                            vendorId,
                            (vendorId_, instrumentPricesForVendor) -> {
                                if (instrumentPricesForVendor == null) {
                                    instrumentPricesForVendor = new ConcurrentHashMap<>();
                                }
                                instrumentPricesForVendor.put(instrumentId, instrumentPrice);
                                return instrumentPricesForVendor;
                            }
                    );

                    return pricesForDate;
                }
        );
    }

    @Override
    public Map<String, InstrumentPrice> getInstrumentPrice(String instrumentId, LocalDate priceDate) {
        if (cachedPricesByInstrument.containsKey(priceDate)) {
            final var pricesForDate = cachedPricesByInstrument.get(priceDate);
            if (pricesForDate.containsKey(instrumentId)) {
                return pricesForDate.get(instrumentId);
            }
            else {
                return Map.of();
            }
        }
        else {
            return Map.of();
        }
    }

    @Override
    public Map<String, InstrumentPrice> getAllInstrumentPricesForVendor(String vendorId, LocalDate priceDate) {
        if (cachedPricesByVendor.containsKey(priceDate)) {
            final var pricesForDate = cachedPricesByVendor.get(priceDate);
            if (pricesForDate.containsKey(vendorId)) {
                return pricesForDate.get(vendorId);
            }
            else {
                return Map.of();
            }
        }
        else {
            return Map.of();
        }
    }

}
