import { formatResponse, isInRange, isNumeric } from "../utils";

const SunCalc = require('suncalc');
const latitudeFallback = 48.6586252;
const longitudeFallback = 16.2444244;

export function isExposed(query: any) {
    const latitude = getValidValue(query.latitude, -90, 90, latitudeFallback, "Latitude");
    const longitude = getValidValue(query.longitude, -180, 180, longitudeFallback, "Longitude");

    const minAzimuthRad = convertDegreeToRadian(query.minAzimuth);
    const maxAzimuthRad = convertDegreeToRadian(query.maxAzimuth);
    const minAltitudeRad = convertDegreeToRadian(query.minAltitude);
    const maxAltitudeRad = convertDegreeToRadian(query.maxAltitude);

    const currentSunPosition = SunCalc.getPosition(new Date(), latitude, longitude);
    const azimuthExposed = isInRange(currentSunPosition.azimuth, minAzimuthRad, maxAzimuthRad);
    const altitudeExposed = isInRange(currentSunPosition.altitude, minAltitudeRad, maxAltitudeRad);
    const response = formatResponse(azimuthExposed && altitudeExposed);

    console.log(JSON.stringify({
        minAzimuthRad: minAzimuthRad,
        maxAzimuthRad: maxAzimuthRad,
        minAltitudeRad: minAltitudeRad,
        maxAltitudeRad: maxAltitudeRad
    }));
    console.log(`Sun exposure requested for parameters ${JSON.stringify(query)}. Responding with ${response}`)

    return response;
}

function getValidValue(value: any, min: number, max: number, fallback: number, name: string): number {
    if (isNumeric(value) && isInRange(value, min, max)) {
        return value;
    } else {
        console.log(`${name} "${value}" is not valid. Using fallback data`);
        return fallback;
    }
}

function convertDegreeToRadian(degrees: number) {
    return degrees % 360 * Math.PI / 180;
}