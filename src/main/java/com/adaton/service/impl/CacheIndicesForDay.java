package com.adaton.service.impl;

import com.adaton.persistence.ICacheIndex;

import java.util.Objects;

public record CacheIndicesForDay(
        // Map of [Instrument ID --> Map of [Vendor ID --> Price]]
        ICacheIndex cachedPricesByInstrument,
        // Map of [Vendor ID --> Map of [Instrument ID --> Price]]
        ICacheIndex cachedPricesByVendor) {

    public CacheIndicesForDay {
        Objects.requireNonNull(cachedPricesByInstrument);
        Objects.requireNonNull(cachedPricesByVendor);
    }

}
