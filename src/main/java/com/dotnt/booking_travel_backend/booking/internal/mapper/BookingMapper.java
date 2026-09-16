package com.dotnt.booking_travel_backend.booking.internal.mapper;

import com.dotnt.booking_travel_backend.booking.api.BookingDto;
import com.dotnt.booking_travel_backend.booking.api.BookingItemDto;
import com.dotnt.booking_travel_backend.booking.internal.entity.Booking;
import com.dotnt.booking_travel_backend.booking.internal.entity.BookingItem;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class BookingMapper {

    private final ObjectMapper objectMapper;
    private final com.dotnt.booking_travel_backend.tour.internal.repository.TourBookingDetailRepository tourBookingDetailRepository;
    private final com.dotnt.booking_travel_backend.airport.internal.repository.AirportBookingDetailRepository airportBookingDetailRepository;
    private final com.dotnt.booking_travel_backend.intercity.internal.repository.IntercityBookingDetailRepository intercityBookingDetailRepository;
    private final com.dotnt.booking_travel_backend.selfdrive.internal.repository.SelfDriveBookingDetailRepository selfDriveBookingDetailRepository;
    private final com.dotnt.booking_travel_backend.tour.api.TourBookingService tourBookingService;

    public BookingDto toDto(Booking booking) {
        if (booking == null) {
            return null;
        }

        BigDecimal total = booking.getTotalAmount() != null ? booking.getTotalAmount() : BigDecimal.ZERO;
        BigDecimal dep = booking.getDepositAmount() != null ? booking.getDepositAmount() : BigDecimal.ZERO;
        BigDecimal rec = booking.getReceivedAmount() != null ? booking.getReceivedAmount() : BigDecimal.ZERO;
        BigDecimal paid = booking.getPaidAmount() != null ? booking.getPaidAmount() : dep.add(rec);
        BigDecimal remaining = total.subtract(paid).max(BigDecimal.ZERO);

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
                .zaloId(booking.getZaloId())
                .kakaoTalkId(booking.getKakaoTalkId())
                .whatsapp(booking.getWhatsapp())
                .internalNotes(booking.getInternalNotes())
                .paymentStatus(booking.getPaymentStatus())
                .paymentMethod(booking.getPaymentMethod())
                .depositType(booking.getDepositType())
                .depositRate(booking.getDepositRate())
                .paidAmount(paid)
                .depositAmount(dep)
                .receivedAmount(rec)
                .remainingAmount(remaining)
                .discountCode(booking.getDiscountCode())
                .discountAmount(booking.getDiscountAmount())
                .discountType(booking.getDiscountType())
                .discountRate(booking.getDiscountRate())
                .deletionReason(booking.getDeletionReason())
                .assignedDriver(booking.getAssignedDriver())
                .assignedVehicle(booking.getAssignedVehicle())
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

                String tourName = "Tour du lịch";
                if (dynamicDetails != null) {
                    try {
                        java.util.Map<String, Object> existing = objectMapper.readValue(
                                dynamicDetails, 
                                new TypeReference<java.util.Map<String, Object>>() {}
                        );
                        if (existing.containsKey("tourId") && existing.get("tourId") != null) {
                            Long tourId = Long.valueOf(existing.get("tourId").toString());
                            tourName = tourBookingService.getTourName(tourId);
                        }
                    } catch (Exception ignored) {}
                }
                serviceNameMap = Map.of("vi", tourName, "en", tourName);

                java.util.Map<String, Object> td = new java.util.LinkedHashMap<>();
                try {
                    if (dynamicDetails != null && !dynamicDetails.isBlank()) {
                        td.putAll(objectMapper.readValue(dynamicDetails, new TypeReference<java.util.Map<String, Object>>() {}));
                    }
                } catch (Exception ignored) {}
                td.put("guestCount", detail.getGuestCount());
                if (detail.getTourPackageId() != null) {
                    td.put("tourPackageId", detail.getTourPackageId());
                }
                if (detail.getPackageName() != null) {
                    td.put("packageName", detail.getPackageName());
                }
                if (detail.getPricingType() != null) {
                    td.put("pricingType", detail.getPricingType());
                }
                td.put("adultCount", detail.getAdultCount());
                td.put("childCount", detail.getChildCount());
                td.put("infantCount", detail.getInfantCount());
                td.put("meetingPoint", detail.getMeetingPoint());
                td.put("startTime", detail.getStartTime() != null ? detail.getStartTime().toString() : null);
                td.put("endTime", detail.getEndTime() != null ? detail.getEndTime().toString() : null);
                td.put("note", detail.getNote());
                try {
                    dynamicDetails = objectMapper.writeValueAsString(td);
                } catch (Exception ignored) {}
            }
        } else if (item.getAirportTransferBookingDetailId() != null) {
            var detail = airportBookingDetailRepository.findById(item.getAirportTransferBookingDetailId()).orElse(null);
            if (detail != null) {
                startDate = detail.getOutboundPickupTime();
                pickupLocation = detail.getOutboundPickupAddress();
                dropoffLocation = detail.getOutboundDropoffAddress();
                flightNumber = detail.getOutboundFlightNumber();
                serviceNameMap = Map.of("vi", "Trung chuyển sân bay", "en", "Airport Transfer");

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
        } else if (item.getIntercityBookingDetailId() != null) {
            var detail = intercityBookingDetailRepository.findById(item.getIntercityBookingDetailId()).orElse(null);
            if (detail != null) {
                if (detail.getDepartureDate() != null) {
                    startDate = detail.getDepartureDate().atTime(
                            detail.getDepartureTime() != null ? detail.getDepartureTime() : java.time.LocalTime.MIDNIGHT
                    );
                }
                if (detail.getReturnDate() != null) {
                    endDate = detail.getReturnDate().atTime(
                            detail.getReturnTime() != null ? detail.getReturnTime() : java.time.LocalTime.MIDNIGHT
                    );
                }
                pickupLocation = detail.getPickupAddress();
                dropoffLocation = detail.getDropoffAddress();
                serviceNameMap = Map.of("vi", "Thuê xe đi tỉnh", "en", "Inter-provincial Rent");

                java.util.Map<String, Object> idMap = new java.util.LinkedHashMap<>();
                try {
                    if (dynamicDetails != null && !dynamicDetails.isBlank()) {
                        idMap.putAll(objectMapper.readValue(dynamicDetails, new TypeReference<java.util.Map<String, Object>>() {}));
                    }
                } catch (Exception ignored) {}
                idMap.put("tripType", detail.getTripType());
                idMap.put("serviceType", detail.getServiceType() != null ? detail.getServiceType() : "PRIVATE");
                idMap.put("routeId", detail.getRouteId());
                idMap.put("vehicleCategoryId", detail.getVehicleCategoryId());
                idMap.put("passengerCount", detail.getPassengerCount());
                idMap.put("departureDate", detail.getDepartureDate() != null ? detail.getDepartureDate().toString() : null);
                idMap.put("departureTime", detail.getDepartureTime() != null ? detail.getDepartureTime().toString() : null);
                idMap.put("returnDate", detail.getReturnDate() != null ? detail.getReturnDate().toString() : null);
                idMap.put("returnTime", detail.getReturnTime() != null ? detail.getReturnTime().toString() : null);
                idMap.put("basePrice", detail.getBasePrice());
                idMap.put("surchargeAmount", detail.getSurchargeAmount());
                idMap.put("note", detail.getNote());
                try {
                    dynamicDetails = objectMapper.writeValueAsString(idMap);
                } catch (Exception ignored) {}
            }
        } else if (item.getSelfDriveBookingDetailId() != null) {
            var detail = selfDriveBookingDetailRepository.findById(item.getSelfDriveBookingDetailId()).orElse(null);
            if (detail != null) {
                if (detail.getStartDate() != null) {
                    startDate = detail.getStartDate().atStartOfDay();
                }
                if (detail.getEndDate() != null) {
                    endDate = detail.getEndDate().atStartOfDay();
                }
                pickupLocation = detail.getPickupLocation();
                dropoffLocation = detail.getDropoffLocation();
                serviceNameMap = Map.of("vi", "Thuê xe tự lái", "en", "Self-drive Rent");

                java.util.Map<String, Object> sdMap = new java.util.LinkedHashMap<>();
                try {
                    if (dynamicDetails != null && !dynamicDetails.isBlank()) {
                        sdMap.putAll(objectMapper.readValue(dynamicDetails, new TypeReference<java.util.Map<String, Object>>() {}));
                    }
                } catch (Exception ignored) {}
                sdMap.put("rentalDays", detail.getRentalDays());
                sdMap.put("vehicleCategoryId", detail.getVehicleCategoryId());
                sdMap.put("basePrice", detail.getBasePrice());
                sdMap.put("deposit", detail.getDeposit());
                sdMap.put("totalPrice", detail.getTotalPrice());
                sdMap.put("currency", detail.getCurrency());
                sdMap.put("note", detail.getNote());
                sdMap.put("idCardImageUrl", detail.getIdCardImageUrl());
                sdMap.put("driverLicenseImageUrl", detail.getDriverLicenseImageUrl());
                try {
                    dynamicDetails = objectMapper.writeValueAsString(sdMap);
                } catch (Exception ignored) {}
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
