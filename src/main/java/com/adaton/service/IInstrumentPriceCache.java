package com.adaton.service;

import com.adaton.model.InstrumentPrice;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Map;

public interface IInstrumentPriceCache {

    /**
     * Publish an instrument price for a vendor to the cache.
     *
     * @param instrumentPrice InstrumentPrice object to be published.
     */
    void publishInstrumentPrice(InstrumentPrice instrumentPrice);

    /**
     * Publish a collection of instrument prices to the cache.
     *
     * @param instrumentPrices Collection of instrument prices to be published.
     */
    void publishInstrumentPrices(Collection<InstrumentPrice> instrumentPrices);

    /**
     * Retrieve all instrument prices stored in the cache for the specified instrument ID.
     * There could be more than one record for a specified instrument ID if there is more than one
     * vendor that provides prices for the instrument.
     *
     * @param instrumentId Instrument ID for which price is to be retrieved.
     * @param priceDate    Trading date for prices.
     * @return A map that associates a vendor ID (key) to its price (value).
     */
    Map<String, InstrumentPrice> getInstrumentPrice(
            String instrumentId,
            LocalDate priceDate);

    /**
     * Retrieve all instrument prices stored in the cache for the specified vendor ID.
     * There will be as many records as there are instrument prices for the specified vendor ID and the price
     * date in the query.
     *
     * @param vendorId  Vendor ID for which instrument prices are to be retrieved.
     * @param priceDate Trading date for prices.
     * @return A map that associates an instrument ID (key) to its prices (value).
     */
    Map<String, InstrumentPrice> getAllInstrumentPricesForVendor(
            String vendorId,
            LocalDate priceDate);

}
