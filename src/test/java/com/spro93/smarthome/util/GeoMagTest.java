package com.spro93.smarthome.util;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class GeoMagTest {

    @Test
    public void testDeclination() {
        // Known declination for Vienna, Austria (48.2, 16.3) around 2024-2025 is approx 5.0 - 5.5 degrees East
        double dec = GeoMag.getDeclination(48.2, 16.3);
        System.out.println("Vienna declination: " + dec);
        // We don't need exact match as it depends on the date, but it should be in the ballpark
        assert(dec > 4.0 && dec < 7.0);
    }

    @Test
    public void testDeclinationEquator() {
        double dec = GeoMag.getDeclination(0, 0);
        System.out.println("Equator declination: " + dec);
        // Around -3.0 to -4.0 degrees West
        assert(dec < 0);
    }
}
