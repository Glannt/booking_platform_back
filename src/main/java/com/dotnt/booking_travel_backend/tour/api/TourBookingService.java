package com.dotnt.booking_travel_backend.tour.api;

import java.time.Instant;

public interface TourBookingService {
    Long createTourBookingDetail(
            Long bookingItemId,
            Long tourId,
            int guestCount,
            String pickupLocation,
            String note,
            Instant startTime,
            Instant endTime
    );

    Long createTourBookingDetail(
            Long bookingItemId,
            Long tourId,
            int guestCount,
            String pickupLocation,
            String note,
            Instant startTime,
            Instant endTime,
            Long tourPackageId,
            String packageName,
            String pricingType,
            int adultCount,
            int childCount,
            int infantCount
    );

    boolean isTour(Long serviceId);

    void updateBookingItemId(Long tourBookingDetailId, Long bookingItemId);

    String getTourName(Long tourId);

    /**
     * Whether the package is a quote request rather than a sale: it is contact-only, so it has no
     * price and books at 0đ.
     *
     * <p>Booking needs to know this at write time so it can stamp its own line item. Reading the
     * flag back later would let an admin who unticks contact-only turn every old 0đ quote into
     * revenue.
     *
     * @param tourPackageId the package being booked; a null or unknown id is not contact-only
     */
    boolean isContactOnlyPackage(Long tourPackageId);

    java.util.Map<String, Object> getTourBookingEmailVariables(Long detailId);
}
