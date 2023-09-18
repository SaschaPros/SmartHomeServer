import { formatResponse } from "../utils";

const oneDay = 60 * 60 * 24 * 1000;
const fifteenMinutes = 15 * 60 * 1000;

var price: ElectricityPrice;
var cacheDate: Date;

export async function isPriceNegative(additionalAmount: any): Promise<string> {
    let now = +new Date();
    const prices = await getElectricityPrices();
    let actualPrice = prices.data.find(element => {
        let diff = +now - +new Date(element.date);
        return diff > 0 && diff <= fifteenMinutes;
    });

    let isNegative: boolean;
    if (actualPrice) {
        if (additionalAmount) {
            console.log(`Additional amount of ${additionalAmount} included.`);
            isNegative = +actualPrice.value + +additionalAmount < 0;
        } else {
            isNegative = actualPrice.value < 0;
        }
    } else {
        isNegative = false;
    }

    const status = formatResponse(isNegative);
    console.log(`Electricity price requested. Price is at ${actualPrice?.date} at ${actualPrice?.value} ct/kWh. Responding with ${status}`);
    return status;
}

function isPricesValid(): boolean {
    if (!price) {
        console.log("Cache empty");
        return false;
    } else {    
        let now = +new Date();
        let diff = now - +cacheDate;
        if (diff >= oneDay) {
            console.log("Cache is outdated");
            return false;
        } else if (+price.data[0].date < now) {
            console.log("Cache's values are invalid");
            return false;
        } else {
            return true;
        }
    }
}

async function getElectricityPrices(): Promise<ElectricityPrice> {
    if (isPricesValid()) {
        console.log("Returning from cache");
        return price;
    }
    return await fetchElectricityPrices();
}

async function fetchElectricityPrices(): Promise<ElectricityPrice> {
    console.log("Fetching new prices");

    const headers: Headers = new Headers();
    headers.set('Content-Type', 'application/json');
    headers.set('Accept', 'application/json');
    
    const request: RequestInfo = new Request('https://apis.smartenergy.at/market/v1/price', {
        method: 'GET',
        headers: headers
    });

    const response = await fetch(request);
    price = await response.json() as ElectricityPrice[][0];
    return price;
}

