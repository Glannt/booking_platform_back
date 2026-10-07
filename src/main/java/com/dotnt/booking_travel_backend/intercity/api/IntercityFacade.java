package com.dotnt.booking_travel_backend.intercity.api;

import com.dotnt.booking_travel_backend.intercity.api.dtos.IntercityPriceCalculateRequest;
import com.dotnt.booking_travel_backend.intercity.api.dtos.IntercityPriceCalculateResponse;
import com.dotnt.booking_travel_backend.intercity.internal.entity.*;
import com.dotnt.booking_travel_backend.intercity.internal.repository.*;
import com.dotnt.booking_travel_backend.intercity.internal.service.IIntercityPriceService;
import com.dotnt.booking_travel_backend.surcharge.api.PricingContext;
import com.dotnt.booking_travel_backend.surcharge.api.SurchargeResult;
import com.dotnt.booking_travel_backend.surcharge.api.SurchargeService;
import com.dotnt.booking_travel_backend.shared.deposit.DepositPolicy;
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
    private final IIntercityPriceService intercityPriceService;
    private final IntercityRouteRepository intercityRouteRepository;
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

        if ("SHARED".equalsIgnoreCase(resolvedServiceType)) {
            if (clientBasePrice != null) basePrice = clientBasePrice;
            if (clientTotalPrice != null) totalPrice = clientTotalPrice;
        } else {
            // PRIVATE — the price_list row for the selected trip type is the only price.
            // The client number is never stored: it was computed browser-side and a same-day
            // round trip used to be billed as 2x the one-way fare whenever the dedicated
            // ROUND_TRIP_SAME_DAY row was ignored.
            // A contact-only route is a quote request: no price by design, so the caller's 0đ
            // is kept instead of asking price_list for a row that must not exist.
            boolean routeContactOnly = routeId != null && isContactOnlyRoute(routeId);
            if (!routeContactOnly && routeId != null && serviceOptionId != null) {
                // quoteIntercityTrip throws when the requested trip type has no configured row,
                // so there is no stand-in price left to fall back on here.
                BigDecimal[] quote = quoteIntercityTrip(routeId, serviceOptionId, tripType, departureDateTime, returnDate);
                BigDecimal serverBase = quote[0];
                BigDecimal serverTotal = quote[1];
                if (!matchesPrice(serverTotal, clientTotalPrice)) {
                    log.warn("Client total {} differs from price_list {} for route {} category {}; stored the price_list value",
                            clientTotalPrice, serverTotal, routeId, serviceOptionId);
                }
                basePrice = serverBase;
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

    /**
     * The server's figure for a PRIVATE intercity trip: the price_list row for the requested trip
     * type plus any applicable surcharge — exactly the pair {@link #createIntercityBookingDetail}
     * stores for a non-contact-only PRIVATE line, so a line is never priced off one row here and
     * another in the detail.
     *
     * <p>Throws (from calculatePrice) when the requested trip type has no configured row — the same
     * contract the detail-creation path relies on, so a caller that can fall back (Booking) catches
     * and treats it as "cannot be priced".
     *
     * @return {base, total} — the pre-surcharge base and the final total, or {@code null} when the
     *         line cannot even be looked up (route or category missing)
     */
    public BigDecimal[] quoteIntercityTrip(Long routeId, Long vehicleCategoryId, String tripType,
                                           LocalDateTime departureDateTime, LocalDate returnDate) {
        if (routeId == null || vehicleCategoryId == null) {
            return null;
        }
        IntercityPriceCalculateResponse quote = intercityPriceService.calculatePrice(
                IntercityPriceCalculateRequest.builder()
                        .routeId(routeId)
                        .vehicleCategoryId(vehicleCategoryId)
                        .priceTypeCode(tripType)
                        .departureDate(departureDateTime != null ? departureDateTime.toLocalDate() : null)
                        .returnDate(returnDate)
                        .build());
        BigDecimal serverBase = quote.getTotalPrice();
        BigDecimal serverTotal = serverBase;
        if (departureDateTime != null) {
            PricingContext pricingContext = PricingContext.builder()
                    .basePrice(serverTotal)
                    .serviceTime(departureDateTime)
                    .routeId(routeId)
                    .vehicleCategoryId(vehicleCategoryId)
                    .tripType(tripType)
                    .build();
            SurchargeResult surchargeResult = surchargeService.calculate("INTERCITY", pricingContext);
            if (surchargeResult != null && surchargeResult.getTotalSurcharge() != null) {
                serverTotal = serverTotal.add(surchargeResult.getTotalSurcharge());
            }
        }
        return new BigDecimal[]{serverBase, serverTotal};
    }

    /**
     * The deposit carried by whichever price row {@link #createIntercityBookingDetail} would price
     * this trip with — the same row, same trip type, so a line is never priced off one row and
     * carries a deposit off another.
     *
     * <p>{@code null} when no row matches or that row was never given a deposit. The caller must
     * be able to see that difference: only a null may fall back to the module default rate.
     *
     * @param routeId           the intercity route
     * @param vehicleCategoryId the vehicle category, which is half of the row's lookup key
     * @param tripType          ONE_WAY / ROUND_TRIP_SAME_DAY / DAILY_RATE
     * @return the stored policy, or {@code null} if the trip has no configured deposit
     */
    public DepositPolicy resolveDepositPolicy(Long routeId, Long vehicleCategoryId, String tripType,
                                              LocalDateTime departureDateTime, LocalDate returnDate) {
        if (routeId == null || vehicleCategoryId == null) return null;
        return intercityPriceService.resolveDepositPolicy(IntercityPriceCalculateRequest.builder()
                .routeId(routeId)
                .vehicleCategoryId(vehicleCategoryId)
                .priceTypeCode(tripType)
                .departureDate(departureDateTime != null ? departureDateTime.toLocalDate() : null)
                .returnDate(returnDate)
                .build());
    }

    /**
     * Whether the route is a quote request rather than a sale (contact-only). Booking stamps its
     * line item from this at write time so unticking the flag later cannot turn an old 0đ quote
     * back into revenue.
     *
     * @param routeId the intercity route
     * @return {@code true} when that route exists and is contact-only
     */
    public boolean isContactOnlyRoute(Long routeId) {
        if (routeId == null) return false;
        return intercityRouteRepository.findById(routeId)
                .map(IntercityRoute::isContactOnly)
                .orElse(false);
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

    private boolean matchesPrice(BigDecimal server, BigDecimal client) {
        if (server == null || client == null || client.signum() == 0) return true;
        BigDecimal tolerance = BigDecimal.valueOf(5000).add(server.abs().multiply(new BigDecimal("0.01")));
        return client.subtract(server).abs().compareTo(tolerance) <= 0;
    }
}
