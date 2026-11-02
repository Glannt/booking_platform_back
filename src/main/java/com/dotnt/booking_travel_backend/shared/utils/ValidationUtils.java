package com.dotnt.booking_travel_backend.shared.utils;

/**
 * Utility class for validation operations.
 */
public class ValidationUtils {

    private ValidationUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Check if an email is valid using a basic regex pattern.
     *
     * @param email the email to validate
     * @return true if the email is valid, false otherwise
     */
    public static boolean isValidEmail(String email) {
        if (StringUtils.isBlank(email)) {
            return false;
        }
        String emailRegex = "^[A-Za-z0-9+_.-]+@(.+)$";
        return email.matches(emailRegex);
    }

    /**
     * Check if a phone number is valid (basic check for digits and length).
     *
     * @param phone the phone number to validate
     * @return true if the phone number is valid, false otherwise
     */
    public static boolean isValidPhone(String phone) {
        if (StringUtils.isBlank(phone)) {
            return false;
        }
        // Remove common phone formatting characters
        String cleaned = phone.replaceAll("[\\s\\-().]", "");
        // Check if it contains only digits and has reasonable length (7-15 digits)
        return cleaned.matches("^\\d{7,15}$");
    }

    /**
     * Check if a string is null or empty, throwing an exception if it is.
     *
     * @param value       the string to check
     * @param fieldName   the name of the field for error message
     * @throws IllegalArgumentException if the string is null or empty
     */
    public static void validateNotEmpty(String value, String fieldName) {
        if (StringUtils.isEmpty(value)) {
            throw new IllegalArgumentException(fieldName + " cannot be null or empty");
        }
    }

    /**
     * Check if a value is null, throwing an exception if it is.
     *
     * @param value     the value to check
     * @param fieldName the name of the field for error message
     * @throws IllegalArgumentException if the value is null
     */
    public static void validateNotNull(Object value, String fieldName) {
        if (value == null) {
            throw new IllegalArgumentException(fieldName + " cannot be null");
        }
    }

    /**
     * Check if a number is positive, throwing an exception if it is not.
     *
     * @param value     the number to check
     * @param fieldName the name of the field for error message
     * @throws IllegalArgumentException if the number is not positive
     */
    public static void validatePositive(Long value, String fieldName) {
        validateNotNull(value, fieldName);
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be positive");
        }
    }

    /**
     * Check if a string length is within a specified range.
     *
     * @param value     the string to check
     * @param minLength the minimum length
     * @param maxLength the maximum length
     * @param fieldName the name of the field for error message
     * @throws IllegalArgumentException if the string length is out of range
     */
    public static void validateLength(String value, int minLength, int maxLength, String fieldName) {
        validateNotEmpty(value, fieldName);
        if (value.length() < minLength || value.length() > maxLength) {
            throw new IllegalArgumentException(
                    fieldName + " length must be between " + minLength + " and " + maxLength
            );
        }
    }
}

