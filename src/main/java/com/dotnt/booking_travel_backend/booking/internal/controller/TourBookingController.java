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
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @PostMapping
    @SuccessResponse("Tour booking created successfully!")
    public ResponseEntity<BookingDto> create(@Valid @RequestBody CreateTourBookingRequest req) {
        LocalDateTime startDateTime = req.getStartTime() != null
                ? LocalDateTime.parse(req.getStartTime())
                : null;
        LocalDateTime endDateTime = req.getEndTime() != null
                ? LocalDateTime.parse(req.getEndTime())
                : null;

        // Build dynamic details JSON map
        java.util.Map<String, Object> detailsMap = new java.util.LinkedHashMap<>();
        detailsMap.put("tourId", req.getTourId());
        if (req.getTourPackageId() != null) {
            detailsMap.put("tourPackageId", req.getTourPackageId());
        }
        if (req.getPackageName() != null) {
            detailsMap.put("packageName", req.getPackageName());
        }
        int adults = req.getAdultCount() != null ? req.getAdultCount() : req.getGuestCount();
        int children = req.getChildCount() != null ? req.getChildCount() : 0;
        int infants = req.getInfantCount() != null ? req.getInfantCount() : 0;
        detailsMap.put("adultCount", adults);
        detailsMap.put("childCount", children);
        detailsMap.put("infantCount", infants);
        detailsMap.put("guestCount", req.getGuestCount());
        if (req.getPickupLocation() != null) {
            detailsMap.put("pickupLocation", req.getPickupLocation());
        }
        if (req.getDescription() != null) {
            detailsMap.put("description", req.getDescription());
        }

        String dynamicDetailsJson;
        try {
            dynamicDetailsJson = objectMapper.writeValueAsString(detailsMap);
        } catch (Exception e) {
            dynamicDetailsJson = req.getDescription() != null ? req.getDescription() : "";
        }

        Long serviceOptionId = req.getTourPackageId();

        CreateBookingRequest generic = CreateBookingRequest.builder()
                .customerName(req.getCustomerName())
                .customerEmail(req.getCustomerEmail())
                .customerPhone(req.getCustomerPhone())
                .totalAmount(req.getTotalPrice())
                .customerNotes(req.getCustomerNotes() != null ? req.getCustomerNotes() : req.getNote())
                .contactChannel(req.getContactChannel())
                .contactId(req.getContactId())
                .zaloId(req.getZaloId())
                .kakaoTalkId(req.getKakaoTalkId())
                .whatsapp(req.getWhatsapp())
                .depositType(req.getDepositType())
                .depositRate(req.getDepositRate())
                .depositAmount(req.getDepositAmount())
                .items(List.of(CreateBookingItemRequest.builder()
                        .serviceId(req.getTourId())
                        .quantity(req.getGuestCount())
                        .unitPrice(req.getTotalPrice())
                        .serviceOptionId(serviceOptionId)
                        .dynamicDetails(dynamicDetailsJson)
                        .startDate(startDateTime)
                        .endDate(endDateTime)
                        .pickupLocation(req.getPickupLocation())
                        .build()))
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.createBooking(generic));
    }
}
