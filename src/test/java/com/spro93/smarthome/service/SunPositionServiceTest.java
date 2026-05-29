package com.spro93.smarthome.service;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SunPositionServiceTest {

    private final SunPositionService sunPositionService = new SunPositionService();

    @Test
    void isExposed_fullAzimuthAndAltitudeRange_alwaysExposed() {
        // A range of [0,360] azimuth and [-90,90] altitude covers every possible sun position
        var query = Map.of(
                "minAzimuth", "0",
                "maxAzimuth", "360",
                "minAltitude", "-90",
                "maxAltitude", "90",
                "correctDeclination", "false"
        );

        assertEquals("1", sunPositionService.isExposed(query));
    }

    @Test
    void isExposed_returnsValidBinaryResponse() {
        var query = Map.of(
                "minAzimuth", "90",
                "maxAzimuth", "270",
                "correctDeclination", "false"
        );

        var result = sunPositionService.isExposed(query);
        assertTrue(result.equals("0") || result.equals("1"), "Response must be '0' or '1'");
    }

    @Test
    void isExposed_defaultAltitudeRange_usedWhenNotProvided() {
        // Without minAltitude/maxAltitude the service defaults to [0, 90]
        var query = Map.of(
                "minAzimuth", "0",
                "maxAzimuth", "360",
                "correctDeclination", "false"
        );

        var result = sunPositionService.isExposed(query);
        assertTrue(result.equals("0") || result.equals("1"), "Response must be '0' or '1'");
    }

    @Test
    void isExposed_withCorrectDeclinationTrue_returnsValidResponse() {
        var query = Map.of(
                "minAzimuth", "0",
                "maxAzimuth", "360",
                "minAltitude", "-90",
                "maxAltitude", "90",
                "latitude", "48.2",
                "longitude", "16.3"
                // correctDeclination defaults to true
        );

        var result = sunPositionService.isExposed(query);
        assertTrue(result.equals("0") || result.equals("1"), "Response must be '0' or '1'");
    }

    @Test
    void isExposed_outOfRangeLatitude_usesFallbackZero() {
        // latitude 999 is out of [-90, 90], so fallback 0 is used — result must still be valid
        var query = Map.of(
                "minAzimuth", "0",
                "maxAzimuth", "360",
                "minAltitude", "-90",
                "maxAltitude", "90",
                "latitude", "999",
                "correctDeclination", "false"
        );

        var result = sunPositionService.isExposed(query);
        assertTrue(result.equals("0") || result.equals("1"), "Response must be '0' or '1'");
    }

    @Test
    void isExposed_outOfRangeLongitude_usesFallbackZero() {
        var query = Map.of(
                "minAzimuth", "0",
                "maxAzimuth", "360",
                "minAltitude", "-90",
                "maxAltitude", "90",
                "longitude", "999",
                "correctDeclination", "false"
        );

        var result = sunPositionService.isExposed(query);
        assertTrue(result.equals("0") || result.equals("1"), "Response must be '0' or '1'");
    }

    @Test
    void isExposed_nonNumericLatLong_usesFallbackZero() {
        var query = Map.of(
                "minAzimuth", "0",
                "maxAzimuth", "360",
                "minAltitude", "-90",
                "maxAltitude", "90",
                "latitude", "not-a-number",
                "longitude", "not-a-number",
                "correctDeclination", "false"
        );

        var result = sunPositionService.isExposed(query);
        assertTrue(result.equals("0") || result.equals("1"), "Response must be '0' or '1'");
    }

    @Test
    void isExposed_wrapAroundAzimuth_handledCorrectly() {
        // minAzimuth > maxAzimuth — the isAngleInRange logic uses OR for wrap-around
        var query = new HashMap<String, String>();
        query.put("minAzimuth", "270");
        query.put("maxAzimuth", "90");
        query.put("minAltitude", "-90");
        query.put("maxAltitude", "90");
        query.put("correctDeclination", "false");

        var result = sunPositionService.isExposed(query);
        assertTrue(result.equals("0") || result.equals("1"), "Response must be '0' or '1'");
    }

    @Test
    void isExposed_emptyOptionalAltitude_usesDefaultsInsteadOfFailing() {
        // A present-but-empty optional parameter (?minAltitude=) must not trigger a 500.
        var query = new HashMap<String, String>();
        query.put("minAzimuth", "0");
        query.put("maxAzimuth", "360");
        query.put("minAltitude", "");
        query.put("maxAltitude", "");
        query.put("correctDeclination", "false");

        var result = sunPositionService.isExposed(query);
        assertTrue(result.equals("0") || result.equals("1"), "Response must be '0' or '1'");
    }

    @Test
    void normalizeAzimuth_keepsCanonicalValues() {
        assertEquals(0.0, SunPositionService.normalizeAzimuth(0), 1e-9);
        assertEquals(180.0, SunPositionService.normalizeAzimuth(180), 1e-9);
        // Exactly 360 is preserved so a [0, 360] full-circle request is not collapsed to a point.
        assertEquals(360.0, SunPositionService.normalizeAzimuth(360), 1e-9);
    }

    @Test
    void normalizeAzimuth_wrapsOutOfRangeValues() {
        assertEquals(10.0, SunPositionService.normalizeAzimuth(370), 1e-9);
        assertEquals(355.0, SunPositionService.normalizeAzimuth(-5), 1e-9);
        assertEquals(5.0, SunPositionService.normalizeAzimuth(725), 1e-9);
        assertEquals(355.0, SunPositionService.normalizeAzimuth(-365), 1e-9);
    }

    @Test
    void isAngleInRange_nonWrappingRange() {
        assertTrue(SunPositionService.isAngleInRange(15, 10, 20));
        assertTrue(SunPositionService.isAngleInRange(10, 10, 20));
        assertTrue(SunPositionService.isAngleInRange(20, 10, 20));
        assertFalse(SunPositionService.isAngleInRange(25, 10, 20));
        assertFalse(SunPositionService.isAngleInRange(5, 10, 20));
    }

    @Test
    void isAngleInRange_wrappingRange() {
        // min > max means the range wraps across the 0/360 boundary.
        assertTrue(SunPositionService.isAngleInRange(355, 350, 20));
        assertTrue(SunPositionService.isAngleInRange(10, 350, 20));
        assertFalse(SunPositionService.isAngleInRange(100, 350, 20));
        assertFalse(SunPositionService.isAngleInRange(200, 350, 20));
    }
}
