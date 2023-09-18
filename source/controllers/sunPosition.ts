import { formatResponse, isInRange, isNumeric } from "../utils";

const SunCalc = require('suncalc3');
const latitudeFallback = 48.6586252;
const longitudeFallback = 16.2444244;
const minAltitudeDefault = 0;
const maxAltitudeDefault = 90;

export function isExposed(query: any) {
    console.log(`Sun exposure requested for query ${JSON.stringify(query)}`);

    const latitude = getValidValue(query.latitude, -90, 90, latitudeFallback, "Latitude");
    const longitude = getValidValue(query.longitude, -180, 180, longitudeFallback, "Longitude");
    const minAltitude = query.minAltitude ? query.minAltitude : minAltitudeDefault;
    const maxAltitude = query.maxAltitude ? query.maxAltitude : maxAltitudeDefault;

    const params = JSON.stringify({
        latitude: latitude,
        longitude: longitude,
        minAzimuth: query.minAzimuth,
        maxAzimuth: query.maxAzimuth,
        minAltitude: minAltitude,
        maxAltitude: maxAltitude,
    });
    console.log(`Using ${params} for calculation`);

    const currentSunPosition = SunCalc.getPosition(new Date(), latitude, longitude);
    const azimuthExposed = isAngleInRange(currentSunPosition.azimuthDegrees, query.minAzimuth, query.maxAzimuth);
    const altitudeExposed = isAngleInRange(currentSunPosition.altitudeDegrees, minAltitude, maxAltitude);

    const response = formatResponse(azimuthExposed && altitudeExposed);

    console.log(`Responding with ${response} (Azimuth exposed: ${azimuthExposed}, Altitude exposed: ${altitudeExposed})`);

    return response;
}

function getValidValue(value: any, min: number, max: number, fallback: number, name: string): number {
    if (isNumeric(value) && isInRange(value, min, max)) {
        return value;
    } else {
        return fallback;
    }
}

function isAngleInRange(value: number, min: number, max: number): boolean {
    if (min <= max) {
        return value >= min && value <= max;
    } else {
        return value >= min || value <= max;
    }
}