export function checkSunPositionParameters(query: any): string {
    let errorMessage = "";

    errorMessage += checkNumericParameterAvailable(query.minAzimuth, "minAzimuth");
    errorMessage += checkNumericParameterAvailable(query.maxAzimuth, "maxAzimuth");
    errorMessage += checkNumericParameter(query.minAltitude, "minAltitude");
    errorMessage += checkNumericParameter(query.maxAltitude, "maxAltitude");

    return errorMessage.trim();
}

function checkNumericParameterAvailable(param: any, paramName: string): string {
    if (!param) {
        return `${paramName} missing `;
    } else {
        return checkNumericParameter(param, paramName);
    }
}

export function checkNumericParameter(param: any, paramName: string): string {
    if (param && !isNumeric(param)) {
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