package com.spro93.smarthome.controller;

import com.spro93.smarthome.service.SunPositionService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.client.RestTemplate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(SunPositionController.class)
class SunPositionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SunPositionService sunPositionService;

    @MockitoBean
    @SuppressWarnings("unused")
    private RestTemplate restTemplate;

    @Test
    void isExposedToSun_validMinimalParams_returnsOk() throws Exception {
        when(sunPositionService.isExposed(any())).thenReturn("0");

        mockMvc.perform(get("/api/isExposedToSun")
                        .param("minAzimuth", "90")
                        .param("maxAzimuth", "270"))
                .andExpect(status().isOk())
                .andExpect(content().string("0"));
    }

    @Test
    void isExposedToSun_allValidParams_returnsOk() throws Exception {
        when(sunPositionService.isExposed(any())).thenReturn("1");

        mockMvc.perform(get("/api/isExposedToSun")
                        .param("minAzimuth", "90")
                        .param("maxAzimuth", "270")
                        .param("minAltitude", "10")
                        .param("maxAltitude", "80"))
                .andExpect(status().isOk())
                .andExpect(content().string("1"));
    }

    @Test
    void isExposedToSun_missingMinAzimuth_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/isExposedToSun")
                        .param("maxAzimuth", "270"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void isExposedToSun_missingMaxAzimuth_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/isExposedToSun")
                        .param("minAzimuth", "90"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void isExposedToSun_missingBothRequiredParams_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/isExposedToSun"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void isExposedToSun_nonNumericMinAzimuth_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/isExposedToSun")
                        .param("minAzimuth", "abc")
                        .param("maxAzimuth", "270"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void isExposedToSun_nonNumericMaxAzimuth_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/isExposedToSun")
                        .param("minAzimuth", "90")
                        .param("maxAzimuth", "abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void isExposedToSun_nonNumericOptionalMinAltitude_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/isExposedToSun")
                        .param("minAzimuth", "90")
                        .param("maxAzimuth", "270")
                        .param("minAltitude", "abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void isExposedToSun_nonNumericOptionalMaxAltitude_returnsBadRequest() throws Exception {
        mockMvc.perform(get("/api/isExposedToSun")
                        .param("minAzimuth", "90")
                        .param("maxAzimuth", "270")
                        .param("maxAltitude", "abc"))
                .andExpect(status().isBadRequest());
    }
}
