package com.dotnt.booking_travel_backend.booking.internal.service;

import com.dotnt.booking_travel_backend.booking.api.*;
import com.dotnt.booking_travel_backend.booking.internal.entity.Booking;
import com.dotnt.booking_travel_backend.booking.internal.entity.BookingItem;
import com.dotnt.booking_travel_backend.booking.internal.mapper.BookingMapper;
import com.dotnt.booking_travel_backend.booking.internal.repository.BookingItemRepository;
import com.dotnt.booking_travel_backend.booking.internal.repository.BookingRepository;
import com.dotnt.booking_travel_backend.tour.api.TourBookingService;
import com.dotnt.booking_travel_backend.shared.deposit.DepositEngine;
import com.dotnt.booking_travel_backend.shared.mailer.MailService;
import com.dotnt.booking_travel_backend.shared.mailer.MailerProperties;
import com.dotnt.booking_travel_backend.shared.module.ModuleEventPublisher;
import com.dotnt.booking_travel_backend.user.api.UserFacade;
import com.dotnt.booking_travel_backend.fleet.api.FleetFacade;
import com.dotnt.booking_travel_backend.airport.api.AirportFacade;
import com.dotnt.booking_travel_backend.intercity.api.IntercityFacade;
import com.dotnt.booking_travel_backend.selfdrive.api.SelfDriveFacade;
import lombok.RequiredArgsConstructor;
import java.time.LocalDate;
import java.time.LocalTime;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.resilience.annotation.ConcurrencyLimit;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service("bookingService")
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final BookingItemRepository bookingItemRepository;
    private final ModuleEventPublisher moduleEventPublisher;
    private final BookingMapper bookingMapper;
    private final TourBookingService tourBookingService;
    private final MailService mailService;
    private final MailerProperties mailerProperties;
    private final BookingEmailBuilder bookingEmailBuilder;
    private final UserFacade userFacade;
    private final FleetFacade fleetFacade;
    private final AirportFacade airportFacade;
    private final IntercityFacade intercityFacade;
    private final SelfDriveFacade selfDriveFacade;
    private final com.dotnt.booking_travel_backend.discount.api.DiscountService discountService;
    private final com.dotnt.booking_travel_backend.promotion.api.PromotionValidationService promotionValidationService;
    private final com.dotnt.booking_travel_backend.promotion.api.PromotionRedemptionService promotionRedemptionService;
    private final com.dotnt.booking_travel_backend.booking.internal.repository.BookingTimelineEventRepository bookingTimelineEventRepository;
    /**
     * Who wins when the browser's figure for a booking line disagrees with the server's.
     * {@link ServerPriceMode#ENFORCE} additionally refuses to book a line the server could not
     * price; {@link ServerPriceMode#WARN} logs it and keeps what was sent so the gap can be
     * measured against real traffic first.
     */
    @org.springframework.beans.factory.annotation.Value("${app.booking.server-price-mode:WARN}")
    private ServerPriceMode serverPriceMode = ServerPriceMode.WARN;
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    @Override
    @Transactional
    @ConcurrencyLimit(20)
    public BookingDto createBooking(CreateBookingRequest request) {
        // Idempotency Check
        if (request.getIdempotencyKey() != null && !request.getIdempotencyKey().isBlank()) {
            Optional<Booking> existingBooking = bookingRepository.findByIdempotencyKey(request.getIdempotencyKey());
            if (existingBooking.isPresent()) {
                log.info("Duplicate booking request detected with idempotency key: {}", request.getIdempotencyKey());
                return bookingMapper.toDto(existingBooking.get());
            }
        }

        Long resolvedUserId = request.getUserId();
        if (resolvedUserId == null) {
            try {
                org.springframework.security.core.Authentication auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
                if (auth != null && auth.getPrincipal() instanceof String email && !"anonymousUser".equals(email)) {
                    resolvedUserId = userFacade.findUserIdByEmail(email);
                }
            } catch (Exception e) {
                log.warn("Failed resolving userId from SecurityContext: {}", e.getMessage());
            }
        }

        // Fallback: If userId is still null, but customerEmail belongs to a registered account, link them!
        if (resolvedUserId == null && request.getCustomerEmail() != null) {
            try {
                resolvedUserId = userFacade.findUserIdByEmail(request.getCustomerEmail());
            } catch (Exception e) {
                log.warn("Failed resolving userId from customerEmail fallback: {}", e.getMessage());
            }
        }

        String bookingCode = generateBookingCode();
        log.info("user Id booking : {}", resolvedUserId);

        Booking booking = Booking.builder()
                .bookingCode(bookingCode)
                .userId(resolvedUserId)
                .customerName(request.getCustomerName())
                .customerEmail(request.getCustomerEmail())
                .customerPhone(request.getCustomerPhone())
                .contactChannel(request.getContactChannel())
                .contactId(request.getContactId())
                .zaloId(request.getZaloId())
                .kakaoTalkId(request.getKakaoTalkId())
                .whatsapp(request.getWhatsapp())
                .idempotencyKey(request.getIdempotencyKey())
                .totalAmount(BigDecimal.ZERO)
                .totalPrice(BigDecimal.ZERO)
                .bookingStatus(BookingStatus.pending)
                .customerNotes(request.getCustomerNotes())
                .paymentStatus("UNPAID")
                .paymentMethod(request.getPaymentMethod())
                .paidAmount(BigDecimal.ZERO)
                .items(new ArrayList<>())
                .build();

        booking = bookingRepository.save(booking);

        BigDecimal calculatedTotal = BigDecimal.ZERO;
        String dominantServiceType = null;
        // The deposit this booking will be charged, resolved from the same rows the price was
        // resolved from further down. Deliberately not request.getDepositType/Rate/Amount: a
        // deposit the browser picks is a deposit the browser can set to 1đ. First line that
        // carries a policy wins, which for a one-line booking is the line itself.
        com.dotnt.booking_travel_backend.shared.deposit.DepositPolicy storedDeposit = null;
        if (request.getItems() != null) {
            for (var itemReq : request.getItems()) {
                int quantity = itemReq.getQuantity() != null ? itemReq.getQuantity() : 1;
                BigDecimal itemUnitPrice = itemReq.getUnitPrice() != null ? itemReq.getUnitPrice() : BigDecimal.ZERO;
                BigDecimal itemTotalPrice = itemUnitPrice;

                Long tourBookingDetailId = null;
                Long airportTransferBookingDetailId = null;
                Long intercityBookingDetailId = null;
                Long selfDriveBookingDetailId = null;
                boolean contactOnlyItem = false;
                // Filled in by whichever branch can price this line; null means the server had
                // nothing to price it with, which WARN logs and ENFORCE refuses to book.
                BigDecimal serverPrice = null;
                boolean isTour = tourBookingService.isTour(itemReq.getServiceId());
                boolean isAirport = airportFacade.existsTransferPriceById(itemReq.getServiceId())
                        || (itemReq.getServiceId() != null && itemReq.getServiceId().equals(101L));
                boolean isIntercity = (itemReq.getServiceId() != null && itemReq.getServiceId().equals(102L))
                        || (itemReq.getDynamicDetails() != null && itemReq.getDynamicDetails().contains("\"routeId\""));
                boolean isSelfDrive = (itemReq.getServiceId() != null && itemReq.getServiceId().equals(103L))
                        || (itemReq.getDynamicDetails() != null && (itemReq.getDynamicDetails().contains("\"rentalId\"") || (itemReq.getDynamicDetails().contains("\"vehicleCategoryId\"") && !isIntercity && !isAirport)));

                if (dominantServiceType == null) {
                    if (isTour) dominantServiceType = "TOUR";
                    else if (isSelfDrive) dominantServiceType = "SELF_DRIVE";
                    else if (isIntercity) dominantServiceType = "INTERCITY";
                    else if (isAirport) dominantServiceType = "AIRPORT";
                }

                if (isTour) {
                    if (itemReq.getServiceOptionId() != null) {
                        try {
                            Integer maxSeat = fleetFacade.getVehicleCategoryMaxSeat(itemReq.getServiceOptionId());
                            if (maxSeat != null && maxSeat > 0 && quantity > maxSeat) {
                                throw new IllegalArgumentException("The number of guests (" + quantity
                                        + ") exceeds the vehicle's maximum seat capacity (" + maxSeat
                                        + " seats)!");
                            }
                        } catch (Exception ignored) {
                            // serviceOptionId is a tour package, not a vehicle category
                        }
                    }

                    java.time.Instant startTime = itemReq.getStartDate() != null
                            ? itemReq.getStartDate().toInstant(java.time.ZoneOffset.UTC)
                            : null;
                    java.time.Instant endTime = itemReq.getEndDate() != null
                            ? itemReq.getEndDate().toInstant(java.time.ZoneOffset.UTC)
                            : null;

                    Long tourPackageId = null;
                    String packageName = null;
                    String pricingType = "PER_PERSON";
                    int adultCount = quantity;
                    int childCount = 0;
                    int infantCount = 0;

                    if (itemReq.getDynamicDetails() != null && !itemReq.getDynamicDetails().isBlank()) {
                        try {
                            com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
                            java.util.Map<String, Object> tdMap = om.readValue(
                                    itemReq.getDynamicDetails(),
                                    new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, Object>>() {});
                            if (tdMap.containsKey("tourPackageId") && tdMap.get("tourPackageId") != null) {
                                tourPackageId = Long.valueOf(tdMap.get("tourPackageId").toString());
                            }
                            if (tdMap.containsKey("packageName") && tdMap.get("packageName") != null) {
                                packageName = tdMap.get("packageName").toString();
                            }
                            if (tdMap.containsKey("pricingType") && tdMap.get("pricingType") != null) {
                                pricingType = tdMap.get("pricingType").toString();
                            }
                            if (tdMap.containsKey("adultCount") && tdMap.get("adultCount") != null) {
                                adultCount = Integer.parseInt(tdMap.get("adultCount").toString());
                            }
                            if (tdMap.containsKey("childCount") && tdMap.get("childCount") != null) {
                                childCount = Integer.parseInt(tdMap.get("childCount").toString());
                            }
                            if (tdMap.containsKey("infantCount") && tdMap.get("infantCount") != null) {
                                infantCount = Integer.parseInt(tdMap.get("infantCount").toString());
                            }
                        } catch (Exception ignored) {}
                    }

                    // A contact-only package has no price by design: it is a quote request. The
                    // booking therefore always totals 0đ, and the row is stamped so that unticking
                    // the flag later cannot turn an old quote back into revenue.
                    contactOnlyItem = tourPackageId != null
                            && tourBookingService.isContactOnlyPackage(tourPackageId);
                    if (contactOnlyItem) {
                        itemUnitPrice = BigDecimal.ZERO;
                        itemTotalPrice = BigDecimal.ZERO;
                    }

                    tourBookingDetailId = tourBookingService.createTourBookingDetail(
                            null,
                            itemReq.getServiceId(),
                            quantity,
                            itemReq.getPickupLocation(),
                            request.getCustomerNotes(),
                            startTime,
                            endTime,
                            tourPackageId,
                            packageName,
                            pricingType,
                            adultCount,
                            childCount,
                            infantCount);
                    serverPrice = tourBookingService.quoteTourPackage(tourPackageId, adultCount, childCount, infantCount);
                    if (storedDeposit == null) storedDeposit = tourBookingService.resolveDepositPolicyForPackage(tourPackageId);
                } else if (isAirport) {
                    com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
                    java.util.Map<String, Object> ad = new java.util.HashMap<>();
                    try {
                        if (itemReq.getDynamicDetails() != null && !itemReq.getDynamicDetails().isBlank()) {
                            ad = om.readValue(itemReq.getDynamicDetails(),
                                    new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, Object>>() {
                                    });
                        }
                    } catch (Exception ignored) {
                    }

                    String tripType = ad.getOrDefault("tripType", "ONE_WAY").toString();
                    String outboundDirection = ad.getOrDefault("outboundDirection", "FROM_AIRPORT").toString();
                    java.time.LocalDateTime outboundPickupTime = itemReq.getStartDate();
                    java.time.LocalDate returnDate = null;
                    if (ad.containsKey("returnDate") && ad.get("returnDate") != null) {
                        try {
                            returnDate = java.time.LocalDate.parse(ad.get("returnDate").toString());
                        } catch (Exception ignored) {
                        }
                    }
                    java.time.LocalDateTime returnPickupTime = null;
                    if (ad.containsKey("returnPickupTime") && ad.get("returnPickupTime") != null) {
                        try {
                            returnPickupTime = java.time.LocalDateTime.parse(ad.get("returnPickupTime").toString());
                        } catch (Exception ignored) {
                        }
                    }
                    if (returnPickupTime == null && itemReq.getEndDate() != null && "ROUND_TRIP".equals(tripType)) {
                        returnPickupTime = itemReq.getEndDate();
                    }

                    Long vehicleCategoryId = null;
                    if (ad.containsKey("vehicleCategoryId") && ad.get("vehicleCategoryId") != null) {
                        try {
                            vehicleCategoryId = Long.valueOf(ad.get("vehicleCategoryId").toString());
                        } catch (Exception ignored) {
                        }
                    }
                    if (vehicleCategoryId == null && itemReq.getServiceId() != null) {
                        vehicleCategoryId = itemReq.getServiceId();
                    }

                    java.math.BigDecimal basePrice = null;
                    if (ad.containsKey("basePrice") && ad.get("basePrice") != null) {
                        try {
                            basePrice = new java.math.BigDecimal(ad.get("basePrice").toString());
                        } catch (Exception ignored) {
                        }
                    }
                    java.math.BigDecimal totalPrice = null;
                    if (ad.containsKey("totalPrice") && ad.get("totalPrice") != null) {
                        try {
                            totalPrice = new java.math.BigDecimal(ad.get("totalPrice").toString());
                        } catch (Exception ignored) {
                        }
                    }

                    Long routeId = null;
                    if (ad.containsKey("routeId") && ad.get("routeId") != null) {
                        try {
                            routeId = Long.valueOf(ad.get("routeId").toString());
                        } catch (Exception ignored) {
                        }
                    }

                    String outboundPickupAddress = itemReq.getPickupLocation();
                    String outboundDropoffAddress = ad.get("outboundDropoffAddress") != null ? ad.get("outboundDropoffAddress").toString() : null;
                    String returnFlightNumber = ad.get("returnFlightNumber") != null ? ad.get("returnFlightNumber").toString() : null;
                    String returnPickupAddress = ad.get("returnPickupAddress") != null ? ad.get("returnPickupAddress").toString() : null;
                    String returnDropoffAddress = ad.get("returnDropoffAddress") != null ? ad.get("returnDropoffAddress").toString() : null;

                    BigDecimal actualKm = null;
                    if (ad.containsKey("actualKm") && ad.get("actualKm") != null) {
                        try {
                            actualKm = new java.math.BigDecimal(ad.get("actualKm").toString());
                        } catch (Exception ignored) {
                        }
                    }


                    airportTransferBookingDetailId = airportFacade.createAirportBookingDetail(
                            vehicleCategoryId,
                            routeId,
                            tripType,
                            outboundDirection,
                            outboundPickupTime,
                            itemReq.getFlightNumber(),
                            outboundPickupAddress,
                            outboundDropoffAddress,
                            ad.get("returnDirection") != null ? ad.get("returnDirection").toString() : null,
                            returnDate,
                            returnPickupTime,
                            returnFlightNumber,
                            returnPickupAddress,
                            returnDropoffAddress,
                            itemUnitPrice,
                            basePrice,
                            totalPrice,
                            request.getCustomerNotes(),
                            actualKm
                    );
                    serverPrice = airportFacade.quoteAirportTransfer(vehicleCategoryId, routeId, tripType, outboundPickupTime, actualKm);
                    if (storedDeposit == null) storedDeposit = airportFacade.resolveDepositPolicy(routeId, vehicleCategoryId);
                } else if (isIntercity) {
                    com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
                    java.util.Map<String, Object> ad = new java.util.HashMap<>();
                    try {
                        if (itemReq.getDynamicDetails() != null && !itemReq.getDynamicDetails().isBlank()) {
                            ad = om.readValue(itemReq.getDynamicDetails(),
                                    new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, Object>>() {
                                    });
                        }
                    } catch (Exception ignored) {
                    }

                    Long routeId = null;
                    if (ad.containsKey("routeId") && ad.get("routeId") != null) {
                        try {
                            routeId = Long.valueOf(ad.get("routeId").toString());
                        } catch (Exception ignored) {
                        }
                    }
                    Long vehicleCategoryId = null;
                    if (ad.containsKey("vehicleCategoryId") && ad.get("vehicleCategoryId") != null) {
                        try {
                            vehicleCategoryId = Long.valueOf(ad.get("vehicleCategoryId").toString());
                        } catch (Exception ignored) {
                        }
                    }
                    String serviceType = ad.getOrDefault("serviceType", "PRIVATE").toString();
                    Integer passengerCount = null;
                    if (ad.containsKey("passengerCount") && ad.get("passengerCount") != null) {
                        try {
                            passengerCount = Integer.valueOf(ad.get("passengerCount").toString());
                        } catch (Exception ignored) {
                        }
                    }
                    if (passengerCount == null) {
                        passengerCount = (itemReq.getQuantity() != null && itemReq.getQuantity() > 0) ? itemReq.getQuantity() : 1;
                    }
                    String tripType = ad.getOrDefault("tripType", "ONE_WAY").toString();
                    java.time.LocalDate departureDate = null;
                    if (ad.containsKey("departureDate") && ad.get("departureDate") != null) {
                        try {
                            departureDate = java.time.LocalDate.parse(ad.get("departureDate").toString());
                        } catch (Exception ignored) {
                        }
                    }
                    java.time.LocalTime departureTime = null;
                    if (ad.containsKey("departureTime") && ad.get("departureTime") != null) {
                        try {
                            departureTime = java.time.LocalTime.parse(ad.get("departureTime").toString());
                        } catch (Exception ignored) {
                        }
                    }
                    java.time.LocalDate returnDate = null;
                    if (ad.containsKey("returnDate") && ad.get("returnDate") != null) {
                        try {
                            returnDate = java.time.LocalDate.parse(ad.get("returnDate").toString());
                        } catch (Exception ignored) {
                        }
                    }
                    java.time.LocalTime returnTime = null;
                    if (ad.containsKey("returnTime") && ad.get("returnTime") != null) {
                        try {
                            returnTime = java.time.LocalTime.parse(ad.get("returnTime").toString());
                        } catch (Exception ignored) {
                        }
                    }
                    java.math.BigDecimal basePrice = null;
                    if (ad.containsKey("basePrice") && ad.get("basePrice") != null) {
                        try {
                            basePrice = new java.math.BigDecimal(ad.get("basePrice").toString());
                        } catch (Exception ignored) {
                        }
                    }
                    java.math.BigDecimal totalPrice = null;
                    if (ad.containsKey("totalPrice") && ad.get("totalPrice") != null) {
                        try {
                            totalPrice = new java.math.BigDecimal(ad.get("totalPrice").toString());
                        } catch (Exception ignored) {
                        }
                    }

                    LocalDateTime departureDateTime = null;
                    if (departureDate != null && departureTime != null) {
                        departureDateTime = LocalDateTime.of(departureDate, departureTime);
                    } else if (itemReq.getStartDate() != null) {
                        departureDateTime = itemReq.getStartDate();
                    }

                    String dropoffAddress = ad.get("dropoffAddress") != null ? ad.get("dropoffAddress").toString() : null;

                    intercityBookingDetailId = intercityFacade.createIntercityBookingDetail(
                            serviceType,
                            passengerCount,
                            vehicleCategoryId,
                            routeId,
                            tripType,
                            departureDateTime,
                            returnDate,
                            returnTime,
                            itemUnitPrice,
                            basePrice,
                            totalPrice,
                            itemReq.getPickupLocation(),
                            dropoffAddress,
                            request.getCustomerNotes()
                    );
                    // SHARED is operator-quoted, so no row priced this line and none has a deposit
                    // to hand over either — the same split the price path makes inside the facade.
                    if (storedDeposit == null && !"SHARED".equalsIgnoreCase(serviceType)) {
                        storedDeposit = intercityFacade.resolveDepositPolicy(
                                routeId, vehicleCategoryId, tripType, departureDateTime, returnDate);
                    }
                } else if (isSelfDrive) {
                    com.fasterxml.jackson.databind.ObjectMapper om = new com.fasterxml.jackson.databind.ObjectMapper();
                    java.util.Map<String, Object> ad = new java.util.HashMap<>();
                    try {
                        if (itemReq.getDynamicDetails() != null && !itemReq.getDynamicDetails().isBlank()) {
                            ad = om.readValue(itemReq.getDynamicDetails(),
                                    new com.fasterxml.jackson.core.type.TypeReference<java.util.Map<String, Object>>() {
                                    });
                        }
                    } catch (Exception ignored) {
                    }

                    Long vehicleCategoryId = null;
                    if (ad.containsKey("vehicleCategoryId") && ad.get("vehicleCategoryId") != null) {
                        try {
                            vehicleCategoryId = Long.valueOf(ad.get("vehicleCategoryId").toString());
                        } catch (Exception ignored) {
                        }
                    }
                    java.time.LocalDate startDate = null;
                    if (ad.containsKey("startDate") && ad.get("startDate") != null) {
                        try {
                            startDate = java.time.LocalDate.parse(ad.get("startDate").toString());
                        } catch (Exception ignored) {
                        }
                    }
                    java.time.LocalDate endDate = null;
                    if (ad.containsKey("endDate") && ad.get("endDate") != null) {
                        try {
                            endDate = java.time.LocalDate.parse(ad.get("endDate").toString());
                        } catch (Exception ignored) {
                        }
                    }
                    java.math.BigDecimal basePrice = null;
                    if (ad.containsKey("basePrice") && ad.get("basePrice") != null) {
                        try {
                            basePrice = new java.math.BigDecimal(ad.get("basePrice").toString());
                        } catch (Exception ignored) {
                        }
                    }
                    java.math.BigDecimal totalPrice = null;
                    if (ad.containsKey("totalPrice") && ad.get("totalPrice") != null) {
                        try {
                            totalPrice = new java.math.BigDecimal(ad.get("totalPrice").toString());
                        } catch (Exception ignored) {
                        }
                    }

                    String dropoffLocation = ad.get("dropoffLocation") != null ? ad.get("dropoffLocation").toString() : null;

                    selfDriveBookingDetailId = selfDriveFacade.createSelfDriveBookingDetail(
                            vehicleCategoryId,
                            startDate,
                            endDate,
                            basePrice,
                            totalPrice,
                            itemReq.getPickupLocation(),
                            dropoffLocation
                    );
                    serverPrice = selfDriveFacade.quoteSelfDriveRental(vehicleCategoryId, startDate, endDate);
                    if (storedDeposit == null) storedDeposit = selfDriveFacade.resolveDepositPolicy(vehicleCategoryId);
                }

                // The figure the browser sent is a cross-check, not an instruction: when the
                // server can price the line, its figure is what gets stored and the booking
                // aggregate is rebuilt from it. Only when the server has nothing to price the
                // line with does the mode decide — warn and keep what was sent, or refuse.
                if ((isTour || isAirport || isSelfDrive) && !contactOnlyItem) {
                    if (serverPrice != null) {
                        itemUnitPrice = serverPrice;
                        itemTotalPrice = serverPrice;
                    } else if (serverPriceMode == ServerPriceMode.ENFORCE) {
                        throw new IllegalArgumentException(
                                "No server-side price for this booking line (service " + itemReq.getServiceId()
                                        + "): refusing to book an unpriced line.");
                    } else {
                        log.warn("No server price for booking line, service {}; keeping the client figure {}",
                                itemReq.getServiceId(), itemUnitPrice);
                    }
                }

                BookingItem item = BookingItem.builder()
                        .booking(booking)
                        .quantity(quantity)
                        .unitPrice(itemUnitPrice)
                        .totalPrice(itemTotalPrice)
                        .isContactOnly(contactOnlyItem)
                        .dynamicDetails(itemReq.getDynamicDetails())
                        .tourBookingDetailId(tourBookingDetailId)
                        .airportTransferBookingDetailId(airportTransferBookingDetailId)
                        .intercityBookingDetailId(intercityBookingDetailId)
                        .selfDriveBookingDetailId(selfDriveBookingDetailId)
                        .build();

                // Summed here rather than up front, so it picks up the price after each branch has
                // settled it — otherwise a contact-only 0đ would be summed at its client-supplied
                // figure and the booking would total more than its own line items.
                calculatedTotal = calculatedTotal.add(itemTotalPrice);

                item = bookingItemRepository.save(item);
                booking.getItems().add(item);

                if (isTour && tourBookingDetailId != null) {
                    tourBookingService.updateBookingItemId(tourBookingDetailId, item.getId());
                } else if (isAirport && airportTransferBookingDetailId != null) {
                    airportFacade.updateAirportBookingItemId(airportTransferBookingDetailId, item.getId());
                } else if (isIntercity && intercityBookingDetailId != null) {
                    intercityFacade.updateIntercityBookingItemId(intercityBookingDetailId, item.getId());
                } else if (isSelfDrive && selfDriveBookingDetailId != null) {
                    selfDriveFacade.updateSelfDriveBookingItemId(selfDriveBookingDetailId, item.getId());
                }
            }
        }

        // Apply discount / promotion if provided
        Long appliedPromoId = null;
        Long appliedGrantId = null;
        if (request.getDiscountCode() != null && !request.getDiscountCode().isBlank()) {
            try {
                var promoReq = com.dotnt.booking_travel_backend.promotion.api.dtos.ValidatePromotionRequest.builder()
                        .code(request.getDiscountCode().trim())
                        .customerId(resolvedUserId)
                        .scopeType(dominantServiceType != null ? dominantServiceType : "GLOBAL")
                        .orderAmount(calculatedTotal)
                        .build();
                var promoRes = promotionValidationService.validateAndApplyPromotion(promoReq);
                if (promoRes != null && promoRes.isValid()) {
                    booking.setDiscountCode(promoRes.getDiscountCode());
                    booking.setDiscountType(promoRes.getDiscountType());
                    booking.setDiscountRate(promoRes.getDiscountValue());
                    booking.setDiscountAmount(promoRes.getDiscountAmount());
                    calculatedTotal = promoRes.getFinalAmount();
                    appliedPromoId = promoRes.getPromotionId();
                    appliedGrantId = promoRes.getGrantId();
                } else {
                    // Fallback to legacy discount service
                    var discountReq = com.dotnt.booking_travel_backend.discount.api.dtos.ValidateDiscountRequest.builder()
                            .code(request.getDiscountCode().trim())
                            .scopeType(dominantServiceType != null ? dominantServiceType : "GLOBAL")
                            .orderAmount(calculatedTotal)
                            .build();
                    var discountRes = discountService.validateAndApplyDiscount(discountReq);
                    if (discountRes != null && discountRes.isValid()) {
                        booking.setDiscountCode(discountRes.getDiscountCode());
                        booking.setDiscountType(discountRes.getDiscountType());
                        booking.setDiscountRate(discountRes.getDiscountValue());
                        booking.setDiscountAmount(discountRes.getDiscountAmount());
                        calculatedTotal = discountRes.getFinalAmount();
                    }
                }
            } catch (Exception e) {
                log.warn("Failed to apply discount code {}: {}", request.getDiscountCode(), e.getMessage());
            }
        }

        // Deposit calculation (calculated from total price after any discount). The policy is the
        // one stored on the rows this booking was priced from — request.getDepositType/Rate/Amount
        // are not read at all, so the browser has no say in what the customer pays upfront. A row
        // that was never configured has no deposit: there is no module default to fall back on,
        // because a default would be indistinguishable from a rate the business actually chose
        // (and the two used to disagree — DTO claimed 20%, the booking charged 30%).
        var depResult = DepositEngine.calculateDeposit(
                com.dotnt.booking_travel_backend.shared.deposit.dto.DepositCalculationRequest.builder()
                        .depositType(storedDeposit != null ? storedDeposit.getType() : null)
                        .depositRate(storedDeposit != null ? storedDeposit.getRate() : null)
                        .fixedDepositAmount(storedDeposit != null ? storedDeposit.getAmount() : null)
                        .totalAmount(calculatedTotal)
                        .build()
        );

        BigDecimal depAmount = depResult.getDepositAmount();
        booking.setDepositType(depResult.getDepositType());
        booking.setDepositRate(depResult.getDepositRate());
        booking.setDepositAmount(depAmount);
        booking.setPaidAmount(BigDecimal.ZERO);
        booking.setReceivedAmount(BigDecimal.ZERO);
        booking.setTotalAmount(calculatedTotal);
        booking.setTotalPrice(calculatedTotal);
        booking = bookingRepository.save(booking);

        // Whatever the browser claimed, the booking now carries what the server priced. Log the
        // gap with the booking id so it can be sized against real traffic before the mode flips
        // from WARN to ENFORCE; both figures are net of any discount, so this is like for like.
        if (request.getTotalAmount() != null
                && request.getTotalAmount().compareTo(booking.getTotalAmount()) != 0) {
            log.warn("Price mismatch for booking {} ({}): browser said {}, server priced {} [mode={}, discount={}]",
                    booking.getId(), booking.getBookingCode(),
                    request.getTotalAmount(), booking.getTotalAmount(),
                    serverPriceMode, booking.getDiscountCode());
        }

        // Record promotion redemption & update counter/grant atomically
        if (appliedPromoId != null) {
            try {
                promotionRedemptionService.redeemPromotion(
                        appliedPromoId, appliedGrantId, resolvedUserId, booking.getId(), booking.getDiscountAmount());
            } catch (Exception e) {
                log.warn("Failed to record promotion redemption for booking {}: {}", booking.getBookingCode(), e.getMessage());
            }
        }

        recordTimelineEvent(booking.getId(), "CREATED", "Tạo đơn đặt", 
                "Đơn đặt " + booking.getBookingCode() + " được tạo thành công cho " + booking.getCustomerName(), 
                null, booking.getBookingStatus().name());

        sendBookingEmail(booking);

        return bookingMapper.toDto(booking);
    }

    private void sendBookingEmail(Booking booking) {
        try {
            // 1. Email cho Customer (xác nhận tiếp nhận đơn đặt chỗ thành công)
            String customerHtml = bookingEmailBuilder.buildEmailHtml(booking, false);
            mailService.send(
                    booking.getCustomerEmail(),
                    "[Minh Khoa Travel] Tiếp nhận đơn đặt chỗ thành công - " + booking.getBookingCode(),
                    customerHtml);
            log.info("Sent customer booking email to {} for {}", booking.getCustomerEmail(), booking.getBookingCode());

            // 2. Email cho Admin (thông báo có đơn mới cần xử lý)
            String adminHtml = bookingEmailBuilder.buildEmailHtml(booking, true);
            String formattedTotal = booking.getTotalAmount() != null
                    ? String.format("%,d ₫", booking.getTotalAmount().longValue())
                    : "0 ₫";
            mailService.send(
                    mailerProperties.getFrom(),
                    "[ĐƠN MỚI] " + booking.getBookingCode() + " - Khách: " + booking.getCustomerName() + " (" + formattedTotal + ")",
                    adminHtml);
            log.info("Sent admin notification email for {}", booking.getBookingCode());
        } catch (Exception e) {
            log.error("Failed to send booking email", e);
        }
    }

    @Override
    public List<BookingDto> getAllBookings() {
        return bookingRepository.findAllByDeletedAtIsNullOrderByCreatedAtDesc()
                .stream()
                .map(bookingMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public BookingDto getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .filter(b -> b.getDeletedAt() == null)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: " + id));
        return bookingMapper.toDto(booking);
    }

    @Override
    public BookingDto getBookingByCode(String code) {
        Booking booking = bookingRepository.findByBookingCode(code)
                .filter(b -> b.getDeletedAt() == null)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with code: " + code));
        return bookingMapper.toDto(booking);
    }

    @Override
    @Transactional
    public BookingDto updateBooking(Long id, UpdateBookingRequest request) {
        Booking booking = bookingRepository.findById(id)
                .filter(b -> b.getDeletedAt() == null)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: " + id));

        BookingStatus oldStatus = booking.getBookingStatus();
        String oldDriver = booking.getAssignedDriver();
        String oldVehicle = booking.getAssignedVehicle();
        BigDecimal oldDeposit = booking.getDepositAmount() != null ? booking.getDepositAmount() : BigDecimal.ZERO;
        BigDecimal oldReceived = booking.getReceivedAmount() != null ? booking.getReceivedAmount() : BigDecimal.ZERO;
        String oldPaymentStatus = booking.getPaymentStatus();
        String oldInternalNotes = booking.getInternalNotes();

        if (request.getCustomerName() != null)
            booking.setCustomerName(request.getCustomerName());
        if (request.getCustomerEmail() != null)
            booking.setCustomerEmail(request.getCustomerEmail());
        if (request.getCustomerPhone() != null)
            booking.setCustomerPhone(request.getCustomerPhone());
        if (request.getTotalAmount() != null)
            booking.setTotalAmount(request.getTotalAmount());
        if (request.getBookingStatus() != null)
            booking.setBookingStatus(request.getBookingStatus());
        if (request.getCustomerNotes() != null)
            booking.setCustomerNotes(request.getCustomerNotes());
        if (request.getContactChannel() != null)
            booking.setContactChannel(request.getContactChannel());
        if (request.getContactId() != null)
            booking.setContactId(request.getContactId());
        if (request.getZaloId() != null)
            booking.setZaloId(request.getZaloId());
        if (request.getKakaoTalkId() != null)
            booking.setKakaoTalkId(request.getKakaoTalkId());
        if (request.getWhatsapp() != null)
            booking.setWhatsapp(request.getWhatsapp());
        if (request.getInternalNotes() != null)
            booking.setInternalNotes(request.getInternalNotes());
        if (request.getPaymentMethod() != null)
            booking.setPaymentMethod(request.getPaymentMethod());

        if (request.getDepositType() != null && !request.getDepositType().isBlank()) {
            booking.setDepositType(request.getDepositType().toUpperCase());
        }
        if (request.getDepositRate() != null) {
            booking.setDepositRate(request.getDepositRate());
        }

        // An admin edit recomputes the deposit from what is on the booking now. There is no
        // default rate any more: a percentage with no rate is a deposit of 0, exactly as it is
        // for a booking created through the public flow.
        if (booking.getDepositType() == null || booking.getDepositType().isBlank()
                || booking.getTotalAmount() == null) {
            // Nothing typed on the booking (or nowhere to calculate against): a bare amount the
            // admin entered is taken as-is, and is ignored if there is none.
            if (request.getDepositAmount() != null) booking.setDepositAmount(request.getDepositAmount());
        } else {
            var editedDeposit = DepositEngine.calculateDeposit(
                    com.dotnt.booking_travel_backend.shared.deposit.dto.DepositCalculationRequest.builder()
                            .depositType(booking.getDepositType())
                            .depositRate(booking.getDepositRate())
                            .fixedDepositAmount(request.getDepositAmount() != null
                                    ? request.getDepositAmount()
                                    : booking.getDepositAmount())
                            .totalAmount(booking.getTotalAmount())
                            .build());
            booking.setDepositAmount(editedDeposit.getDepositAmount());
            if (DepositPolicy.TYPE_RATE.equalsIgnoreCase(booking.getDepositType())
                    || DepositPolicy.TYPE_PERCENT.equalsIgnoreCase(booking.getDepositType())) {
                booking.setDepositRate(editedDeposit.getDepositRate());
            }
        }

        if (request.getReceivedAmount() != null)
            booking.setReceivedAmount(request.getReceivedAmount());

        BigDecimal currentDeposit = booking.getDepositAmount() != null ? booking.getDepositAmount() : BigDecimal.ZERO;
        BigDecimal currentReceived = booking.getReceivedAmount() != null ? booking.getReceivedAmount() : BigDecimal.ZERO;
        booking.setPaidAmount(currentDeposit.add(currentReceived));

        if (request.getPaymentStatus() != null && !request.getPaymentStatus().isBlank()) {
            booking.setPaymentStatus(request.getPaymentStatus());
        } else {
            BigDecimal total = booking.getTotalAmount() != null ? booking.getTotalAmount() : BigDecimal.ZERO;
            if (booking.getPaidAmount().compareTo(BigDecimal.ZERO) == 0) {
                booking.setPaymentStatus("UNPAID");
            } else if (booking.getPaidAmount().compareTo(total) >= 0 && total.compareTo(BigDecimal.ZERO) > 0) {
                booking.setPaymentStatus("PAID");
            } else {
                booking.setPaymentStatus("DEPOSITED");
            }
        }

        if (request.getAssignedDriver() != null)
            booking.setAssignedDriver(request.getAssignedDriver());
        if (request.getAssignedVehicle() != null) {
            String targetVehicle = request.getAssignedVehicle().trim();
            booking.setAssignedVehicle(targetVehicle);

            if (!targetVehicle.isBlank() && booking.getBookingStatus() != BookingStatus.cancelled) {
                java.time.LocalDate[] targetDates = extractBookingDates(booking);
                if (targetDates != null && targetDates[0] != null && targetDates[1] != null) {
                    java.time.LocalDate start = targetDates[0];
                    java.time.LocalDate end = targetDates[1];

                    List<Booking> allBookings = bookingRepository.findAllByDeletedAtIsNullOrderByCreatedAtDesc();
                    for (Booking other : allBookings) {
                        if (other.getId().equals(id)) continue;
                        if (other.getBookingStatus() == BookingStatus.cancelled) continue;
                        if (other.getAssignedVehicle() == null || other.getAssignedVehicle().isBlank()) continue;

                        if (other.getAssignedVehicle().trim().equalsIgnoreCase(targetVehicle)) {
                            java.time.LocalDate[] otherDates = extractBookingDates(other);
                            if (otherDates != null && otherDates[0] != null && otherDates[1] != null) {
                                java.time.LocalDate otherStart = otherDates[0];
                                java.time.LocalDate otherEnd = otherDates[1];

                                if (!start.isAfter(otherEnd) && !end.isBefore(otherStart)) {
                                    throw new IllegalArgumentException("Xe \"" + targetVehicle + "\" đã được phân công cho đơn " +
                                            other.getBookingCode() + " (" + otherStart + " đến " + otherEnd + "). Không thể phân công trùng lịch!");
                                }
                            }
                        }
                    }
                }
            }
        }

        booking = bookingRepository.save(booking);

        // Record timeline events for detected operational changes
        if (booking.getBookingStatus() != null && !booking.getBookingStatus().equals(oldStatus)) {
            recordTimelineEvent(booking.getId(), "STATUS_CHANGE", "Thay đổi trạng thái",
                    "Trạng thái đơn chuyển từ " + oldStatus + " sang " + booking.getBookingStatus(),
                    oldStatus != null ? oldStatus.name() : null, booking.getBookingStatus().name());

            if (booking.getBookingStatus() == BookingStatus.cancelled) {
                try {
                    promotionRedemptionService.reverseRedemption(booking.getId());
                } catch (Exception e) {
                    log.warn("Failed reversing promotion redemption for booking {}: {}", booking.getId(), e.getMessage());
                }
            }
        }

        boolean driverChanged = !java.util.Objects.equals(oldDriver, booking.getAssignedDriver());
        boolean vehicleChanged = !java.util.Objects.equals(oldVehicle, booking.getAssignedVehicle());
        if (driverChanged || vehicleChanged) {
            recordTimelineEvent(booking.getId(), "DISPATCH_UPDATE", "Điều phối xe / tài xế",
                    "Cập nhật điều phối: Tài xế [" + (booking.getAssignedDriver() != null ? booking.getAssignedDriver() : "Chưa gán") +
                            "], Xe [" + (booking.getAssignedVehicle() != null ? booking.getAssignedVehicle() : "Chưa gán") + "]",
                    "Tài xế: " + (oldDriver != null ? oldDriver : "Chưa có") + ", Xe: " + (oldVehicle != null ? oldVehicle : "Chưa có"),
                    "Tài xế: " + (booking.getAssignedDriver() != null ? booking.getAssignedDriver() : "Chưa có") + ", Xe: " + (booking.getAssignedVehicle() != null ? booking.getAssignedVehicle() : "Chưa có"));
        }

        if (oldDeposit.compareTo(currentDeposit) != 0 || oldReceived.compareTo(currentReceived) != 0 || !java.util.Objects.equals(oldPaymentStatus, booking.getPaymentStatus())) {
            recordTimelineEvent(booking.getId(), "PAYMENT_UPDATE", "Cập nhật thanh toán",
                    "Cọc: " + currentDeposit + ", Đã nhận sau: " + currentReceived + ", Tổng thu: " + booking.getPaidAmount() + ", Trạng thái: " + booking.getPaymentStatus(),
                    "Cọc: " + oldDeposit + ", Nhận sau: " + oldReceived + ", TT: " + oldPaymentStatus,
                    "Cọc: " + currentDeposit + ", Nhận sau: " + currentReceived + ", TT: " + booking.getPaymentStatus());
        }

        if (!java.util.Objects.equals(oldInternalNotes, booking.getInternalNotes())) {
            recordTimelineEvent(booking.getId(), "NOTE_UPDATE", "Cập nhật ghi chú nội bộ",
                    "Cập nhật ghi chú vận hành nội bộ",
                    oldInternalNotes, booking.getInternalNotes());
        }

        return bookingMapper.toDto(booking);
    }

    private java.time.LocalDate[] extractBookingDates(Booking b) {
        if (b.getItems() == null) return null;
        for (BookingItem item : b.getItems()) {
            if (item.getSelfDriveBookingDetailId() != null) {
                java.time.LocalDate[] dates = selfDriveFacade.getSelfDriveBookingDates(item.getSelfDriveBookingDetailId());
                if (dates != null) return dates;
            }
            if (item.getDynamicDetails() != null && !item.getDynamicDetails().isBlank()) {
                try {
                    com.fasterxml.jackson.databind.JsonNode node = new com.fasterxml.jackson.databind.ObjectMapper().readTree(item.getDynamicDetails());
                    if (node.has("startDate") && node.has("endDate")) {
                        java.time.LocalDate s = java.time.LocalDate.parse(node.get("startDate").asText().substring(0, 10));
                        java.time.LocalDate e = java.time.LocalDate.parse(node.get("endDate").asText().substring(0, 10));
                        return new java.time.LocalDate[]{s, e};
                    }
                } catch (Exception ignored) {
                }
            }
        }
        return null;
    }

    @Override
    @Transactional
    public void deleteBooking(Long id) {
        deleteBooking(id, "Xóa bởi quản trị viên");
    }

    @Override
    @Transactional
    public void deleteBooking(Long id, String reason) {
        Booking booking = bookingRepository.findById(id)
                .filter(b -> b.getDeletedAt() == null)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: " + id));

        BigDecimal dep = booking.getDepositAmount() != null ? booking.getDepositAmount() : BigDecimal.ZERO;
        BigDecimal rec = booking.getReceivedAmount() != null ? booking.getReceivedAmount() : BigDecimal.ZERO;
        BigDecimal paid = booking.getPaidAmount() != null ? booking.getPaidAmount() : BigDecimal.ZERO;
        if (dep.compareTo(BigDecimal.ZERO) > 0 || rec.compareTo(BigDecimal.ZERO) > 0 || paid.compareTo(BigDecimal.ZERO) > 0
                || "PAID".equalsIgnoreCase(booking.getPaymentStatus()) || "DEPOSITED".equalsIgnoreCase(booking.getPaymentStatus())) {
            throw new IllegalStateException("Đơn đặt " + booking.getBookingCode() + " đã ghi nhận dòng tiền (Cọc: " + dep + ", Nhận sau: " + rec + ", Đã thanh toán: " + paid + "). Không thể xóa đơn hàng production! Vui lòng chuyển trạng thái Hủy (Cancelled) hoặc thực hiện hoàn tiền trước.");
        }

        String actualReason = (reason != null && !reason.isBlank()) ? reason.trim() : "Xóa bởi quản trị viên";
        booking.setDeletionReason(actualReason);
        booking.setDeletedAt(LocalDateTime.now());
        bookingRepository.save(booking);

        recordTimelineEvent(booking.getId(), "DELETED", "Xóa đơn đặt",
                "Đơn đặt đã bị soft-delete với lý do: " + actualReason,
                "Active", "Deleted");
    }

    @Override
    public List<BookingTimelineDto> getBookingTimeline(Long id) {
        return bookingTimelineEventRepository.findByBookingIdOrderByCreatedAtAsc(id)
                .stream()
                .map(e -> BookingTimelineDto.builder()
                        .id(e.getId())
                        .bookingId(e.getBookingId())
                        .actorId(e.getActorId())
                        .actorName(e.getActorName())
                        .eventType(e.getEventType())
                        .title(e.getTitle())
                        .description(e.getDescription())
                        .oldValue(e.getOldValue())
                        .newValue(e.getNewValue())
                        .createdAt(e.getCreatedAt())
                        .build())
                .collect(Collectors.toList());
    }

    private void recordTimelineEvent(Long bookingId, String eventType, String title, String description, String oldValue, String newValue) {
        try {
            String actorName = "Hệ thống";
            Long actorId = null;
            var auth = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.isAuthenticated() && auth.getName() != null && !auth.getName().equalsIgnoreCase("anonymousUser")) {
                actorName = auth.getName();
            }
            com.dotnt.booking_travel_backend.booking.internal.entity.BookingTimelineEvent event = com.dotnt.booking_travel_backend.booking.internal.entity.BookingTimelineEvent.builder()
                    .bookingId(bookingId)
                    .actorId(actorId)
                    .actorName(actorName)
                    .eventType(eventType)
                    .title(title)
                    .description(description)
                    .oldValue(oldValue)
                    .newValue(newValue)
                    .createdAt(LocalDateTime.now())
                    .build();
            bookingTimelineEventRepository.save(event);
        } catch (Exception e) {
            log.error("Failed to record timeline event for booking {}: {}", bookingId, e.getMessage());
        }
    }

    @Override
    public List<BookingDto> getMyBookings(Long userId) {
        return bookingRepository.findAllByUserIdAndDeletedAtIsNullOrderByCreatedAtDesc(userId)
                .stream()
                .map(bookingMapper::toDto)
                .collect(Collectors.toList());
    }

    private String generateBookingCode() {
        String dateStr = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        int randNum = 100000 + SECURE_RANDOM.nextInt(900000);
        return "BK-" + dateStr + "-" + randNum;
    }
}
