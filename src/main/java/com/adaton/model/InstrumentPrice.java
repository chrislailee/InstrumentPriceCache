package com.adaton.model;

import org.apache.commons.lang3.StringUtils;

import java.time.LocalDate;
import java.util.Objects;

public record InstrumentPrice(
        String vendorId,
        String instrumentId,
        LocalDate priceDate,
        double price) {

    public InstrumentPrice {
        if (StringUtils.isEmpty(vendorId))
            throw new IllegalArgumentException("vendorId cannot be empty");
        if (StringUtils.isEmpty(instrumentId))
            throw new IllegalArgumentException("instrumentId cannot be empty");
        Objects.requireNonNull(priceDate);
    }

}
