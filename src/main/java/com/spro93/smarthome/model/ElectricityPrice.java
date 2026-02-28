package com.spro93.smarthome.model;

import java.util.List;

public record ElectricityPrice(String tariff, String unit, int interval, List<PriceData> data) {
}
