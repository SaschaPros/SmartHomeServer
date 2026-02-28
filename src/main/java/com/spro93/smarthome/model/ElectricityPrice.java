package com.spro93.smarthome.model;

import lombok.Data;
import java.util.List;

@Data
public class ElectricityPrice {
    private String tariff;
    private String unit;
    private int interval;
    private List<PriceData> data;
}
