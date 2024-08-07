package com.adaton.service.impl;

import com.adaton.model.InstrumentPrice;

import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public record CacheIndicesForDay(
        // Map of [Instrument ID --> Map of [Vendor ID --> Price]]
        ConcurrentHashMap<String, ConcurrentHashMap<String, InstrumentPrice>> cachedPricesByInstrument,
        // Map of [Vendor ID --> Map of [Instrument ID --> Price]]
        ConcurrentHashMap<String, ConcurrentHashMap<String, InstrumentPrice>> cachedPricesByVendor) {

    public CacheIndicesForDay {
        Objects.requireNonNull(cachedPricesByInstrument);
        Objects.requireNonNull(cachedPricesByVendor);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CacheIndicesForDay that = (CacheIndicesForDay) o;
        return Objects.equals(cachedPricesByVendor, that.cachedPricesByVendor) && Objects.equals(cachedPricesByInstrument, that.cachedPricesByInstrument);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cachedPricesByInstrument, cachedPricesByVendor);
    }

    @Override
    public String toString() {
        return "CacheIndicesForDay{" +
                "cachedPricesByInstrument=" + cachedPricesByInstrument +
                ", cachedPricesByVendor=" + cachedPricesByVendor +
                '}';
    }

}
