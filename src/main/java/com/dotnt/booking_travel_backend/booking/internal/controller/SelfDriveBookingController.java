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
    private final com.dotnt.booking_travel_backend.selfdrive.api.SelfDriveFacade selfDriveFacade;

    @PostMapping
    @SuccessResponse("Self-drive booking created successfully!")
    public ResponseEntity<BookingDto> create(@Valid @RequestBody CreateSelfDriveBookingRequest req) throws JsonProcessingException {
        LocalDate endDate = req.getEndDate() != null ? req.getEndDate()
                : (req.getRentalDays() != null ? req.getStartDate().plusDays(req.getRentalDays() - 1) : req.getStartDate());
        int rentalDays = req.getRentalDays() != null ? req.getRentalDays()
                : (int) java.time.temporal.ChronoUnit.DAYS.between(req.getStartDate(), endDate) + 1;

        // Availability Guard (P0-01)
        com.dotnt.booking_travel_backend.selfdrive.api.dtos.CategoryAvailabilityDto availability =
                selfDriveFacade.checkSelfDriveAvailability(req.getVehicleCategoryId(), req.getStartDate(), endDate, "vi");
        if (availability == null || !Boolean.TRUE.equals(availability.getIsAvailable()) || availability.getAvailableVehicles() <= 0) {
            throw new org.springframework.web.server.ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Phân khúc xe " + (availability != null ? availability.getCategoryName() : "") + " đã hết xe khả dụng trong khoảng thời gian từ " + req.getStartDate() + " đến " + endDate + ". Vui lòng chọn ngày khác hoặc phân khúc khác.");
        }

        java.util.Map<String, Object> detailsMap = new java.util.HashMap<>();
        detailsMap.put("vehicleCategoryId", req.getVehicleCategoryId());
        detailsMap.put("startDate", req.getStartDate().toString());
        detailsMap.put("endDate", endDate.toString());
        detailsMap.put("rentalDays", rentalDays);
        detailsMap.put("basePrice", req.getBasePrice());
        detailsMap.put("deposit", req.getDeposit());
        detailsMap.put("totalPrice", req.getTotalPrice());
        detailsMap.put("pickupLocation", req.getPickupLocation());
        detailsMap.put("dropoffLocation", req.getDropoffLocation() != null ? req.getDropoffLocation() : "");
        if (req.getSeatAddOnId() != null) detailsMap.put("seatAddOnId", req.getSeatAddOnId());
        if (req.getFuelAddOnId() != null) detailsMap.put("fuelAddOnId", req.getFuelAddOnId());
        if (req.getAddOnIds() != null) detailsMap.put("addOnIds", req.getAddOnIds());

        String dynamicDetails = objectMapper.writeValueAsString(detailsMap);

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
                .depositType(req.getDepositType())
                .depositRate(req.getDepositRate())
                .depositAmount(req.getDepositAmount())
                .discountCode(req.getDiscountCode())
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
