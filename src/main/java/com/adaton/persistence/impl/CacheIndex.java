package com.adaton.persistence.impl;

import com.adaton.model.InstrumentPrice;
import com.adaton.persistence.ICacheIndex;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CacheIndex implements ICacheIndex {

    private final ConcurrentHashMap<String, ConcurrentHashMap<String, InstrumentPrice>> cacheIndex;

    public CacheIndex() {
        this.cacheIndex = new ConcurrentHashMap<>();
    }

    @Override
    public void updateCacheIndex(String outerKey, String innerKey, InstrumentPrice instrumentPrice) {
        cacheIndex.compute(
            outerKey,
            (oldKey_, instrumentPrices) -> {
                if (instrumentPrices == null) {
                    instrumentPrices = new ConcurrentHashMap<>();
                }
                instrumentPrices.put(innerKey, instrumentPrice);
                return instrumentPrices;
            }
        );
    }

    @Override
    public Map<String, InstrumentPrice> getCachedInstrumentPrices(String outerKey) {
        if (cacheIndex.containsKey(outerKey)) {
            return Collections.unmodifiableMap(cacheIndex.get(outerKey));
        }
        else {
            return Collections.emptyMap();
        }
    }

}
