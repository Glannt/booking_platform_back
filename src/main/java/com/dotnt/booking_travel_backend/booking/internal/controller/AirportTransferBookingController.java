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

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/airport-transfer/bookings")
@RequiredArgsConstructor
public class AirportTransferBookingController {

    private final BookingService bookingService;
    private final ObjectMapper objectMapper;

    @PostMapping
    @SuccessResponse("Airport transfer booking created successfully!")
    public ResponseEntity<BookingDto> create(@Valid @RequestBody CreateAirportTransferBookingRequest req) throws JsonProcessingException {
        Map<String, Object> ad = new LinkedHashMap<>();
        ad.put("tripType", req.getTripType());
        ad.put("outboundDirection", req.getOutboundDirection());
        ad.put("outboundDate", req.getOutboundDate() != null ? req.getOutboundDate().toString() : null);
        ad.put("outboundPickupTime", req.getOutboundPickupTime() != null ? req.getOutboundPickupTime().toString() : null);
        ad.put("outboundFlightNumber", req.getOutboundFlightNumber());
        ad.put("outboundPickupAddress", req.getOutboundPickupAddress());
        ad.put("outboundDropoffAddress", req.getOutboundDropoffAddress());
        ad.put("returnDirection", req.getReturnDirection());
        ad.put("returnDate", req.getReturnDate() != null ? req.getReturnDate().toString() : null);
        ad.put("returnPickupTime", req.getReturnPickupTime() != null ? req.getReturnPickupTime().toString() : null);
        ad.put("returnFlightNumber", req.getReturnFlightNumber());
        ad.put("returnPickupAddress", req.getReturnPickupAddress());
        ad.put("returnDropoffAddress", req.getReturnDropoffAddress());
        ad.put("routeId", req.getRouteId());
        ad.put("actualKm", req.getActualKm());
        ad.put("vehicleCategoryId", req.getVehicleCategoryId());
        ad.put("passengerCount", req.getPassengerCount());
        ad.put("luggageWeight", req.getLuggageWeight());

        String dynamicDetails = objectMapper.writeValueAsString(ad);

        CreateBookingRequest generic = CreateBookingRequest.builder()
                .customerName(req.getCustomerName())
                .customerEmail(req.getCustomerEmail())
                .customerPhone(req.getCustomerPhone())
                .customerNotes(req.getCustomerNotes())
                .contactChannel(req.getContactChannel())
                .contactId(req.getContactId())
                .zaloId(req.getZaloId())
                .kakaoTalkId(req.getKakaoTalkId())
                .whatsapp(req.getWhatsapp())
                .items(List.of(CreateBookingItemRequest.builder()
                        .serviceId(101L)
                        .serviceOptionId(req.getVehicleCategoryId())
                        .quantity(req.getPassengerCount() != null ? req.getPassengerCount() : 1)
                        .startDate(req.getOutboundPickupTime())
                        .endDate(req.getReturnPickupTime())
                        .flightNumber(req.getOutboundFlightNumber())
                        .pickupLocation(req.getOutboundPickupAddress())
                        .dropoffLocation(req.getOutboundDropoffAddress())
                        .dynamicDetails(dynamicDetails)
                        .build()))
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(bookingService.createBooking(generic));
    }
}
