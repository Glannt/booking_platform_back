package com.dotnt.booking_travel_backend.sharedroute.service;

import com.dotnt.booking_travel_backend.sharedroute.dto.SharedRouteBookingRequest;
import com.dotnt.booking_travel_backend.sharedroute.dto.SharedRouteBookingResponse;

import java.util.List;

public interface ISharedBookingService {
    SharedRouteBookingResponse createBooking(SharedRouteBookingRequest request);
    SharedRouteBookingResponse getBookingByGroupId(Long bookingGroupId);
    List<SharedRouteBookingResponse.BookingItemDto> getAllBookings();
    void updateBookingStatus(Long bookingId, String status);
}
