export function checkSunPositionParameters(query: any): string {
    const minAzimuth = query.minAzimuth;
    const maxAzimuth = query.maxAzimuth;
    const minAltitude = query.minAltitude;
    const maxAltitude = query.maxAltitude;

    let errorMessage = "";

    errorMessage += checkNumericParameter(minAzimuth, "minAzimuth");
    errorMessage += checkNumericParameter(maxAzimuth, "maxAzimuth");
    errorMessage += checkNumericParameter(minAltitude, "minAltitude");
    errorMessage += checkNumericParameter(maxAltitude, "maxAltitude");

    return errorMessage.trim();
}

function checkNumericParameter(param: any, paramName: string): string {
    if (!param) {
        return `${paramName} missing `;
    } else if (!isNumeric(param)) {
        return `${paramName} is not a number `;
    }
    return "";
}

export function isNumeric(param: any): boolean {
    return !isNaN(Number(param));
}

export function formatResponse(resp: boolean): string {
    return resp ? "1": "0";
}

export function isInRange(value: number, min: number, max: number): boolean{
    return value >= min && value <= max;
}