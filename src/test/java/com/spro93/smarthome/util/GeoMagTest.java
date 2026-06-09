package com.spro93.smarthome.util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeoMagTest {

    @Test
    void declinationVienna_isAFewDegreesEast() {
        // Known declination for Vienna, Austria (48.2, 16.3) in the mid-2020s is roughly 5-6 degrees East.
        var declination = GeoMag.getDeclination(48.2, 16.3);

        assertTrue(declination > 4.0 && declination < 7.0,
                "Vienna declination should be between 4 and 7 degrees East but was " + declination);
    }

    @Test
    void declinationAtEquatorPrimeMeridian_isWest() {
        // At (0, 0) the declination is a few degrees West (negative) in the mid-2020s.
        var declination = GeoMag.getDeclination(0, 0);

        assertTrue(declination < 0 && declination > -10,
                "Equator/prime meridian declination should be slightly West but was " + declination);
    }

    @Test
    void declinationAtModerateAltitude_staysCloseToSeaLevelValue() {
        var seaLevel = GeoMag.getDeclination(48.2, 16.3);
        var elevated = GeoMag.getDeclination(48.2, 16.3, 1.0); // 1 km above sea level

        assertEquals(seaLevel, elevated, 0.5,
                "Declination 1 km up should differ only marginally from the sea-level value");
    }
}
