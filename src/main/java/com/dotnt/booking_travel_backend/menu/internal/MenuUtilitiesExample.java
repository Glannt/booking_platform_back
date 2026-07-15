package com.dotnt.booking_travel_backend.menu.internal;

import com.dotnt.booking_travel_backend.shared.utils.*;
import org.springframework.stereotype.Component;

/**
 * Example component showing how to use utilities from shared.utils module.
 * This is for reference and can be deleted or used as template.
 */
@Component
public class MenuUtilitiesExample {

    /**
     * Example method showing StringUtils usage
     */
    public void stringUtilsExample() {
        String name = "coffee menu";

        // Check if string is empty/blank
        if (StringUtils.isNotBlank(name)) {
            System.out.println("Name is valid: " + name);
        }

        // Capitalize
        String capitalizedName = StringUtils.capitalize(name);
        System.out.println("Capitalized: " + capitalizedName);

        // Case-insensitive comparison
        if (StringUtils.equalsIgnoreCase(name, "Coffee Menu")) {
            System.out.println("Names match (case-insensitive)");
        }
    }

    /**
     * Example method showing ValidationUtils usage
     */
    public void validationUtilsExample(String email, String phone) {
        // Validate email
        if (ValidationUtils.isValidEmail(email)) {
            System.out.println("Valid email: " + email);
        }

        // Validate phone
        if (ValidationUtils.isValidPhone(phone)) {
            System.out.println("Valid phone: " + phone);
        }

        // Validate with exceptions
        try {
            ValidationUtils.validateNotEmpty(email, "email");
            ValidationUtils.validateLength(email, 5, 255, "email");
        } catch (IllegalArgumentException e) {
            System.err.println("Validation error: " + e.getMessage());
        }
    }

    /**
     * Example method showing DateTimeUtils usage
     */
    public void dateTimeUtilsExample() {
        // Get current datetime
        var now = DateTimeUtils.now();
        System.out.println("Current time: " + DateTimeUtils.formatDateTime(now));

        // Parse datetime
        var parsed = DateTimeUtils.parseDateTime("2026-06-27 10:30:00");
        if (parsed != null) {
            System.out.println("Parsed: " + parsed);
            System.out.println("Is past: " + DateTimeUtils.isPast(parsed));
            System.out.println("Is today: " + DateTimeUtils.isToday(parsed));
        }

        // Format with custom pattern
        String formatted = DateTimeUtils.formatDateTime(now, "dd/MM/yyyy");
        System.out.println("Custom format: " + formatted);
    }

    /**
     * Example method showing PageableUtils usage
     */
    public void pageableUtilsExample() {
        // Simple pageable (page 0, size 20)
        var pageable1 = PageableUtils.getPageable(0);
        System.out.println("Default pageable: " + pageable1);

        // Custom page size
        var pageable2 = PageableUtils.getPageable(0, 50);
        System.out.println("Custom size pageable: " + pageable2);

        // With sorting
        var pageable3 = PageableUtils.getPageableWithSort(0, 20, "name", "asc");
        System.out.println("Sorted pageable: " + pageable3);
    }

    /**
     * Example method showing ConversionUtils usage
     */
    public void conversionUtilsExample(String idString) {
        // Convert to Long
        Long id = ConversionUtils.toLong(idString);
        if (id != null) {
            System.out.println("Converted ID: " + id);
        }

        // Convert with default value
        Long pageSize = ConversionUtils.toLong("abc", 20L);
        System.out.println("Page size: " + pageSize); // Will be 20 since conversion failed

        // Convert to Integer
        Integer count = ConversionUtils.toInteger("100");
        System.out.println("Count: " + count);

        // Convert to Boolean
        Boolean isActive = ConversionUtils.toBoolean("true");
        System.out.println("Is active: " + isActive);
    }
}

