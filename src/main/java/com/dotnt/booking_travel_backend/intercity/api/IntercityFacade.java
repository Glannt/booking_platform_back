package com.dotnt.booking_travel_backend.intercity.api;

import com.dotnt.booking_travel_backend.intercity.internal.entity.*;
import com.dotnt.booking_travel_backend.intercity.internal.repository.*;
import com.dotnt.booking_travel_backend.intercity.internal.service.IntercityPriceCalculator;
import com.dotnt.booking_travel_backend.surcharge.api.PricingContext;
import com.dotnt.booking_travel_backend.surcharge.api.SurchargeResult;
import com.dotnt.booking_travel_backend.surcharge.api.SurchargeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Transactional
@Slf4j
public class IntercityFacade {

    private final SurchargeService surchargeService;
    private final IntercityRouteRepository intercityRouteRepository;
    private final IntercityRoutePriceRepository intercityRoutePriceRepository;
    private final IntercityBookingDetailRepository intercityBookingDetailRepository;

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
        return createIntercityBookingDetail(
                "PRIVATE",
                1,
                serviceOptionId,
                routeId,
                tripType,
                departureDateTime,
                returnDate,
                null,
                clientUnitPrice,
                clientBasePrice,
                clientTotalPrice,
                pickupLocation,
                dropoffAddress,
                customerNotes
        );
    }

    public Long createIntercityBookingDetail(
            String serviceType,
            Integer passengerCount,
            Long serviceOptionId,
            Long routeId,
            String tripType,
            LocalDateTime departureDateTime,
            LocalDate returnDate,
            LocalTime returnTime,
            BigDecimal clientUnitPrice,
            BigDecimal clientBasePrice,
            BigDecimal clientTotalPrice,
            String pickupLocation,
            String dropoffAddress,
            String customerNotes
    ) {
        BigDecimal basePrice = clientBasePrice;
        BigDecimal totalPrice = clientTotalPrice;
        String resolvedServiceType = (serviceType != null && !serviceType.isBlank()) ? serviceType.toUpperCase() : "PRIVATE";
        int resolvedPax = (passengerCount != null && passengerCount > 0) ? passengerCount : 1;
        boolean roundTrip = "ROUND_TRIP".equalsIgnoreCase(tripType) || "round-trip".equalsIgnoreCase(tripType);

        if ("SHARED".equalsIgnoreCase(resolvedServiceType)) {
            if (clientBasePrice != null) basePrice = clientBasePrice;
            if (clientTotalPrice != null) totalPrice = clientTotalPrice;
        } else {
            // PRIVATE
            if (routeId != null && serviceOptionId != null) {
                IntercityRoute intercityRoute = intercityRouteRepository.findById(routeId).orElse(null);
                IntercityRoutePrice routePrice = intercityRoutePriceRepository
                        .findByRouteIdAndVehicleCategoryId(routeId, serviceOptionId)
                        .orElse(null);

                if (intercityRoute == null || routePrice == null) {
                    log.warn("No intercity price for route {} category {}; trusting client price", routeId, serviceOptionId);
                } else {
                    BigDecimal serverOneWay = IntercityPriceCalculator.pricePrivate(
                            intercityRoute.getDistanceKm(),
                            routePrice.getPricingType(),
                            routePrice.getPerKmAmount(),
                            routePrice.getFixedPriceOneWay(),
                            routePrice.getFixedPriceRoundTrip(),
                            routePrice.getRoundTripPrice(),
                            routePrice.getRoundTripPerKmAmount(),
                            false
                    );
                    BigDecimal serverTotal = IntercityPriceCalculator.pricePrivate(
                            intercityRoute.getDistanceKm(),
                            routePrice.getPricingType(),
                            routePrice.getPerKmAmount(),
                            routePrice.getFixedPriceOneWay(),
                            routePrice.getFixedPriceRoundTrip(),
                            routePrice.getRoundTripPrice(),
                            routePrice.getRoundTripPerKmAmount(),
                            roundTrip
                    );

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
        }

        LocalDate departureDate = departureDateTime != null ? departureDateTime.toLocalDate() : null;
        LocalTime departureTime = departureDateTime != null ? departureDateTime.toLocalTime() : null;

        BigDecimal surchargeAmount = totalPrice != null && basePrice != null && totalPrice.compareTo(basePrice) > 0
                ? totalPrice.subtract(basePrice)
                : BigDecimal.ZERO;

        IntercityBookingDetail detail = IntercityBookingDetail.builder()
                .routeId(routeId)
                .vehicleCategoryId(serviceOptionId)
                .serviceType(resolvedServiceType)
                .passengerCount(resolvedPax)
                .tripType(tripType)
                .pickupAddress(pickupLocation)
                .dropoffAddress(dropoffAddress)
                .departureDate(departureDate)
                .departureTime(departureTime)
                .returnDate(returnDate)
                .returnTime(returnTime)
                .note(customerNotes)
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

    private void validateServerPrice(String label, BigDecimal serverOneWay, BigDecimal serverTotal,
                                     BigDecimal clientUnitPrice, BigDecimal clientBasePrice, BigDecimal clientTotal) {
        checkPrice(label, "base price", serverOneWay, clientBasePrice);
        checkPrice(label, "total price", serverTotal, clientTotal);
        if (clientUnitPrice != null && clientUnitPrice.signum() > 0) {
            if (!matchesPrice(serverOneWay, clientUnitPrice) && !matchesPrice(serverTotal, clientUnitPrice)) {
                log.debug("Unit price {} differs from serverOneWay {} and serverTotal {}", clientUnitPrice, serverOneWay, serverTotal);
            }
        }
    }

    private boolean matchesPrice(BigDecimal server, BigDecimal client) {
        if (server == null || client == null || client.signum() == 0) return true;
        BigDecimal tolerance = BigDecimal.valueOf(5000).add(server.abs().multiply(new BigDecimal("0.01")));
        return client.subtract(server).abs().compareTo(tolerance) <= 0;
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
