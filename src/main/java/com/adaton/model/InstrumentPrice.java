package com.adaton.model;

import java.time.LocalDate;

public record InstrumentPrice(
        String vendorId,
        String instrumentId,
        LocalDate priceDate,
        Double price) {
}
