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

    public String isExposed(Map<String, String> query) {
        log.info("Sun exposure requested for query {}", query);

        double latitude = getValidValue(query.get("latitude"), -90, 90, LATITUDE_FALLBACK);
        double longitude = getValidValue(query.get("longitude"), -180, 180, LONGITUDE_FALLBACK);

        double minAltitude = query.containsKey("minAltitude") ? Double.parseDouble(query.get("minAltitude")) : MIN_ALTITUDE_DEFAULT;
        double maxAltitude = query.containsKey("maxAltitude") ? Double.parseDouble(query.get("maxAltitude")) : MAX_ALTITUDE_DEFAULT;

        boolean correctDeclination = !query.containsKey("correctDeclination") || Boolean.parseBoolean(query.get("correctDeclination"));

        double minAzimuthRaw = Double.parseDouble(query.get("minAzimuth"));
        double maxAzimuthRaw = Double.parseDouble(query.get("maxAzimuth"));

        double minAzimuth = correctDeclination ? correctAngle(minAzimuthRaw, latitude, longitude) : minAzimuthRaw;
        double maxAzimuth = correctDeclination ? correctAngle(maxAzimuthRaw, latitude, longitude) : maxAzimuthRaw;

        log.info("Using {\"latitude\":{},\"longitude\":{},\"minAzimuth\":{},\"maxAzimuth\":{},\"minAltitude\":{},\"maxAltitude\":{}} for calculation",
                latitude, longitude, minAzimuth, maxAzimuth, minAltitude, maxAltitude);

        SunPosition currentSunPosition = SunPosition.compute()
                .at(latitude, longitude)
                .execute();

        boolean azimuthExposed = isAngleInRange(currentSunPosition.getAzimuth(), minAzimuth, maxAzimuth);
        boolean altitudeExposed = isAngleInRange(currentSunPosition.getAltitude(), minAltitude, maxAltitude);

        String response = formatResponse(azimuthExposed && altitudeExposed);

        log.info("Responding with {} (Azimuth exposed: {}, Altitude exposed: {})", response, azimuthExposed, altitudeExposed);

        return response;
    }

    private double getValidValue(String value, double min, double max, double fallback) {
        try {
            if (value != null) {
                double val = Double.parseDouble(value);
                if (val >= min && val <= max) {
                    return val;
                }
            }
        } catch (NumberFormatException ignored) {}
        return fallback;
    }

    private boolean isAngleInRange(double value, double min, double max) {
        if (min <= max) {
            return value >= min && value <= max;
        } else {
            return value >= min || value <= max;
        }
    }

    private double correctAngle(double value, double latitude, double longitude) {
        double currentDeclination = GeoMag.getDeclination(latitude, longitude);
        log.info("Calculated declination: {}", currentDeclination);
        return value + currentDeclination;
    }

    private String formatResponse(boolean resp) {
        return resp ? "1" : "0";
    }
}
