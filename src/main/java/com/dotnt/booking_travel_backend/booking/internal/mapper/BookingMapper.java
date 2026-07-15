package com.dotnt.booking_travel_backend.booking.internal.mapper;

import com.dotnt.booking_travel_backend.booking.api.BookingDto;
import com.dotnt.booking_travel_backend.booking.api.BookingItemDto;
import com.dotnt.booking_travel_backend.booking.internal.entity.Booking;
import com.dotnt.booking_travel_backend.booking.internal.entity.BookingItem;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BookingMapper {

    private final ObjectMapper objectMapper;
    private final com.dotnt.booking_travel_backend.tour.internal.repository.TourBookingDetailRepository tourBookingDetailRepository;
    private final com.dotnt.booking_travel_backend.airport.internal.repository.AirportBookingDetailRepository airportBookingDetailRepository;

    public BookingDto toDto(Booking booking) {
        if (booking == null) {
            return null;
        }

        return BookingDto.builder()
                .id(booking.getId())
                .bookingCode(booking.getBookingCode())
                .userId(booking.getUserId())
                .customerName(booking.getCustomerName())
                .customerEmail(booking.getCustomerEmail())
                .customerPhone(booking.getCustomerPhone())
                .totalAmount(booking.getTotalAmount())
                .bookingStatus(booking.getBookingStatus())
                .customerNotes(booking.getCustomerNotes())
                .idempotencyKey(booking.getIdempotencyKey())
                .contactChannel(booking.getContactChannel())
                .contactId(booking.getContactId())
                .items(booking.getItems() != null ? booking.getItems().stream()
                        .map(this::toDto)
                        .collect(Collectors.toList()) : Collections.emptyList())
                .createdAt(booking.getCreatedAt())
                .updatedAt(booking.getUpdatedAt())
                .build();
    }

    public BookingItemDto toDto(BookingItem item) {
        if (item == null) {
            return null;
        }

        Map<String, String> serviceNameMap = Collections.emptyMap();
        Map<String, String> serviceOptionNameMap = Collections.emptyMap();

        java.time.LocalDateTime startDate = null;
        java.time.LocalDateTime endDate = null;
        String pickupLocation = null;
        String dropoffLocation = null;
        String flightNumber = null;
        String dynamicDetails = item.getDynamicDetails();

        if (item.getTourBookingDetailId() != null) {
            var detail = tourBookingDetailRepository.findById(item.getTourBookingDetailId()).orElse(null);
            if (detail != null) {
                if (detail.getStartTime() != null) {
                    startDate = java.time.LocalDateTime.ofInstant(detail.getStartTime(), java.time.ZoneOffset.UTC);
                }
                if (detail.getEndTime() != null) {
                    endDate = java.time.LocalDateTime.ofInstant(detail.getEndTime(), java.time.ZoneOffset.UTC);
                }
                pickupLocation = detail.getMeetingPoint();
            }
        } else if (item.getAirportTransferBookingDetailId() != null) {
            var detail = airportBookingDetailRepository.findById(item.getAirportTransferBookingDetailId()).orElse(null);
            if (detail != null) {
                startDate = detail.getOutboundPickupTime();
                pickupLocation = detail.getOutboundPickupAddress();
                dropoffLocation = detail.getOutboundDropoffAddress();
                flightNumber = detail.getOutboundFlightNumber();

                // Pack all airport detail fields into dynamicDetails as JSON for frontend
                java.util.Map<String, Object> ad = new java.util.LinkedHashMap<>();
                ad.put("tripType", detail.getTripType());
                ad.put("outboundDirection", detail.getOutboundDirection());
                ad.put("outboundDate", detail.getOutboundDate() != null ? detail.getOutboundDate().toString() : null);
                ad.put("outboundPickupTime", detail.getOutboundPickupTime() != null ? detail.getOutboundPickupTime().toString() : null);
                ad.put("outboundFlightNumber", detail.getOutboundFlightNumber());
                ad.put("outboundPickupAddress", detail.getOutboundPickupAddress());
                ad.put("outboundDropoffAddress", detail.getOutboundDropoffAddress());
                ad.put("returnDirection", detail.getReturnDirection());
                ad.put("returnDate", detail.getReturnDate() != null ? detail.getReturnDate().toString() : null);
                ad.put("returnPickupTime", detail.getReturnPickupTime() != null ? detail.getReturnPickupTime().toString() : null);
                ad.put("returnFlightNumber", detail.getReturnFlightNumber());
                ad.put("returnPickupAddress", detail.getReturnPickupAddress());
                ad.put("returnDropoffAddress", detail.getReturnDropoffAddress());
                ad.put("vehicleCategoryId", detail.getVehicleCategoryId());
                ad.put("basePrice", detail.getBasePrice());
                ad.put("totalPrice", detail.getTotalPrice());
                ad.put("currency", detail.getCurrency());
                ad.put("passenger", detail.getPassenger());
                try {
                    dynamicDetails = objectMapper.writeValueAsString(ad);
                } catch (Exception e) {
                    dynamicDetails = item.getDynamicDetails();
                }
            }
        }

        return BookingItemDto.builder()
                .id(item.getId())
                .quantity(item.getQuantity())
                .unitPrice(item.getUnitPrice())
                .totalPrice(item.getTotalPrice())
                .dynamicDetails(dynamicDetails)
                .serviceName(serviceNameMap)
                .serviceOptionName(serviceOptionNameMap)
                .startDate(startDate)
                .endDate(endDate)
                .pickupLocation(pickupLocation)
                .dropoffLocation(dropoffLocation)
                .flightNumber(flightNumber)
                .intercityBookingDetailId(item.getIntercityBookingDetailId())
                .selfDriveBookingDetailId(item.getSelfDriveBookingDetailId())
                .build();
    }
}
