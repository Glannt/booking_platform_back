package com.dotnt.booking_travel_backend.booking.internal.controller;

import com.dotnt.booking_travel_backend.booking.api.BookingDto;
import com.dotnt.booking_travel_backend.booking.api.CreateBookingRequest;
import com.dotnt.booking_travel_backend.booking.internal.service.BookingService;
import com.dotnt.booking_travel_backend.shared.annotation.SuccessResponse;
import com.dotnt.booking_travel_backend.user.api.UserFacade;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/bookings")
@RequiredArgsConstructor
@Slf4j
public class BookingPublicController {

    private final BookingService bookingService;
    private final UserFacade userFacade;

    @GetMapping("/my-bookings")
    @SuccessResponse("My bookings retrieved successfully!")
    public ResponseEntity<List<BookingDto>> getMyBookings() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !(auth.getPrincipal() instanceof String email)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        Long userId = userFacade.findUserIdByEmail(email);
        if (userId == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok(bookingService.getMyBookings(userId));
    }

    @PostMapping
    @SuccessResponse("Booking created successfully!")
    public ResponseEntity<BookingDto> createBooking(@Valid @RequestBody CreateBookingRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.createBooking(request));
    }

    @GetMapping("/code/{code}")
    @SuccessResponse("Booking details retrieved successfully!")
    public ResponseEntity<BookingDto> getBookingByCode(@PathVariable String code) {
        return ResponseEntity.ok(bookingService.getBookingByCode(code));
    }
}
