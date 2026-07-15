package com.dotnt.booking_travel_backend.booking.internal.controller;

import com.dotnt.booking_travel_backend.booking.api.BookingDto;
import com.dotnt.booking_travel_backend.booking.api.CreateBookingRequest;
import com.dotnt.booking_travel_backend.booking.internal.service.BookingService;
import com.dotnt.booking_travel_backend.shared.annotation.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
public class BookingPublicController {

    private final BookingService bookingService;

    @PostMapping
    @SuccessResponse("Booking created successfully!")
    public ResponseEntity<BookingDto> createBooking(@RequestBody CreateBookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.createBooking(request));
    }

    @GetMapping("/code/{code}")
    @SuccessResponse("Booking details retrieved successfully!")
    public ResponseEntity<BookingDto> getBookingByCode(@PathVariable String code) {
        return ResponseEntity.ok(bookingService.getBookingByCode(code));
    }
}
