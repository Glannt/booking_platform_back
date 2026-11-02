package com.dotnt.booking_travel_backend.shared.utils;

/**
 * Utility class for type conversion operations.
 */
public class ConversionUtils {

    private ConversionUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Convert a string to a Long.
     *
     * @param value       the string value to convert
     * @param defaultValue the default value if conversion fails
     * @return the converted Long, or defaultValue if conversion fails
     */
    public static Long toLong(String value, Long defaultValue) {
        try {
            if (StringUtils.isBlank(value)) {
                return defaultValue;
            }
            return Long.parseLong(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Convert a string to a Long, default to null.
     *
     * @param value the string value to convert
     * @return the converted Long, or null if conversion fails
     */
    public static Long toLong(String value) {
        return toLong(value, null);
    }

    /**
     * Convert a string to an Integer.
     *
     * @param value        the string value to convert
     * @param defaultValue the default value if conversion fails
     * @return the converted Integer, or defaultValue if conversion fails
     */
    public static Integer toInteger(String value, Integer defaultValue) {
        try {
            if (StringUtils.isBlank(value)) {
                return defaultValue;
            }
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Convert a string to an Integer, default to null.
     *
     * @param value the string value to convert
     * @return the converted Integer, or null if conversion fails
     */
    public static Integer toInteger(String value) {
        return toInteger(value, null);
    }

    /**
     * Convert a string to a Double.
     *
     * @param value        the string value to convert
     * @param defaultValue the default value if conversion fails
     * @return the converted Double, or defaultValue if conversion fails
     */
    public static Double toDouble(String value, Double defaultValue) {
        try {
            if (StringUtils.isBlank(value)) {
                return defaultValue;
            }
            return Double.parseDouble(value.trim());
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    /**
     * Convert a string to a Double, default to null.
     *
     * @param value the string value to convert
     * @return the converted Double, or null if conversion fails
     */
    public static Double toDouble(String value) {
        return toDouble(value, null);
    }

    /**
     * Convert a string to a Boolean.
     *
     * @param value        the string value to convert
     * @param defaultValue the default value if conversion fails
     * @return the converted Boolean, or defaultValue if input is invalid
     */
    public static Boolean toBoolean(String value, Boolean defaultValue) {
        if (StringUtils.isBlank(value)) {
            return defaultValue;
        }
        String trimmed = value.trim().toLowerCase();
        if ("true".equals(trimmed) || "1".equals(trimmed) || "yes".equals(trimmed)) {
            return true;
        } else if ("false".equals(trimmed) || "0".equals(trimmed) || "no".equals(trimmed)) {
            return false;
        }
        return defaultValue;
    }

    /**
     * Convert a string to a Boolean, default to null.
     *
     * @param value the string value to convert
     * @return the converted Boolean, or null if conversion fails
     */
    public static Boolean toBoolean(String value) {
        return toBoolean(value, null);
    }
}

