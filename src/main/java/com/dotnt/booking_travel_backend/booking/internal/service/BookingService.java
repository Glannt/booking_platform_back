package com.dotnt.booking_travel_backend.booking.internal.service;

import com.dotnt.booking_travel_backend.booking.api.BookingDto;
import com.dotnt.booking_travel_backend.booking.api.CreateBookingRequest;
import com.dotnt.booking_travel_backend.booking.api.UpdateBookingRequest;

import java.util.List;

public interface BookingService {
    BookingDto createBooking(CreateBookingRequest request);
    List<BookingDto> getAllBookings();
    BookingDto getBookingById(Long id);
    BookingDto getBookingByCode(String code);
    BookingDto updateBooking(Long id, UpdateBookingRequest request);
    void deleteBooking(Long id);
}
