import express from "express";
import { isPriceNegative } from "./source/controllers/electricity";
import { isExposed } from "./source/controllers/sunPosition";
import { checkSunPositionParameters } from "./source/utils";

const app = express();

app.get('/api/electricityPrice', async (_req, res) => {
    res.send(await isPriceNegative());
});

app.get('/api/isExposedToSun', async (req, res) => {
    const errorMessage = checkSunPositionParameters(req.query);
    if (errorMessage) {
        console.log(`Parameters invalid, responding with HTTP 500. Errors: ${errorMessage}`);
        res.status(500).send(errorMessage);
    } else {
        res.send(isExposed(req.query));
    }
})

app.get('/api/overview', async (_req, res) => {
    res.status(200).send({
        electricityPriceNegative: await isPriceNegative()
    })
})

const port = process.env.PORT || 3000;

app.listen(port, () => console.log(`App listening on PORT ${port}`));