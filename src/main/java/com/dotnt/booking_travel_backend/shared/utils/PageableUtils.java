package com.dotnt.booking_travel_backend.shared.utils;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Utility class for pagination operations.
 */
public class PageableUtils {

    private PageableUtils() {
        // Private constructor to prevent instantiation
    }

    public static final int DEFAULT_PAGE_NUMBER = 0;
    public static final int DEFAULT_PAGE_SIZE = 20;
    public static final int MAX_PAGE_SIZE = 100;

    /**
     * Create a Pageable with default page size.
     *
     * @param pageNumber the page number (0-indexed)
     * @return a Pageable instance
     */
    public static Pageable getPageable(int pageNumber) {
        return getPageable(pageNumber, DEFAULT_PAGE_SIZE);
    }

    /**
     * Create a Pageable with custom page size.
     *
     * @param pageNumber the page number (0-indexed)
     * @param pageSize   the page size
     * @return a Pageable instance
     */
    public static Pageable getPageable(int pageNumber, int pageSize) {
        return getPageable(pageNumber, pageSize, Sort.unsorted());
    }

    /**
     * Create a Pageable with custom page size and sorting.
     *
     * @param pageNumber the page number (0-indexed)
     * @param pageSize   the page size
     * @param sort       the Sort specification
     * @return a Pageable instance
     */
    public static Pageable getPageable(int pageNumber, int pageSize, Sort sort) {
        // Validate and adjust parameters
        int validPageNumber = Math.max(pageNumber, DEFAULT_PAGE_NUMBER);
        int validPageSize = Math.max(1, Math.min(pageSize, MAX_PAGE_SIZE));

        return PageRequest.of(validPageNumber, validPageSize, sort);
    }

    /**
     * Create a Pageable with sorting by a single field.
     *
     * @param pageNumber  the page number (0-indexed)
     * @param pageSize    the page size
     * @param sortBy      the field name to sort by
     * @param sortOrder   the sort direction (asc or desc)
     * @return a Pageable instance
     */
    public static Pageable getPageableWithSort(int pageNumber, int pageSize, String sortBy, String sortOrder) {
        Sort.Direction direction = "desc".equalsIgnoreCase(sortOrder) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(direction, sortBy);
        return getPageable(pageNumber, pageSize, sort);
    }

    /**
     * Validate page number.
     *
     * @param pageNumber the page number to validate
     * @return true if the page number is valid, false otherwise
     */
    public static boolean isValidPageNumber(int pageNumber) {
        return pageNumber >= 0;
    }

    /**
     * Validate page size.
     *
     * @param pageSize the page size to validate
     * @return true if the page size is valid (between 1 and MAX_PAGE_SIZE), false otherwise
     */
    public static boolean isValidPageSize(int pageSize) {
        return pageSize > 0 && pageSize <= MAX_PAGE_SIZE;
    }
}

