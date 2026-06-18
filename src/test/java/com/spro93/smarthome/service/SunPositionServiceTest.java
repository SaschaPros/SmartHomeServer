package com.spro93.smarthome.service;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SunPositionServiceTest {

    /**
     * Around the March 2026 equinox the sun's declination is ~0, which makes positions at the
     * equator/prime meridian easy to reason about: at 12:00 UTC the sun is almost at the zenith,
     * at 09:00 UTC it stands almost due east, and at midnight it is far below the horizon.
     */
    private static final String EQUINOX_NOON_UTC = "2026-03-20T12:00:00Z";
    private static final String EQUINOX_MORNING_UTC = "2026-03-20T09:00:00Z";
    private static final String EQUINOX_MIDNIGHT_UTC = "2026-03-20T00:00:00Z";

    private static SunPositionService serviceAt(final String instant) {
        return new SunPositionService(Clock.fixed(Instant.parse(instant), ZoneOffset.UTC));
    }

    @Test
    void isExposed_sunNearZenithAtEquatorialNoon_returnsExposed() {
        var query = Map.of(
                "minAzimuth", "0",
                "maxAzimuth", "360",
                "minAltitude", "60",
                "maxAltitude", "90",
                "correctDeclination", "false"
        );

        assertEquals("1", serviceAt(EQUINOX_NOON_UTC).isExposed(query));
    }

    @Test
    void isExposed_nightAtEquator_returnsNotExposed() {
        // Default altitude range is [0, 90]; at midnight the sun is far below the horizon.
        var query = Map.of(
                "minAzimuth", "0",
                "maxAzimuth", "360",
                "correctDeclination", "false"
        );

        assertEquals("0", serviceAt(EQUINOX_MIDNIGHT_UTC).isExposed(query));
    }

    @Test
    void isExposed_morningSunAtEquator_matchesEastFacingWindowOnly() {
        var eastWindow = Map.of(
                "minAzimuth", "45",
                "maxAzimuth", "135",
                "minAltitude", "-90",
                "maxAltitude", "90",
                "correctDeclination", "false"
        );
        var westWindow = Map.of(
                "minAzimuth", "225",
                "maxAzimuth", "315",
                "minAltitude", "-90",
                "maxAltitude", "90",
                "correctDeclination", "false"
        );

        assertEquals("1", serviceAt(EQUINOX_MORNING_UTC).isExposed(eastWindow));
        assertEquals("0", serviceAt(EQUINOX_MORNING_UTC).isExposed(westWindow));
    }

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

        assertEquals("1", serviceAt(EQUINOX_MIDNIGHT_UTC).isExposed(query));
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

        assertEquals("1", serviceAt(EQUINOX_NOON_UTC).isExposed(query));
    }

    @Test
    void isExposed_outOfRangeLatitude_usesFallbackZero() {
        // latitude 999 is out of [-90, 90], so fallback 0 is used — same result as the equator case
        var query = Map.of(
                "minAzimuth", "0",
                "maxAzimuth", "360",
                "minAltitude", "60",
                "maxAltitude", "90",
                "latitude", "999",
                "correctDeclination", "false"
        );

        assertEquals("1", serviceAt(EQUINOX_NOON_UTC).isExposed(query));
    }

    @Test
    void isExposed_outOfRangeLongitude_usesFallbackZero() {
        var query = Map.of(
                "minAzimuth", "0",
                "maxAzimuth", "360",
                "minAltitude", "60",
                "maxAltitude", "90",
                "longitude", "999",
                "correctDeclination", "false"
        );

        assertEquals("1", serviceAt(EQUINOX_NOON_UTC).isExposed(query));
    }

    @Test
    void isExposed_nonNumericLatLong_usesFallbackZero() {
        var query = Map.of(
                "minAzimuth", "0",
                "maxAzimuth", "360",
                "minAltitude", "60",
                "maxAltitude", "90",
                "latitude", "not-a-number",
                "longitude", "not-a-number",
                "correctDeclination", "false"
        );

        assertEquals("1", serviceAt(EQUINOX_NOON_UTC).isExposed(query));
    }

    @Test
    void isExposed_wrapAroundAzimuth_handledCorrectly() {
        // minAzimuth > maxAzimuth wraps across the 0/360 boundary. The morning sun stands at
        // ~90 degrees: inside the wrapping window [315, 135], outside the wrapping window [135, 45].
        var northEastWindow = new HashMap<String, String>();
        northEastWindow.put("minAzimuth", "315");
        northEastWindow.put("maxAzimuth", "135");
        northEastWindow.put("minAltitude", "-90");
        northEastWindow.put("maxAltitude", "90");
        northEastWindow.put("correctDeclination", "false");

        var southWestWindow = new HashMap<String, String>();
        southWestWindow.put("minAzimuth", "135");
        southWestWindow.put("maxAzimuth", "45");
        southWestWindow.put("minAltitude", "-90");
        southWestWindow.put("maxAltitude", "90");
        southWestWindow.put("correctDeclination", "false");

        assertEquals("1", serviceAt(EQUINOX_MORNING_UTC).isExposed(northEastWindow));
        assertEquals("0", serviceAt(EQUINOX_MORNING_UTC).isExposed(southWestWindow));
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

        assertEquals("1", serviceAt(EQUINOX_NOON_UTC).isExposed(query));
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
