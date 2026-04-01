package com.spro93.smarthome.controller;

sealed interface ValidationResult permits ValidationResult.Valid, ValidationResult.Invalid {
    record Valid() implements ValidationResult {}
    record Invalid(String errorMessage) implements ValidationResult {}
}
