package com.spro93.smarthome.model;

import java.time.ZonedDateTime;
import java.util.Objects;

public record PriceData(ZonedDateTime date, double value) {
    public PriceData {
        Objects.requireNonNull(date, "date must not be null");
    }
}
