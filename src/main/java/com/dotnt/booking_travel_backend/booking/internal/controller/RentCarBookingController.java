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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/rent-car/bookings")
@RequiredArgsConstructor
public class RentCarBookingController {

    private final BookingService bookingService;
    private final ObjectMapper objectMapper;

    @PostMapping
    @SuccessResponse("Rent-car booking created successfully!")
    public ResponseEntity<BookingDto> create(@Valid @RequestBody CreateRentCarBookingRequest req) throws JsonProcessingException {
        LocalDate startDate = req.getDepartureDate();

        Map<String, Object> dd = new LinkedHashMap<>();
        dd.put("routeId", req.getRouteId());
        dd.put("vehicleCategoryId", req.getVehicleCategoryId());
        dd.put("serviceType", req.getServiceType() != null && !req.getServiceType().isBlank() ? req.getServiceType().toUpperCase() : "PRIVATE");
        dd.put("passengerCount", req.getPassengerCount() != null && req.getPassengerCount() > 0 ? req.getPassengerCount() : 1);
        dd.put("tripType", req.getTripType());
        dd.put("departureDate", req.getDepartureDate().toString());
        dd.put("departureTime", req.getDepartureTime());
        if (req.getReturnDate() != null) dd.put("returnDate", req.getReturnDate().toString());
        if (req.getReturnTime() != null) dd.put("returnTime", req.getReturnTime());
        dd.put("basePrice", req.getBasePrice());
        dd.put("totalPrice", req.getTotalPrice());
        dd.put("routeName", "");
        dd.put("vehicleCategory", "");
        dd.put("pickupAddress", req.getPickupAddress());
        dd.put("dropoffAddress", req.getDropoffAddress());
        String dynamicDetails = objectMapper.writeValueAsString(dd);

        int quantity = req.getPassengerCount() != null && req.getPassengerCount() > 0 ? req.getPassengerCount() : 1;

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
                        .serviceId(102L)
                        .serviceOptionId(req.getVehicleCategoryId())
                        .quantity(quantity)
                        .unitPrice(req.getTotalPrice())
                        .startDate(startDate.atStartOfDay())
                        .pickupLocation(req.getPickupAddress())
                        .dropoffLocation(req.getDropoffAddress())
                        .dynamicDetails(dynamicDetails)
                        .build()))
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.createBooking(generic));
    }
}
