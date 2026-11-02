package com.dotnt.booking_travel_backend.shared.utils;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility class for date and time operations.
 */
public class DateTimeUtils {

    private DateTimeUtils() {
        // Private constructor to prevent instantiation
    }

    public static final String DEFAULT_DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";
    public static final String DEFAULT_DATE_FORMAT = "yyyy-MM-dd";
    public static final String DEFAULT_TIME_FORMAT = "HH:mm:ss";

    /**
     * Format a LocalDateTime using the default format.
     *
     * @param dateTime the LocalDateTime to format
     * @return the formatted string, or null if input is null
     */
    public static String formatDateTime(LocalDateTime dateTime) {
        return formatDateTime(dateTime, DEFAULT_DATE_TIME_FORMAT);
    }

    /**
     * Format a LocalDateTime using a custom format.
     *
     * @param dateTime the LocalDateTime to format
     * @param pattern  the format pattern
     * @return the formatted string, or null if input is null
     */
    public static String formatDateTime(LocalDateTime dateTime, String pattern) {
        if (dateTime == null) {
            return null;
        }
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
        return dateTime.format(formatter);
    }

    /**
     * Parse a string to LocalDateTime using the default format.
     *
     * @param dateTimeString the string to parse
     * @return the parsed LocalDateTime, or null if input is null or invalid
     */
    public static LocalDateTime parseDateTime(String dateTimeString) {
        return parseDateTime(dateTimeString, DEFAULT_DATE_TIME_FORMAT);
    }

    /**
     * Parse a string to LocalDateTime using a custom format.
     *
     * @param dateTimeString the string to parse
     * @param pattern        the format pattern
     * @return the parsed LocalDateTime, or null if input is null or invalid
     */
    public static LocalDateTime parseDateTime(String dateTimeString, String pattern) {
        if (StringUtils.isBlank(dateTimeString)) {
            return null;
        }
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(pattern);
            return LocalDateTime.parse(dateTimeString, formatter);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Get the current LocalDateTime.
     *
     * @return the current LocalDateTime
     */
    public static LocalDateTime now() {
        return LocalDateTime.now();
    }

    /**
     * Check if a LocalDateTime is in the past.
     *
     * @param dateTime the LocalDateTime to check
     * @return true if the LocalDateTime is in the past, false otherwise
     */
    public static boolean isPast(LocalDateTime dateTime) {
        if (dateTime == null) {
            return false;
        }
        return dateTime.isBefore(now());
    }

    /**
     * Check if a LocalDateTime is in the future.
     *
     * @param dateTime the LocalDateTime to check
     * @return true if the LocalDateTime is in the future, false otherwise
     */
    public static boolean isFuture(LocalDateTime dateTime) {
        if (dateTime == null) {
            return false;
        }
        return dateTime.isAfter(now());
    }

    /**
     * Check if a LocalDateTime is today.
     *
     * @param dateTime the LocalDateTime to check
     * @return true if the LocalDateTime is today, false otherwise
     */
    public static boolean isToday(LocalDateTime dateTime) {
        if (dateTime == null) {
            return false;
        }
        LocalDateTime now = now();
        return dateTime.toLocalDate().equals(now.toLocalDate());
    }
}

