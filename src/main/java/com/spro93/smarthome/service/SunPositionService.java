package com.spro93.smarthome.service;

import com.spro93.smarthome.util.GeoMag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.shredzone.commons.suncalc.SunPosition;
import org.springframework.stereotype.Service;

import java.time.Clock;
import java.time.ZonedDateTime;
import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class SunPositionService {

    private static final double LATITUDE_FALLBACK = 0;
    private static final double LONGITUDE_FALLBACK = 0;
    private static final double MIN_ALTITUDE_DEFAULT = 0;
    private static final double MAX_ALTITUDE_DEFAULT = 90;

    private final Clock clock;

    public String isExposed(final Map<String, String> query) {
        var latitude = getValidValue(query.get("latitude"), -90, 90, LATITUDE_FALLBACK);
        var longitude = getValidValue(query.get("longitude"), -180, 180, LONGITUDE_FALLBACK);

        var minAltitude = parseOrDefault(query.get("minAltitude"), MIN_ALTITUDE_DEFAULT);
        var maxAltitude = parseOrDefault(query.get("maxAltitude"), MAX_ALTITUDE_DEFAULT);

        var correctDeclination = !query.containsKey("correctDeclination") || Boolean.parseBoolean(query.get("correctDeclination"));

        var minAzimuthRaw = Double.parseDouble(query.get("minAzimuth"));
        var maxAzimuthRaw = Double.parseDouble(query.get("maxAzimuth"));

        var declination = 0.0;
        if (correctDeclination) {
            declination = GeoMag.getDeclination(latitude, longitude);
            log.info("Calculated declination: {}", declination);
        }
        var correctedMin = minAzimuthRaw + declination;
        var correctedMax = maxAzimuthRaw + declination;

        // A request spanning the full circle must stay a full circle: normalizing both shifted
        // bounds independently would otherwise collapse e.g. [0+d, 360+d] to the single point [d, d].
        var fullCircle = correctedMax - correctedMin >= 360;
        var minAzimuth = fullCircle ? 0 : normalizeAzimuth(correctedMin);
        var maxAzimuth = fullCircle ? 360 : normalizeAzimuth(correctedMax);

        // Only the parsed numeric values are logged; the raw query strings are user-controlled
        // and must not reach the log unsanitized (log injection).
        log.info("Using {\"latitude\":{},\"longitude\":{},\"minAzimuth\":{},\"maxAzimuth\":{},\"minAltitude\":{},\"maxAltitude\":{},\"correctDeclination\":{}} for calculation",
                latitude, longitude, minAzimuth, maxAzimuth, minAltitude, maxAltitude, correctDeclination);

        var currentSunPosition = SunPosition.compute()
                .on(ZonedDateTime.now(clock))
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

    private String formatResponse(final boolean resp) {
        return resp ? "1" : "0";
    }
}
