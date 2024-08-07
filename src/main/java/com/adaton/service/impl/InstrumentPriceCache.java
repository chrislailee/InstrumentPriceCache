package com.adaton.service.impl;

import com.adaton.model.InstrumentPrice;
import com.adaton.service.IInstrumentPriceCache;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class InstrumentPriceCache implements IInstrumentPriceCache {

    // Price date, instrument ID, list of prices for each vendor
    final private ConcurrentHashMap<LocalDate, ConcurrentHashMap<String, ConcurrentHashMap<String, InstrumentPrice>>> cachedPricesByInstrument;
    // Price date, vendor ID, list of prices for each instrument
    final private ConcurrentHashMap<LocalDate, ConcurrentHashMap<String, ConcurrentHashMap<String, InstrumentPrice>>> cachedPricesByVendor;

    public InstrumentPriceCache() {
        this.cachedPricesByInstrument = new ConcurrentHashMap<>();
        this.cachedPricesByVendor = new ConcurrentHashMap<>();
    }

    @Override
    public void publishInstrumentPrice(InstrumentPrice instrumentPrice) {
        cachedPricesByInstrument.compute(
                instrumentPrice.priceDate(),
                (date, pricesForDate) -> {
                    if (pricesForDate == null) {
                        pricesForDate = new ConcurrentHashMap<>();
                    }

                    pricesForDate.compute(
                            instrumentPrice.instrumentId(),
                            (instrumentId_, vendorPricesForInstr) -> {
                                if (vendorPricesForInstr == null) {
                                    vendorPricesForInstr = new ConcurrentHashMap<>();
                                }
                                vendorPricesForInstr.put(instrumentPrice.vendorId(), instrumentPrice);
                                return vendorPricesForInstr;
                            }
                    );

                    return pricesForDate;
                }
        );

        cachedPricesByVendor.compute(
                instrumentPrice.priceDate(),
                (date, pricesForDate) -> {
                    if (pricesForDate == null) {
                        pricesForDate = new ConcurrentHashMap<>();
                    }

                    pricesForDate.compute(
                            instrumentPrice.vendorId(),
                            (vendorId_, instrumentPricesForVendor) -> {
                                if (instrumentPricesForVendor == null) {
                                    instrumentPricesForVendor = new ConcurrentHashMap<>();
                                }
                                instrumentPricesForVendor.put(instrumentPrice.instrumentId(), instrumentPrice);
                                return instrumentPricesForVendor;
                            }
                    );

                    return pricesForDate;
                }
        );
    }

    @Override
    public void publishInstrumentPrices(Collection<InstrumentPrice> instrumentPrices) {
        for (InstrumentPrice instrumentPrice : instrumentPrices) {
            publishInstrumentPrice(instrumentPrice);
        }
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
