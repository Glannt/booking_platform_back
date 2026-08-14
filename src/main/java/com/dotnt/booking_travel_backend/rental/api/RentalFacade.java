package com.dotnt.booking_travel_backend.rental.api;

import com.dotnt.booking_travel_backend.rental.internal.entity.*;
import com.dotnt.booking_travel_backend.rental.internal.repository.*;
import com.dotnt.booking_travel_backend.rental.internal.service.IntercityPriceCalculator;
import com.dotnt.booking_travel_backend.surcharge.api.PricingContext;
import com.dotnt.booking_travel_backend.surcharge.api.SurchargeResult;
import com.dotnt.booking_travel_backend.surcharge.api.SurchargeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@Component
@RequiredArgsConstructor
@Transactional
@Slf4j
public class RentalFacade {

    private final SurchargeService surchargeService;
    private final IntercityRouteRepository intercityRouteRepository;
    private final IntercityRoutePriceRepository intercityRoutePriceRepository;
    private final IntercityBookingDetailRepository intercityBookingDetailRepository;
    private final SelfDriveBookingDetailRepository selfDriveBookingDetailRepository;

    public Long createIntercityBookingDetail(
            Long serviceOptionId,
            Long routeId,
            String tripType,
            LocalDateTime departureDateTime,
            LocalDate returnDate,
            BigDecimal clientUnitPrice,
            BigDecimal clientBasePrice,
            BigDecimal clientTotalPrice,
            String pickupLocation,
            String dropoffAddress,
            String customerNotes
    ) {
        BigDecimal basePrice = clientBasePrice;
        BigDecimal totalPrice = clientTotalPrice;

        if (routeId != null && serviceOptionId != null) {
            IntercityRoute intercityRoute = intercityRouteRepository.findById(routeId).orElse(null);
            IntercityRoutePrice routePrice = intercityRoutePriceRepository
                    .findByRouteIdAndVehicleCategoryId(routeId, serviceOptionId)
                    .orElse(null);

            if (intercityRoute == null || routePrice == null) {
                log.warn("No intercity price for route {} category {}; trusting client price", routeId, serviceOptionId);
            } else {
                boolean roundTrip = "ROUND_TRIP".equalsIgnoreCase(tripType) || "round-trip".equalsIgnoreCase(tripType);
                BigDecimal serverOneWay = IntercityPriceCalculator.price(
                        intercityRoute.getDistanceKm(), routePrice.getPerKmAmount(), false);
                BigDecimal serverTotal = IntercityPriceCalculator.price(
                        intercityRoute.getDistanceKm(), routePrice.getPerKmAmount(), roundTrip);

                if (departureDateTime != null) {
                    PricingContext pricingContext = PricingContext.builder()
                            .basePrice(serverTotal)
                            .serviceTime(departureDateTime)
                            .routeId(routeId)
                            .vehicleCategoryId(serviceOptionId)
                            .tripType(tripType)
                            .build();
                    SurchargeResult surchargeResult = surchargeService.calculate("INTERCITY", pricingContext);
                    if (surchargeResult != null && surchargeResult.getTotalSurcharge() != null) {
                        serverTotal = serverTotal.add(surchargeResult.getTotalSurcharge());
                    }
                }

                validateServerPrice("intercity", serverOneWay, serverTotal, clientUnitPrice, clientBasePrice, clientTotalPrice);
                basePrice = serverOneWay;
                totalPrice = serverTotal;
            }
        }

        LocalDate departureDate = departureDateTime != null ? departureDateTime.toLocalDate() : null;
        LocalTime departureTime = departureDateTime != null ? departureDateTime.toLocalTime() : null;

        BigDecimal surchargeAmount = totalPrice != null && basePrice != null && totalPrice.compareTo(basePrice) > 0
                ? totalPrice.subtract(basePrice)
                : BigDecimal.ZERO;

        IntercityBookingDetail detail = IntercityBookingDetail.builder()
                .routeId(routeId)
                .vehicleCategoryId(serviceOptionId)
                .tripType(tripType)
                .pickupAddress(pickupLocation)
                .dropoffAddress(dropoffAddress)
                .departureDate(departureDate)
                .departureTime(departureTime)
                .returnDate(returnDate)
                .basePrice(basePrice)
                .surchargeAmount(surchargeAmount)
                .totalPrice(totalPrice)
                .currency("VND")
                .status("PENDING")
                .build();

        detail = intercityBookingDetailRepository.save(detail);
        return detail.getId();
    }

    public void updateIntercityBookingItemId(Long detailId, Long bookingItemId) {
        IntercityBookingDetail detail = intercityBookingDetailRepository.findById(detailId).orElse(null);
        if (detail != null) {
            detail.setBookingItemId(bookingItemId);
            intercityBookingDetailRepository.save(detail);
        }
    }

    public Long createSelfDriveBookingDetail(
            Long serviceOptionId,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal clientBasePrice,
            BigDecimal clientTotalPrice,
            String pickupLocation,
            String dropoffLocation
    ) {
        int rentalDays = 1;
        if (startDate != null && endDate != null) {
            rentalDays = (int) java.time.temporal.ChronoUnit.DAYS.between(startDate, endDate);
            if (rentalDays <= 0) rentalDays = 1;
        }

        SelfDriveBookingDetail detail = SelfDriveBookingDetail.builder()
                .vehicleCategoryId(serviceOptionId)
                .startDate(startDate)
                .endDate(endDate)
                .rentalDays(rentalDays)
                .pickupLocation(pickupLocation)
                .dropoffLocation(dropoffLocation)
                .basePrice(clientBasePrice)
                .deposit(BigDecimal.ZERO)
                .totalPrice(clientTotalPrice)
                .currency("VND")
                .depositStatus("PENDING")
                .status("PENDING")
                .build();

        detail = selfDriveBookingDetailRepository.save(detail);
        return detail.getId();
    }

    public void updateSelfDriveBookingItemId(Long detailId, Long bookingItemId) {
        SelfDriveBookingDetail detail = selfDriveBookingDetailRepository.findById(detailId).orElse(null);
        if (detail != null) {
            detail.setBookingItemId(bookingItemId);
            selfDriveBookingDetailRepository.save(detail);
        }
    }

    public Map<String, Object> getIntercityBookingEmailVariables(Long detailId, String locale) {
        IntercityBookingDetail detail = intercityBookingDetailRepository.findById(detailId).orElse(null);
        if (detail == null) return Map.of();

        Map<String, Object> detailMap = new HashMap<>();
        detailMap.put("tripType", "ROUND_TRIP".equals(detail.getTripType()) ? "Khứ hồi" : "Một chiều");
        detailMap.put("departureDate", detail.getDepartureDate());
        detailMap.put("departureTime", detail.getDepartureTime());
        detailMap.put("returnDate", detail.getReturnDate());
        detailMap.put("pickupAddress", detail.getPickupAddress());
        detailMap.put("dropoffAddress", detail.getDropoffAddress());
        detailMap.put("note", detail.getNote());
        detailMap.put("vehicleCategoryId", detail.getVehicleCategoryId());

        Map<String, Object> variables = new HashMap<>();
        variables.put("detail", detailMap);

        if (detail.getRouteId() != null) {
            IntercityRoute route = intercityRouteRepository.findById(detail.getRouteId()).orElse(null);
            if (route != null) {
                Map<String, Object> routeMap = new HashMap<>();
                routeMap.put("distanceKm", route.getDistanceKm());
                routeMap.put("originCity", route.getOriginCity());
                routeMap.put("destinationCity", route.getDestinationCity());
                
                String routeName = route.getOriginCity() + " ➡️ " + route.getDestinationCity();
                if (route.getTranslations() != null) {
                    String targetLocale = (locale == null || locale.isBlank()) ? "vi" : locale;
                    routeName = route.getTranslations().stream()
                            .filter(t -> targetLocale.equalsIgnoreCase(t.getLocale()))
                            .map(IntercityRouteTranslation::getName)
                            .findFirst()
                            .orElse(routeName);
                }
                routeMap.put("routeName", routeName);
                variables.put("route", routeMap);
            }
        }
        return variables;
    }

    public Map<String, Object> getSelfDriveBookingEmailVariables(Long detailId, String locale) {
        SelfDriveBookingDetail detail = selfDriveBookingDetailRepository.findById(detailId).orElse(null);
        if (detail == null) return Map.of();

        Map<String, Object> detailMap = new HashMap<>();
        detailMap.put("startDate", detail.getStartDate());
        detailMap.put("endDate", detail.getEndDate());
        detailMap.put("rentalDays", detail.getRentalDays());
        detailMap.put("pickupLocation", detail.getPickupLocation());
        detailMap.put("dropoffLocation", detail.getDropoffLocation());
        detailMap.put("vehicleCategoryId", detail.getVehicleCategoryId());

        Map<String, Object> variables = new HashMap<>();
        variables.put("detail", detailMap);
        return variables;
    }

    private void validateServerPrice(String label, BigDecimal serverOneWay, BigDecimal serverTotal,
                                     BigDecimal clientUnitPrice, BigDecimal clientBasePrice, BigDecimal clientTotal) {
        checkPrice(label, "base price", serverOneWay, clientBasePrice);
        checkPrice(label, "total price", serverTotal, clientTotal);
        checkPrice(label, "unit price", serverTotal, clientUnitPrice);
    }

    private void checkPrice(String label, String field, BigDecimal server, BigDecimal client) {
        if (server == null || client == null || client.signum() == 0) return;
        BigDecimal tolerance = BigDecimal.valueOf(5000).add(server.abs().multiply(new BigDecimal("0.01")));
        if (client.subtract(server).abs().compareTo(tolerance) > 0) {
            throw new IllegalArgumentException(
                    "Price mismatch for " + label + " (" + field + "): client " + client + " vs server " + server);
        }
    }
}
