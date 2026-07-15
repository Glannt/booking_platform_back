package com.dotnt.booking_travel_backend.booking.internal.controller;

import com.dotnt.booking_travel_backend.booking.api.BookingDto;
import com.dotnt.booking_travel_backend.booking.api.UpdateBookingRequest;
import com.dotnt.booking_travel_backend.booking.internal.service.BookingService;
import com.dotnt.booking_travel_backend.shared.annotation.SuccessResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/bookings")
@RequiredArgsConstructor
public class BookingAdminController {

    private final BookingService bookingService;

    @GetMapping
    @SuccessResponse("Bookings retrieved successfully!")
    public ResponseEntity<List<BookingDto>> getAllBookings() {
        return ResponseEntity.ok(bookingService.getAllBookings());
    }

    @GetMapping("/{id}")
    @SuccessResponse("Booking retrieved successfully!")
    public ResponseEntity<BookingDto> getBookingById(@PathVariable Long id) {
        return ResponseEntity.ok(bookingService.getBookingById(id));
    }

    @PreAuthorize("hasAuthority('BOOKING_UPDATE')")
    @PutMapping("/{id}")
    @SuccessResponse("Booking updated successfully!")
    public ResponseEntity<BookingDto> updateBooking(@PathVariable Long id, @RequestBody UpdateBookingRequest request) {
        return ResponseEntity.ok(bookingService.updateBooking(id, request));
    }

    @PreAuthorize("hasAuthority('BOOKING_DELETE')")
    @DeleteMapping("/{id}")
    @SuccessResponse("Booking deleted successfully!")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id) {
        bookingService.deleteBooking(id);
        return ResponseEntity.ok().build();
    }
}
