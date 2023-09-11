import { formatResponse, isInRange, isNumeric } from "../utils";

const SunCalc = require('suncalc');
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

    const minAzimuthRad = convertDegreeToRadian(query.minAzimuth);
    const maxAzimuthRad = convertDegreeToRadian(query.maxAzimuth);
    const minAltitudeRad = convertDegreeToRadian(minAltitude);
    const maxAltitudeRad = convertDegreeToRadian(maxAltitude);

    const params = JSON.stringify({
        latitude: latitude,
        longitude: longitude,
        minAzimuth: query.minAzimuth,
        minAzimuthRad: minAzimuthRad,
        maxAzimuth: query.maxAzimuth,
        maxAzimuthRad: maxAzimuthRad,
        minAltitude: minAltitude,
        minAltitudeRad: minAltitudeRad,
        maxAltitude: maxAltitude,
        maxAltitudeRad: maxAltitudeRad
    });
    console.log(`Using ${params} for calculation`);

    const currentSunPosition = SunCalc.getPosition(new Date(), latitude, longitude);
    const azimuthExposed = isInRange(currentSunPosition.azimuth, minAzimuthRad, maxAzimuthRad);
    const altitudeExposed = isInRange(currentSunPosition.altitude, minAltitudeRad, maxAltitudeRad);
    const response = formatResponse(azimuthExposed && altitudeExposed);

    console.log(`Responding with ${response}`)

    return response;
}

function getValidValue(value: any, min: number, max: number, fallback: number, name: string): number {
    if (isNumeric(value) && isInRange(value, min, max)) {
        return value;
    } else {
        return fallback;
    }
}

function convertDegreeToRadian(degrees: number) {
    return degrees % 360 * Math.PI / 180;
}