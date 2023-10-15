import { formatResponse, isInRange, isNumeric } from "../utils";
import GeoMag from 'geomag';

const SunCalc = require('suncalc3');

const latitudeFallback = 48.6586252;
const longitudeFallback = 16.2444244;
const minAltitudeDefault = 0;
const maxAltitudeDefault = 90;

export function isExposed(query: any) {
    console.log(`Sun exposure requested for query ${JSON.stringify(query)}`);

    const latitude = getValidValue(query.latitude, -90, 90, latitudeFallback);
    const longitude = getValidValue(query.longitude, -180, 180, longitudeFallback);
    const minAltitude = query.minAltitude ? query.minAltitude : minAltitudeDefault;
    const maxAltitude = query.maxAltitude ? query.maxAltitude : maxAltitudeDefault;
    const correctDeclination = query.correctDeclination ? query.correctDeclination.toLowerCase() === 'true' : true;
    const minAzimuth = correctDeclination ? correctAngle(query.minAzimuth, latitude, longitude) : query.minAzimuth;
    const maxAzimuth = correctDeclination ? correctAngle(query.maxAzimuth, latitude, longitude) : query.maxAzimuth;

    const params = JSON.stringify({
        latitude: latitude,
        longitude: longitude,
        minAzimuth: minAzimuth,
        maxAzimuth: maxAzimuth,
        minAltitude: minAltitude,
        maxAltitude: maxAltitude,
    });
    console.log(`Using ${params} for calculation`);

    const currentSunPosition = SunCalc.getPosition(new Date(), latitude, longitude);

    const azimuthExposed = isAngleInRange(currentSunPosition.azimuthDegrees, minAzimuth, maxAzimuth);
    const altitudeExposed = isAngleInRange(currentSunPosition.altitudeDegrees, minAltitude, maxAltitude);

    const response = formatResponse(azimuthExposed && altitudeExposed);

    console.log(`Responding with ${response} (Azimuth exposed: ${azimuthExposed}, Altitude exposed: ${altitudeExposed})`);

    return response;
}

function getValidValue(value: any, min: number, max: number, fallback: number): number {
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

function correctAngle(value: number, latitude: number, longitude: number): number {
    const currentDeclination: number = GeoMag.field(latitude, longitude).declination;
    console.log(`Calculated declination: ${currentDeclination}`);
    return +value + +currentDeclination;
}