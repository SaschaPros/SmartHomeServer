package com.spro93.smarthome.service;

import com.spro93.smarthome.model.ElectricityPrice;
import com.spro93.smarthome.model.PriceData;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.client.RestTemplate;

import java.time.ZonedDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ElectricityServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private ElectricityService electricityService;

    /**
     * Creates a price list with:
     * - a future entry (so the cache validity check passes on the second call)
     * - a "current" entry dated 5 minutes ago (within the 15-minute matching window)
     */
    private ElectricityPrice createPrices(double currentValue) {
        var future = new PriceData(ZonedDateTime.now().plusHours(1), 0.0);
        var current = new PriceData(ZonedDateTime.now().minusMinutes(5), currentValue);
        return new ElectricityPrice("tariff", "ct/kWh", 15, List.of(future, current));
    }

    private ElectricityPrice createPricesOutsideWindow() {
        var future = new PriceData(ZonedDateTime.now().plusHours(1), 0.0);
        var old = new PriceData(ZonedDateTime.now().minusMinutes(20), -10.0);
        return new ElectricityPrice("tariff", "ct/kWh", 15, List.of(future, old));
    }

    @Test
    void isPriceNegative_negativePriceNoAdditional_returnsOne() {
        when(restTemplate.getForObject(any(String.class), eq(ElectricityPrice.class)))
                .thenReturn(createPrices(-10.0));

        assertEquals("1", electricityService.isPriceNegative(null));
    }

    @Test
    void isPriceNegative_positivePriceNoAdditional_returnsZero() {
        when(restTemplate.getForObject(any(String.class), eq(ElectricityPrice.class)))
                .thenReturn(createPrices(10.0));

        assertEquals("0", electricityService.isPriceNegative(null));
    }

    @Test
    void isPriceNegative_noPriceInCurrentWindow_returnsZero() {
        when(restTemplate.getForObject(any(String.class), eq(ElectricityPrice.class)))
                .thenReturn(createPricesOutsideWindow());

        assertEquals("0", electricityService.isPriceNegative(null));
    }

    @Test
    void isPriceNegative_additionalAmountMakesPriceNegative_returnsOne() {
        when(restTemplate.getForObject(any(String.class), eq(ElectricityPrice.class)))
                .thenReturn(createPrices(5.0));

        assertEquals("1", electricityService.isPriceNegative(-10.0));
    }

    @Test
    void isPriceNegative_additionalAmountKeepsPricePositive_returnsZero() {
        when(restTemplate.getForObject(any(String.class), eq(ElectricityPrice.class)))
                .thenReturn(createPrices(-5.0));

        assertEquals("0", electricityService.isPriceNegative(10.0));
    }

    @Test
    void isPriceNegative_cacheValid_doesNotFetchAgain() {
        when(restTemplate.getForObject(any(String.class), eq(ElectricityPrice.class)))
                .thenReturn(createPrices(-10.0));

        electricityService.isPriceNegative(null);
        electricityService.isPriceNegative(null);

        verify(restTemplate, times(1)).getForObject(any(String.class), eq(ElectricityPrice.class));
    }
}
