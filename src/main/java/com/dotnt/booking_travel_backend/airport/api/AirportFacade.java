package com.dotnt.booking_travel_backend.airport.api;

import com.dotnt.booking_travel_backend.airport.internal.entity.*;
import com.dotnt.booking_travel_backend.airport.internal.repository.*;
import com.dotnt.booking_travel_backend.airport.internal.service.AirportTransferPriceCalculator;
import com.dotnt.booking_travel_backend.surcharge.api.PricingContext;
import com.dotnt.booking_travel_backend.surcharge.api.SurchargeResult;
import com.dotnt.booking_travel_backend.surcharge.api.SurchargeService;
import com.dotnt.booking_travel_backend.shared.deposit.DepositPolicies;
import com.dotnt.booking_travel_backend.shared.deposit.DepositPolicy;
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

        ServerPrice resolved = resolveServerPrice(vehicleCategoryId, routeId, tripType, outboundPickupTime, actualKm);
        if (resolved != null) {
            validateServerPrice("airport transfer", resolved.base(), resolved.total(), clientUnitPrice, clientBasePrice, clientTotalPrice);
            basePrice = resolved.base();
            totalPrice = resolved.total();
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

    /**
     * What this line is worth when the server prices it — the transfer price row for the route and
     * vehicle category, plus whatever surcharge the time of day calls for. The same arithmetic
     * {@link #createAirportBookingDetail} runs when it stores the detail, so the two can never
     * disagree; this entry point exists so the booking aggregate can be priced before the line is
     * written rather than read back afterwards.
     *
     * @param routeId            the chosen route, and {@code vehicleCategoryId} the chosen vehicle;
     *                           either one missing means there is nothing to look a price up by
     * @return the server-computed trip total, or {@code null} if the line cannot be priced
     */
    public BigDecimal quoteAirportTransfer(
            Long vehicleCategoryId,
            Long routeId,
            String tripType,
            LocalDateTime outboundPickupTime,
            BigDecimal actualKm
    ) {
        ServerPrice resolved = resolveServerPrice(vehicleCategoryId, routeId, tripType, outboundPickupTime, actualKm);
        if (resolved != null) return resolved.total();

        log.warn("No server price for airport transfer, route {} category {} — the client figure is what gets kept",
                routeId, vehicleCategoryId);
        return null;
    }

    /**
     * The deposit rule behind this line: the transfer price row that priced it first, then the
     * route it travels, then nothing at all. Same precedence the confirm page applies, and one
     * row one source — the deposit is read from the row the price came from.
     *
     * <p>{@code null} when neither level has a policy. That is not "0%", it is "unconfigured",
     * and it is the only state in which the module default rate is allowed to apply.
     *
     * @param routeId           the chosen route; without it there is nothing to look a policy up by
     * @param vehicleCategoryId the chosen vehicle, whose row may carry a policy of its own
     * @return the stored policy, or {@code null} if neither level has one
     */
    public DepositPolicy resolveDepositPolicy(Long routeId, Long vehicleCategoryId) {
        if (routeId == null) return null;

        if (vehicleCategoryId != null) {
            DepositPolicy row = airportTransferPriceRepository
                    .findByAirportRouteIdAndVehicleCategoryId(routeId, vehicleCategoryId)
                    .map(AirportTransferPrice::getDepositPolicy)
                    .orElse(null);
            if (DepositPolicies.configured(row) != null) return row;
        }

        return DepositPolicies.configured(
                airportRouteRepository.findById(routeId).map(AirportRoute::getDepositPolicy).orElse(null));
    }

    /**
     * @return the priced line, or {@code null} when no row backs it — a null is a hole to report,
     *         never a zero, so that a caller in ENFORCE mode can refuse instead of selling free
     */
    private ServerPrice resolveServerPrice(
            Long vehicleCategoryId,
            Long routeId,
            String tripType,
            LocalDateTime outboundPickupTime,
            BigDecimal actualKm
    ) {
        if (routeId == null || vehicleCategoryId == null) return null;

        AirportTransferPrice transferPrice = airportTransferPriceRepository
                .findByAirportRouteIdAndVehicleCategoryId(routeId, vehicleCategoryId)
                .orElse(null);
        if (transferPrice == null) return null;

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

        return new ServerPrice(serverOneWay, serverTotal.add(surcharge));
    }

    private record ServerPrice(BigDecimal base, BigDecimal total) {}

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
