import express from "express";
import { isPriceNegative } from "./source/controllers/electricity";
import { isExposed } from "./source/controllers/sunPosition";
import { checkNumericParameter, checkSunPositionParameters } from "./source/utils";

const app = express();

app.get('/api/electricityPrice', async (req, res) => {
    const errorMessage = checkNumericParameter(req.query.additionalAmount, "additionalAmount")
    if (errorMessage) {
        console.log(`Parameter invalid, responding with HTTP 500. Error: ${errorMessage}`);
        res.status(500).send(errorMessage);
    } else {
        res.send(await isPriceNegative(req.query.additionalAmount));
    }
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

const port = process.env.PORT || 3000;

app.listen(port, () => console.log(`App listening on PORT ${port}`));