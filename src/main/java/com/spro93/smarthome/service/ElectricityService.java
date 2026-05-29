package com.spro93.smarthome.service;

import com.spro93.smarthome.model.ElectricityPrice;
import com.spro93.smarthome.model.PriceData;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Clock;
import java.time.Duration;
import java.time.ZonedDateTime;
import java.util.Comparator;

@Service
@Slf4j
public class ElectricityService {

    private static final String API_URL = "https://apis.smartenergy.at/market/v1/price";
    private static final Duration MAX_CACHE_AGE = Duration.ofHours(24);
    private static final int DEFAULT_INTERVAL_MINUTES = 15;

    private final RestClient restClient;
    private final Clock clock;

    /**
     * Immutable view of the cached prices together with the instant they stop being usable.
     * Holding both values in a single object lets us publish them atomically through one
     * {@code volatile} reference instead of juggling two independently-written fields.
     */
    private record CachedPrices(ElectricityPrice price, ZonedDateTime validUntil) {
        boolean isUsableAt(final ZonedDateTime now) {
            return now.isBefore(validUntil);
        }
    }

    private volatile CachedPrices cache;

    public ElectricityService(final RestClient.Builder restClientBuilder, final Clock clock) {
        this.restClient = restClientBuilder.build();
        this.clock = clock;
    }

    public String isPriceNegative(final Double additionalAmount) {
        var now = ZonedDateTime.now(clock);
        var prices = getElectricityPrices(now);
        var windowMillis = intervalMinutes(prices) * 60_000L;

        return prices.data().stream()
                .filter(element -> {
                    var diff = Duration.between(element.date(), now).toMillis();
                    return diff > 0 && diff <= windowMillis;
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

    private ElectricityPrice getElectricityPrices(final ZonedDateTime now) {
        var current = cache;
        if (current != null && current.isUsableAt(now)) {
            log.info("Returning from cache");
            return current.price();
        }

        synchronized (this) {
            // Re-check inside the lock: another thread may have refreshed while we waited.
            current = cache;
            if (current != null && current.isUsableAt(now)) {
                log.info("Returning from cache");
                return current.price();
            }
            try {
                var refreshed = fetchElectricityPrices(now);
                cache = refreshed;
                return refreshed.price();
            } catch (RuntimeException ex) {
                if (current != null) {
                    log.warn("Failed to refresh electricity prices, serving last known data. Cause: {}", ex.getMessage());
                    return current.price();
                }
                throw ex;
            }
        }
    }

    private CachedPrices fetchElectricityPrices(final ZonedDateTime now) {
        log.info("Fetching new prices");
        var price = restClient.get()
                .uri(API_URL)
                .retrieve()
                .body(ElectricityPrice.class);

        if (price == null || price.data().isEmpty()) {
            throw new IllegalStateException("Electricity price API returned no usable data");
        }

        // The cache stays valid until the data no longer covers the current time (independent of
        // the order in which the API returns the slots), bounded by a hard maximum age.
        var coverageEnd = price.data().stream()
                .map(PriceData::date)
                .max(Comparator.naturalOrder())
                .orElse(now)
                .plusMinutes(intervalMinutes(price));
        var ttlCap = now.plus(MAX_CACHE_AGE);
        var validUntil = coverageEnd.isBefore(ttlCap) ? coverageEnd : ttlCap;

        return new CachedPrices(price, validUntil);
    }

    private int intervalMinutes(final ElectricityPrice price) {
        return price.interval() > 0 ? price.interval() : DEFAULT_INTERVAL_MINUTES;
    }

    private String formatResponse(final boolean resp) {
        return resp ? "1" : "0";
    }
}
