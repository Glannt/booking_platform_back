package com.dotnt.booking_travel_backend.shared.utils;

import java.util.Objects;

/**
 * Utility class for string operations.
 */
public class StringUtils {

    private StringUtils() {
        // Private constructor to prevent instantiation
    }

    /**
     * Check if a string is null or empty.
     *
     * @param str the string to check
     * @return true if the string is null or empty, false otherwise
     */
    public static boolean isEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    /**
     * Check if a string is not null and not empty.
     *
     * @param str the string to check
     * @return true if the string is not null and not empty, false otherwise
     */
    public static boolean isNotEmpty(String str) {
        return !isEmpty(str);
    }

    /**
     * Check if a string is null or contains only whitespace.
     *
     * @param str the string to check
     * @return true if the string is null or blank, false otherwise
     */
    public static boolean isBlank(String str) {
        return str == null || str.isBlank();
    }

    /**
     * Check if a string is not null and not blank.
     *
     * @param str the string to check
     * @return true if the string is not null and not blank, false otherwise
     */
    public static boolean isNotBlank(String str) {
        return !isBlank(str);
    }

    /**
     * Capitalize the first character of a string.
     *
     * @param str the string to capitalize
     * @return the capitalized string, or null if input is null
     */
    public static String capitalize(String str) {
        if (isEmpty(str)) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * Check if two strings are equal, ignoring case.
     *
     * @param str1 the first string
     * @param str2 the second string
     * @return true if the strings are equal (case-insensitive), false otherwise
     */
    public static boolean equalsIgnoreCase(String str1, String str2) {
        if (str1 == null && str2 == null) {
            return true;
        }
        if (str1 == null || str2 == null) {
            return false;
        }
        return str1.equalsIgnoreCase(str2);
    }
}

