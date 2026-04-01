package com.spro93.smarthome.controller;

import com.spro93.smarthome.service.SunPositionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@Slf4j
@RequiredArgsConstructor
public class SunPositionController {

    private final SunPositionService sunPositionService;

    @GetMapping("/api/isExposedToSun")
    public ResponseEntity<String> isExposedToSun(@RequestParam final Map<String, String> query) {
        return switch (validate(query)) {
            case ValidationResult.Invalid(var errorMessage) -> {
                log.info("Parameters invalid, responding with HTTP 400. Errors: {}", errorMessage);
                yield ResponseEntity.badRequest().body(errorMessage);
            }
            case ValidationResult.Valid() -> ResponseEntity.ok(sunPositionService.isExposed(query));
        };
    }

    private ValidationResult validate(final Map<String, String> query) {
        var errors = checkSunPositionParameters(query);
        return errors.isEmpty() ? new ValidationResult.Valid() : new ValidationResult.Invalid(errors);
    }

    private String checkSunPositionParameters(final Map<String, String> query) {
        var errorMessage = new StringBuilder();

        errorMessage.append(checkNumericParameterAvailable(query.get("minAzimuth"), "minAzimuth"));
        errorMessage.append(checkNumericParameterAvailable(query.get("maxAzimuth"), "maxAzimuth"));
        errorMessage.append(checkNumericParameter(query.get("minAltitude"), "minAltitude"));
        errorMessage.append(checkNumericParameter(query.get("maxAltitude"), "maxAltitude"));

        return errorMessage.toString().trim();
    }

    private String checkNumericParameterAvailable(final String param, final String paramName) {
        return switch (param) {
            case null -> paramName + " missing ";
            case "" -> paramName + " missing ";
            default -> checkNumericParameter(param, paramName);
        };
    }

    private String checkNumericParameter(final String param, final String paramName) {
        if (param != null && !param.isEmpty() && !isNumeric(param)) {
            return paramName + " is not a number ";
        }
        return "";
    }

    private boolean isNumeric(final String param) {
        try {
            Double.parseDouble(param);
            return true;
        } catch (NumberFormatException _) {
            return false;
        }
    }
}

