import express from "express";
import { isPriceNegative } from "./source/controllers/electricity";

const app = express();

app.get('/api/electricitySwitchStatus', async (req, res) => {
    const negative = await isPriceNegative();
    console.log(`Electricity price requested. Responding with ${negative}`);
    res.send(negative);
});

const port = process.env.PORT || 3000;

app.listen(port, () => console.log(`App listening on PORT ${port}`));