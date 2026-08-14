package com.dotnt.booking_travel_backend.booking.internal.service;

import com.dotnt.booking_travel_backend.booking.api.*;
import com.dotnt.booking_travel_backend.booking.internal.entity.Booking;
import com.dotnt.booking_travel_backend.booking.internal.entity.BookingItem;
import com.dotnt.booking_travel_backend.booking.internal.mapper.BookingMapper;
import com.dotnt.booking_travel_backend.booking.internal.repository.BookingItemRepository;
import com.dotnt.booking_travel_backend.booking.internal.repository.BookingRepository;
import com.dotnt.booking_travel_backend.tour.api.TourBookingService;
import com.dotnt.booking_travel_backend.shared.mailer.MailService;
import com.dotnt.booking_travel_backend.shared.module.ModuleEventPublisher;
import com.dotnt.booking_travel_backend.user.api.UserFacade;
import com.dotnt.booking_travel_backend.fleet.api.FleetFacade;
import com.dotnt.booking_travel_backend.airport.api.AirportFacade;
import com.dotnt.booking_travel_backend.rental.api.RentalFacade;
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
    private final BookingEmailBuilder bookingEmailBuilder;
    private final UserFacade userFacade;
    private final FleetFacade fleetFacade;
    private final AirportFacade airportFacade;
    private final RentalFacade rentalFacade;
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
                .idempotencyKey(request.getIdempotencyKey())
                .totalAmount(BigDecimal.ZERO)
                .totalPrice(BigDecimal.ZERO)
                .bookingStatus(BookingStatus.pending)
                .customerNotes(request.getCustomerNotes())
                .items(new ArrayList<>())
                .build();

        booking = bookingRepository.save(booking);

        BigDecimal calculatedTotal = BigDecimal.ZERO;
        if (request.getItems() != null) {
            for (var itemReq : request.getItems()) {
                int quantity = itemReq.getQuantity() != null ? itemReq.getQuantity() : 1;
                BigDecimal itemUnitPrice = itemReq.getUnitPrice() != null ? itemReq.getUnitPrice() : BigDecimal.ZERO;
                BigDecimal itemTotalPrice = itemUnitPrice;
                calculatedTotal = calculatedTotal.add(itemTotalPrice);

                Long tourBookingDetailId = null;
                Long airportTransferBookingDetailId = null;
                Long intercityBookingDetailId = null;
                Long selfDriveBookingDetailId = null;
                boolean isTour = tourBookingService.isTour(itemReq.getServiceId());
                boolean isAirport = airportFacade.existsTransferPriceById(itemReq.getServiceId())
                        || (itemReq.getServiceId() != null && itemReq.getServiceId().equals(101L));
                boolean isIntercity = itemReq.getServiceId() != null && itemReq.getServiceId().equals(102L);
                boolean isSelfDrive = itemReq.getServiceId() != null && itemReq.getServiceId().equals(103L);

                if (isTour) {
                    if (itemReq.getServiceOptionId() != null) {
                        Integer maxSeat = fleetFacade.getVehicleCategoryMaxSeat(itemReq.getServiceOptionId());
                        if (maxSeat != null && quantity > maxSeat) {
                            throw new IllegalArgumentException("The number of guests (" + quantity
                                    + ") exceeds the vehicle's maximum seat capacity (" + maxSeat
                                    + " seats)!");
                        }
                    }

                    java.time.Instant startTime = itemReq.getStartDate() != null
                            ? itemReq.getStartDate().toInstant(java.time.ZoneOffset.UTC)
                            : null;
                    java.time.Instant endTime = itemReq.getEndDate() != null
                            ? itemReq.getEndDate().toInstant(java.time.ZoneOffset.UTC)
                            : null;

                    tourBookingDetailId = tourBookingService.createTourBookingDetail(
                            null,
                            itemReq.getServiceId(),
                            quantity,
                            itemReq.getPickupLocation(),
                            request.getCustomerNotes(),
                            startTime,
                            endTime);
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

                    intercityBookingDetailId = rentalFacade.createIntercityBookingDetail(
                            vehicleCategoryId,
                            routeId,
                            tripType,
                            departureDateTime,
                            returnDate,
                            itemUnitPrice,
                            basePrice,
                            totalPrice,
                            itemReq.getPickupLocation(),
                            dropoffAddress,
                            request.getCustomerNotes()
                    );
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

                    selfDriveBookingDetailId = rentalFacade.createSelfDriveBookingDetail(
                            vehicleCategoryId,
                            startDate,
                            endDate,
                            basePrice,
                            totalPrice,
                            itemReq.getPickupLocation(),
                            dropoffLocation
                    );
                }

                BookingItem item = BookingItem.builder()
                        .booking(booking)
                        .quantity(quantity)
                        .unitPrice(itemUnitPrice)
                        .totalPrice(itemTotalPrice)
                        .dynamicDetails(itemReq.getDynamicDetails())
                        .tourBookingDetailId(tourBookingDetailId)
                        .airportTransferBookingDetailId(airportTransferBookingDetailId)
                        .intercityBookingDetailId(intercityBookingDetailId)
                        .selfDriveBookingDetailId(selfDriveBookingDetailId)
                        .build();

                item = bookingItemRepository.save(item);
                booking.getItems().add(item);

                if (isTour && tourBookingDetailId != null) {
                    tourBookingService.updateBookingItemId(tourBookingDetailId, item.getId());
                } else if (isAirport && airportTransferBookingDetailId != null) {
                    airportFacade.updateAirportBookingItemId(airportTransferBookingDetailId, item.getId());
                } else if (isIntercity && intercityBookingDetailId != null) {
                    rentalFacade.updateIntercityBookingItemId(intercityBookingDetailId, item.getId());
                } else if (isSelfDrive && selfDriveBookingDetailId != null) {
                    rentalFacade.updateSelfDriveBookingItemId(selfDriveBookingDetailId, item.getId());
                }
            }
        }

        booking.setTotalAmount(calculatedTotal);
        booking.setTotalPrice(calculatedTotal);
        booking = bookingRepository.save(booking);

        sendBookingEmail(booking);

        return bookingMapper.toDto(booking);
    }

    private void sendBookingEmail(Booking booking) {
        try {
            String htmlContent = bookingEmailBuilder.buildEmailHtml(booking);
            mailService.send(
                    "tongnguyenthanhdo@gmail.com",
                    "[Minh Khoa Travel] Yêu cầu đặt dịch vụ mới - " + booking.getBookingCode(),
                    htmlContent);
            log.info("Sent booking email confirmation for {}", booking.getBookingCode());
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

        booking = bookingRepository.save(booking);
        return bookingMapper.toDto(booking);
    }

    @Override
    @Transactional
    public void deleteBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .filter(b -> b.getDeletedAt() == null)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: " + id));
        booking.setDeletedAt(LocalDateTime.now());
        bookingRepository.save(booking);
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
