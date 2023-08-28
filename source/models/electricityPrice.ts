interface ElectricityPrice {
    tariff: string
    unit: string
    interval: number
    data: [{
        date: Date
        value: number
    }]
}