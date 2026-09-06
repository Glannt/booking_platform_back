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
    private final com.dotnt.booking_travel_backend.rental.api.RentalFacade rentalFacade;

    @PostMapping
    @SuccessResponse("Self-drive booking created successfully!")
    public ResponseEntity<BookingDto> create(@Valid @RequestBody CreateSelfDriveBookingRequest req) throws JsonProcessingException {
        LocalDate endDate = req.getEndDate() != null ? req.getEndDate()
                : (req.getRentalDays() != null ? req.getStartDate().plusDays(req.getRentalDays() - 1) : req.getStartDate());
        int rentalDays = req.getRentalDays() != null ? req.getRentalDays()
                : (int) java.time.temporal.ChronoUnit.DAYS.between(req.getStartDate(), endDate) + 1;

        // Availability Guard (P0-01)
        com.dotnt.booking_travel_backend.rental.api.dtos.CategoryAvailabilityDto availability =
                rentalFacade.checkSelfDriveAvailability(req.getVehicleCategoryId(), req.getStartDate(), endDate, "vi");
        if (availability == null || !Boolean.TRUE.equals(availability.getIsAvailable()) || availability.getAvailableVehicles() <= 0) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Phân khúc xe " + (availability != null ? availability.getCategoryName() : "") + " đã hết xe khả dụng trong khoảng thời gian từ " + req.getStartDate() + " đến " + endDate + ". Vui lòng chọn ngày khác hoặc phân khúc khác.");
        }

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
                .zaloId(req.getZaloId())
                .kakaoTalkId(req.getKakaoTalkId())
                .whatsapp(req.getWhatsapp())
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
