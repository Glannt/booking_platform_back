package com.dotnt.booking_travel_backend.booking.internal.controller;

import com.dotnt.booking_travel_backend.booking.api.*;
import com.dotnt.booking_travel_backend.booking.internal.service.BookingService;
import com.dotnt.booking_travel_backend.shared.annotation.SuccessResponse;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/self-drive/bookings")
@RequiredArgsConstructor
public class SelfDriveBookingController {

    private final BookingService bookingService;
    private final ObjectMapper objectMapper;

    @PostMapping
    @SuccessResponse("Self-drive booking created successfully!")
    public ResponseEntity<BookingDto> create(@Valid @RequestBody CreateSelfDriveBookingRequest req) throws JsonProcessingException {
        LocalDate endDate = req.getEndDate() != null ? req.getEndDate()
                : (req.getRentalDays() != null ? req.getStartDate().plusDays(req.getRentalDays() - 1) : req.getStartDate());
        int rentalDays = req.getRentalDays() != null ? req.getRentalDays()
                : (int) java.time.temporal.ChronoUnit.DAYS.between(req.getStartDate(), endDate) + 1;

        String dynamicDetails = objectMapper.writeValueAsString(Map.of(
                "vehicleCategoryId", req.getVehicleCategoryId(),
                "startDate", req.getStartDate().toString(),
                "endDate", endDate.toString(),
                "rentalDays", rentalDays,
                "basePrice", req.getBasePrice(),
                "deposit", req.getDeposit(),
                "totalPrice", req.getTotalPrice(),
                "pickupLocation", req.getPickupLocation(),
                "dropoffLocation", req.getDropoffLocation() != null ? req.getDropoffLocation() : ""
        ));

        CreateBookingRequest generic = CreateBookingRequest.builder()
                .customerName(req.getCustomerName())
                .customerEmail(req.getCustomerEmail())
                .customerPhone(req.getCustomerPhone())
                .totalAmount(req.getTotalPrice())
                .customerNotes(req.getCustomerNotes())
                .contactChannel(req.getContactChannel())
                .contactId(req.getContactId())
                .items(List.of(CreateBookingItemRequest.builder()
                        .serviceId(103L)
                        .serviceOptionId(req.getVehicleCategoryId())
                        .quantity(rentalDays)
                        .unitPrice(req.getTotalPrice())
                        .startDate(req.getStartDate().atStartOfDay())
                        .endDate(endDate.atStartOfDay())
                        .pickupLocation(req.getPickupLocation())
                        .dropoffLocation(req.getDropoffLocation())
                        .dynamicDetails(dynamicDetails)
                        .build()))
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.createBooking(generic));
    }
}
