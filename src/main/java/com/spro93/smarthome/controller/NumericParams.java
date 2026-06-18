package com.spro93.smarthome.controller;

/**
 * Shared validation for numeric query parameters. Only finite numbers are accepted:
 * {@code NaN} and {@code Infinity} parse successfully but would silently corrupt the
 * downstream calculations, so they are rejected like any other non-numeric input.
 */
final class NumericParams {

    private NumericParams() {
    }

    /** Returns an error fragment when the parameter is missing, empty or not a finite number. */
    static String checkRequired(final String param, final String paramName) {
        if (param == null || param.isEmpty()) {
            return paramName + " missing ";
        }
        return checkOptional(param, paramName);
    }

    /** Returns an error fragment when the parameter is present but not a finite number. */
    static String checkOptional(final String param, final String paramName) {
        if (param != null && !param.isEmpty() && !isFiniteNumber(param)) {
            return paramName + " is not a number ";
        }
        return "";
    }

    private static boolean isFiniteNumber(final String param) {
        try {
            return Double.isFinite(Double.parseDouble(param));
        } catch (NumberFormatException _) {
            return false;
        }
    }
}
