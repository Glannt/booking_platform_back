package com.dotnt.booking_travel_backend.airport.api;

import com.dotnt.booking_travel_backend.airport.internal.entity.*;
import com.dotnt.booking_travel_backend.airport.internal.repository.*;
import com.dotnt.booking_travel_backend.airport.internal.service.AirportTransferPriceCalculator;
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
import java.util.Map;
import java.util.HashMap;

@Component
@RequiredArgsConstructor
@Transactional
@Slf4j
public class AirportFacade {

    private final AirportTransferPriceRepository airportTransferPriceRepository;
    private final AirportBookingDetailRepository airportBookingDetailRepository;
    private final SurchargeService surchargeService;
    private final AirportRouteRepository airportRouteRepository;
    private final AirportRepository airportRepository;

    public boolean existsTransferPriceById(Long id) {
        return id != null && airportTransferPriceRepository.existsById(id);
    }

    public Long getDefaultAirportId() {
        return airportRepository.findByActiveTrue().stream()
                .filter(Airport::isDefault)
                .map(Airport::getId)
                .findFirst()
                .orElse(null);
    }

    public Long createAirportBookingDetail(
            Long vehicleCategoryId,
            Long routeId,
            String tripType,
            String outboundDirection,
            LocalDateTime outboundPickupTime,
            String outboundFlightNumber,
            String outboundPickupAddress,
            String outboundDropoffAddress,
            String returnDirection,
            LocalDate returnDate,
            LocalDateTime returnPickupTime,
            String returnFlightNumber,
            String returnPickupAddress,
            String returnDropoffAddress,
            BigDecimal clientUnitPrice,
            BigDecimal clientBasePrice,
            BigDecimal clientTotalPrice,
            String customerNotes,
            BigDecimal actualKm
    ) {
        BigDecimal basePrice = clientBasePrice;
        BigDecimal totalPrice = clientTotalPrice;

        if (routeId != null && vehicleCategoryId != null) {
            AirportTransferPrice transferPrice = airportTransferPriceRepository
                    .findByAirportRouteIdAndVehicleCategoryId(routeId, vehicleCategoryId)
                    .orElse(null);

            if (transferPrice == null) {
                log.warn("No airport transfer price for route {} category {}; trusting client price", routeId, vehicleCategoryId);
            } else {
                boolean isFixedRoute = airportRouteRepository.existsById(routeId);
                BigDecimal calculationKm = isFixedRoute ? null : actualKm;

                BigDecimal serverOneWay = AirportTransferPriceCalculator.oneWay(transferPrice, calculationKm);
                BigDecimal serverTotal = "ROUND_TRIP".equals(tripType)
                        ? AirportTransferPriceCalculator.roundTrip(transferPrice, calculationKm)
                        : serverOneWay;

                BigDecimal surchargeBase = "ROUND_TRIP".equals(tripType)
                        ? transferPrice.getBaseAmount().multiply(BigDecimal.valueOf(2))
                        : transferPrice.getBaseAmount();

                PricingContext pricingContext = PricingContext.builder()
                        .basePrice(surchargeBase)
                        .serviceTime(outboundPickupTime)
                        .routeId(routeId)
                        .vehicleCategoryId(vehicleCategoryId)
                        .tripType(tripType)
                        .build();

                SurchargeResult surchargeResult = surchargeService.calculate("AIRPORT_TRANSFER", pricingContext);
                BigDecimal surcharge = surchargeResult != null && surchargeResult.getTotalSurcharge() != null
                        ? surchargeResult.getTotalSurcharge()
                        : BigDecimal.ZERO;
                serverTotal = serverTotal.add(surcharge);

                validateServerPrice("airport transfer", serverOneWay, serverTotal, clientUnitPrice, clientBasePrice, clientTotalPrice);
                basePrice = serverOneWay;
                totalPrice = serverTotal;
            }
        }

        AirportBookingDetail detail = AirportBookingDetail.builder()
                .routeId(routeId)
                .vehicleCategoryId(vehicleCategoryId)
                .tripType(tripType)
                .outboundDirection(outboundDirection)
                .outboundPickupTime(outboundPickupTime)
                .outboundFlightNumber(outboundFlightNumber)
                .outboundPickupAddress(outboundPickupAddress)
                .outboundDropoffAddress(outboundDropoffAddress)
                .returnDirection(returnDirection)
                .returnDate(returnDate)
                .returnPickupTime(returnPickupTime)
                .returnFlightNumber(returnFlightNumber)
                .returnPickupAddress(returnPickupAddress)
                .returnDropoffAddress(returnDropoffAddress)
                .basePrice(basePrice)
                .surchargeAmount(totalPrice.subtract(basePrice != null ? basePrice : BigDecimal.ZERO))
                .totalPrice(totalPrice)
                .currency("VND")
                .note(customerNotes)
                .build();

        detail = airportBookingDetailRepository.save(detail);
        return detail.getId();
    }

    public void updateAirportBookingItemId(Long detailId, Long bookingItemId) {
        AirportBookingDetail detail = airportBookingDetailRepository.findById(detailId).orElse(null);
        if (detail != null) {
            detail.setBookingItemId(bookingItemId);
            airportBookingDetailRepository.save(detail);
        }
    }

    public Map<String, Object> getAirportBookingEmailVariables(Long detailId, String locale) {
        AirportBookingDetail detail = airportBookingDetailRepository.findById(detailId).orElse(null);
        if (detail == null) return Map.of();

        Map<String, Object> detailMap = new HashMap<>();
        detailMap.put("tripType", detail.getTripType() != null ? detail.getTripType() : "ONE_WAY");
        detailMap.put("tripTypeDisplay", "ROUND_TRIP".equals(detail.getTripType()) ? "Khứ hồi" : "Một chiều");
        detailMap.put("outboundDirection", detail.getOutboundDirection());
        detailMap.put("outboundDate", detail.getOutboundDate());
        detailMap.put("outboundPickupTime", detail.getOutboundPickupTime());
        detailMap.put("outboundPickupAddress", detail.getOutboundPickupAddress());
        detailMap.put("outboundDropoffAddress", detail.getOutboundDropoffAddress());
        detailMap.put("outboundFlightNumber", detail.getOutboundFlightNumber());
        detailMap.put("returnDirection", detail.getReturnDirection());
        detailMap.put("returnDate", detail.getReturnDate());
        detailMap.put("returnPickupTime", detail.getReturnPickupTime());
        detailMap.put("returnFlightNumber", detail.getReturnFlightNumber());
        detailMap.put("returnPickupAddress", detail.getReturnPickupAddress());
        detailMap.put("returnDropoffAddress", detail.getReturnDropoffAddress());
        detailMap.put("vehicleCategoryId", detail.getVehicleCategoryId());
        detailMap.put("basePrice", detail.getBasePrice() != null ? detail.getBasePrice() : BigDecimal.ZERO);
        detailMap.put("surchargeAmount", detail.getSurchargeAmount() != null ? detail.getSurchargeAmount() : BigDecimal.ZERO);
        detailMap.put("nightSurcharge", detail.getSurchargeAmount() != null ? detail.getSurchargeAmount() : BigDecimal.ZERO);
        detailMap.put("totalPrice", detail.getTotalPrice() != null ? detail.getTotalPrice() : BigDecimal.ZERO);
        detailMap.put("passenger", detail.getPassenger() != null ? detail.getPassenger() : 1);
        detailMap.put("passengerCount", detail.getPassenger() != null ? detail.getPassenger() : 1);
        detailMap.put("note", detail.getNote());

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
