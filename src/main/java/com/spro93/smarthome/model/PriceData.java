package com.spro93.smarthome.model;

import lombok.Data;
import java.time.ZonedDateTime;

@Data
public class PriceData {
    private ZonedDateTime date;
    private double value;
}
