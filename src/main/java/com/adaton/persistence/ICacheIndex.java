package com.adaton.persistence;

import com.adaton.model.InstrumentPrice;

import java.util.Map;

public interface ICacheIndex {

    /**
     * Update the cache index. The index is organised according to a 2-layer associative maps:
     * Map of [Instrument ID --> Map of [Vendor ID --> Price]]
     * or
     * Map of [Vendor ID --> Map of [Instrument ID --> Price]]
     *
     * @param outerKey  Key to the first layer map.
     * @param innerKey  Key to the second layer map.
     * @param instrumentPrice Instrument price object to be kept in cache.
     */
    void updateCacheIndex(
            String outerKey,
            String innerKey,
            InstrumentPrice instrumentPrice
    );

    /**
     * Retrieve instrument prices using the first layer key.
     *
     * @param outerKey  Key to the first layer map.
     * @return The second layer map associated with the outer key.
     */
    Map<String, InstrumentPrice> getCachedInstrumentPrices(String outerKey);

}
