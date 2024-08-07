package com.adaton;

import com.adaton.model.InstrumentPrice;
import com.adaton.service.IInstrumentPriceCache;
import com.adaton.service.impl.InstrumentPriceCache;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

public class AppTest {

    private IInstrumentPriceCache getCacheInstance() {
        return new InstrumentPriceCache();
    }

    private final InstrumentPrice testV1I1Today = new InstrumentPrice("Vendor 1", "Instrument 1", LocalDate.now(), 100.0d);
    private final InstrumentPrice testV1I2Today = new InstrumentPrice("Vendor 1", "Instrument 2", LocalDate.now(), 200.0d);
    private final InstrumentPrice testV2I1Today = new InstrumentPrice("Vendor 2", "Instrument 1", LocalDate.now(), 110.0d);
    private final InstrumentPrice testV2I2Today = new InstrumentPrice("Vendor 2", "Instrument 2", LocalDate.now(), 210.0d);
    private final InstrumentPrice testV1I1Yesterday = new InstrumentPrice("Vendor 1", "Instrument 1", LocalDate.now().minusDays(1L), 90.0d);
    private final InstrumentPrice testV1I2Yesterday = new InstrumentPrice("Vendor 1", "Instrument 2", LocalDate.now().minusDays(1L), 190.0d);
    private final InstrumentPrice testV2I1Yesterday = new InstrumentPrice("Vendor 2", "Instrument 1", LocalDate.now().minusDays(1L), 105.0d);
    private final InstrumentPrice testV2I2Yesterday = new InstrumentPrice("Vendor 2", "Instrument 2", LocalDate.now().minusDays(1L), 205.0d);

    private void testRetrieveByInstrumentId(IInstrumentPriceCache instrumentPriceCache, InstrumentPrice retrievalObj, int expectedRecordCount) {
        final Map<String, InstrumentPrice> instrumentPrices =
                instrumentPriceCache.getInstrumentPrice(retrievalObj.instrumentId(), retrievalObj.priceDate());

        assertNotNull(instrumentPrices);
        assertEquals(expectedRecordCount, instrumentPrices.size());

        assertTrue(instrumentPrices.containsKey(retrievalObj.vendorId()));

        final InstrumentPrice instrumentPrice = instrumentPrices.get(retrievalObj.vendorId());
        assertEquals(retrievalObj.vendorId(), instrumentPrice.vendorId());
        assertEquals(retrievalObj.instrumentId(), instrumentPrice.instrumentId());
        assertEquals(retrievalObj.priceDate(), instrumentPrice.priceDate());
        assertEquals(retrievalObj.price(), instrumentPrice.price(), 1.0e-6);
    }

    private void testRetrieveByVendorId(IInstrumentPriceCache instrumentPriceCache, InstrumentPrice retrievalObj, int expectedRecordCount) {
        final Map<String, InstrumentPrice> allPrices =
                instrumentPriceCache.getAllInstrumentPricesForVendor(retrievalObj.vendorId(), retrievalObj.priceDate());

        assertNotNull(allPrices);
        assertEquals(expectedRecordCount, allPrices.size());

        assertTrue(allPrices.containsKey(retrievalObj.instrumentId()));

        final InstrumentPrice instrumentPrice = allPrices.get(retrievalObj.instrumentId());
        assertEquals(retrievalObj.vendorId(), instrumentPrice.vendorId());
        assertEquals(retrievalObj.instrumentId(), instrumentPrice.instrumentId());
        assertEquals(retrievalObj.priceDate(), instrumentPrice.priceDate());
        assertEquals(retrievalObj.price(), instrumentPrice.price(), 1.0e-6);
    }

    @Test
    public void testSinglePricePublication() {
        final var instrumentPriceCache = getCacheInstance();

        // Publish single instrument price for a single vendor
        instrumentPriceCache.publishInstrumentPrice(testV1I1Today);

        // Retrieve price by instrument ID
        testRetrieveByInstrumentId(instrumentPriceCache, testV1I1Today, 1);

        // Retrieve price by vendor ID
        testRetrieveByVendorId(instrumentPriceCache, testV1I1Today, 1);

        // Retrieve for a non-existent date
        Map<String, InstrumentPrice> nonExistDate =
                instrumentPriceCache.getInstrumentPrice(testV1I1Today.instrumentId(), testV1I1Today.priceDate().minusDays(1L));
        assertNotNull(nonExistDate);
        assertEquals(0, nonExistDate.size());

        nonExistDate =
                instrumentPriceCache.getAllInstrumentPricesForVendor(testV1I1Today.vendorId(), testV1I1Today.priceDate().minusDays(1L));
        assertNotNull(nonExistDate);
        assertEquals(0, nonExistDate.size());

        // Retrieve for a non-existent instrument ID
        Map<String, InstrumentPrice> nonExistInstrumentID =
                instrumentPriceCache.getInstrumentPrice("Non-existent Instrument ID", testV1I1Today.priceDate());
        assertNotNull(nonExistInstrumentID);
        assertEquals(0, nonExistInstrumentID.size());

        // Retrieve for a non-existent vendor ID
        Map<String, InstrumentPrice> nonExistVendorID =
                instrumentPriceCache.getAllInstrumentPricesForVendor("Non-existent Vendor ID", testV1I1Today.priceDate());
        assertNotNull(nonExistVendorID);
        assertEquals(0, nonExistVendorID.size());
    }

    @Test
    public void testSingleVendorMultipleInstrumentPricesPublication() {
        final var instrumentPriceCache = getCacheInstance();

        // Publish multiple instrument prices for a single vendor
        instrumentPriceCache.publishInstrumentPrice(testV1I1Today);
        instrumentPriceCache.publishInstrumentPrice(testV1I2Today);

        // Retrieve price by instrument ID
        testRetrieveByInstrumentId(instrumentPriceCache, testV1I1Today, 1);
        testRetrieveByInstrumentId(instrumentPriceCache, testV1I2Today, 1);

        // Retrieve prices by vendor ID
        testRetrieveByVendorId(instrumentPriceCache, testV1I1Today, 2);
        testRetrieveByVendorId(instrumentPriceCache, testV1I2Today, 2);
    }

    @Test
    public void testMultipleVendorsMultipleInstrumentPricesPublication() {
        final var instrumentPriceCache = getCacheInstance();

        // Publish multiple instrument prices for multiple vendors
        instrumentPriceCache.publishInstrumentPrices(
                List.of(testV1I1Today, testV1I2Today, testV2I1Today, testV2I2Today)
        );

        // Retrieve prices by instrument ID
        testRetrieveByInstrumentId(instrumentPriceCache, testV1I1Today, 2);
        testRetrieveByInstrumentId(instrumentPriceCache, testV1I2Today, 2);
        testRetrieveByInstrumentId(instrumentPriceCache, testV2I1Today, 2);
        testRetrieveByInstrumentId(instrumentPriceCache, testV2I2Today, 2);

        // Retrieve prices by vendor ID
        testRetrieveByVendorId(instrumentPriceCache, testV1I1Today, 2);
        testRetrieveByVendorId(instrumentPriceCache, testV1I2Today, 2);
        testRetrieveByVendorId(instrumentPriceCache, testV2I1Today, 2);
        testRetrieveByVendorId(instrumentPriceCache, testV2I2Today, 2);
    }

    @Test
    public void testMultipleVendorsMultipleInstrumentsMultipleDaysPublication() {
        final var instrumentPriceCache = getCacheInstance();

        // Publish multiple instrument prices for multiple vendors across different dates
        instrumentPriceCache.publishInstrumentPrices(
                List.of(
                        testV1I1Today, testV1I2Today, testV2I1Today, testV2I2Today,
                        testV1I1Yesterday, testV1I2Yesterday, testV2I1Yesterday, testV2I2Yesterday
                )
        );

        // Retrieve prices by instrument ID
        testRetrieveByInstrumentId(instrumentPriceCache, testV1I1Today, 2);
        testRetrieveByInstrumentId(instrumentPriceCache, testV1I2Today, 2);
        testRetrieveByInstrumentId(instrumentPriceCache, testV2I1Today, 2);
        testRetrieveByInstrumentId(instrumentPriceCache, testV2I2Today, 2);
        testRetrieveByInstrumentId(instrumentPriceCache, testV1I1Yesterday, 2);
        testRetrieveByInstrumentId(instrumentPriceCache, testV1I2Yesterday, 2);
        testRetrieveByInstrumentId(instrumentPriceCache, testV2I1Yesterday, 2);
        testRetrieveByInstrumentId(instrumentPriceCache, testV2I2Yesterday, 2);

        // Retrieve prices by vendor ID
        testRetrieveByVendorId(instrumentPriceCache, testV1I1Today, 2);
        testRetrieveByVendorId(instrumentPriceCache, testV1I2Today, 2);
        testRetrieveByVendorId(instrumentPriceCache, testV2I1Today, 2);
        testRetrieveByVendorId(instrumentPriceCache, testV2I2Today, 2);
        testRetrieveByVendorId(instrumentPriceCache, testV1I1Yesterday, 2);
        testRetrieveByVendorId(instrumentPriceCache, testV1I2Yesterday, 2);
        testRetrieveByVendorId(instrumentPriceCache, testV2I1Yesterday, 2);
        testRetrieveByVendorId(instrumentPriceCache, testV2I2Yesterday, 2);
    }

    @Test
    public void testMaxCachedDays() {
        final var instrumentPriceCache = getCacheInstance();

        // Create 31 days of test records
        final List<InstrumentPrice> testRecords31Days =
            IntStream.range(0, 31)
                .mapToObj(dayIndex -> new InstrumentPrice("Vendor 1", "Instrument 1", LocalDate.now().minusDays(dayIndex), 100.0d + dayIndex))
                .toList();
        final List<InstrumentPrice> testRecords30Days = testRecords31Days.subList(0, 30);

        // Publish the first 30 days of prices
        instrumentPriceCache.publishInstrumentPrices(testRecords30Days);

        // Cache should contain exactly 30 days of prices
        for (InstrumentPrice instrumentPrice : testRecords30Days) {
            // Cached prices should match with test record for all days
            testRetrieveByInstrumentId(instrumentPriceCache, instrumentPrice, 1);
        }

        // Publish all 31 days of prices (the earliest day should be evicted)
        instrumentPriceCache.publishInstrumentPrices(testRecords31Days);

        // Cache should still contain exactly 30 days of prices
        for (InstrumentPrice instrumentPrice : testRecords31Days.subList(0, 30)) {
            // Cached prices should match with test record for all days
            testRetrieveByInstrumentId(instrumentPriceCache, instrumentPrice, 1);
        }

        // Record for the earliest date should be evicted
        final InstrumentPrice evictedDay = testRecords31Days.getLast();

        Map<String, InstrumentPrice> nonExistDate =
                instrumentPriceCache.getInstrumentPrice(evictedDay.instrumentId(), evictedDay.priceDate());
        assertNotNull(nonExistDate);
        assertEquals(0, nonExistDate.size());

        nonExistDate =
                instrumentPriceCache.getAllInstrumentPricesForVendor(evictedDay.vendorId(), evictedDay.priceDate());
        assertNotNull(nonExistDate);
        assertEquals(0, nonExistDate.size());
    }

    @Test
    public void testMaxCachedDaysWithInversePublicationOrder() {
        final var instrumentPriceCache = getCacheInstance();

        // Create 31 days of test records
        final List<InstrumentPrice> testRecords31DaysReversed =
                IntStream.range(0, 31)
                        .mapToObj(dayIndex -> new InstrumentPrice("Vendor 1", "Instrument 1", LocalDate.now().minusDays(dayIndex), 100.0d + dayIndex))
                        .toList()
                        .reversed();

        // Publish all 31 days of prices (the earliest day should be evicted)
        instrumentPriceCache.publishInstrumentPrices(testRecords31DaysReversed);

        // Cache should still contain exactly 30 days of prices
        for (InstrumentPrice instrumentPrice : testRecords31DaysReversed.subList(1, 31)) {
            // Cached prices should match with test record for all days
            testRetrieveByInstrumentId(instrumentPriceCache, instrumentPrice, 1);
        }

        // Record for the earliest date should be evicted
        final InstrumentPrice evictedDay = testRecords31DaysReversed.getFirst();

        Map<String, InstrumentPrice> nonExistDate =
                instrumentPriceCache.getInstrumentPrice(evictedDay.instrumentId(), evictedDay.priceDate());
        assertNotNull(nonExistDate);
        assertEquals(0, nonExistDate.size());

        nonExistDate =
                instrumentPriceCache.getAllInstrumentPricesForVendor(evictedDay.vendorId(), evictedDay.priceDate());
        assertNotNull(nonExistDate);
        assertEquals(0, nonExistDate.size());
    }

}