package com.dotnt.booking_travel_backend.booking.internal.controller;

import com.dotnt.booking_travel_backend.booking.api.*;
import com.dotnt.booking_travel_backend.booking.internal.service.BookingService;
import com.dotnt.booking_travel_backend.shared.annotation.SuccessResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/tours/bookings")
@RequiredArgsConstructor
public class TourBookingController {

    private final BookingService bookingService;

    @PostMapping
    @SuccessResponse("Tour booking created successfully!")
    public ResponseEntity<BookingDto> create(@Valid @RequestBody CreateTourBookingRequest req) {
        LocalDateTime startDateTime = req.getStartTime() != null
                ? LocalDateTime.parse(req.getStartTime())
                : null;
        LocalDateTime endDateTime = req.getEndTime() != null
                ? LocalDateTime.parse(req.getEndTime())
                : null;

        CreateBookingRequest generic = CreateBookingRequest.builder()
                .customerName(req.getCustomerName())
                .customerEmail(req.getCustomerEmail())
                .customerPhone(req.getCustomerPhone())
                .totalAmount(req.getTotalPrice())
                .customerNotes(req.getCustomerNotes() != null ? req.getCustomerNotes() : req.getNote())
                .contactChannel(req.getContactChannel())
                .contactId(req.getContactId())
                .items(List.of(CreateBookingItemRequest.builder()
                        .serviceId(req.getTourId())
                        .quantity(req.getGuestCount())
                        .unitPrice(req.getTotalPrice())
                        .serviceOptionId(req.getVehicleCategoryId())
                        .dynamicDetails(req.getDescription() != null ? req.getDescription() : "")
                        .startDate(startDateTime)
                        .endDate(endDateTime)
                        .pickupLocation(req.getPickupLocation())
                        .build()))
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.createBooking(generic));
    }
}
