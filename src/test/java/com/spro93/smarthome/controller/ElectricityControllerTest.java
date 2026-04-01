package com.spro93.smarthome.controller;

import com.spro93.smarthome.service.ElectricityService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ElectricityController.class)
class ElectricityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ElectricityService electricityService;

    @MockitoBean
    @SuppressWarnings("unused")
    private RestTemplate restTemplate;

    @Test
    void getElectricityPrice_noParam_returnsOk() throws Exception {
        when(electricityService.isPriceNegative(null)).thenReturn("0");

        mockMvc.perform(get("/api/electricityPrice"))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));
    }

    @Test
    void getElectricityPrice_validPositiveParam_returnsOk() throws Exception {
        when(electricityService.isPriceNegative(5.0)).thenReturn("1");

        mockMvc.perform(get("/api/electricityPrice").param("additionalAmount", "5.0"))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
    }

    @Test
    void getElectricityPrice_validNegativeParam_returnsOk() throws Exception {
        when(electricityService.isPriceNegative(-3.5)).thenReturn("0");

        mockMvc.perform(get("/api/electricityPrice").param("additionalAmount", "-3.5"))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));
    }

    @Test
    void getElectricityPrice_zeroParam_returnsOk() throws Exception {
        when(electricityService.isPriceNegative(0.0)).thenReturn("0");

        mockMvc.perform(get("/api/electricityPrice").param("additionalAmount", "0"))
                .andExpect(status().isOk());
    }

    @Test
    void getElectricityPrice_nonNumericParam_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/electricityPrice").param("additionalAmount", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("additionalAmount is not a number "));
    }

    @Test
    void getElectricityPrice_specialCharParam_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/electricityPrice").param("additionalAmount", "1e!"))
                .andExpect(status().isBadRequest());
    }
}
