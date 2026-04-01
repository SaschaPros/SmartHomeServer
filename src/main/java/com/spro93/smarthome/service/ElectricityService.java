package com.spro93.smarthome.service;

import com.spro93.smarthome.model.ElectricityPrice;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;
import java.time.ZonedDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class ElectricityService {

    private static final String API_URL = "https://apis.smartenergy.at/market/v1/price";
    private ElectricityPrice cachedPrice;
    private ZonedDateTime cacheDate;

    private final RestTemplate restTemplate;

    public String isPriceNegative(final Double additionalAmount) {
        var now = ZonedDateTime.now();
        var prices = getElectricityPrices();

        return prices.data().stream()
                .filter(element -> {
                    var diff = Duration.between(element.date(), now).toMillis();
                    return diff > 0 && diff <= 15 * 60 * 1000;
                })
                .findFirst()
                .map(priceData -> {
                    if (additionalAmount != null) {
                        log.info("Additional amount of {} included.", additionalAmount);
                    }
                    var effectivePrice = additionalAmount != null ? priceData.value() + additionalAmount : priceData.value();
                    var status = formatResponse(effectivePrice < 0);
                    log.info("Electricity price requested. Price is at {} at {} ct/kWh. Responding with {}",
                            priceData.date(), priceData.value(), status);
                    return status;
                })
                .orElseGet(() -> {
                    var status = formatResponse(false);
                    log.info("Electricity price requested. No actual price found. Responding with {}", status);
                    return status;
                });
    }

    private ElectricityPrice getElectricityPrices() {
        if (isPricesValid()) {
            log.info("Returning from cache");
            return cachedPrice;
        }
        return fetchElectricityPrices();
    }

    private boolean isPricesValid() {
        if (cachedPrice == null || cachedPrice.data().isEmpty()) {
            log.info("Cache empty");
            return false;
        }

        var now = ZonedDateTime.now();
        var diff = Duration.between(cacheDate, now);

        if (diff.toMillis() >= 24 * 60 * 60 * 1000) {
            log.info("Cache is outdated");
            return false;
        } else if (cachedPrice.data().getFirst().date().isBefore(now)) {
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

