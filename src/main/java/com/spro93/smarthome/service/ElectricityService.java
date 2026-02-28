package com.spro93.smarthome.service;

import com.spro93.smarthome.model.ElectricityPrice;
import com.spro93.smarthome.model.PriceData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.ZonedDateTime;

@Service
@Slf4j
public class ElectricityService {

    private static final String API_URL = "https://apis.smartenergy.at/market/v1/price";
    private ElectricityPrice cachedPrice;
    private ZonedDateTime cacheDate;

    private final RestTemplate restTemplate;

    public ElectricityService(final RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String isPriceNegative(final Double additionalAmount) {
        var now = ZonedDateTime.now();
        var prices = getElectricityPrices();

        var actualPrice = prices.data().stream()
                .filter(element -> {
                    var duration = Duration.between(element.date(), now);
                    var diff = duration.toMillis();
                    return diff > 0 && diff <= 15 * 60 * 1000;
                })
                .findFirst();

        boolean isNegative;
        if (actualPrice.isPresent()) {
            var priceValue = actualPrice.get().value();
            if (additionalAmount != null) {
                log.info("Additional amount of {} included.", additionalAmount);
                isNegative = priceValue + additionalAmount < 0;
            } else {
                isNegative = priceValue < 0;
            }
            var status = formatResponse(isNegative);
            log.info("Electricity price requested. Price is at {} at {} ct/kWh. Responding with {}",
                    actualPrice.get().date(), priceValue, status);
            return status;
        } else {
            isNegative = false;
            var status = formatResponse(isNegative);
            log.info("Electricity price requested. No actual price found. Responding with {}", status);
            return status;
        }
    }

    private ElectricityPrice getElectricityPrices() {
        if (isPricesValid()) {
            log.info("Returning from cache");
            return cachedPrice;
        }
        return fetchElectricityPrices();
    }

    private boolean isPricesValid() {
        if (cachedPrice == null || cachedPrice.data() == null || cachedPrice.data().isEmpty()) {
            log.info("Cache empty");
            return false;
        }

        var now = ZonedDateTime.now();
        var diff = Duration.between(cacheDate, now);

        if (diff.toMillis() >= 24 * 60 * 60 * 1000) {
            log.info("Cache is outdated");
            return false;
        } else if (cachedPrice.data().get(0).date().isBefore(now)) {
            log.info("Cache's values are invalid");
            return false;
        } else {
            return true;
        }
    }

    private ElectricityPrice fetchElectricityPrices() {
        log.info("Fetching new prices");
        var price = restTemplate.getForObject(API_URL, ElectricityPrice.class);
        this.cachedPrice = price;
        this.cacheDate = ZonedDateTime.now();
        return price;
    }

    private String formatResponse(final boolean resp) {
        return resp ? "1" : "0";
    }
}
