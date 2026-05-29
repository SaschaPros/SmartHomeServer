package com.spro93.smarthome.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.ExpectedCount.once;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

class ElectricityServiceTest {

    private static final String API_URL = "https://apis.smartenergy.at/market/v1/price";

    private MutableClock clock;
    private MockRestServiceServer server;
    private ElectricityService electricityService;

    @BeforeEach
    void setUp() {
        clock = new MutableClock(Instant.parse("2026-05-29T10:00:00Z"), ZoneOffset.UTC);
        var builder = RestClient.builder();
        server = MockRestServiceServer.bindTo(builder).build();
        electricityService = new ElectricityService(builder, clock);
    }

    @Test
    void isPriceNegative_negativePriceNoAdditional_returnsOne() {
        expectFetch(priceJson(now(), -10.0, false));

        assertEquals("1", electricityService.isPriceNegative(null));
        server.verify();
    }

    @Test
    void isPriceNegative_positivePriceNoAdditional_returnsZero() {
        expectFetch(priceJson(now(), 10.0, false));

        assertEquals("0", electricityService.isPriceNegative(null));
        server.verify();
    }

    @Test
    void isPriceNegative_additionalAmountMakesPriceNegative_returnsOne() {
        expectFetch(priceJson(now(), 5.0, false));

        assertEquals("1", electricityService.isPriceNegative(-10.0));
        server.verify();
    }

    @Test
    void isPriceNegative_additionalAmountKeepsPricePositive_returnsZero() {
        expectFetch(priceJson(now(), -5.0, false));

        assertEquals("0", electricityService.isPriceNegative(10.0));
        server.verify();
    }

    @Test
    void isPriceNegative_noPriceInCurrentWindow_returnsZero() {
        // The only past slot is 20 minutes old (outside the 15-minute window).
        var json = """
                {"tariff":"EPEX","unit":"ct/kWh","interval":15,"data":[
                  {"date":"%s","value":-10.0},
                  {"date":"%s","value":0.0}
                ]}""".formatted(iso(now().minusMinutes(20)), iso(now().plusHours(1)));
        expectFetch(json);

        assertEquals("0", electricityService.isPriceNegative(null));
        server.verify();
    }

    @Test
    void isPriceNegative_cacheValid_doesNotFetchAgain() {
        expectFetch(priceJson(now(), -10.0, false));

        electricityService.isPriceNegative(null);
        electricityService.isPriceNegative(null);

        // A single expectation: a second HTTP call would be reported as unexpected.
        server.verify();
    }

    @Test
    void isPriceNegative_chronologicallyOrderedData_cacheStaysValid() {
        // The real API returns the current slot first. The previous cache logic invalidated the
        // cache immediately for this ordering; a single expectation proves the fix.
        expectFetch(priceJson(now(), -10.0, true));

        electricityService.isPriceNegative(null);
        electricityService.isPriceNegative(null);

        server.verify();
    }

    @Test
    void isPriceNegative_cacheExpired_refetches() {
        var start = now();
        expectFetch(priceJson(start, -10.0, false));
        expectFetch(priceJson(start.plusHours(2), 5.0, false));

        assertEquals("1", electricityService.isPriceNegative(null));
        clock.advance(Duration.ofHours(2)); // past the cached data's coverage
        assertEquals("0", electricityService.isPriceNegative(null));

        server.verify();
    }

    @Test
    void isPriceNegative_upstreamFailsWithStaleCache_servesStaleInsteadOfThrowing() {
        expectFetch(priceJson(now(), -10.0, false));
        server.expect(once(), requestTo(API_URL)).andRespond(withServerError());

        assertEquals("1", electricityService.isPriceNegative(null));
        clock.advance(Duration.ofHours(2)); // expire the cache so the next call refetches

        // The refresh fails, so the last known data is served. It no longer covers "now",
        // hence "0" — but crucially the call does not propagate the upstream error.
        assertEquals("0", electricityService.isPriceNegative(null));
        server.verify();
    }

    @Test
    void isPriceNegative_upstreamFailsWithNoCache_propagates() {
        server.expect(once(), requestTo(API_URL)).andRespond(withServerError());

        assertThrows(RuntimeException.class, () -> electricityService.isPriceNegative(null));
        server.verify();
    }

    @Test
    void isPriceNegative_emptyDataWithNoCache_throws() {
        expectFetch("""
                {"tariff":"EPEX","unit":"ct/kWh","interval":15,"data":[]}""");

        assertThrows(IllegalStateException.class, () -> electricityService.isPriceNegative(null));
        server.verify();
    }

    // --- helpers ---

    private ZonedDateTime now() {
        return ZonedDateTime.now(clock);
    }

    private static String iso(final ZonedDateTime time) {
        return time.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    /**
     * Builds an API response with a "current" slot five minutes before {@code reference} and a
     * future slot one hour later. {@code currentFirst} controls whether the current slot is listed
     * before the future one, so both API orderings can be exercised.
     */
    private static String priceJson(final ZonedDateTime reference, final double currentValue, final boolean currentFirst) {
        var current = "{\"date\":\"%s\",\"value\":%s}".formatted(iso(reference.minusMinutes(5)), currentValue);
        var future = "{\"date\":\"%s\",\"value\":0.0}".formatted(iso(reference.plusHours(1)));
        var data = currentFirst ? current + "," + future : future + "," + current;
        return "{\"tariff\":\"EPEX\",\"unit\":\"ct/kWh\",\"interval\":15,\"data\":[%s]}".formatted(data);
    }

    private void expectFetch(final String body) {
        server.expect(once(), requestTo(API_URL))
                .andRespond(withSuccess(body, MediaType.APPLICATION_JSON));
    }

    /** A {@link Clock} whose instant can be advanced, so cache expiry can be tested deterministically. */
    private static final class MutableClock extends Clock {
        private Instant instant;
        private final ZoneId zone;

        MutableClock(final Instant instant, final ZoneId zone) {
            this.instant = instant;
            this.zone = zone;
        }

        void advance(final Duration amount) {
            this.instant = this.instant.plus(amount);
        }

        @Override
        public ZoneId getZone() {
            return zone;
        }

        @Override
        public Clock withZone(final ZoneId zone) {
            return new MutableClock(instant, zone);
        }

        @Override
        public Instant instant() {
            return instant;
        }
    }
}
