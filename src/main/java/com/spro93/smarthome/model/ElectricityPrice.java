package com.spro93.smarthome.model;

import java.util.List;
import java.util.Objects;

public record ElectricityPrice(String tariff, String unit, int interval, List<PriceData> data) {
    public ElectricityPrice {
        data = List.copyOf(Objects.requireNonNullElse(data, List.of()));
    }
}
