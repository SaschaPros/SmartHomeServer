package com.spro93.smarthome;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class SmarthomeApplicationTests {

	@Autowired
	private MockMvc mockMvc;

	@Test
	public void contextLoads() {
	}

	@Test
	public void testElectricityPriceInvalidParam() throws Exception {
		mockMvc.perform(get("/api/electricityPrice").param("additionalAmount", "abc"))
				.andExpect(status().isBadRequest());
	}

	@Test
	public void testIsExposedToSunInvalidParam() throws Exception {
		mockMvc.perform(get("/api/isExposedToSun").param("minAzimuth", "abc"))
				.andExpect(status().isBadRequest());
	}

    @Test
	public void testIsExposedToSunMissingParam() throws Exception {
		mockMvc.perform(get("/api/isExposedToSun"))
				.andExpect(status().isBadRequest());
	}
}
