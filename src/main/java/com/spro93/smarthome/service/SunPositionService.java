package com.spro93.smarthome.service;

import com.spro93.smarthome.util.GeoMag;
import lombok.extern.slf4j.Slf4j;
import org.shredzone.commons.suncalc.SunPosition;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
public class SunPositionService {

    private static final double LATITUDE_FALLBACK = 0;
    private static final double LONGITUDE_FALLBACK = 0;
    private static final double MIN_ALTITUDE_DEFAULT = 0;
    private static final double MAX_ALTITUDE_DEFAULT = 90;

    public String isExposed(final Map<String, String> query) {
        log.info("Sun exposure requested for query {}", query);

        var latitude = getValidValue(query.get("latitude"), -90, 90, LATITUDE_FALLBACK);
        var longitude = getValidValue(query.get("longitude"), -180, 180, LONGITUDE_FALLBACK);

        var minAltitude = parseOrDefault(query.get("minAltitude"), MIN_ALTITUDE_DEFAULT);
        var maxAltitude = parseOrDefault(query.get("maxAltitude"), MAX_ALTITUDE_DEFAULT);

        var correctDeclination = !query.containsKey("correctDeclination") || Boolean.parseBoolean(query.get("correctDeclination"));

        var minAzimuthRaw = Double.parseDouble(query.get("minAzimuth"));
        var maxAzimuthRaw = Double.parseDouble(query.get("maxAzimuth"));

        var minAzimuth = normalizeAzimuth(correctDeclination ? correctAngle(minAzimuthRaw, latitude, longitude) : minAzimuthRaw);
        var maxAzimuth = normalizeAzimuth(correctDeclination ? correctAngle(maxAzimuthRaw, latitude, longitude) : maxAzimuthRaw);

        log.info("Using {\"latitude\":{},\"longitude\":{},\"minAzimuth\":{},\"maxAzimuth\":{},\"minAltitude\":{},\"maxAltitude\":{}} for calculation",
                latitude, longitude, minAzimuth, maxAzimuth, minAltitude, maxAltitude);

        var currentSunPosition = SunPosition.compute()
                .at(latitude, longitude)
                .execute();

        var azimuthExposed = isAngleInRange(currentSunPosition.getAzimuth(), minAzimuth, maxAzimuth);
        var altitudeExposed = isAngleInRange(currentSunPosition.getAltitude(), minAltitude, maxAltitude);

        var response = formatResponse(azimuthExposed && altitudeExposed);

        log.info("Responding with {} (Azimuth exposed: {}, Altitude exposed: {})", response, azimuthExposed, altitudeExposed);

        return response;
    }

    private double getValidValue(final String value, final double min, final double max, final double fallback) {
        try {
            if (value != null) {
                var val = Double.parseDouble(value);
                if (val >= min && val <= max) {
                    return val;
                }
            }
        } catch (NumberFormatException _) {}
        return fallback;
    }

    /**
     * Parses an optional numeric parameter, falling back to {@code fallback} when it is absent,
     * blank or not a number. This keeps a present-but-empty query parameter (e.g. {@code ?minAltitude=})
     * from blowing up with a {@link NumberFormatException}.
     */
    private double parseOrDefault(final String value, final double fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return Double.parseDouble(value);
        } catch (NumberFormatException _) {
            return fallback;
        }
    }

    /**
     * Checks whether {@code value} (an angle in {@code [0, 360)}) falls inside the inclusive range.
     * When {@code min > max} the range is treated as wrapping around the 0/360 boundary.
     */
    static boolean isAngleInRange(final double value, final double min, final double max) {
        if (min <= max) {
            return value >= min && value <= max;
        } else {
            return value >= min || value <= max;
        }
    }

    /**
     * Maps an arbitrary azimuth onto {@code [0, 360]} so that the declination correction cannot push
     * a bound outside the range produced by {@link SunPosition#getAzimuth()}. Values that are already
     * canonical (including an explicit {@code 360}) are left untouched.
     */
    static double normalizeAzimuth(final double azimuth) {
        if (azimuth >= 0 && azimuth <= 360) {
            return azimuth;
        }
        var wrapped = azimuth % 360;
        return wrapped < 0 ? wrapped + 360 : wrapped;
    }

    private double correctAngle(final double value, final double latitude, final double longitude) {
        var currentDeclination = GeoMag.getDeclination(latitude, longitude);
        log.info("Calculated declination: {}", currentDeclination);
        return value + currentDeclination;
    }

    private String formatResponse(final boolean resp) {
        return resp ? "1" : "0";
    }
}
