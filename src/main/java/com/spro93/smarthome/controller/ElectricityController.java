package com.spro93.smarthome.controller;

import com.spro93.smarthome.service.ElectricityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
public class ElectricityController {

    private final ElectricityService electricityService;

    @GetMapping("/api/electricityPrice")
    public ResponseEntity<String> getElectricityPrice(@RequestParam(required = false) final String additionalAmount) {
        var errorMessage = checkNumericParameter(additionalAmount, "additionalAmount");
        if (!errorMessage.isEmpty()) {
            log.info("Parameter invalid, responding with HTTP 400. Error: {}", errorMessage);
            return ResponseEntity.badRequest().body(errorMessage);
        } else {
            var amount = additionalAmount != null ? Double.valueOf(additionalAmount) : null;
            return ResponseEntity.ok(electricityService.isPriceNegative(amount));
        }
    }

    private String checkNumericParameter(final String param, final String paramName) {
        if (param != null && !isNumeric(param)) {
            return paramName + " is not a number ";
        }
        return "";
    }

    private boolean isNumeric(final String param) {
        try {
            Double.parseDouble(param);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
