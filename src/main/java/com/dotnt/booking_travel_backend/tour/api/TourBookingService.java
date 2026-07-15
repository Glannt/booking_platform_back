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

    boolean isTour(Long serviceId);

    void updateBookingItemId(Long tourBookingDetailId, Long bookingItemId);
}
