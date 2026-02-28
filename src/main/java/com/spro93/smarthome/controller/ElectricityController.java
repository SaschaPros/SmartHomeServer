package com.spro93.smarthome.controller;

import com.spro93.smarthome.service.ElectricityService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class ElectricityController {

    private final ElectricityService electricityService;

    public ElectricityController(ElectricityService electricityService) {
        this.electricityService = electricityService;
    }

    @GetMapping("/api/electricityPrice")
    public ResponseEntity<String> getElectricityPrice(@RequestParam(required = false) String additionalAmount) {
        String errorMessage = checkNumericParameter(additionalAmount, "additionalAmount");
        if (!errorMessage.isEmpty()) {
            log.info("Parameter invalid, responding with HTTP 400. Error: {}", errorMessage);
            return ResponseEntity.badRequest().body(errorMessage);
        } else {
            Double amount = additionalAmount != null ? Double.parseDouble(additionalAmount) : null;
            return ResponseEntity.ok(electricityService.isPriceNegative(amount));
        }
    }

    private String checkNumericParameter(String param, String paramName) {
        if (param != null && !isNumeric(param)) {
            return paramName + " is not a number ";
        }
        return "";
    }

    private boolean isNumeric(String param) {
        try {
            Double.parseDouble(param);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
