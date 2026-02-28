package com.spro93.smarthome.service;

import com.spro93.smarthome.model.ElectricityPrice;
import com.spro93.smarthome.model.PriceData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Optional;

@Service
@Slf4j
public class ElectricityService {

    private static final String API_URL = "https://apis.smartenergy.at/market/v1/price";
    private ElectricityPrice cachedPrice;
    private ZonedDateTime cacheDate;

    private final RestTemplate restTemplate;

    public ElectricityService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String isPriceNegative(Double additionalAmount) {
        ZonedDateTime now = ZonedDateTime.now();
        ElectricityPrice prices = getElectricityPrices();

        Optional<PriceData> actualPrice = prices.getData().stream()
                .filter(element -> {
                    Duration duration = Duration.between(element.getDate(), now);
                    long diff = duration.toMillis();
                    return diff > 0 && diff <= 15 * 60 * 1000;
                })
                .findFirst();

        boolean isNegative;
        if (actualPrice.isPresent()) {
            double priceValue = actualPrice.get().getValue();
            if (additionalAmount != null) {
                log.info("Additional amount of {} included.", additionalAmount);
                isNegative = priceValue + additionalAmount < 0;
            } else {
                isNegative = priceValue < 0;
            }
            String status = formatResponse(isNegative);
            log.info("Electricity price requested. Price is at {} at {} ct/kWh. Responding with {}",
                    actualPrice.get().getDate(), priceValue, status);
            return status;
        } else {
            isNegative = false;
            String status = formatResponse(isNegative);
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
        if (cachedPrice == null || cachedPrice.getData() == null || cachedPrice.getData().isEmpty()) {
            log.info("Cache empty");
            return false;
        }

        ZonedDateTime now = ZonedDateTime.now();
        Duration diff = Duration.between(cacheDate, now);

        if (diff.toMillis() >= 24 * 60 * 60 * 1000) {
            log.info("Cache is outdated");
            return false;
        } else if (cachedPrice.getData().get(0).getDate().isBefore(now)) {
            log.info("Cache's values are invalid");
            return false;
        } else {
            return true;
        }
    }

    private ElectricityPrice fetchElectricityPrices() {
        log.info("Fetching new prices");
        ElectricityPrice price = restTemplate.getForObject(API_URL, ElectricityPrice.class);
        this.cachedPrice = price;
        this.cacheDate = ZonedDateTime.now();
        return price;
    }

    private String formatResponse(boolean resp) {
        return resp ? "1" : "0";
    }
}
