package com.adaton;

import com.adaton.model.InstrumentPrice;
import com.adaton.service.IInstrumentPriceCache;
import com.adaton.service.impl.InstrumentPriceCache;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class AppTest {

    private IInstrumentPriceCache getCacheInstance() {
        return new InstrumentPriceCache();
    }

    private InstrumentPrice testV1I1Today = new InstrumentPrice("Vendor 1", "Instrument 1", LocalDate.now(), 100.0d);
    private InstrumentPrice testV1I2Today = new InstrumentPrice("Vendor 1", "Instrument 2", LocalDate.now(), 200.0d);
    private InstrumentPrice testV2I1Today = new InstrumentPrice("Vendor 2", "Instrument 1", LocalDate.now(), 110.0d);
    private InstrumentPrice testV2I2Today = new InstrumentPrice("Vendor 2", "Instrument 2", LocalDate.now(), 210.0d);
    private InstrumentPrice testV1I1Yesterday = new InstrumentPrice("Vendor 1", "Instrument 1", LocalDate.now().minusDays(1L), 90.0d);
    private InstrumentPrice testV121Yesterday = new InstrumentPrice("Vendor 1", "Instrument 2", LocalDate.now().minusDays(1L), 190.0d);
    private InstrumentPrice testV2I1Yesterday = new InstrumentPrice("Vendor 2", "Instrument 1", LocalDate.now().minusDays(1L), 105.0d);
    private InstrumentPrice testV2I2Yesterday = new InstrumentPrice("Vendor 2", "Instrument 2", LocalDate.now().minusDays(1L), 205.0d);

    private void verifyInstrumentPrice(Map<String, InstrumentPrice> instrumentPricesByVendor, InstrumentPrice expectedPrice) {
        assertTrue(instrumentPricesByVendor.containsKey(expectedPrice.vendorId()));

        final InstrumentPrice instrumentPrice = instrumentPricesByVendor.get(expectedPrice.vendorId());
        assertEquals(expectedPrice.vendorId(), instrumentPrice.vendorId());
        assertEquals(expectedPrice.instrumentId(), instrumentPrice.instrumentId());
        assertEquals(expectedPrice.priceDate(), instrumentPrice.priceDate());
        assertEquals(expectedPrice.price(), instrumentPrice.price(), 1.0e-6);
    }

    private void verifyAllInstrumentPricesForVendor(Map<String, InstrumentPrice> allInstrumentPricesForVendor, InstrumentPrice expectedPrice) {
        assertTrue(allInstrumentPricesForVendor.containsKey(expectedPrice.instrumentId()));

        final InstrumentPrice instrumentPrice = allInstrumentPricesForVendor.get(expectedPrice.instrumentId());
        assertEquals(expectedPrice.vendorId(), instrumentPrice.vendorId());
        assertEquals(expectedPrice.instrumentId(), instrumentPrice.instrumentId());
        assertEquals(expectedPrice.priceDate(), instrumentPrice.priceDate());
        assertEquals(expectedPrice.price(), instrumentPrice.price(), 1.0e-6);
    }

    @Test
    public void testSinglePricePublication() {
        final var instrumentPriceCache = getCacheInstance();

        // Publish single instrument price for a single vendor
        instrumentPriceCache.publishInstrumentPrice(
                testV1I1Today.vendorId(),
                testV1I1Today.instrumentId(),
                testV1I1Today.priceDate(),
                testV1I1Today.price()
        );

        // Retrieve the price by instrument ID
        final Map<String, InstrumentPrice> v1I1Price =
                instrumentPriceCache.getInstrumentPrice(testV1I1Today.instrumentId(), testV1I1Today.priceDate());

        assertNotNull(v1I1Price);
        assertEquals(1, v1I1Price.size());
        verifyInstrumentPrice(v1I1Price, testV1I1Today);

        // Retrieve the price by vendor ID
        final Map<String, InstrumentPrice> allPricesForV1 =
                instrumentPriceCache.getAllInstrumentPricesForVendor(testV1I1Today.vendorId(), testV1I1Today.priceDate());

        assertNotNull(allPricesForV1);
        assertEquals(1, allPricesForV1.size());
        verifyAllInstrumentPricesForVendor(allPricesForV1, testV1I1Today);

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

}
