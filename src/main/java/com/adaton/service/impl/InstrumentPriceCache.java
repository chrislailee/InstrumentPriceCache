package com.adaton.service.impl;

import com.adaton.model.InstrumentPrice;
import com.adaton.service.IInstrumentPriceCache;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;

public class InstrumentPriceCache implements IInstrumentPriceCache {

    private static final int MAX_DAY_COUNT = 30;

    // Sorted map with price date as key and cache indices as value that supports concureent access
    private final ConcurrentSkipListMap<LocalDate, CacheIndicesForDay> cacheIndicesByDate;

    public InstrumentPriceCache() {
        this.cacheIndicesByDate = new ConcurrentSkipListMap<>();
    }

    @Override
    public void publishInstrumentPrice(InstrumentPrice instrumentPrice) {
        if (cacheIndicesByDate.size() >= MAX_DAY_COUNT &&
            ! cacheIndicesByDate.containsKey(instrumentPrice.priceDate())) {
            // If the cache has reached MAX_DAY_COUNT and the cache does not contain the new price date to be
            // published, an existing date needs to be evicted from the cache to make room for the new date.
            final LocalDate earliestDateInCache = cacheIndicesByDate.firstKey();
            if (earliestDateInCache.isBefore(instrumentPrice.priceDate())) {
                // If the earliest date in the cache is older than the new price date to be published,
                // evict the old date.
                cacheIndicesByDate.remove(earliestDateInCache);
            }
            else {
                // Else, the new price date to be published is somehow older than the earliest date in the cache,
                // the new price date is ignored.
                return;
            }
        }

        cacheIndicesByDate.compute(
                instrumentPrice.priceDate(),
                (date, cacheIndicesByDate) -> {
                    if (cacheIndicesByDate == null) {
                        cacheIndicesByDate =
                                new CacheIndicesForDay(
                                        new ConcurrentHashMap<>(),
                                        new ConcurrentHashMap<>()
                                );
                    }

                    updateCacheIndex(
                            cacheIndicesByDate.cachedPricesByInstrument(),
                            instrumentPrice.instrumentId(),
                            instrumentPrice.vendorId(),
                            instrumentPrice
                    );
                    updateCacheIndex(
                            cacheIndicesByDate.cachedPricesByVendor(),
                            instrumentPrice.vendorId(),
                            instrumentPrice.instrumentId(),
                            instrumentPrice
                    );

                    return cacheIndicesByDate;
                }
        );
    }

    private void updateCacheIndex(
            ConcurrentHashMap<String, ConcurrentHashMap<String, InstrumentPrice>> cacheIndex,
            String outerKey,
            String innerKey,
            InstrumentPrice instrumentPrice) {
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
    public void publishInstrumentPrices(Collection<InstrumentPrice> instrumentPrices) {
        for (InstrumentPrice instrumentPrice : instrumentPrices) {
            publishInstrumentPrice(instrumentPrice);
        }
    }

    @Override
    public Map<String, InstrumentPrice> getInstrumentPrice(String instrumentId, LocalDate priceDate) {
        if (cacheIndicesByDate.containsKey(priceDate)) {
            final var pricesForDate = cacheIndicesByDate.get(priceDate).cachedPricesByInstrument();
            if (pricesForDate.containsKey(instrumentId)) {
                return Collections.unmodifiableMap(pricesForDate.get(instrumentId));
            }
            else {
                return Collections.emptyMap();
            }
        }
        else {
            return Collections.emptyMap();
        }
    }

    @Override
    public Map<String, InstrumentPrice> getAllInstrumentPricesForVendor(String vendorId, LocalDate priceDate) {
        if (cacheIndicesByDate.containsKey(priceDate)) {
            final var pricesForDate = cacheIndicesByDate.get(priceDate).cachedPricesByVendor();
            if (pricesForDate.containsKey(vendorId)) {
                return Collections.unmodifiableMap(pricesForDate.get(vendorId));
            }
            else {
                return Collections.emptyMap();
            }
        }
        else {
            return Collections.emptyMap();
        }
    }

}
